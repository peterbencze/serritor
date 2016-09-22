package com.serritor.internal;

import java.io.Serializable;
import java.net.URL;

/**
 * Represents a crawl request that should be processed by the crawler.
 *
 * @author Krisztian Mozsi
 * @author Peter Bencze
 */
public final class CrawlRequest implements Serializable {

    private final URL refererUrl;
    private final URL requestUrl;
    private final String topPrivateDomain;
    private final int crawlDepth;

    private CrawlRequest(CrawlRequestBuilder builder) {
        this.refererUrl = builder.refererUrl;
        this.requestUrl = builder.requestUrl;
        this.topPrivateDomain = builder.topPrivateDomain;
        this.crawlDepth = builder.crawlDepth;
    }

    /**
     * Returns the referer's URL.
     *
     * @return The URL of the referer.
     */
    public URL getRefererUrl() {
        return refererUrl;
    }

    /**
     * Returns the request's URL.
     *
     * @return The URL of the request
     */
    public URL getRequestUrl() {
        return requestUrl;
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

    public static class CrawlRequestBuilder {

        private URL refererUrl;
        private URL requestUrl;
        private String topPrivateDomain;
        private int crawlDepth;

        public CrawlRequestBuilder setRefererUrl(URL refererUrl) {
            this.refererUrl = refererUrl;
            return this;
        }

        public CrawlRequestBuilder setRequestUrl(URL requestUrl) {
            this.requestUrl = requestUrl;
            return this;
        }

        public CrawlRequestBuilder setTopPrivateDomain(String topPrivateDomain) {
            this.topPrivateDomain = topPrivateDomain;
            return this;
        }

        public CrawlRequestBuilder setCrawlDepth(int crawlDepth) {
            this.crawlDepth = crawlDepth;
            return this;
        }

        public CrawlRequest build() {
            return new CrawlRequest(this);
        }
    }
}
