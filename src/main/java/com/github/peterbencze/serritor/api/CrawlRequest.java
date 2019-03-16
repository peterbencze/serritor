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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.net.InternetDomainName;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.http.client.utils.URIBuilder;

/**
 * Represents a crawl request that may be completed by the crawler in the future. If request
 * filtering is enabled, it could be filtered out.
 */
public final class CrawlRequest implements Serializable {

    private final URI requestUrl;
    private final int priority;

    @JsonIgnore
    private final Serializable metadata;

    @JsonIgnore
    private transient InternetDomainName domain;

    private CrawlRequest(final CrawlRequestBuilder builder) {
        requestUrl = builder.requestUrl;
        domain = builder.domain;
        priority = builder.priority;
        metadata = builder.metadata;
    }

    /**
     * Creates a crawl request with the default configuration.
     *
     * @param requestUrl the request URL
     *
     * @return the crawl request with the default configuration
     */
    public static CrawlRequest createDefault(final URI requestUrl) {
        return new CrawlRequestBuilder(requestUrl).build();
    }

    /**
     * Creates a crawl request with the default configuration.
     *
     * @param requestUrl the request URL
     *
     * @return the crawl request with the default configuration
     */
    public static CrawlRequest createDefault(final String requestUrl) {
        return new CrawlRequestBuilder(requestUrl).build();
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
     * Returns a string representation of this crawl request.
     *
     * @return a string representation of this crawl request
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("requestUrl", requestUrl)
                .append("domain", domain)
                .append("priority", priority)
                .toString();
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
            Validate.notNull(requestUrl, "The requestUrl parameter cannot be null.");

            if (StringUtils.isEmpty(requestUrl.getPath())) {
                try {
                    // Define a non-empty path for the URI
                    this.requestUrl = new URIBuilder(requestUrl).setPath("/").build();
                } catch (URISyntaxException e) {
                    throw new IllegalArgumentException(e.getMessage(), e);
                }
            } else {
                this.requestUrl = requestUrl;
            }

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
            this(URI.create(Validate.notNull(requestUrl,
                    "The requestUrl parameter cannot be null")));
        }

        /**
         * Sets the priority of the request.
         *
         * @param priority the priority of the request (higher number means higher priority)
         *
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
         *
         * @return the <code>CrawlRequestBuilder</code> instance
         */
        public CrawlRequestBuilder setMetadata(final Serializable metadata) {
            this.metadata = Validate.notNull(metadata, "The metadata parameter cannot be null.");
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
