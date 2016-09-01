package com.serritor;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
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
    
    private Thread crawlerThread;
    
    public BaseCrawler() {
        // Create default configuration
        config = new CrawlerConfiguration();
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
     * Contains the workflow of the crawler.
     */
    private void run() {
        WebDriver driver = WebDriverFactory.getDriver(config);
        
        CrawlFrontier frontier = new CrawlFrontier(getSeeds(), config.getCrawlingStrategy());
        
        while (frontier.hasNextRequest()) {
            String nextRequestUrl = frontier.getNextRequest().getUrl();

            driver.get(nextRequestUrl);
            
            try {
                onUrlOpen(driver);
            } catch (Exception ex) {
                onUrlOpenError(nextRequestUrl);
            }
            
            /* TODO: 
            1. Call onUrlOpen (call onUrlOpenError if an exceptions occurs)
            2. Send a HEAD request to each extracted URLs 
            3. Add verified ones (status OK, content-type is HTML) to the frontier */
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
        String contentType = response.getFirstHeader("content-type").getValue();
        
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
