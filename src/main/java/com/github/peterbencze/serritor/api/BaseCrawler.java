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
import com.github.peterbencze.serritor.api.event.CrawlEvent;
import com.github.peterbencze.serritor.api.event.NetworkErrorEvent;
import com.github.peterbencze.serritor.api.event.NonHtmlContentEvent;
import com.github.peterbencze.serritor.api.event.PageLoadEvent;
import com.github.peterbencze.serritor.api.event.PageLoadTimeoutEvent;
import com.github.peterbencze.serritor.api.event.RequestErrorEvent;
import com.github.peterbencze.serritor.api.event.RequestRedirectEvent;
import com.github.peterbencze.serritor.internal.CookieConverter;
import com.github.peterbencze.serritor.internal.CrawlFrontier;
import com.github.peterbencze.serritor.internal.CrawlerState;
import com.github.peterbencze.serritor.internal.WebDriverFactory;
import com.github.peterbencze.serritor.internal.crawldelaymechanism.AdaptiveCrawlDelayMechanism;
import com.github.peterbencze.serritor.internal.crawldelaymechanism.CrawlDelayMechanism;
import com.github.peterbencze.serritor.internal.crawldelaymechanism.FixedCrawlDelayMechanism;
import com.github.peterbencze.serritor.internal.crawldelaymechanism.RandomCrawlDelayMechanism;
import com.github.peterbencze.serritor.internal.event.EventCallbackManager;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.UnsupportedCharsetException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.client.ClientUtil;
import net.lightbody.bmp.core.har.HarResponse;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.Validate;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.eclipse.jetty.http.HttpStatus;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

/**
 * Provides a skeletal implementation of a crawler to minimize the effort for users to implement
 * their own.
 *
 * @author Peter Bencze
 */
public abstract class BaseCrawler {

    private static final Logger LOGGER = Logger.getLogger(BaseCrawler.class.getName());

    private CrawlerConfiguration config;
    private EventCallbackManager callbackManager;
    private CrawlFrontier crawlFrontier;
    private BasicCookieStore cookieStore;
    private CloseableHttpClient httpClient;
    private BrowserMobProxyServer proxyServer;
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
        callbackManager = new EventCallbackManager();
        callbackManager.setDefaultEventCallback(CrawlEvent.PAGE_LOAD, this::onPageLoad);
        callbackManager.setDefaultEventCallback(CrawlEvent.NON_HTML_CONTENT,
                this::onNonHtmlContent);
        callbackManager.setDefaultEventCallback(CrawlEvent.PAGE_LOAD_TIMEOUT,
                this::onPageLoadTimeout);
        callbackManager.setDefaultEventCallback(CrawlEvent.REQUEST_REDIRECT,
                this::onRequestRedirect);
        callbackManager.setDefaultEventCallback(CrawlEvent.NETWORK_ERROR, this::onNetworkError);
        callbackManager.setDefaultEventCallback(CrawlEvent.REQUEST_ERROR, this::onRequestError);

