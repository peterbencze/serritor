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
import java.net.URI;
import java.util.Optional;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Represents a candidate for crawling.
 */
public final class CrawlCandidate implements Serializable {

    private final URI refererUrl;
    private final int crawlDepth;
    private final CrawlRequest crawlRequest;

    private CrawlCandidate(final CrawlCandidateBuilder builder) {
        this.crawlRequest = builder.crawlRequest;
        this.refererUrl = builder.refererUrl;
        this.crawlDepth = builder.crawlDepth;
    }

    /**
     * Returns the referer URL.
     *
     * @return the URL of the referer
     */
    public URI getRefererUrl() {
        return refererUrl;
    }

    /**
     * Returns the request URL.
     *
     * @return the URL of the request
     */
    public URI getRequestUrl() {
        return crawlRequest.getRequestUrl();
    }

    /**
     * Returns the domain of the request URL.
     *
     * @return the domain of the request URL
     */
    public InternetDomainName getDomain() {
        return crawlRequest.getDomain();
    }

    /**
     * Returns the crawl depth of the request.
     *
     * @return the crawl depth of the request
     */
    public int getCrawlDepth() {
        return crawlDepth;
    }

    /**
     * Returns the priority of the request.
     *
     * @return the priority of the request
     */
    public int getPriority() {
        return crawlRequest.getPriority();
    }

    /**
     * Returns the metadata associated with the request.
     *
     * @return the metadata associated with the request
     */
    public Optional<Serializable> getMetadata() {
        return crawlRequest.getMetadata();
    }

    /**
     * Returns a string representation of this crawl candidate.
     *
     * @return a string representation of this crawl candidate
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("refererUrl", refererUrl)
                .append("requestUrl", getRequestUrl())
                .append("domain", getDomain())
                .append("crawlDepth", crawlDepth)
                .append("priority", getPriority())
                .toString();
    }

    /**
     * Builds {@link CrawlCandidate} instances.
     */
    public static final class CrawlCandidateBuilder {

        private final CrawlRequest crawlRequest;

        private URI refererUrl;
        private int crawlDepth;

        /**
         * Creates a {@link CrawlCandidateBuilder} instance.
         *
         * @param request the <code>CrawlRequest</code> instance from which this candidate is built
         */
        public CrawlCandidateBuilder(final CrawlRequest request) {
            crawlRequest = request;
        }

        /**
         * Sets the referer URL.
         *
         * @param refererUrl the referer URL
         *
         * @return the <code>CrawlCandidateBuilder</code> instance
         */
        public CrawlCandidateBuilder setRefererUrl(final URI refererUrl) {
            this.refererUrl = refererUrl;
            return this;
        }

        /**
         * Sets the crawl depth of the request.
         *
         * @param crawlDepth the crawl depth of the request
         *
         * @return the <code>CrawlCandidateBuilder</code> instance
         */
        public CrawlCandidateBuilder setCrawlDepth(final int crawlDepth) {
            this.crawlDepth = crawlDepth;
            return this;
        }

        /**
         * Builds the configured <code>CrawlCandidate</code> instance.
         *
         * @return the configured <code>CrawlCandidate</code> instance
         */
        public CrawlCandidate build() {
            return new CrawlCandidate(this);
        }
    }
}
