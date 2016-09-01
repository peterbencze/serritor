package com.serritor;

import java.net.URL;
import java.util.List;

/**
 * Represents a crawl response with the extracted URLs and the crawl depth.
 * 
 * @author Krisztian Mozsi
 */
public class CrawlResponse {
    
    private final List<URL> extractedUrls;
    private final int crawlDepth;
    
    public CrawlResponse(List<URL> extractedUrls, int crawlDepth) {
        this.extractedUrls = extractedUrls;
        this.crawlDepth = crawlDepth;
    }
    
    public List<URL> getExtractedUrls() {
        return extractedUrls;
    }

    public int getCrawlDepth() {
        return crawlDepth;
    }
    
}
