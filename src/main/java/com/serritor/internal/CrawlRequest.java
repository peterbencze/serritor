/* 
 * Copyright 2016 Peter Bencze.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
        refererUrl = builder.refererUrl;
        requestUrl = builder.requestUrl;
        topPrivateDomain = builder.topPrivateDomain;
        crawlDepth = builder.crawlDepth;
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
