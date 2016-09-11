package com.serritor;

import java.io.Serializable;
import java.net.URL;

/**
 * Represents a crawl request with the requested URL, top private domain and crawl depth.
 * 
 * @author Krisztian Mozsi
 * @author Peter Bencze
 */
public class CrawlRequest implements Serializable {

    private final String topPrivateDomain;
    private final URL url;
    private final int crawlDepth;
    
    
    public CrawlRequest(URL url, String topPrivateDomain, int crawlDepth) {
        this.url = url;
        this.topPrivateDomain = topPrivateDomain;
        this.crawlDepth = crawlDepth;
    }
    
    public CrawlRequest(URL url, String topPrivateDomain) {
        this(url, topPrivateDomain, 0);
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
