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
import com.github.peterbencze.serritor.api.event.NetworkErrorEvent;
import com.github.peterbencze.serritor.api.event.NonHtmlResponseEvent;
import com.github.peterbencze.serritor.api.event.PageLoadTimeoutEvent;
import com.github.peterbencze.serritor.api.event.RequestRedirectEvent;
import com.github.peterbencze.serritor.api.event.ResponseErrorEvent;
import com.github.peterbencze.serritor.api.event.ResponseSuccessEvent;
import com.github.peterbencze.serritor.internal.CrawlEvent;
import com.github.peterbencze.serritor.internal.CrawlFrontier;
import com.github.peterbencze.serritor.internal.CustomCallbackManager;
import com.github.peterbencze.serritor.internal.WebDriverFactory;
import com.github.peterbencze.serritor.internal.crawldelaymechanism.AdaptiveCrawlDelayMechanism;
import com.github.peterbencze.serritor.internal.crawldelaymechanism.CrawlDelayMechanism;
import com.github.peterbencze.serritor.internal.crawldelaymechanism.FixedCrawlDelayMechanism;
import com.github.peterbencze.serritor.internal.crawldelaymechanism.RandomCrawlDelayMechanism;
import com.github.peterbencze.serritor.internal.stats.StatsCounter;
import com.github.peterbencze.serritor.internal.util.CookieConverter;
import com.github.peterbencze.serritor.internal.util.stopwatch.Stopwatch;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.client.ClientUtil;
import net.lightbody.bmp.core.har.HarResponse;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.Validate;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
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
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.Options;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a skeletal implementation of a crawler to minimize the effort for users to implement
 * their own.
 */
public abstract class Crawler {

    private static final Logger LOGGER = LoggerFactory.getLogger(Crawler.class);

    private final CrawlerConfiguration config;
    private final Stopwatch runTimeStopwatch;
    private final StatsCounter statsCounter;
    private final CrawlFrontier crawlFrontier;
    private final CustomCallbackManager callbackManager;
    private final AtomicBoolean isStopped;
    private final AtomicBoolean isStopInitiated;

    private BasicCookieStore cookieStore;
    private CloseableHttpClient httpClient;
    private BrowserMobProxyServer proxyServer;
    private WebDriver webDriver;
    private CrawlDelayMechanism crawlDelayMechanism;

    /**
     * Base constructor which sets up the crawler with the provided configuration.
     *
     * @param config the configuration of the crawler
     */
    protected Crawler(final CrawlerConfiguration config) {
        this(new CrawlerState(Validate.notNull(config, "The config parameter cannot be null")));
    }

    /**
     * Base constructor which restores the crawler to the provided state.
     *
     * @param state the state to restore the crawler to
     */
    protected Crawler(final CrawlerState state) {
        Validate.notNull(state, "The state parameter cannot be null");

        config = state.getStateObject(CrawlerConfiguration.class)
                .orElseThrow(() -> new IllegalArgumentException("Invalid crawler state provided"));
        runTimeStopwatch = state.getStateObject(Stopwatch.class).orElseGet(Stopwatch::new);
        statsCounter = state.getStateObject(StatsCounter.class).orElseGet(StatsCounter::new);
        crawlFrontier = state.getStateObject(CrawlFrontier.class)
                .orElseGet(() -> new CrawlFrontier(config, statsCounter));

        callbackManager = new CustomCallbackManager();

        isStopInitiated = new AtomicBoolean(false);
        isStopped = new AtomicBoolean(true);
    }

    /**
     * Returns the configuration of the crawler. This method is thread-safe.
     *
     * @return the configuration of the crawler
     */
    public final CrawlerConfiguration getCrawlerConfiguration() {
        return config;
    }

