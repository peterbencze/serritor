/*
 * Copyright 2017 Peter Bencze.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.peterbencze.serritor.api;

import com.gargoylesoftware.htmlunit.WebClient;
import com.github.peterbencze.serritor.api.CrawlRequest.CrawlRequestBuilder;
import com.github.peterbencze.serritor.api.event.NonHtmlContentEvent;
import com.github.peterbencze.serritor.api.event.PageLoadEvent;
import com.github.peterbencze.serritor.api.event.PageLoadTimeoutEvent;
import com.github.peterbencze.serritor.api.event.RequestErrorEvent;
import com.github.peterbencze.serritor.api.event.RequestRedirectEvent;
import com.github.peterbencze.serritor.internal.CookieConverter;
import com.github.peterbencze.serritor.internal.CrawlFrontier;
import com.github.peterbencze.serritor.internal.CrawlerState;
import com.github.peterbencze.serritor.internal.crawldelaymechanism.AdaptiveCrawlDelayMechanism;
import com.github.peterbencze.serritor.internal.crawldelaymechanism.CrawlDelayMechanism;
import com.github.peterbencze.serritor.internal.crawldelaymechanism.FixedCrawlDelayMechanism;
import com.github.peterbencze.serritor.internal.crawldelaymechanism.RandomCrawlDelayMechanism;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.UnsupportedCharsetException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.Validate;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

/**
 * Provides a skeletal implementation of a crawler to minimize the effort for users to implement
 * their own.
 *
 * @author Peter Bencze
 */
public abstract class BaseCrawler {

    private static final Logger LOGGER = Logger.getLogger(BaseCrawler.class.getName());

    private CrawlerConfiguration config;
    private CrawlFrontier crawlFrontier;
    private BasicCookieStore cookieStore;
    private CloseableHttpClient httpClient;
    private WebDriver webDriver;
    private CrawlDelayMechanism crawlDelayMechanism;
    private boolean isStopped;
    private boolean isStopping;

    /**
     * Base constructor which is used to configure the crawler.
     *
     * @param config the configuration of the crawler
     */
    protected BaseCrawler(final CrawlerConfiguration config) {
        this();

        this.config = config;
    }

    /**
     * Base constructor which loads a previously saved state.
     *
     * @param inStream the input stream from which the state should be loaded
     */
    protected BaseCrawler(final InputStream inStream) {
        this();

        CrawlerState state = SerializationUtils.deserialize(inStream);
        config = state.getStateObject(CrawlerConfiguration.class);
        crawlFrontier = state.getStateObject(CrawlFrontier.class);
    }

    /**
     * Private base constructor which does simple initialization.
     */
    private BaseCrawler() {
        isStopping = false;
        isStopped = true;
    }

    /**
     * Starts the crawler using HtmlUnit headless browser. This method will block until the crawler
     * finishes.
     */
    public final void start() {
        start(new HtmlUnitDriver(true));
    }

    /**
     * Starts the crawler using the browser specified by the given <code>WebDriver</code> instance.
     * This method will block until the crawler finishes.
     *
     * @param webDriver the <code>WebDriver</code> instance to control the browser
     */
    public final void start(final WebDriver webDriver) {
        start(webDriver, false);
    }

    /**
     * Performs initialization and runs the crawler.
     *
     * @param isResuming indicates if a previously saved state is to be resumed
     */
    private void start(final WebDriver webDriver, final boolean isResuming) {
        try {
            Validate.validState(isStopped, "The crawler is already running.");
            this.webDriver = Validate.notNull(webDriver, "The webdriver cannot be null.");

            // If the crawl delay strategy is set to adaptive, we check if the browser supports the
            // Navigation Timing API or not. However HtmlUnit requires a page to be loaded first
            // before executing JavaScript, so we load a blank page.
            if (webDriver instanceof HtmlUnitDriver
                    && config.getCrawlDelayStrategy().equals(CrawlDelayStrategy.ADAPTIVE)) {
                webDriver.get(WebClient.ABOUT_BLANK);
            }

            if (!isResuming) {
                crawlFrontier = new CrawlFrontier(config);
            }

            cookieStore = new BasicCookieStore();
            httpClient = HttpClientBuilder.create()
                    .setDefaultCookieStore(cookieStore)
                    .useSystemProperties()
                    .build();
            crawlDelayMechanism = createCrawlDelayMechanism();
            isStopped = false;

            run();
        } finally {
            HttpClientUtils.closeQuietly(httpClient);

            if (this.webDriver != null) {
                this.webDriver.quit();
            }

            isStopping = false;
            isStopped = true;
        }
    }