        isStopping = false;
        isStopped = true;
    }

    /**
     * Starts the crawler. The crawler will use HtmlUnit headless browser to visit URLs. This method
     * will block until the crawler finishes.
     */
    public final void start() {
        start(Browser.HTML_UNIT);
    }

    /**
     * Starts the crawler. The crawler will use the specified browser to visit URLs. This method
     * will block until the crawler finishes.
     *
     * @param browser the browser type to use for crawling
     */
    public final void start(final Browser browser) {
        start(browser, new DesiredCapabilities());
    }

    /**
     * Starts the crawler. The crawler will use the specified browser to visit URLs.
     *
     * @param browser      the type of the browser to use for crawling
     * @param capabilities the browser properties
     */
    public final void start(final Browser browser, final DesiredCapabilities capabilities) {
        start(browser, capabilities, false);
    }

    /**
     * Performs initialization and runs the crawler.
     *
     * @param isResuming indicates if a previously saved state is to be resumed
     */
    private void start(final Browser browser,
                       final DesiredCapabilities capabilities,
                       final boolean isResuming) {
        try {
            Validate.validState(isStopped, "The crawler is already running.");

            DesiredCapabilities capabilitiesClone = new DesiredCapabilities(capabilities);
            proxyServer = new BrowserMobProxyServer();

            Proxy chainedProxy = (Proxy) capabilitiesClone.getCapability(CapabilityType.PROXY);
            if (chainedProxy != null && chainedProxy.getHttpProxy() != null) {
                String[] urlComponents = chainedProxy.getHttpProxy().split(":");
                String host = urlComponents[0];
                int port = Integer.valueOf(urlComponents[1]);

                proxyServer.setChainedProxy(new InetSocketAddress(host, port));
            }

            proxyServer.start();
            capabilitiesClone.setCapability(CapabilityType.PROXY,
                    ClientUtil.createSeleniumProxy(proxyServer));

            webDriver = WebDriverFactory.createWebDriver(browser, capabilitiesClone);
            webDriver.manage().window().maximize();

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
                    .disableRedirectHandling()
                    .setDefaultCookieStore(cookieStore)
                    .useSystemProperties()
                    .build();
            crawlDelayMechanism = createCrawlDelayMechanism();
            isStopped = false;

            run();
        } finally {
            HttpClientUtils.closeQuietly(httpClient);

            if (webDriver != null) {
                webDriver.quit();
            }

            proxyServer.stop();

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
     * Resumes the previously loaded state. The crawler will use HtmlUnit headless browser to visit
     * URLs. This method will block until the crawler finishes.
     */
    public final void resumeState() {
        resumeState(Browser.HTML_UNIT);
    }

    /**
     * Resumes the previously loaded state. The crawler will use the specified browser to visit
     * URLs. This method will block until the crawler finishes.
     *
     * @param browser the type of the browser to use for crawling
     */
    public final void resumeState(final Browser browser) {
        resumeState(browser, new DesiredCapabilities());
    }

    /**
     * Resumes the previously loaded state. The crawler will use the specified browser to visit
     * URLs. This method will block until the crawler finishes.
     *
     * @param browser      the type of the browser to use for crawling
     * @param capabilities the browser properties
     */
    public final void resumeState(final Browser browser, final DesiredCapabilities capabilities) {
        Validate.validState(crawlFrontier != null, "Cannot resume state at this point.");

        start(browser, capabilities, true);
    }

    /**
     * Registers an operation which is invoked when the specific event occurs and the provided
     * pattern matches the request URL.
     *
     * @param event    the event for which the callback should be triggered
     * @param callback the pattern matching callback to invoke
     */
    protected final void registerCustomEventCallback(
            final CrawlEvent event,
            final PatternMatchingCallback callback) {
        Validate.notNull(event, "The event cannot be null.");
        Validate.notNull(callback, "The callback cannot be null.");

        callbackManager.addCustomEventCallback(event, callback);
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

        boolean shouldPerformDelay = false;

        while (!isStopping && crawlFrontier.hasNextCandidate()) {
            // Do not perform delay in the first iteration
            if (shouldPerformDelay) {
                performDelay();
            } else {
                shouldPerformDelay = true;
            }

            CrawlCandidate currentCandidate = crawlFrontier.getNextCandidate();
            String candidateUrl = currentCandidate.getRequestUrl().toString();
            CloseableHttpResponse httpHeadResponse = null;

            try {
                try {
                    httpHeadResponse = httpClient.execute(new HttpHead(candidateUrl));
                } catch (IOException exception) {
                    callbackManager.call(CrawlEvent.NETWORK_ERROR,
                            new NetworkErrorEvent(currentCandidate, exception.toString()));

                    continue;
                }

                int statusCode = httpHeadResponse.getStatusLine().getStatusCode();
                Header locationHeader = httpHeadResponse.getFirstHeader(HttpHeaders.LOCATION);
                if (HttpStatus.isRedirection(statusCode) && locationHeader != null) {
                    // Create a new crawl request for the redirected URL (HTTP redirect)
                    handleRequestRedirect(currentCandidate,
                            new PartialCrawlResponse(httpHeadResponse), locationHeader.getValue());

                    continue;
                }

                String mimeType = getResponseMimeType(httpHeadResponse);
                if (!mimeType.equals(ContentType.TEXT_HTML.getMimeType())) {
                    // URLs that point to non-HTML content should not be opened in the browser
                    callbackManager.call(CrawlEvent.NON_HTML_CONTENT,
                            new NonHtmlContentEvent(currentCandidate,
                                    new PartialCrawlResponse(httpHeadResponse)));

                    continue;
                }

                proxyServer.newHar();

                try {
                    webDriver.get(candidateUrl);

                    // Ensure HTTP client and Selenium have the same cookies
                    syncHttpClientCookies();
                } catch (TimeoutException exception) {
                    callbackManager.call(CrawlEvent.PAGE_LOAD_TIMEOUT,
                            new PageLoadTimeoutEvent(currentCandidate,
                                    new PartialCrawlResponse(httpHeadResponse)));

                    continue;
                }
            } finally {
                HttpClientUtils.closeQuietly(httpHeadResponse);
            }

            HarResponse harResponse = proxyServer.getHar()
                    .getLog()
                    .getEntries()
                    .get(0)
                    .getResponse();
            if (harResponse.getError() != null) {
                callbackManager.call(CrawlEvent.NETWORK_ERROR,
                        new NetworkErrorEvent(currentCandidate, harResponse.getError()));

                continue;
            }

            int statusCode = harResponse.getStatus();
            if (HttpStatus.isClientError(statusCode) || HttpStatus.isServerError(statusCode)) {
                callbackManager.call(CrawlEvent.REQUEST_ERROR,
                        new RequestErrorEvent(currentCandidate,
                                new CompleteCrawlResponse(harResponse, webDriver)));

                continue;
            }

            String loadedPageUrl = webDriver.getCurrentUrl();
            if (!loadedPageUrl.equals(candidateUrl)) {
                // Create a new crawl request for the redirected URL (JS redirect)
                handleRequestRedirect(currentCandidate,
                        new PartialCrawlResponse(harResponse), loadedPageUrl);

                continue;
            }

            callbackManager.call(CrawlEvent.PAGE_LOAD,
                    new PageLoadEvent(currentCandidate,
                            new CompleteCrawlResponse(harResponse, webDriver)));
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
     * @param crawlCandidate       the current crawl candidate
     * @param partialCrawlResponse the partial crawl response
     * @param redirectedUrl        the URL of the redirected request
     */
    private void handleRequestRedirect(
            final CrawlCandidate crawlCandidate,
            final PartialCrawlResponse partialCrawlResponse,
            final String redirectedUrl) {
        CrawlRequestBuilder builder = new CrawlRequestBuilder(redirectedUrl)
                .setPriority(crawlCandidate.getPriority());
        crawlCandidate.getMetadata().ifPresent(builder::setMetadata);
        CrawlRequest redirectedRequest = builder.build();

        crawlFrontier.feedRequest(redirectedRequest, false);

        callbackManager.call(CrawlEvent.REQUEST_REDIRECT,
                new RequestRedirectEvent(crawlCandidate, partialCrawlResponse, redirectedRequest));
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
     * Callback which gets called when a network error occurs.
     *
     * @param event the <code>NetworkErrorEvent</code> instance
     */
    protected void onNetworkError(final NetworkErrorEvent event) {
        LOGGER.log(Level.INFO, "onNetworkError: {0}", event.getErrorMessage());
    }

    /**
     * Callback which gets called when a request error (an error with HTTP status code 4xx or 5xx)
     * occurs.
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