    /**
     * Returns summary statistics about the crawl progress. This method is thread-safe.
     *
     * @return summary statistics about the crawl progress
     */
    public final CrawlStats getCrawlStats() {
        return new CrawlStats(runTimeStopwatch.getElapsedDuration(), statsCounter.getSnapshot());
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
    public final void start(final Browser browser, final MutableCapabilities capabilities) {
        Validate.notNull(browser, "The browser parameter cannot be null.");
        Validate.notNull(capabilities, "The capabilities parameter cannot be null.");

        start(browser, capabilities, false);
    }

    /**
     * Performs initialization and runs the crawler.
     *
     * @param isResuming indicates if a previously saved state is to be resumed
     */
    private void start(
            final Browser browser,
            final MutableCapabilities capabilities,
            final boolean isResuming) {
        try {
            Validate.validState(isStopped.get(), "The crawler is already running.");

            LOGGER.debug("Crawler is starting (resuming crawl: {})", isResuming);
            LOGGER.debug("Using configuration: {}", config);

            isStopped.set(false);
            runTimeStopwatch.start();

            if (!isResuming) {
                crawlFrontier.reset();
                statsCounter.reset();
                config.getCrawlSeeds().forEach(request -> crawlFrontier.feedRequest(request, true));
            }

            cookieStore = new BasicCookieStore();
            HttpClientBuilder httpClientBuilder = HttpClientBuilder.create()
                    .disableRedirectHandling()
                    .setDefaultCookieStore(cookieStore);

            // Create a copy of the original capabilities before we make changes to it (we don't
            // want to cause any unwanted side effects)
            MutableCapabilities capabilitiesClone = new MutableCapabilities(capabilities);

            proxyServer = new BrowserMobProxyServer();

            // If a user-defined proxy is set, chain it to our internal one
            Proxy proxyCapability = (Proxy) capabilitiesClone.getCapability(CapabilityType.PROXY);
            if (proxyCapability != null && proxyCapability.getHttpProxy() != null) {
                HttpHost proxyHost = HttpHost.create(proxyCapability.getHttpProxy());
                String hostname = proxyHost.getHostName();
                int port = proxyHost.getPort();

                LOGGER.debug("Using chained HTTP proxy with address {}:{}", hostname, port);

                httpClientBuilder.setProxy(proxyHost);
                proxyServer.setChainedProxy(new InetSocketAddress(hostname, port));
            }

            // The internal proxy server must be started before creating the Selenium proxy
            // because the port is dynamically chosen by the server when it starts
            proxyServer.start();
            LOGGER.debug("Internal proxy server started on port {}", proxyServer.getPort());

            // Set our internal proxy
            capabilitiesClone.setCapability(CapabilityType.PROXY,
                    ClientUtil.createSeleniumProxy(proxyServer));

            httpClient = httpClientBuilder.build();

            LOGGER.debug("Starting {} browser", browser);
            webDriver = WebDriverFactory.createWebDriver(browser, capabilitiesClone);

            LOGGER.debug("Calling onBrowserInit callback");
            onBrowserInit(webDriver.manage());

            // If the crawl delay strategy is set to adaptive, we check if the browser supports the
            // Navigation Timing API or not. However HtmlUnit requires a page to be loaded first
            // before executing JavaScript, so we load a blank page.
            if (Browser.HTML_UNIT.equals(browser)
                    && CrawlDelayStrategy.ADAPTIVE.equals(config.getCrawlDelayStrategy())) {
                webDriver.get(WebClient.ABOUT_BLANK);
            }

            // Must be created here (the adaptive crawl delay strategy depends on the WebDriver)
            crawlDelayMechanism = createCrawlDelayMechanism();

            LOGGER.debug("Calling onStart callback");
            onStart();

            run();
        } finally {
            LOGGER.debug("Crawler is stopping");

            try {
                LOGGER.debug("Calling onStop callback");
                onStop();
            } finally {
                HttpClientUtils.closeQuietly(httpClient);

                if (webDriver != null) {
                    LOGGER.debug("Closing browser");
                    webDriver.quit();
                }

                if (proxyServer != null && proxyServer.isStarted()) {
                    LOGGER.debug("Stopping proxy server");
                    proxyServer.stop();
                }

                runTimeStopwatch.stop();

                isStopInitiated.set(false);
                isStopped.set(true);
            }
        }
    }

    /**
     * Returns the current state of the crawler.
     *
     * @return the current state of the crawler
     */
    public final CrawlerState getState() {
        return new CrawlerState(Arrays.asList(config, crawlFrontier, runTimeStopwatch,
                statsCounter));
    }

    /**
     * Resumes the crawl. The crawler will use HtmlUnit headless browser to visit URLs. This method
     * will block until the crawler finishes.
     */
    public final void resume() {
        resume(Browser.HTML_UNIT);
    }

    /**
     * Resumes the crawl. The crawler will use the specified browser to visit URLs. This method will
     * block until the crawler finishes.
     *
     * @param browser the type of the browser to use for crawling
     */
    public final void resume(final Browser browser) {
        resume(browser, new DesiredCapabilities());
    }

    /**
     * Resumes the crawl. The crawler will use the specified browser to visit URLs. This method will
     * block until the crawler finishes.
     *
     * @param browser      the type of the browser to use for crawling
     * @param capabilities the browser properties
     */
    public final void resume(final Browser browser, final DesiredCapabilities capabilities) {
        start(browser, capabilities, true);
    }

    /**
     * Registers an operation which is invoked when the specific event occurs and the provided
     * pattern matches the request URL.
     *
     * @param <T>        the type of the input to the operation
     * @param eventClass the runtime class of the event for which the callback should be invoked
     * @param callback   the pattern matching callback to invoke
     */
    protected final <T extends CrawlEvent> void registerCustomCallback(
            final Class<T> eventClass,
            final PatternMatchingCallback<T> callback) {
        Validate.notNull(eventClass, "The eventClass parameter cannot be null.");
        Validate.notNull(callback, "The callback parameter cannot be null.");

        callbackManager.addCustomCallback(eventClass, callback);
    }

    /**
     * Gracefully stops the crawler. This method is thread-safe.
     */
    protected final void stop() {
        Validate.validState(!isStopped.get(), "The crawler is not started.");

        LOGGER.debug("Initiating stop");

        // Indicate that the crawling should be stopped
        isStopInitiated.set(true);
    }

    /**
     * Feeds a crawl request to the crawler. The crawler should be running, otherwise the request
     * has to be added as a crawl seed instead.
     *
     * @param request the crawl request
     */
    protected final void crawl(final CrawlRequest request) {
        Validate.validState(!isStopped.get(),
                "The crawler is not started. Maybe you meant to add this request as a crawl seed?");
        Validate.notNull(request, "The request parameter cannot be null.");

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
        Validate.validState(!isStopped.get(),
                "Cannot download file when the crawler is not started.");
        Validate.notNull(source, "The source parameter cannot be null.");
        Validate.notNull(destination, "The destination parameter cannot be null.");

        LOGGER.debug("Downloading file from {} to {}", source, destination);

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
        boolean shouldPerformDelay = false;

        while (!isStopInitiated.get() && crawlFrontier.hasNextCandidate()) {
            // Do not perform delay in the first iteration
            if (shouldPerformDelay) {
                performDelay();
            } else {
                shouldPerformDelay = true;
            }

            CrawlCandidate currentCandidate = crawlFrontier.getNextCandidate();
            LOGGER.debug("Next crawl candidate: {}", currentCandidate);

            String candidateUrl = currentCandidate.getRequestUrl().toString();
            CloseableHttpResponse httpHeadResponse = null;

            try {
                LOGGER.debug("Sending HTTP head request to URL {}", candidateUrl);

                try {
                    httpHeadResponse = httpClient.execute(new HttpHead(candidateUrl));
                } catch (IOException exception) {
                    handleNetworkError(new NetworkErrorEvent(currentCandidate,
                            exception.toString()));

                    continue;
                }

                int statusCode = httpHeadResponse.getStatusLine().getStatusCode();

                // Check if there was an HTTP redirect
                Header locationHeader = httpHeadResponse.getFirstHeader(HttpHeaders.LOCATION);
                if (HttpStatus.isRedirection(statusCode) && locationHeader != null) {
                    // Create a new crawl request for the redirected URL (HTTP redirect)
                    CrawlRequest redirectedRequest =
                            createCrawlRequestForRedirect(currentCandidate,
                                    locationHeader.getValue());

                    handleRequestRedirect(new RequestRedirectEvent(currentCandidate,
                            new PartialCrawlResponse(httpHeadResponse), redirectedRequest));

                    continue;
                }

                String mimeType = getResponseMimeType(httpHeadResponse);
                if (!mimeType.equals(ContentType.TEXT_HTML.getMimeType())) {
                    // URLs that point to non-HTML content should not be opened in the browser
                    handleNonHtmlResponse(new NonHtmlResponseEvent(currentCandidate,
                            new PartialCrawlResponse(httpHeadResponse)));

                    continue;
                }

                proxyServer.newHar();

                LOGGER.debug("Opening URL {} in browser", candidateUrl);
                try {
                    webDriver.get(candidateUrl);

                    // Ensure HTTP client and Selenium have the same cookies
                    syncHttpClientCookies();
                } catch (TimeoutException exception) {
                    handlePageLoadTimeout(new PageLoadTimeoutEvent(currentCandidate,
                            new PartialCrawlResponse(httpHeadResponse)));

                    continue;
                }
            } finally {
                HttpClientUtils.closeQuietly(httpHeadResponse);
            }

            HarResponse harResponse = proxyServer.getHar().getLog().getEntries().stream()
                    .filter(harEntry -> candidateUrl.equals(harEntry.getRequest().getUrl()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("No HAR entry for request URL"))
                    .getResponse();
            if (harResponse.getError() != null) {
                handleNetworkError(new NetworkErrorEvent(currentCandidate, harResponse.getError()));

                continue;
            }

            // We need to check both the redirect URL in the HAR response and the URL of the
            // loaded page to see if there was a JS redirect
            String redirectUrl = harResponse.getRedirectURL();
            String loadedPageUrl = webDriver.getCurrentUrl();
            if (!redirectUrl.isEmpty() || !loadedPageUrl.equals(candidateUrl)) {
                if (redirectUrl.isEmpty()) {
                    redirectUrl = loadedPageUrl;
                }

                CrawlRequest request = createCrawlRequestForRedirect(currentCandidate, redirectUrl);

                handleRequestRedirect(new RequestRedirectEvent(currentCandidate,
                        new PartialCrawlResponse(harResponse), request));

                continue;
            }

            int statusCode = harResponse.getStatus();
            if (HttpStatus.isClientError(statusCode) || HttpStatus.isServerError(statusCode)) {
                handleResponseError(new ResponseErrorEvent(currentCandidate,
                        new CompleteCrawlResponse(harResponse, webDriver)));

                continue;
            }

            handleResponseSuccess(new ResponseSuccessEvent(currentCandidate,
                    new CompleteCrawlResponse(harResponse, webDriver)));
        }
    }

    /**
     * Creates the crawl delay mechanism according to the configuration.
     *
     * @return the created crawl delay mechanism
     */
    private CrawlDelayMechanism createCrawlDelayMechanism() {
        switch (config.getCrawlDelayStrategy()) {
            case FIXED:
                return new FixedCrawlDelayMechanism(config);
            case RANDOM:
                return new RandomCrawlDelayMechanism(config);
            case ADAPTIVE:
                return new AdaptiveCrawlDelayMechanism(config, (JavascriptExecutor) webDriver);
            default:
                throw new IllegalArgumentException("Unsupported crawl delay strategy");
        }
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
     * Handles network errors.
     *
     * @param event the event which gets delivered when a network error occurs
     */
    private void handleNetworkError(final NetworkErrorEvent event) {
        LOGGER.debug("Network error occurred: {}", event.getErrorMessage());

        callbackManager.callCustomOrDefault(NetworkErrorEvent.class, event, this::onNetworkError);

        statsCounter.recordNetworkError();
    }

    /**
     * Handles request redirects.
     *
     * @param event the event which gets delivered when a request is redirected
     */
    private void handleRequestRedirect(final RequestRedirectEvent event) {
        LOGGER.debug("Request redirected from {} to {}",
                event.getCrawlCandidate().getRequestUrl(),
                event.getRedirectedCrawlRequest().getRequestUrl());

        crawl(event.getRedirectedCrawlRequest());

        callbackManager.callCustomOrDefault(RequestRedirectEvent.class, event,
                this::onRequestRedirect);

        statsCounter.recordRequestRedirect();
    }

    /**
     * Handles responses with non-HTML content.
     *
     * @param event the event which gets delivered when the content type of the response is not
     *              text/html
     */
    private void handleNonHtmlResponse(final NonHtmlResponseEvent event) {
        LOGGER.debug("Received response with non-HTML content");

        callbackManager.callCustomOrDefault(NonHtmlResponseEvent.class, event,
                this::onNonHtmlResponse);

        statsCounter.recordNonHtmlResponse();
    }

    /**
     * Handles page load timeouts.
     *
     * @param event the event which gets delivered when a page does not load in the browser within
     *              the timeout period
     */
    private void handlePageLoadTimeout(final PageLoadTimeoutEvent event) {
        LOGGER.debug("Page did not load in the browser within the timeout period");

        callbackManager.callCustomOrDefault(PageLoadTimeoutEvent.class, event,
                this::onPageLoadTimeout);

        statsCounter.recordPageLoadTimeout();
    }

    /**
     * Handles responses whose HTTP status code indicates an error.
     *
     * @param event the event which gets delivered when the browser loads the page and the HTTP
     *              status code indicates error (4xx or 5xx)
     */
    private void handleResponseError(final ResponseErrorEvent event) {
        LOGGER.debug("Received response whose status code ({}) indicates error",
                event.getCompleteCrawlResponse().getStatusCode());

        callbackManager.callCustomOrDefault(ResponseErrorEvent.class, event, this::onResponseError);

        statsCounter.recordResponseError();
    }

    /**
     * Handles responses whose HTTP status code indicates success.
     *
     * @param event the event which gets delivered when the browser loads the page and the HTTP
     *              status code indicates success (2xx)
     */
    private void handleResponseSuccess(final ResponseSuccessEvent event) {
        LOGGER.debug("Received response whose status code ({}) indicates success",
                event.getCompleteCrawlResponse().getStatusCode());

        callbackManager.callCustomOrDefault(ResponseSuccessEvent.class, event,
                this::onResponseSuccess);

        statsCounter.recordResponseSuccess();
    }

    /**
     * Copies all the Selenium cookies for the current domain to the HTTP client cookie store.
     */
    private void syncHttpClientCookies() {
        LOGGER.debug("Synchronizing HTTP client cookies");

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
        LOGGER.debug("Performing delay");

        try {
            TimeUnit.MILLISECONDS.sleep(crawlDelayMechanism.getDelay());
        } catch (InterruptedException ex) {
            LOGGER.debug("Delay interrupted, stopping crawler");
            Thread.currentThread().interrupt();
            isStopInitiated.set(true);
        }
    }

    /**
     * Helper method that is used to create crawl requests for redirects. The newly created request
     * will have the same attributes as the redirected one.
     *
     * @param currentCandidate the current crawl candidate
     * @param redirectUrl      the redirect URL
     *
     * @return the crawl request for the redirect URL
     */
    private static CrawlRequest createCrawlRequestForRedirect(
            final CrawlCandidate currentCandidate,
            final String redirectUrl) {
        // Handle relative redirect URLs
        URI resolvedUrl = currentCandidate.getRequestUrl().resolve(redirectUrl);

        CrawlRequestBuilder builder = new CrawlRequestBuilder(resolvedUrl)
                .setPriority(currentCandidate.getPriority());
        currentCandidate.getMetadata().ifPresent(builder::setMetadata);

        return builder.build();
    }

    /**
     * Callback which is used to configure the browser before the crawling begins.
     *
     * @param options an interface for managing stuff you would do in a browser menu
     */
    protected void onBrowserInit(final Options options) {
        LOGGER.info("onBrowserInit");

        options.timeouts()
                .pageLoadTimeout(CrawlerConfiguration.DEFAULT_PAGE_LOAD_TIMEOUT_IN_MILLIS,
                        TimeUnit.MILLISECONDS);

        options.window().maximize();
    }

    /**
     * Callback which gets called when the crawler starts.
     */
    protected void onStart() {
        LOGGER.info("onStart");
    }

    /**
     * Callback which gets called when the browser loads the page and the HTTP status code of the
     * response indicates success (2xx).
     *
     * @param event the <code>ResponseSuccessEvent</code> instance
     */
    protected void onResponseSuccess(final ResponseSuccessEvent event) {
        LOGGER.info("onResponseSuccess: {}", event.getCrawlCandidate().getRequestUrl());
    }

    /**
     * Callback which gets called when the content type of the response is not text/html.
     *
     * @param event the <code>NonHtmlResponseEvent</code> instance
     */
    protected void onNonHtmlResponse(final NonHtmlResponseEvent event) {
        LOGGER.info("onNonHtmlResponse: {}", event.getCrawlCandidate().getRequestUrl());
    }

    /**
     * Callback which gets called when a network error occurs.
     *
     * @param event the <code>NetworkErrorEvent</code> instance
     */
    protected void onNetworkError(final NetworkErrorEvent event) {
        LOGGER.info("onNetworkError: {}", event.getErrorMessage());
    }

    /**
     * Callback which gets called when the browser loads the page and the HTTP status code of the
     * response indicates error (4xx or 5xx).
     *
     * @param event the <code>ResponseErrorEvent</code> instance
     */
    protected void onResponseError(final ResponseErrorEvent event) {
        LOGGER.info("onResponseError: {}", event.getCrawlCandidate().getRequestUrl());
    }

    /**
     * Callback which gets called when a request is redirected.
     *
     * @param event the <code>RequestRedirectEvent</code> instance
     */
    protected void onRequestRedirect(final RequestRedirectEvent event) {
        LOGGER.info("onRequestRedirect: {} -> {}", event.getCrawlCandidate().getRequestUrl(),
                event.getRedirectedCrawlRequest().getRequestUrl());
    }

    /**
     * Callback which gets called when the page does not load in the browser within the timeout
     * period.
     *
     * @param event the <code>PageLoadTimeoutEvent</code> instance
     */
    protected void onPageLoadTimeout(final PageLoadTimeoutEvent event) {
        LOGGER.info("onPageLoadTimeout: {}", event.getCrawlCandidate().getRequestUrl());
    }

    /**
     * Callback which gets called when the crawler stops.
     */
    protected void onStop() {
        LOGGER.info("onStop");
    }
}
