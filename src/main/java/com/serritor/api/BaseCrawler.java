/* 
 * Copyright 2016 Peter Bencze.
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
package com.serritor.api;

import com.google.common.net.InternetDomainName;
import com.serritor.internal.CrawlFrontier;
import com.serritor.internal.CrawlRequest;
import com.serritor.internal.CrawlRequest.CrawlRequestBuilder;
import com.serritor.internal.CrawlerConfiguration;
import com.serritor.api.HtmlResponse.HtmlResponseBuilder;
import com.serritor.api.NonHtmlResponse.NonHtmlResponseBuilder;
import com.serritor.api.UnsuccessfulRequest.UnsuccessfulRequestBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
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

/**
 * Provides a skeletal implementation of a crawler to minimize the effort for
 * users to implement their own.
 *
 * @author Peter Bencze
 */
public abstract class BaseCrawler {

    /**
     * Allows the application to configure the crawler.
     */
    protected final CrawlerConfiguration config;

    private boolean stopCrawling;
    private boolean isStopped;
    private HttpClient httpClient;
    private WebDriver webDriver;
    private CrawlFrontier frontier;
    private int currentCrawlDepth;
    private URL currentRequestUrl;

    protected BaseCrawler() {
        // Create the default configuration
        config = new CrawlerConfiguration();

        isStopped = true;
    }

    /**
     * Starts the crawler.
     */
    public final void start() {
        start(null);
    }

    /**
     * Resumes a previously saved state.
     *
     * @param in The input stream to use
     * @throws IOException Any of the usual Input/Output related exceptions.
     * @throws ClassNotFoundException Class of a serialized object cannot be
     * found.
     */
    public final void resume(InputStream in) throws IOException, ClassNotFoundException {
        try (ObjectInputStream objectInputStream = new ObjectInputStream(in)) {
            CrawlFrontier frontierToUse = (CrawlFrontier) objectInputStream.readObject();
            start(frontierToUse);
        }
    }

    /**
     * Stops the crawler.
     */
    public final void stop() {
        if (isStopped) {
            throw new IllegalStateException("The crawler is not started.");
        }

        if (stopCrawling) {
            throw new IllegalStateException("Stop has already been called.");
        }

        stopCrawling = true;
    }

