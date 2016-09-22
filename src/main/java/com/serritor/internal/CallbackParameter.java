package com.serritor.internal;

import java.net.URL;

/**
 * The base class that all callback parameters inherit from.
 * 
 * @author Peter Bencze
 */
public abstract class CallbackParameter {

    private final int crawlDepth;
    private final URL referer;

    protected CallbackParameter(int crawlDepth, URL referer) {
        this.crawlDepth = crawlDepth;
        this.referer = referer;
    }

    /**
     * Returns the crawl depth of the current request or response.
     * 
     * @return The crawl depth of the current request or response
     */
    public final int getCrawlDepth() {
        return crawlDepth;
    }

    /**
     * Returns the referer URL of the current request or response.
     * 
     * @return The referer URL of the current request or response
     */
    public final URL getRefererUrl() {
        return referer;
    }
}
