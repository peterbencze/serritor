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
 * Provides a skeletal implementation of a crawler to minimize the effort for
 * users to implement their own.
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
        
        CrawlFrontier frontier = new CrawlFrontier();
        frontier.addUrls(getSeeds());
        
        while(frontier.hasNextUrl()) {
            String nextUrl = frontier.getNextUrl();

            driver.get(nextUrl);
            
            /* TODO: 
            1. Call onUrlOpen
            2. Call onUrlExtract (to extract URLs)
            3. Send a HEAD request to each extracted URLs 
            4. Add verified ones (status OK, content-type is HTML) to the frontier */
        }
        
        driver.quit();
    }
    
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
    
    private List<URL> getSeeds() {
        List<URL> seeds = new ArrayList<>();
        
        for (String url : config.getSeeds()) {
            try {
                if (!url.startsWith("http"))
                    url = "http://" + url;
                
                HttpHeadResponse response = getHttpHeadResponse(url);
                
                if (response.isStatusOk() && response.isHtmlContent())
                    seeds.add(response.getFinalUrl());
            } catch (IOException ex) {
                // Call error handling callback
            }      
        }
        
        return seeds;
    }
}
