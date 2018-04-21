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
import com.github.peterbencze.serritor.internal.CrawlCandidate.CrawlCandidateBuilder;
import java.io.Serializable;
import java.net.URI;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * Provides an interface for the crawler to manage crawl requests while
 * crawling.
 *
 * @author Peter Bencze
 */
public final class CrawlFrontier implements Serializable {

    private final CrawlerConfiguration config;

    private final Set<CrawlDomain> allowedCrawlDomains;
    private final Set<String> urlFingerprints;

    private final Queue<CrawlCandidate> candidates;

    private CrawlCandidate currentCandidate;

    public CrawlFrontier(final CrawlerConfiguration config) {
        this.config = config;

        allowedCrawlDomains = config.getAllowedCrawlDomains();
        
        urlFingerprints = new HashSet<>();

        // Construct a priority queue according to the crawl strategy specified in the configuration
        candidates = createPriorityQueue();

        // Feed initial crawl requests (seeds)
        config.getCrawlSeeds()
                .forEach((CrawlRequest request) -> {
                    feedRequest(request, true);
                });
    }

    /**
     * Feeds a crawl request to the frontier.
     *
     * @param request The <code>CrawlRequest</code> instance to be fed
     * @param isCrawlSeed <code>true</code> if the request is a crawl seed,
     * <code>false</code> otherwise
     */
    public void feedRequest(final CrawlRequest request, final boolean isCrawlSeed) {
        if (config.isOffsiteRequestFilteringEnabled()) {
            // Check if the request's domain is in the allowed crawl domains
            
            boolean inCrawlDomain = false;
            
            for (CrawlDomain allowedCrawlDomain : allowedCrawlDomains) {
                if (allowedCrawlDomain.contains(request.getDomain())) {
                    inCrawlDomain = true;
                    break;
                }
            }
            
            if (!inCrawlDomain) {
                return;
            }
        }

        if (config.isDuplicateRequestFilteringEnabled()) {
            // Check if the URL has already been crawled
            
            String urlFingerprint = createFingerprintForUrl(request.getRequestUrl());

            
            if (urlFingerprints.contains(urlFingerprint)) {
                return;
            }

            urlFingerprints.add(urlFingerprint);
        }

        CrawlCandidateBuilder builder;

        if (!isCrawlSeed) {
            int crawlDepthLimit = config.getMaximumCrawlDepth();
            int nextCrawlDepth = currentCandidate.getCrawlDepth() + 1;

            // If a crawl depth limit is set, check if the candidate's crawl depth is less than or equal to the limit
            if (crawlDepthLimit != 0 && nextCrawlDepth > crawlDepthLimit) {
                return;
            }

            builder = new CrawlCandidateBuilder(request).setRefererUrl(currentCandidate.getCandidateUrl())
                    .setCrawlDepth(nextCrawlDepth);
        } else {
            builder = new CrawlCandidateBuilder(request);
        }

        // Finally, add constructed candidate to the queue
        candidates.add(builder.build());
    }

    /**
     * Indicates if there are any candidates left in the queue.
     *
     * @return <code>true</code> if there are candidates in the queue,
     * <code>false</code> otherwise
     */
    public boolean hasNextCandidate() {
        return !candidates.isEmpty();
    }

    /**
     * Gets the next candidate from the queue.
     *
     * @return The next <code>CrawlCandidate</code> instance
     */
    public CrawlCandidate getNextCandidate() {
        currentCandidate = candidates.poll();
        return currentCandidate;
    }

    /**
     * Creates the fingerprint of the given URL.
     *
     * @param url The URL that the fingerprint will be created for
     * @return The fingerprint of the URL
     */
    private static String createFingerprintForUrl(final URI url) {
        // First, we start off with the host only
        StringBuilder truncatedUrl = new StringBuilder(url.getHost());

        // If there is a path in the URL, we append it after the host
        String path = url.getPath();
        if (path != null && !"/".equals(path)) {
            truncatedUrl.append(path);
        }

        // If there are any query params, we sort and append them to what we got so far
        // This is required in order to detect already crawled URLs with different order of query params
        String query = url.getQuery();
        if (query != null) {
            truncatedUrl.append("?");

            String[] queryParams = url.getQuery().split("&");

            List<String> queryParamList = Arrays.asList(queryParams);
            queryParamList.stream()
                    .sorted(String::compareToIgnoreCase)
                    .forEachOrdered(truncatedUrl::append);
        }

        // Finally, create the SHA-256 hash
        return DigestUtils.sha256Hex(truncatedUrl.toString());
    }

    /**
     * Creates a new priority queue using the specified strategy.
     *
     * @return The <code>PriorityQueue</code> instance for crawl requests using
     * the given comparator
     */
    private PriorityQueue<CrawlCandidate> createPriorityQueue() {
        switch (config.getCrawlStrategy()) {
            case BREADTH_FIRST:
                return new PriorityQueue<>(Comparator.comparing((Function<CrawlCandidate, Integer> & Serializable) CrawlCandidate::getCrawlDepth)
                        .thenComparing((Function<CrawlCandidate, Integer> & Serializable) CrawlCandidate::getPriority, Comparator.reverseOrder()));
            case DEPTH_FIRST:
                return new PriorityQueue<>(Comparator.comparing((Function<CrawlCandidate, Integer> & Serializable) CrawlCandidate::getCrawlDepth, Comparator.reverseOrder())
                        .thenComparing((Function<CrawlCandidate, Integer> & Serializable) CrawlCandidate::getPriority, Comparator.reverseOrder()));
        }

        throw new IllegalArgumentException("Unsupported crawl strategy.");
    }
}
