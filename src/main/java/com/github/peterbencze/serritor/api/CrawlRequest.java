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
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.net.URI;
import java.util.Optional;

/**
 * Represents a crawl request that might be processed by the crawler in the
 * future. The reason why it is not sure that it will be processed is because it
 * might get filtered out by one of the enabled filters.
 *
 * @author Peter Bencze
 */
public final class CrawlRequest implements Serializable {

    private final URI requestUrl;
    private final int priority;
    private final Serializable metadata;
    
    private transient InternetDomainName domain;

    private CrawlRequest(final CrawlRequestBuilder builder) {
        requestUrl = builder.requestUrl;
        domain = builder.domain;
        priority = builder.priority;
        metadata = builder.metadata;
    }

    /**
     * Returns the request's URL.
     *
     * @return The URL of the request
     */
    public URI getRequestUrl() {
        return requestUrl;
    }

    /**
     * Returns the domain of the request's URL.
     *
     * @return The domain of the request URL
     */
    public InternetDomainName getDomain() {
        return domain;
    }

    /**
     * Returns the request's priority.
     *
     * @return The priority of the request
     */
    public int getPriority() {
        return priority;
    }

    /**
     * Returns metadata associated with the request.
     *
     * @return The request's metadata
     */
    public Optional<Serializable> getMetadata() {
        return Optional.ofNullable(metadata);
    }

    public static final class CrawlRequestBuilder {

        private static final int DEFAULT_PRIORITY = 0;

        private final URI requestUrl;
        private final InternetDomainName domain;
        
        private int priority;
        private Serializable metadata;

        /**
         * Constructs a <code>CrawlRequestBuilder</code> instance that can be
         * used to create CrawRequest instances.
         *
         * @param requestUrl The request's URL given as a <code>URL</code>
         * instance
         */
        public CrawlRequestBuilder(final URI requestUrl) {
            this.requestUrl = requestUrl;

            // Extract the domain from the request URL
            domain = InternetDomainName.from(requestUrl.getHost());

            // Set default priority
            priority = DEFAULT_PRIORITY;
        }

        /**
         * Constructs a <code>CrawlRequestBuilder</code> instance that can be
         * used to create <code>CrawRequest</code> instances.
         *
         * @param requestUrl The request's URL given as a <code>String</code>
         * instance
         */
        public CrawlRequestBuilder(final String requestUrl) {
            this(URI.create(requestUrl));
        }

        /**
         * Sets the request's priority.
         *
         * @param priority The priority of the request (higher number means
         * higher priority)
         * @return The <code>CrawlRequestBuilder</code> instance
         */
        public CrawlRequestBuilder setPriority(final int priority) {
            this.priority = priority;
            return this;
        }

        /**
         * Sets additional metadata for the request which can be later accessed
         * when the crawler processed the request.
         *
         * @param metadata The metadata associated with the request
         * @return The <code>CrawlRequestBuilder</code> instance
         */
        public CrawlRequestBuilder setMetadata(final Serializable metadata) {
            this.metadata = metadata;
            return this;
        }

        /**
         * Builds the configured <code>CrawlRequest</code> instance.
         *
         * @return The configured <code>CrawlRequest</code> instance
         */
        public CrawlRequest build() {
            return new CrawlRequest(this);
        }
    }

    private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        
        domain = InternetDomainName.from(requestUrl.getHost());
    }
}
