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

/**
 * Represents a candidate to be crawled by the crawler.
 *
 * @author Peter Bencze
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
     * @return The URL of the referer
     */
    public URI getRefererUrl() {
        return refererUrl;
    }

    /**
     * Returns the candidate URL.
     *
     * @return The URL of the candidate
     */
    public URI getCandidateUrl() {
        return crawlRequest.getRequestUrl();
    }

    /**
     * Returns the domain of the candidate URL.
     *
     * @return The domain of the candidate URL
     */
    public InternetDomainName getDomain() {
        return crawlRequest.getDomain();
    }

    /**
     * Returns the crawl depth of the candidate.
     *
     * @return The crawl depth of the candidate
     */
    public int getCrawlDepth() {
        return crawlDepth;
    }

    /**
     * Returns the priority of the candidate.
     *
     * @return The priority of the candidate
     */
    public int getPriority() {
        return crawlRequest.getPriority();
    }

    /**
     * Returns the metadata associated with the candidate.
     *
     * @return The metadata associated with the candidate
     */
    public Optional<Serializable> getMetadata() {
        return crawlRequest.getMetadata();
    }

    /**
     * Builds crawl candidates to be crawled by the crawler.
     */
    public static final class CrawlCandidateBuilder {

        private final CrawlRequest crawlRequest;

        private URI refererUrl;
        private int crawlDepth;

        /**
         * Creates a {@link CrawlCandidateBuilder} instance.
         *
         * @param request The {@link CrawlRequest} instance from which this
         * candidate is built
         */
        public CrawlCandidateBuilder(final CrawlRequest request) {
            crawlRequest = request;
        }

        /**
         * Sets the referer URL.
         *
         * @param refererUrl The referer URL
         * @return The {@link CrawlCandidateBuilder} instance
         */
        public CrawlCandidateBuilder setRefererUrl(final URI refererUrl) {
            this.refererUrl = refererUrl;
            return this;
        }

        /**
         * Sets the crawl depth of the candidate.
         *
         * @param crawlDepth The crawl depth of the candidate
         * @return The {@link CrawlCandidateBuilder} instance
         */
        public CrawlCandidateBuilder setCrawlDepth(final int crawlDepth) {
            this.crawlDepth = crawlDepth;
            return this;
        }

        /**
         * Builds the configured {@link CrawlCandidate} instance.
         *
         * @return The configured {@link CrawlCandidate} instance
         */
        public CrawlCandidate build() {
            return new CrawlCandidate(this);
        }
    }
}
