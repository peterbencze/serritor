package com.serritor;

import java.io.IOException;
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
    
    protected final CrawlerConfiguration config;
    
    /**
     * A temporary list that is used to store the URLs that were added from the callbacks.
     */
    private final List<String> urlsToVisit;
    
    private Thread crawlerThread;
    
    public BaseCrawler() {
        // Create default configuration
        config = new CrawlerConfiguration();
        
        urlsToVisit = new ArrayList<>();
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
     * @param url The URL to be visited by the crawler
     */
    protected final void visitUrl(String url) {
        urlsToVisit.add(url);
    }
    
    /**
     * Extends the list of URLs that should be visited by the crawler with a list of URLs.
     * 
     * @param urls The list of URLs to be visited by the crawler
     */
    protected final void visitUrls(List<String> urls) {
        urlsToVisit.addAll(urls);
    }
    
    /**
     * Contains the workflow of the crawler.
     */
    private void run() {
        WebDriver driver = WebDriverFactory.getDriver(config);
        
        CrawlFrontier frontier = new CrawlFrontier(getSeeds(), config.getCrawlingStrategy());
        
        while (frontier.hasNextRequest()) {
            CrawlRequest nextRequest = frontier.getNextRequest();
            String nextRequestUrl = nextRequest.getUrl();

            driver.get(nextRequestUrl);
            
            onUrlOpen(driver);
            
            // Send an HTTP HEAD request to each URL (added by the user) to determine their content type and availability.
            List<URL> urls = new ArrayList<>();
            urlsToVisit.stream().forEach((String url) -> {
                try {
                    HttpHeadResponse response = getHttpHeadResponse(url);
                    
                    if (response.isStatusOk() && response.isHtmlContent())
                        urls.add(response.getFinalUrl());
                } catch (IOException ex) {
                    // If for some reason the given URL is malformed or unavailable, call the appropriate callback to handle this situation.
                    onUrlOpenError(url);
                }
            });

            // Provide a response to the frontier with the list of URLs to visit and the crawl depth.
            CrawlResponse response = new CrawlResponse(urls, nextRequest.getCrawlDepth() + 1);
            frontier.addCrawlResponse(response);
            
            // Clear the list for the next iteration.
            urlsToVisit.clear();
        }
        
        driver.quit();
    }

    /**
     * Returns a HTTP HEAD response for the given URL that can be used to decide if the given URL should be opened in the browser or not.
     *
     * @param url The URL to crawl
     * @return A HTTP HEAD response with only the necessary properties
     */
    private HttpHeadResponse getHttpHeadResponse(String url) throws IOException {
        HttpClient client = HttpClientBuilder.create().build();
        HttpClientContext context = HttpClientContext.create();
        HttpHead headRequest = new HttpHead(url);    
        HttpResponse response = client.execute(headRequest, context);
        
        URL finalUrl = headRequest.getURI().toURL();      
        List<URI> redirectLocations = context.getRedirectLocations();
        if (redirectLocations != null)
            finalUrl = redirectLocations.get(redirectLocations.size() - 1).toURL();
         
        int statusCode = response.getStatusLine().getStatusCode();
        
        String contentType = null;
        Header contentTypeHeader = response.getFirstHeader("Content-Type");
        if (contentTypeHeader != null)
            contentType = contentTypeHeader.getValue();
        
        return new HttpHeadResponse(finalUrl, statusCode, contentType);
    }

    /**
     * Returns a list of start URLs (known as seeds).
     *
     * @return A list of seed URLs
     */
    private List<URL> getSeeds() {
        List<URL> seeds = new ArrayList<>();
        
        config.getSeeds().stream().forEach((url) -> {
            try {
                if (!url.startsWith("http"))
                    url = "http://" + url;
                
                HttpHeadResponse response = getHttpHeadResponse(url);
                
                if (response.isStatusOk() && response.isHtmlContent())
                    seeds.add(response.getFinalUrl());
            } catch (IOException ex) {
                onUrlOpenError(url);
            }
        });
        
        return seeds;
    }

    /**
     * Abstract method to be called when the browser opens an URL.
     *
     * @param driver The driver instance of the browser.
     */
    protected abstract void onUrlOpen(WebDriver driver);

    /**
     * Abstract method to be called when an exception occurs while trying to open an URL.
     *
     * @param url The URL of the failed request
     */
    protected abstract void onUrlOpenError(String url);
}
