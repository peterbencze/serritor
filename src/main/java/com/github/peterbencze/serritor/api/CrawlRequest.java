/* 
 * Copyright 2017 Peter Bencze.
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
package com.github.peterbencze.serritor.api;

import com.google.common.net.InternetDomainName;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Represents a crawl request that might be processed by the crawler in the
 * future. The reason why it is not sure that it will be processed is because it
 * might get filtered out by one of the enabled filters.
 *
 * @author Krisztian Mozsi
 * @author Peter Bencze
 */
public final class CrawlRequest implements Serializable {

    private final URL requestUrl;
    private final String topPrivateDomain;
    private final int priority;

    private CrawlRequest(final CrawlRequestBuilder builder) {
        requestUrl = builder.requestUrl;
        topPrivateDomain = builder.topPrivateDomain;
        priority = builder.priority;
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
     * Returns the request's priority.
     *
     * @return The priority of the request
     */
    public int getPriority() {
        return priority;
    }

    public static final class CrawlRequestBuilder {

        private final URL requestUrl;

        private String topPrivateDomain;
        private int priority;

        public CrawlRequestBuilder(final URL requestUrl) {
            this.requestUrl = requestUrl;

            // Extract the top private domain from the request URL
            try {
                topPrivateDomain = InternetDomainName.from(requestUrl.getHost())
                        .topPrivateDomain()
                        .toString();
            } catch (IllegalStateException ex) {
                throw new IllegalArgumentException(String.format("The top private domain cannot be extracted from the provided request URL (\"%s\").", requestUrl), ex);
            }

            // Default priority is 0
            priority = 0;
        }

        public CrawlRequestBuilder(final String requestUrl) {
            this(getUrlFromString(requestUrl));
        }

        public CrawlRequestBuilder setPriority(final int priority) {
            this.priority = priority;
            return this;
        }

        public CrawlRequest build() {
            return new CrawlRequest(this);
        }

        /**
         * Constructs a URL instance based on the specified URL string. Since
         * call to this must be the first statement in a constructor, this
         * method is necessary for the conversion to be made.
         *
         * @param requestUrl The request URL as String
         * @return The request URL
         */
        private static URL getUrlFromString(final String requestUrl) {
            try {
                return new URL(requestUrl);
            } catch (MalformedURLException ex) {
                throw new IllegalArgumentException(String.format("The provided request URL (\"%s\") is malformed.", requestUrl), ex);
            }
        }
    }
}
