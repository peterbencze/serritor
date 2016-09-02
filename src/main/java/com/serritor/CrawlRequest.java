package com.serritor;

import java.net.URL;

/**
 * Represents a crawl request with the requested URL to crawl and its depth.
 * 
 * @author Krisztian Mozsi
 * @author Peter Bencze
 */
public class CrawlRequest {

    private final String topPrivateDomain;
    private final URL url;
    private final int crawlDepth;
    
    public CrawlRequest(String topPrivateDomain, URL url, int crawlDepth) {
        this.url = url;
        this.topPrivateDomain = topPrivateDomain;
        this.crawlDepth = crawlDepth;
    }

    /**
     * Returns the request's URL.
     *
     * @return The URL of the request
     */
    public URL getUrl() {
        return url;
    }

    /**
     * Returns the top private domain of the request's URL.
     *
     * @return The top private domain of the URL
     */
    public String getTopPrivateDomain() {
        return topPrivateDomain;
    }

    /**
     * Returns the crawl depth of the request.
     *
     * @return The crawl depth
     */
    public int getCrawlDepth() {
        return crawlDepth;
    }
    
}
