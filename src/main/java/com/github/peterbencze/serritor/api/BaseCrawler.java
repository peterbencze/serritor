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
import com.github.peterbencze.serritor.api.HtmlResponse.HtmlResponseBuilder;
import com.github.peterbencze.serritor.api.NonHtmlResponse.NonHtmlResponseBuilder;
import com.github.peterbencze.serritor.api.UnsuccessfulRequest.UnsuccessfulRequestBuilder;
import com.github.peterbencze.serritor.internal.AdaptiveCrawlDelayMechanism;
import com.github.peterbencze.serritor.internal.CrawlCandidate;
import com.github.peterbencze.serritor.internal.CrawlDelayMechanism;
import com.github.peterbencze.serritor.internal.CrawlFrontier;
import com.github.peterbencze.serritor.internal.FixedCrawlDelayMechanism;
import com.github.peterbencze.serritor.internal.RandomCrawlDelayMechanism;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

/**
 * Provides a skeletal implementation of a crawler to minimize the effort for
 * users to implement their own.
 *
 * @author Peter Bencze
 */
public abstract class BaseCrawler {

    private final CrawlerConfiguration config;

    private boolean isStopped;
    private boolean stopCrawling;
    private BasicCookieStore cookieStore;
    private HttpClient httpClient;
    private WebDriver webDriver;
    private CrawlFrontier crawlFrontier;
    private CrawlDelayMechanism crawlDelayMechanism;

    protected BaseCrawler(final CrawlerConfiguration config) {
        this.config = config;

        // Indicate that the crawler is not running
        isStopped = true;
    }

    /**
     * Starts the crawler using HtmlUnit headless browser.
     */
    public final void start() {
        start(new HtmlUnitDriver(true));
    }

    /**
     * Starts the crawler using the browser specified by the
     * <code>WebDriver</code> instance.
     *
     * @param driver The <code>WebDriver</code> instance that will be used by
     * the crawler
     */
    public final void start(final WebDriver driver) {
        start(driver, new CrawlFrontier(config));
    }

    /**
     * Constructs all the necessary objects and runs the crawler.
     *
     * @param frontierToUse The <code>CrawlFrontier</code> instance to be used
     * by the crawler.
     */
    private void start(final WebDriver driver, final CrawlFrontier frontierToUse) {
        try {
            Validate.validState(isStopped, "The crawler is already started.");

            isStopped = false;
            cookieStore = new BasicCookieStore();
            httpClient = HttpClientBuilder.create()
                    .setDefaultCookieStore(cookieStore)
                    .build();
            webDriver = Validate.notNull(driver, "The webdriver cannot be null.");
            crawlFrontier = frontierToUse;
            crawlDelayMechanism = createCrawlDelayMechanism();

            run();
        } finally {
            // Always close the browser
            webDriver.quit();

            stopCrawling = false;
            isStopped = true;
        }
    }

    /**
     * Saves the current state of the crawler to the specified output stream.
     *
     * @param out The <code>OutputStream</code> instance to use
     */
    public final void saveState(final OutputStream out) {
        // Check if the crawler has been started at least once, otherwise we have nothing to save
        Validate.validState(crawlFrontier != null, "Cannot save state at this point. The crawler should be started first.");

        // Save the crawl frontier's current state
        SerializationUtils.serialize(crawlFrontier, out);
    }

    /**
     * Resumes a previously saved state using HtmlUnit headless browser.
     *
     * @param in The <code>InputStream</code> instance to use
     */
    public final void resumeState(final InputStream in) {
        resumeState(new HtmlUnitDriver(true), in);
    }

    /**
     * Resumes a previously saved state using the browser specified by the
     * WebDriver instance.
     *
     * @param driver The <code>WebDriver</code> instance to be used by the
     * crawler
     * @param in The <code>InputStream</code> instance to use
     */
    public final void resumeState(final WebDriver driver, final InputStream in) {
        // Re-create crawl frontier from the saved state
        CrawlFrontier frontierToUse = SerializationUtils.deserialize(in);

        start(driver, frontierToUse);
    }

