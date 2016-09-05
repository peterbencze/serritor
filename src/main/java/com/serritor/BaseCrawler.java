package com.serritor;

import com.google.common.net.InternetDomainName;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.HttpClientBuilder;
import org.openqa.selenium.WebDriver;

/**
 * Provides a skeletal implementation of a crawler to minimize the effort for users to implement their own.
  * 
 * @author Peter Bencze
 */
public abstract class BaseCrawler {
    
    /**
     * Allows the application to configure the crawler.
     */
    protected final CrawlerConfiguration config;
    
    /**
     * Used for storing the new crawl requests that were added from the callbacks.
     */
    private final List<CrawlRequest> newCrawlRequests;
    
    private Thread crawlerThread;
    private HttpClient httpClient;
    private WebDriver driver;
    private CrawlFrontier frontier;
    private int currentCrawlDepth;
    
    
    protected BaseCrawler() {
        // Create the default configuration
        config = new CrawlerConfiguration();

        newCrawlRequests = new ArrayList<>();
    }
   
    /**
     * Starts the crawler.
     */
    public final void start() {
        if (crawlerThread != null)
            throw new IllegalStateException("The crawler is already started.");
        
        if (config.getRunInBackground()) {
            crawlerThread = new Thread() {
                @Override
                public void run() {
                    BaseCrawler.this.run();
                }
            };

            crawlerThread.start();
        } else {
            run();
        }
    }
    
    /**
     * Appends an URL to the list of URLs that should be visited by the crawler.
     * 
     * @param urlToVisit The URL to be visited by the crawler
     */
    protected final void visitUrl(String urlToVisit) {
        try {
            URL requestUrl = new URL(urlToVisit);
            String topPrivateDomain = getTopPrivateDomain(requestUrl);
            
            newCrawlRequests.add(new CrawlRequest(requestUrl, topPrivateDomain, currentCrawlDepth));
        } catch (MalformedURLException | IllegalStateException ex) {
            throw new IllegalArgumentException(ex.getMessage());
        }
    }
    
    /**
     * Extends the list of URLs that should be visited by the crawler with a list of URLs.
     * 
     * @param urlsToVisit The list of URLs to be visited by the crawler
     */
    protected final void visitUrls(List<String> urlsToVisit) {
        urlsToVisit.stream().forEach(this::visitUrl);
    }
    
    /**
     * Defines the workflow of the crawler.
     */
    private void run() {
        initialize();
        
        onBegin(driver);
        
        while (frontier.hasNextRequest()) {
            CrawlRequest currentRequest = frontier.getNextRequest();
            
            URL requestUrl = currentRequest.getUrl();
            currentCrawlDepth = currentRequest.getCrawlDepth();
            
            try {
                // Send an HTTP HEAD request to the current URL to determine its availability and content type
                HttpHeadResponse response = getHttpHeadResponse(requestUrl);

                URL responseUrl = response.getUrl();
                
                // If the request has been redirected, a new crawl request should be created for the redirected URL
                if (!responseUrl.equals(requestUrl)) {
                    frontier.feedRequest(new CrawlRequest(responseUrl, getTopPrivateDomain(responseUrl), currentCrawlDepth));
                    continue;
                }
                
                // If the HTTP status of the response is not 200 (OK), ignore it
                if (!response.isStatusOk()) {
                    onUnsuccessfulResponseStatus(responseUrl);
                    continue;
                }
                
                // URLs that point to non-HTML content should not be opened in the browser
                if (!response.isHtmlContent()) {
                    onNonHtmlResponse(responseUrl);
                    continue;
                }
                
                driver.get(requestUrl.toString());

                onUrlOpen(driver);
                
                // Add the new crawl requests (added from the callbacks) to the frontier
                newCrawlRequests.stream().forEach(frontier::feedRequest);

                // Clear the list for the next iteration.
                newCrawlRequests.clear();
            } catch (IOException ex) {
                // If for some reason the given URL is unreachable, call the appropriate callback to handle this situation
                onUnreachableUrl(requestUrl);
            }
        }
        
        driver.quit();
        
        onEnd();
    }

    /**
     * Constructs all the objects that are required to run the crawler.
     */
    private void initialize() {
        httpClient = HttpClientBuilder.create().build();
        driver = WebDriverFactory.getDriver(config);
        frontier = new CrawlFrontier(config);
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
     * Returns a HTTP HEAD response for the given URL that can be used to decide if the given URL should be opened in the browser or not.
     *
     * @param destinationUrl The URL to crawl
     * @return A HTTP HEAD response with only the necessary properties
     */
    private HttpHeadResponse getHttpHeadResponse(URL destinationUrl) throws IOException {
        HttpClientContext context = HttpClientContext.create();
        HttpHead headRequest = new HttpHead(destinationUrl.toString());
        HttpResponse response = httpClient.execute(headRequest, context);
        
        URL responseUrl = headRequest.getURI().toURL();    
        
        // If the request has been redirected, get the final URL
        List<URI> redirectLocations = context.getRedirectLocations();
        if (redirectLocations != null)
            responseUrl = redirectLocations.get(redirectLocations.size() - 1).toURL();
         
        int statusCode = response.getStatusLine().getStatusCode();
        
        String contentType = null;
        
        // Get the value of the "Content-Type" header
        Header contentTypeHeader = response.getFirstHeader("Content-Type");
        if (contentTypeHeader != null)
            contentType = contentTypeHeader.getValue();
        
        return new HttpHeadResponse(responseUrl, statusCode, contentType);
    }

    /**
     * Called when the crawler is about to begin its operation.
     *
     * @param driver
     */
    protected abstract void onBegin(WebDriver driver);

    /**
     * Called after the browser opens an URL.
     *
     * @param driver The driver instance of the browser
     */
    protected abstract void onUrlOpen(WebDriver driver);
    
    /**
     * Called when getting a non-HTML response.
      * 
     * @param requestUrl The URL of the non-HTML response
     */
    protected abstract void onNonHtmlResponse(URL requestUrl);

    /**
     * Called when getting an unsuccessful HTTP status code in the HEAD response.
     *
     * @param requestUrl
     */
    protected abstract void onUnsuccessfulResponseStatus(URL requestUrl);

    /**
     * Called when an exception occurs while sending a HEAD request to a URL.
     *
     * @param requestUrl The URL of the failed request
     */
    protected abstract void onUnreachableUrl(URL requestUrl);

    /**
     * Called when the crawler ends its operation.
     */
    protected abstract void onEnd();
}
