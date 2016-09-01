package com.serritor;

/**
 * Represents a crawl request with the requested URL to crawl and its depth.
 * 
 * @author Krisztian Mozsi
 */
public class CrawlRequest {

    private final String url;
    private final int crawlDepth;
    
    public CrawlRequest(String url, int crawlDepth) {
        this.url = url;
        this.crawlDepth = crawlDepth;
    }
    
    public String getUrl() {
        return url;
    }

    public int getCrawlDepth() {
        return crawlDepth;
    }
    
}