    /**
     * Stops the crawler.
     */
    public final void stop() {
        Validate.validState(!isStopped, "The crawler is not started.");
        Validate.validState(!stopCrawling, "The stop method has already been called.");

        // Indicate that the crawling should be stopped
        stopCrawling = true;
    }

    /**
     * Passes a crawl request to the crawl frontier. The crawler must be
     * running, otherwise use
     * {@link CrawlerConfiguration.CrawlerConfigurationBuilder#addCrawlSeed(com.github.peterbencze.serritor.api.CrawlRequest)}
     * for adding crawl seeds.
     *
     * @param request The <code>CrawlRequest</code> instance
     */
    protected final void crawl(final CrawlRequest request) {
        Validate.notNull(request, "The request cannot be null.");
        Validate.validState(!isStopped, "The crawler is not started. Maybe you meant to add this request as a crawl seed?");

        crawlFrontier.feedRequest(request, false);
    }

    /**
     * Passes multiple crawl requests to the crawl frontier. The crawler must be
     * running, otherwise use
     * {@link CrawlerConfiguration.CrawlerConfigurationBuilder#addCrawlSeeds(java.util.List)}
     * for adding crawl seeds.
     *
     * @param requests The list of <code>CrawlRequest</code> instances
     */
    protected final void crawl(final List<CrawlRequest> requests) {
        requests.forEach(this::crawl);
    }

    /**
     * Defines the workflow of the crawler.
     */
    private void run() {
        onBegin();

        while (!stopCrawling && crawlFrontier.hasNextCandidate()) {
            // Get the next crawl candidate from the queue
            CrawlCandidate currentCandidate = crawlFrontier.getNextCandidate();

            URI currentCandidateUrl = currentCandidate.getCandidateUrl();
            String currentRequestUrlAsString = currentCandidateUrl.toString();

            HttpHeadResponse httpHeadResponse;
            URI responseUrl = currentCandidateUrl;

            try {
                HttpClientContext context = HttpClientContext.create();

                // Send an HTTP HEAD request to the current URL to determine its availability and content type
                httpHeadResponse = getHttpHeadResponse(currentCandidateUrl, context);

                // If the request has been redirected, get the final URL
                List<URI> redirectLocations = context.getRedirectLocations();
                if (redirectLocations != null) {
                    responseUrl = redirectLocations.get(redirectLocations.size() - 1);
                }
            } catch (IOException ex) {
                UnsuccessfulRequest unsuccessfulRequest = new UnsuccessfulRequestBuilder(currentCandidate.getRefererUrl(), currentCandidate.getCrawlDepth(),
                        currentCandidate.getCrawlRequest())
                        .setException(ex)
                        .build();

                onUnsuccessfulRequest(unsuccessfulRequest);
                continue;
            }

            // If the request has been redirected, a new crawl request should be created for the redirected URL
            if (!responseUrl.toString().equals(currentRequestUrlAsString)) {
                CrawlRequest redirectedCrawlRequest = new CrawlRequestBuilder(responseUrl).setPriority(currentCandidate.getPriority()).build();
                crawlFrontier.feedRequest(redirectedCrawlRequest, false);

                continue;
            }

            // Check if the content of the response is HTML
            if (isContentHtml(httpHeadResponse)) {
                boolean timedOut = false;

                try {
                    // Open the URL in the browser
                    webDriver.get(currentRequestUrlAsString);
                } catch (TimeoutException ex) {
                    timedOut = true;
                }

                HtmlResponse htmlResponse = new HtmlResponseBuilder(currentCandidate.getRefererUrl(), currentCandidate.getCrawlDepth(),
                        currentCandidate.getCrawlRequest())
                        .setHttpHeadResponse(httpHeadResponse)
                        .setWebDriver(webDriver)
                        .build();

                // Check if the request has timed out
                if (!timedOut) {
                    onResponseComplete(htmlResponse);
                } else {
                    onResponseTimeout(htmlResponse);
                }

                // Update the client's cookie store, so it will have the same state as the browser.
                updateClientCookieStore();
            } else {
                // URLs that point to non-HTML content should not be opened in the browser

                NonHtmlResponse nonHtmlResponse = new NonHtmlResponseBuilder(currentCandidate.getRefererUrl(), currentCandidate.getCrawlDepth(),
                        currentCandidate.getCrawlRequest())
                        .setHttpHeadResponse(httpHeadResponse)
                        .build();

                onNonHtmlResponse(nonHtmlResponse);
            }

            performDelay();
        }

        onFinish();
    }

