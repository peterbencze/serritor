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
import org.apache.commons.lang3.Validate;

/**
 * Represents a crawl request that may be completed by the crawler. If request
 * filtering is enabled, it could get filtered out.
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
     * Returns the request URL.
     *
     * @return the request URL
     */
    public URI getRequestUrl() {
        return requestUrl;
    }

    /**
     * Returns the domain of the request URL.
     *
     * @return the domain of the request URL
     */
    public InternetDomainName getDomain() {
        return domain;
    }

    /**
     * Returns the priority of the request.
     *
     * @return the priority of the request
     */
    public int getPriority() {
        return priority;
    }

    /**
     * Returns the metadata associated with the request.
     *
     * @return the metadata associated with the request
     */
    public Optional<Serializable> getMetadata() {
        return Optional.ofNullable(metadata);
    }

    /**
     * Builds {@link CrawlRequest} instances.
     */
    public static final class CrawlRequestBuilder {

        private static final int DEFAULT_PRIORITY = 0;

        private final URI requestUrl;
        private final InternetDomainName domain;

        private int priority;
        private Serializable metadata;

        /**
         * Creates a {@link CrawlRequestBuilder} instance.
         *
         * @param requestUrl the request URL
         */
        public CrawlRequestBuilder(final URI requestUrl) {
            this.requestUrl = requestUrl;

            // Extract the domain from the request URL
            domain = InternetDomainName.from(requestUrl.getHost());

            // Set default priority
            priority = DEFAULT_PRIORITY;
        }

        /**
         * Creates a {@link CrawlRequestBuilder} instance.
         *
         * @param requestUrl the request URL
         */
        public CrawlRequestBuilder(final String requestUrl) {
            this(URI.create(requestUrl));
        }

        /**
         * Sets the priority of the request.
         *
         * @param priority the priority of the request (higher number means
         * higher priority)
         * @return the <code>CrawlRequestBuilder</code> instance
         */
        public CrawlRequestBuilder setPriority(final int priority) {
            this.priority = priority;
            return this;
        }

        /**
         * Sets the metadata associated with the request.
         *
         * @param metadata the metadata associated with the request
         * @return the <code>CrawlRequestBuilder</code> instance
         */
        public CrawlRequestBuilder setMetadata(final Serializable metadata) {
            this.metadata = Validate.notNull(metadata, "The metadata cannot be null.");
            return this;
        }

        /**
         * Builds the configured <code>CrawlRequest</code> instance.
         *
         * @return the configured <code>CrawlRequest</code> instance
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
