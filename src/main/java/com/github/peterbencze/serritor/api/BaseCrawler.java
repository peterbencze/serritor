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

import com.github.peterbencze.serritor.api.CrawlRequest.CrawlRequestBuilder;
import com.github.peterbencze.serritor.api.event.NonHtmlContentEvent;
import com.github.peterbencze.serritor.api.event.PageLoadEvent;
import com.github.peterbencze.serritor.api.event.PageLoadTimeoutEvent;
import com.github.peterbencze.serritor.api.event.RequestErrorEvent;
import com.github.peterbencze.serritor.api.event.RequestRedirectEvent;
import com.github.peterbencze.serritor.internal.CrawlFrontier;
import com.github.peterbencze.serritor.internal.crawldelaymechanism.AdaptiveCrawlDelayMechanism;
import com.github.peterbencze.serritor.internal.crawldelaymechanism.CrawlDelayMechanism;
import com.github.peterbencze.serritor.internal.crawldelaymechanism.FixedCrawlDelayMechanism;
import com.github.peterbencze.serritor.internal.crawldelaymechanism.RandomCrawlDelayMechanism;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.openqa.selenium.Cookie;
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
    private boolean canSaveState;

    /**
     * Base constructor of all crawlers.
     *
     * @param config the configuration of the crawler
     */
    protected BaseCrawler(final CrawlerConfiguration config) {
        this.config = config;

        isStopping = false;
        isStopped = true;

        // Cannot save state until the crawler has not been started at least once
        canSaveState = false;
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

            if (!isResuming) {
                cookieStore = new BasicCookieStore();
                crawlFrontier = new CrawlFrontier(config);
            }

            httpClient = HttpClientBuilder.create()
                    .setDefaultCookieStore(cookieStore)
                    .build();
            crawlDelayMechanism = createCrawlDelayMechanism();
            isStopped = false;
            canSaveState = true;

            run();
        } finally {
            HttpClientUtils.closeQuietly(httpClient);
            webDriver.quit();

            isStopping = false;
            isStopped = true;
        }
    }

    /**
     * Saves the current state of the crawler to the given output stream.
     *
     * @param out the output stream
     */
    public final void saveState(final OutputStream out) {
        Validate.validState(canSaveState,
                "Cannot save state at this point. The crawler should be started at least once.");

        HashMap<Class<? extends Serializable>, Serializable> stateObjects = new HashMap<>();
        stateObjects.put(config.getClass(), config);
        stateObjects.put(crawlFrontier.getClass(), crawlFrontier);
        stateObjects.put(cookieStore.getClass(), cookieStore);

        SerializationUtils.serialize(stateObjects, out);
    }

    /**
     * Resumes a previously saved state using HtmlUnit headless browser. This method will block
     * until the crawler finishes.
     *
     * @param in the input stream from which the state should be loaded
     */
    public final void resumeState(final InputStream in) {
        resumeState(new HtmlUnitDriver(true), in);
    }

    /**
     * Resumes a previously saved state using the browser specified by the given
     * <code>WebDriver</code> instance. This method will block until the crawler finishes.
     *
     * @param webDriver the <code>WebDriver</code> instance to control the browser
     * @param in        the input stream from which the state should be loaded
     */
    public final void resumeState(final WebDriver webDriver, final InputStream in) {
        HashMap<Class<? extends Serializable>, Serializable> stateObjects
                = SerializationUtils.deserialize(in);

        config = (CrawlerConfiguration) stateObjects.get(CrawlerConfiguration.class);
        crawlFrontier = (CrawlFrontier) stateObjects.get(CrawlFrontier.class);
        cookieStore = (BasicCookieStore) stateObjects.get(BasicCookieStore.class);

        // Resume crawling
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
        Validate.notNull(request, "The request cannot be null.");
        Validate.validState(!isStopped,
                "The crawler is not started. Maybe you meant to add this request as a crawl seed?");

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

            // Update the client's cookie store, so it will have the same state as the browser
            updateClientCookieStore();

            try {
                // Send an HTTP HEAD request to determine its availability and content type
                httpHeadResponse = getHttpHeadResponse(candidateUrl, context);
            } catch (IOException exception) {
                onRequestError(new RequestErrorEvent(currentCandidate, exception));
                isUnsuccessfulRequest = true;
            }

            if (!isUnsuccessfulRequest) {
                String responseUrl = candidateUrl;
                List<URI> redirectLocations = context.getRedirectLocations();
                if (redirectLocations != null) {
                    // If the request was redirected, get the final URL
                    responseUrl = redirectLocations.get(redirectLocations.size() - 1).toString();
                }

                if (!responseUrl.equals(candidateUrl)) {
                    // Create a new crawl request for the redirected URL
                    handleRequestRedirect(currentCandidate, responseUrl);
                } else if (isContentHtml(httpHeadResponse)) {
                    boolean isTimedOut = false;

                    try {
                        // Open URL in browser
                        webDriver.get(candidateUrl);
                    } catch (TimeoutException exception) {
                        isTimedOut = true;
                        onPageLoadTimeout(new PageLoadTimeoutEvent(currentCandidate, exception));
                    }

                    if (!isTimedOut) {
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
                    onNonHtmlContent(new NonHtmlContentEvent(currentCandidate));
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
     * Indicates if the response's content type is HTML.
     *
     * @param httpHeadResponse the HTTP HEAD response
     *
     * @return <code>true</code> if the content type is HTML, <code>false</code> otherwise
     */
    private static boolean isContentHtml(final HttpResponse httpHeadResponse) {
        Header contentTypeHeader = httpHeadResponse.getFirstHeader("Content-Type");
        return contentTypeHeader != null && contentTypeHeader.getValue().contains("text/html");
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
     * Adds all the browser cookies for the current domain to the HTTP client's cookie store,
     * replacing any existing equivalent ones.
     */
    private void updateClientCookieStore() {
        webDriver.manage()
                .getCookies()
                .stream()
                .map(BaseCrawler::convertBrowserCookie)
                .forEach(cookieStore::addCookie);
    }

    /**
     * Converts a browser cookie to a HTTP client one.
     *
     * @param browserCookie the browser cookie to be converted
     *
     * @return the converted HTTP client cookie
     */
    private static BasicClientCookie convertBrowserCookie(final Cookie browserCookie) {
        BasicClientCookie clientCookie
                = new BasicClientCookie(browserCookie.getName(), browserCookie.getValue());
        clientCookie.setDomain(browserCookie.getDomain());
        clientCookie.setPath(browserCookie.getPath());
        clientCookie.setExpiryDate(browserCookie.getExpiry());
        clientCookie.setSecure(browserCookie.isSecure());

        if (browserCookie.isHttpOnly()) {
            clientCookie.setAttribute("httponly", StringUtils.EMPTY);
        }

        return clientCookie;
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