    /**
     * Returns a HTTP HEAD response for the given URL.
     *
     * @param destinationUrl The URL to crawl
     * @return The HTTP HEAD response
     */
    private HttpHeadResponse getHttpHeadResponse(final URI destinationUrl, final HttpClientContext context) throws IOException {
        HttpHead headRequest = new HttpHead(destinationUrl.toString());
        HttpResponse response = httpClient.execute(headRequest, context);
        return new HttpHeadResponse(response);
    }

    /**
     * Indicates if the content of the response is HTML or not.
     *
     * @param httpHeadResponse The HTTP HEAD response
     * @return <code>true</code> if the content is HTML, <code>false</code>
     * otherwise
     */
    private static boolean isContentHtml(final HttpHeadResponse httpHeadResponse) {
        Header contentTypeHeader = httpHeadResponse.getFirstHeader("Content-Type");
        return contentTypeHeader != null && contentTypeHeader.getValue().contains("text/html");
    }

    /**
     * Constructs the crawl delay mechanism specified in the configuration.
     *
     * @return The crawl delay mechanism
     */
    private CrawlDelayMechanism createCrawlDelayMechanism() {
        switch (config.getCrawlDelayStrategy()) {
            case FIXED:
                return new FixedCrawlDelayMechanism(config);
            case RANDOM:
                return new RandomCrawlDelayMechanism(config);
            case ADAPTIVE:
                AdaptiveCrawlDelayMechanism adaptiveCrawlDelay = new AdaptiveCrawlDelayMechanism(config, (JavascriptExecutor) webDriver);
                if (!adaptiveCrawlDelay.isBrowserCompatible()) {
                    throw new UnsupportedOperationException("The Navigation Timing API is not supported by the browser.");
                }

                return adaptiveCrawlDelay;
        }

        throw new IllegalArgumentException("Unsupported crawl delay strategy.");
    }

    /**
     * Delays the next request.
     */
    private void performDelay() {
        try {
            TimeUnit.MILLISECONDS.sleep(crawlDelayMechanism.getDelay());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            stopCrawling = true;
        }
    }

    /**
     * Adds all the browser cookies for the current domain to the HTTP client's
     * cookie store, replacing any existing equivalent ones.
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
     * @param browserCookie The browser cookie to be converted
     * @return The converted HTTP client cookie
     */
    private static BasicClientCookie convertBrowserCookie(final Cookie browserCookie) {
        BasicClientCookie clientCookie = new BasicClientCookie(browserCookie.getName(), browserCookie.getValue());
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
     * Called when the crawler is about to begin its operation.
     */
    protected void onBegin() {
    }

    /**
     * Called after the browser loads the given URL.
     *
     * @param response The HTML response
     */
    protected void onResponseComplete(final HtmlResponse response) {
    }

    /**
     * Called when the loading of the given URL times out in the browser. Use
     * this callback with caution: the page might be half-loaded or not loaded
     * at all.
     *
     * @param response The HTML response
     */
    protected void onResponseTimeout(final HtmlResponse response) {
    }

    /**
     * Called when getting a non-HTML response.
     *
     * @param response The non-HTML response
     */
    protected void onNonHtmlResponse(final NonHtmlResponse response) {
    }

    /**
     * Called when an exception occurs while sending an initial HEAD request to
     * the given URL.
     *
     * @param request The unsuccessful request
     */
    protected void onUnsuccessfulRequest(final UnsuccessfulRequest request) {
    }

    /**
     * Called when the crawler successfully finishes its operation.
     */
    protected void onFinish() {
    }
}