    /**
     * Saves the current state of the crawler to the given output stream.
     *
     * @param outStream the output stream
     */
    public final void saveState(final OutputStream outStream) {
        Validate.validState(crawlFrontier != null, "Cannot save state at this point.");

        CrawlerState state = new CrawlerState();
        state.putStateObject(config);
        state.putStateObject(crawlFrontier);

        SerializationUtils.serialize(state, outStream);
    }

    /**
     * Resumes the previously loaded state using HtmlUnit headless browser. This method will block
     * until the crawler finishes.
     */
    public final void resumeState() {
        resumeState(new HtmlUnitDriver(true));
    }

    /**
     * Resumes the previously loaded state using the browser specified by the given
     * <code>WebDriver</code> instance. This method will block until the crawler finishes.
     *
     * @param webDriver the <code>WebDriver</code> instance to control the browser
     */
    public final void resumeState(final WebDriver webDriver) {
        Validate.validState(crawlFrontier != null, "Cannot resume state at this point.");

        start(webDriver, true);
    }

    /**
     * Gracefully stops the crawler.
     */
    protected final void stop() {
        Validate.validState(!isStopped, "The crawler is not started.");
        Validate.validState(!isStopping, "The crawler is already stopping.");

        // Indicate that the crawling should be stopped
        isStopping = true;
    }

    /**
     * Feeds a crawl request to the crawler. The crawler should be running, otherwise the request
     * has to be added as a crawl seed instead.
     *
     * @param request the crawl request
     */
    protected final void crawl(final CrawlRequest request) {
        Validate.validState(!isStopped,
                "The crawler is not started. Maybe you meant to add this request as a crawl seed?");
        Validate.validState(!isStopping, "Cannot add request when the crawler is stopping.");
        Validate.notNull(request, "The request cannot be null.");

        crawlFrontier.feedRequest(request, false);
    }

    /**
     * Feeds multiple crawl requests to the crawler. The crawler should be running, otherwise the
     * requests have to be added as crawl seeds instead.
     *
     * @param requests the list of crawl requests
     */
    protected final void crawl(final List<CrawlRequest> requests) {
        requests.forEach(this::crawl);
    }