    /**
     * Saves the current state of the crawler to the specified output stream.
     *
     * @param out The output stream to use
     * @throws IOException Any exception thrown by the underlying OutputStream.
     */
    public final void saveState(OutputStream out) throws IOException {
        if (frontier == null) {
            throw new IllegalStateException("No state to save.");
        }

        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(out)) {
            objectOutputStream.writeObject(frontier);
        }
    }

    /**
     * Appends an URL to the list of URLs that should be crawled.
     *
     * @param urlToCrawl The URL to be crawled
     */
    protected final void crawlUrl(URL urlToCrawl) {
        try {
            String topPrivateDomain = getTopPrivateDomain(urlToCrawl);

            CrawlRequest newCrawlRequest = new CrawlRequestBuilder()
                    .setRefererUrl(currentRequestUrl)
                    .setRequestUrl(urlToCrawl)
                    .setTopPrivateDomain(topPrivateDomain)
                    .setCrawlDepth(currentCrawlDepth + 1)
                    .build();

            frontier.feedRequest(newCrawlRequest);
        } catch (IllegalStateException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    /**
     * Appends an URL (as String) to the list of URLs that should be crawled.
     *
     * @param urlToCrawl The URL to be crawled
     */
    protected final void crawlUrlAsString(String urlToCrawl) {
        try {
            crawlUrl(new URL(urlToCrawl));
        } catch (MalformedURLException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    /**
     * Extends the list of URLs that should be crawled with a list of URLs.
     *
     * @param urlsToCrawl The list of URLs to be crawled
     */
    protected final void crawlUrls(List<URL> urlsToCrawl) {
        urlsToCrawl.stream().forEach(this::crawlUrl);
    }

    /**
     * Extends the list of URLs (as Strings) that should be crawled with a list
     * of URLs.
     *
     * @param urlsToCrawl The list of URLs to be crawled
     */
    protected final void crawlUrlsAsStrings(List<String> urlsToCrawl) {
        urlsToCrawl.stream().forEach(this::crawlUrlAsString);
    }

    /**
     * Constructs all the necessary objects and runs the crawler.
     *
     * @param frontierToUse Previously saved frontier to be used by the crawler.
     */
    private void start(CrawlFrontier frontierToUse) {
        if (!isStopped) {
            throw new IllegalStateException("The crawler is already started.");
        }

        isStopped = false;

        httpClient = HttpClientBuilder.create().build();
        webDriver = config.getWebDriver();

        frontier = frontierToUse != null ? frontierToUse : new CrawlFrontier(config);

        run();
    }

    /**
     * Defines the workflow of the crawler.
     */
    private void run() {
        try {
            onBegin();
            
            while (!stopCrawling && frontier.hasNextRequest()) {
                CrawlRequest currentRequest = frontier.getNextRequest();

                currentRequestUrl = currentRequest.getRequestUrl();
                String currentRequestUrlAsString = currentRequestUrl.toString();
                currentCrawlDepth = currentRequest.getCrawlDepth();

                HttpHeadResponse httpHeadResponse;
                URL responseUrl = currentRequestUrl;

                try {
                    HttpClientContext context = HttpClientContext.create();
                    
                    // Send an HTTP HEAD request to the current URL to determine its availability and content type
                    httpHeadResponse = getHttpHeadResponse(currentRequestUrl, context);

                    // If the request has been redirected, get the final URL
                    List<URI> redirectLocations = context.getRedirectLocations();
                    if (redirectLocations != null) {
                        responseUrl = redirectLocations.get(redirectLocations.size() - 1).toURL();
                    }
                } catch (IOException ex) {
                    UnsuccessfulRequest unsuccessfulRequest = new UnsuccessfulRequestBuilder()
                            .setCrawlDepth(currentCrawlDepth)
                            .setRefererUrl(currentRequest.getRefererUrl())
                            .setCurrentUrl(currentRequestUrl)
                            .setException(ex)
                            .build();

                    onUnsuccessfulRequest(unsuccessfulRequest);
                    continue;
                }

                // If the request has been redirected, a new crawl request should be created for the redirected URL
                if (!responseUrl.toString().equals(currentRequestUrlAsString)) {
                    CrawlRequest newCrawlRequest = new CrawlRequestBuilder()
                            .setRefererUrl(currentRequestUrl)
                            .setRequestUrl(responseUrl)
                            .setTopPrivateDomain(getTopPrivateDomain(responseUrl))
                            .setCrawlDepth(currentCrawlDepth)
                            .build();

                    frontier.feedRequest(newCrawlRequest);
                    continue;
                }

                // Get the content type of the response
                String contentType = getContentType(httpHeadResponse);
                if (contentType != null && contentType.contains("text/html")) {
                    boolean timedOut = false;

                    try {
                        // Open the URL in the browser
                        webDriver.get(currentRequestUrlAsString);
                    } catch (TimeoutException ex) {
                        timedOut = true;
                    }

                    HtmlResponse htmlResponse = new HtmlResponseBuilder()
                            .setCrawlDepth(currentCrawlDepth)
                            .setRefererUrl(currentRequest.getRefererUrl())
                            .setCurrentUrl(currentRequestUrl)
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

                    NonHtmlResponse nonHtmlResponse = new NonHtmlResponseBuilder()
                            .setCrawlDepth(currentCrawlDepth)
                            .setRefererUrl(currentRequest.getRefererUrl())
                            .setCurrentUrl(currentRequestUrl)
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
     * Returns the top private domain for the given URL.
     *
     * @param url The URL to parse
     * @return The top private domain
     */
    private String getTopPrivateDomain(URL url) {
        return InternetDomainName.from(url.getHost()).topPrivateDomain().toString();
    }

    /**
     * Returns a HTTP HEAD response for the given URL.
     *
     * @param destinationUrl The URL to crawl
     * @return The HTTP HEAD response
     */
    private HttpHeadResponse getHttpHeadResponse(URL destinationUrl, HttpClientContext context) throws IOException {
        HttpHead headRequest = new HttpHead(destinationUrl.toString());
        HttpResponse response = httpClient.execute(headRequest, context);
        return new HttpHeadResponse(response);
    }
    
    /**
     * Returns the content type of the response.
     * 
     * @param httpHeadResponse The HTTP HEAD response
     * @return The content type of the response
     */
    private String getContentType(HttpHeadResponse httpHeadResponse) {
        String contentType = null;
        
        Header contentTypeHeader = httpHeadResponse.getFirstHeader("Content-Type");
        if (contentTypeHeader != null) {
            contentType = contentTypeHeader.getValue();
        }
        
        return contentType;
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
    protected void onResponseComplete(HtmlResponse response) {
    }

    /**
     * Called when the loading of the given URL times out in the browser. Use
     * this callback with caution: the page might be half-loaded or not loaded
     * at all.
     *
     * @param response The HTML response
     */
    protected void onResponseTimeout(HtmlResponse response) {
    }

    /**
     * Called when getting a non-HTML response.
     *
     * @param response The non-HTML response
     */
    protected void onNonHtmlResponse(NonHtmlResponse response) {
    }

    /**
     * Called when an exception occurs while sending an initial HEAD request to
     * the given URL.
     *
     * @param request The unsuccessful request
     */
    protected void onUnsuccessfulRequest(UnsuccessfulRequest request) {
    }

    /**
     * Called when the crawler successfully finishes its operation.
     */
    protected void onFinish() {
    }
}
