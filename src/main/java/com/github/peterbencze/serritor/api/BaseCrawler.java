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
import com.github.peterbencze.serritor.internal.CrawlCandidate;
import com.github.peterbencze.serritor.internal.CrawlFrontier;
import com.github.peterbencze.serritor.internal.CrawlerConfiguration;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.HttpClientBuilder;
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

    //Allows the application to configure the crawler
    protected final CrawlerConfiguration config;

    // Indicates if the crawler is currently running or not
    private boolean isStopped;

    // Indicates if the crawling should be stopped (used for cancelling the loop in the run method)
    private boolean stopCrawling;

    // Used for sending HTTP HEAD requests and receiving associate responses
    private HttpClient httpClient;

    private WebDriver webDriver;

    private CrawlFrontier frontier;

    protected BaseCrawler() {
        // Create a default configuration
        config = new CrawlerConfiguration();

        // Indicate that the crawler is not running
        isStopped = true;
    }
    
    /**
     * Starts the crawler using HtmlUnit headless browser.
     */
    public final void start() {
        start(new HtmlUnitDriver(true), null);
    }

    /**
     * Starts the crawler.
     * 
     * @param driver The WebDriver instance that will be used by the crawler
     */
    public final void start(final WebDriver driver) {
        start(driver, null);
    }

    /**
     * Constructs all the necessary objects and runs the crawler.
     *
     * @param frontierToUse Previously saved frontier to be used by the crawler.
     */
    private void start(final WebDriver driver, final CrawlFrontier frontierToUse) {
        // Check if the crawler is running
        if (!isStopped) {
            throw new IllegalStateException("The crawler is already started.");
        }

        isStopped = false;

        httpClient = HttpClientBuilder.create().build();

        webDriver = driver;

        frontier = frontierToUse != null ? frontierToUse : new CrawlFrontier(config);

        run();
    }

    /**
     * Saves the current state of the crawler to the specified output stream.
     *
     * @param out The output stream to use
     * @throws IOException Any exception thrown by the underlying OutputStream.
     */
    public final void saveState(final OutputStream out) throws IOException {
        // Check if the crawler has been started, otherwise we have nothing to save
        if (frontier == null) {
            throw new IllegalStateException("No state to save.");
        }

        // Save the frontier's current state
        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(out)) {
            objectOutputStream.writeObject(frontier);
        }
    }
    
    /**
     * Resumes a previously saved state using HtmlUnit headless browser.
     *
     * @param in The input stream to use
     * @throws IOException Any of the usual Input/Output related exceptions.
     * @throws ClassNotFoundException Class of a serialized object cannot be
     * found.
     */
    public final void resume(final InputStream in) throws IOException, ClassNotFoundException {
        resume(new HtmlUnitDriver(true), in);
    }

    /**
     * Resumes a previously saved state.
     *
     * @param driver The WebDriver instance that will be used by the crawler
     * @param in The input stream to use
     * @throws IOException Any of the usual Input/Output related exceptions.
     * @throws ClassNotFoundException Class of a serialized object cannot be
     * found.
     */
    public final void resume(final WebDriver driver, final InputStream in) throws IOException, ClassNotFoundException {
        try (ObjectInputStream objectInputStream = new ObjectInputStream(in)) {
            CrawlFrontier frontierToUse = (CrawlFrontier) objectInputStream.readObject();
            start(driver, frontierToUse);
        }
    }

    /**
     * Stops the crawler.
     */
    public final void stop() {
        // Check if the crawler is running
        if (isStopped) {
            throw new IllegalStateException("The crawler is not started.");
        }

        if (stopCrawling) {
            throw new IllegalStateException("Stop has already been called.");
        }

        // Indicate that the crawling should be stopped
        stopCrawling = true;
    }

    /**
     * Passes a crawl request to the crawl frontier.
     *
     * @param request The crawl request
     */
    protected final void crawl(final CrawlRequest request) {
        frontier.feedRequest(request, false);
    }

    /**
     * Passes multiple crawl requests to the crawl frontier.
     *
     * @param requests The list of crawl requests
     */
    protected final void crawl(final List<CrawlRequest> requests) {
        requests.stream().forEach(this::crawl);
    }

    /**
     * Defines the workflow of the crawler.
     */
    private void run() {
        try {
            onBegin();

            while (!stopCrawling && frontier.hasNextCandidate()) {
                // Get the next crawl candidate from the queue
                CrawlCandidate currentCandidate = frontier.getNextCandidate();

                URL currentCandidateUrl = currentCandidate.getCandidateUrl();
                String currentRequestUrlAsString = currentCandidateUrl.toString();

                HttpHeadResponse httpHeadResponse;
                URL responseUrl = currentCandidateUrl;

                try {
                    HttpClientContext context = HttpClientContext.create();

                    // Send an HTTP HEAD request to the current URL to determine its availability and content type
                    httpHeadResponse = getHttpHeadResponse(currentCandidateUrl, context);

                    // If the request has been redirected, get the final URL
                    List<URI> redirectLocations = context.getRedirectLocations();
                    if (redirectLocations != null) {
                        responseUrl = redirectLocations.get(redirectLocations.size() - 1).toURL();
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
                    frontier.feedRequest(redirectedCrawlRequest, false);

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
                } else {
                    // URLs that point to non-HTML content should not be opened in the browser

                    NonHtmlResponse nonHtmlResponse = new NonHtmlResponseBuilder(currentCandidate.getRefererUrl(), currentCandidate.getCrawlDepth(),
                            currentCandidate.getCrawlRequest())
                            .setHttpHeadResponse(httpHeadResponse)
                            .build();

                    onNonHtmlResponse(nonHtmlResponse);
                }

                TimeUnit.MILLISECONDS.sleep(config.getDelayBetweenRequests().toMillis());
            }

            onFinish();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } finally {
            // Always close the WebDriver
            webDriver.quit();

            stopCrawling = false;
            isStopped = true;
        }
    }

    /**
     * Returns a HTTP HEAD response for the given URL.
     *
     * @param destinationUrl The URL to crawl
     * @return The HTTP HEAD response
     */
    private HttpHeadResponse getHttpHeadResponse(final URL destinationUrl, final HttpClientContext context) throws IOException {
        HttpHead headRequest = new HttpHead(destinationUrl.toString());
        HttpResponse response = httpClient.execute(headRequest, context);
        return new HttpHeadResponse(response);
    }

    /**
     * Indicates if the content of the response is HTML or not.
     *
     * @param httpHeadResponse The HTTP HEAD response
     * @return True if the content is HTML, false otherwise
     */
    private boolean isContentHtml(final HttpHeadResponse httpHeadResponse) {
        Header contentTypeHeader = httpHeadResponse.getFirstHeader("Content-Type");
        return contentTypeHeader != null && contentTypeHeader.getValue().contains("text/html");
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