    /**
     * Downloads the file specified by the URL.
     *
     * @param source      the source URL
     * @param destination the destination file
     *
     * @throws IOException if an I/O error occurs while downloading the file
     */
    protected final void downloadFile(final URI source, final File destination) throws IOException {
        Validate.validState(!isStopped, "Cannot download file when the crawler is not started.");
        Validate.validState(!isStopping, "Cannot download file when the crawler is stopping.");
        Validate.notNull(source, "The source URL cannot be null.");
        Validate.notNull(destination, "The destination file cannot be null.");

        HttpGet request = new HttpGet(source);
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                FileUtils.copyInputStreamToFile(entity.getContent(), destination);
            }
        }
    }

    /**
     * Defines the workflow of the crawler.
     */
    private void run() {
        onStart();

        while (!isStopping && crawlFrontier.hasNextCandidate()) {
            CrawlCandidate currentCandidate = crawlFrontier.getNextCandidate();
            String candidateUrl = currentCandidate.getRequestUrl().toString();
            HttpClientContext context = HttpClientContext.create();
            CloseableHttpResponse httpHeadResponse = null;
            boolean isUnsuccessfulRequest = false;

            try {
                // Send an HTTP HEAD request to determine its availability and content type
                httpHeadResponse = getHttpHeadResponse(candidateUrl, context);
            } catch (IOException exception) {
                onRequestError(new RequestErrorEvent(currentCandidate, exception));
                isUnsuccessfulRequest = true;
            }

            if (!isUnsuccessfulRequest) {
                String responseUrl = getFinalResponseUrl(context, candidateUrl);
                if (responseUrl.equals(candidateUrl)) {
                    String responseMimeType = getResponseMimeType(httpHeadResponse);
                    if (responseMimeType.equals(ContentType.TEXT_HTML.getMimeType())) {
                        boolean isTimedOut = false;
                        TimeoutException requestTimeoutException = null;

                        try {
                            // Open URL in browser
                            webDriver.get(candidateUrl);
                        } catch (TimeoutException exception) {
                            isTimedOut = true;
                            requestTimeoutException = exception;
                        }

                        // Ensure the HTTP client and Selenium have the same state
                        syncHttpClientCookies();

                        if (isTimedOut) {
                            onPageLoadTimeout(new PageLoadTimeoutEvent(currentCandidate,
                                    requestTimeoutException));
                        } else {
                            String loadedPageUrl = webDriver.getCurrentUrl();
                            if (!loadedPageUrl.equals(candidateUrl)) {
                                // Create a new crawl request for the redirected URL (JS redirect)
                                handleRequestRedirect(currentCandidate, loadedPageUrl);
                            } else {
                                onPageLoad(new PageLoadEvent(currentCandidate, webDriver));
                            }
                        }
                    } else {
                        // URLs that point to non-HTML content should not be opened in the browser
                        onNonHtmlContent(new NonHtmlContentEvent(currentCandidate,
                                responseMimeType));
                    }
                } else {
                    // Create a new crawl request for the redirected URL
                    handleRequestRedirect(currentCandidate, responseUrl);
                }
            }

            HttpClientUtils.closeQuietly(httpHeadResponse);
            performDelay();
        }

        onStop();
    }

    /**
     * Creates the crawl delay mechanism according to the configuration.
     *
     * @return the created crawl delay mechanism
     */
    @SuppressWarnings("checkstyle:MissingSwitchDefault")
    private CrawlDelayMechanism createCrawlDelayMechanism() {
        switch (config.getCrawlDelayStrategy()) {
            case FIXED:
                return new FixedCrawlDelayMechanism(config);
            case RANDOM:
                return new RandomCrawlDelayMechanism(config);
            case ADAPTIVE:
                return new AdaptiveCrawlDelayMechanism(config, (JavascriptExecutor) webDriver);
        }

        throw new IllegalArgumentException("Unsupported crawl delay strategy.");
    }

    /**
     * Sends an HTTP HEAD request to the given URL and returns the response.
     *
     * @param destinationUrl the destination URL
     *
     * @return the HTTP HEAD response
     *
     * @throws IOException if an error occurs while trying to fulfill the request
     */
    private CloseableHttpResponse getHttpHeadResponse(
            final String destinationUrl,
            final HttpClientContext context) throws IOException {
        HttpHead request = new HttpHead(destinationUrl);
        return httpClient.execute(request, context);
    }

    /**
     * If the HTTP HEAD request was redirected, it returns the final redirected URL. If not, it
     * returns the original URL of the candidate.
     *
     * @param context      the current HTTP client context
     * @param candidateUrl the URL of the candidate
     *
     * @return the final response URL
     */
    private static String getFinalResponseUrl(
            final HttpClientContext context,
            final String candidateUrl) {
        List<URI> redirectLocations = context.getRedirectLocations();
        if (redirectLocations != null) {
            return redirectLocations.get(redirectLocations.size() - 1).toString();
        }

        return candidateUrl;
    }

    /**
     * Returns the MIME type of the HTTP HEAD response. If the Content-Type header is not present in
     * the response it returns "text/plain".
     *
     * @param httpHeadResponse the HTTP HEAD response
     *
     * @return the MIME type of the response
     */
    private static String getResponseMimeType(final HttpResponse httpHeadResponse) {
        Header contentTypeHeader = httpHeadResponse.getFirstHeader("Content-Type");
        if (contentTypeHeader != null) {
            String contentType = contentTypeHeader.getValue();
            if (contentType != null) {
                try {
                    return ContentType.parse(contentType).getMimeType();
                } catch (ParseException | UnsupportedCharsetException exception) {
                    return contentType.split(";")[0].trim();
                }
            }
        }

        return ContentType.DEFAULT_TEXT.getMimeType();
    }

    /**
     * Creates a crawl request for the redirected URL, feeds it to the crawler and calls the
     * appropriate event callback.
     *
     * @param currentCrawlCandidate the current crawl candidate
     * @param redirectedUrl         the URL of the redirected request
     */
    private void handleRequestRedirect(
            final CrawlCandidate currentCrawlCandidate,
            final String redirectedUrl) {
        CrawlRequestBuilder builder = new CrawlRequestBuilder(redirectedUrl)
                .setPriority(currentCrawlCandidate.getPriority());
        currentCrawlCandidate.getMetadata().ifPresent(builder::setMetadata);
        CrawlRequest redirectedRequest = builder.build();

        crawlFrontier.feedRequest(redirectedRequest, false);
        onRequestRedirect(new RequestRedirectEvent(currentCrawlCandidate, redirectedRequest));
    }

    /**
     * Copies all the Selenium cookies for the current domain to the HTTP client cookie store.
     */
    private void syncHttpClientCookies() {
        webDriver.manage()
                .getCookies()
                .stream()
                .map(CookieConverter::convertToHttpClientCookie)
                .forEach(cookieStore::addCookie);
    }

    /**
     * Delays the next request.
     */
    private void performDelay() {
        try {
            TimeUnit.MILLISECONDS.sleep(crawlDelayMechanism.getDelay());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            isStopping = true;
        }
    }

    /**
     * Callback which gets called when the crawler is started.
     */
    protected void onStart() {
        LOGGER.info("onStart");
    }

    /**
     * Callback which gets called when the browser loads the page.
     *
     * @param event the <code>PageLoadEvent</code> instance
     */
    protected void onPageLoad(final PageLoadEvent event) {
        LOGGER.log(Level.INFO, "onPageLoad: {0}", event.getCrawlCandidate().getRequestUrl());
    }

    /**
     * Callback which gets called when the content type is not HTML.
     *
     * @param event the <code>NonHtmlContentEvent</code> instance
     */
    protected void onNonHtmlContent(final NonHtmlContentEvent event) {
        LOGGER.log(Level.INFO, "onNonHtmlContent: {0}", event.getCrawlCandidate().getRequestUrl());
    }

    /**
     * Callback which gets called when a request error occurs.
     *
     * @param event the <code>RequestErrorEvent</code> instance
     */
    protected void onRequestError(final RequestErrorEvent event) {
        LOGGER.log(Level.INFO, "onRequestError: {0}", event.getCrawlCandidate().getRequestUrl());
    }

    /**
     * Callback which gets called when a request is redirected.
     *
     * @param event the <code>RequestRedirectEvent</code> instance
     */
    protected void onRequestRedirect(final RequestRedirectEvent event) {
        LOGGER.log(Level.INFO, "onRequestRedirect: {0} -> {1}",
                new Object[]{
                    event.getCrawlCandidate().getRequestUrl(),
                    event.getRedirectedCrawlRequest().getRequestUrl()
                });
    }

    /**
     * Callback which gets called when the page does not load in the browser within the timeout
     * period.
     *
     * @param event the <code>PageLoadTimeoutEvent</code> instance
     */
    protected void onPageLoadTimeout(final PageLoadTimeoutEvent event) {
        LOGGER.log(Level.INFO, "onPageLoadTimeout: {0}", event.getCrawlCandidate().getRequestUrl());
    }

    /**
     * Callback which gets called when the crawler is stopped.
     */
    protected void onStop() {
        LOGGER.info("onStop");
    }
}
