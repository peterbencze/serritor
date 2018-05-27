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
package com.github.peterbencze.serritor.internal;

import com.github.peterbencze.serritor.api.CrawlRequest;
import com.google.common.net.InternetDomainName;
import java.io.Serializable;
import java.net.URI;
import java.util.Optional;

/**
 * Represents a candidate for crawling that will be surely processed by the
 * crawler.
 *
 * @author Peter Bencze
 */
public final class CrawlCandidate implements Serializable {

    private final URI refererUrl;
    private final int crawlDepth;
    private final CrawlRequest crawlRequest;

    public CrawlCandidate(final CrawlCandidateBuilder builder) {
        this.crawlRequest = builder.crawlRequest;
        this.refererUrl = builder.refererUrl;
        this.crawlDepth = builder.crawlDepth;
    }

    /**
     * Returns the referer's URL.
     *
     * @return The URL of the referer
     */
    public URI getRefererUrl() {
        return refererUrl;
    }

    /**
     * Returns the candidate's URL.
     *
     * @return The URL of the candidate
     */
    public URI getCandidateUrl() {
        return crawlRequest.getRequestUrl();
    }

    /**
     * Returns the domain of the candidate's URL.
     *
     * @return The domain of the candidate URL
     */
    public InternetDomainName getDomain() {
        return crawlRequest.getDomain();
    }

    /**
     * Returns the crawl depth of the candidate.
     *
     * @return The crawl depth
     */
    public int getCrawlDepth() {
        return crawlDepth;
    }

    /**
     * Returns the priority of the candidate.
     *
     * @return The priority
     */
    public int getPriority() {
        return crawlRequest.getPriority();
    }
    
    /**
     * Returns metadata associated with the request.
     *
     * @return The request's metadata
     */
    public Optional<Serializable> getMetadata() {
        return crawlRequest.getMetadata();
    }

    /**
     * Returns the crawl request from which this candidate was constructed.
     *
     * @return The <code>CrawlRequest</code> instance
     */
    public CrawlRequest getCrawlRequest() {
        return crawlRequest;
    }

    public static final class CrawlCandidateBuilder {

        private final CrawlRequest crawlRequest;

        private URI refererUrl;
        private int crawlDepth;

        public CrawlCandidateBuilder(final CrawlRequest request) {
            crawlRequest = request;
        }

        public CrawlCandidateBuilder setRefererUrl(final URI refererUrl) {
            this.refererUrl = refererUrl;
            return this;
        }

        public CrawlCandidateBuilder setCrawlDepth(final int crawlDepth) {
            this.crawlDepth = crawlDepth;
            return this;
        }

        public CrawlCandidate build() {
            return new CrawlCandidate(this);
        }
    }
}
