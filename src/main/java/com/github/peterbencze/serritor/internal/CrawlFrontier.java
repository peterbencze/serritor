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
import java.net.URL;
import java.util.Arrays;
import java.util.Comparator;
import static java.util.Comparator.reverseOrder;
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
 * @author Krisztian Mozsi
 */
public final class CrawlFrontier implements Serializable {

    private final CrawlerConfiguration config;

    private final Set<String> allowedDomains;
    private final Set<String> urlFingerprints;

    private final Queue<CrawlCandidate> candidates;

    private CrawlCandidate currentCandidate;

    public CrawlFrontier(final CrawlerConfiguration config) {
        this.config = config;

        allowedDomains = new HashSet<>();
        urlFingerprints = new HashSet<>();

        // Construct a priority queue according to the crawl strategy specified in the configuration
        candidates = getPriorityQueue();

        // Feed initial crawl requests (seeds)
        config.getCrawlSeeds().stream()
                .forEach((CrawlRequest request) -> {
                    feedRequest(request, true);
                });
    }

    /**
     * Feeds a crawl request to the frontier.
     *
     * @param request The request to be fed
     * @param isCrawlSeed True if the request is a crawl seed, false otherwise
     */
    public void feedRequest(final CrawlRequest request, final boolean isCrawlSeed) {
        if (config.isOffsiteRequestFilteringEnabled()) {
            if (isCrawlSeed) {
                allowedDomains.add(request.getTopPrivateDomain());
            } else {
                if (!allowedDomains.contains(request.getTopPrivateDomain())) {
                    return;
                }
            }
        }

        if (config.isDuplicateRequestFilteringEnabled()) {
            String urlFingerprint = getFingerprintForUrl(request.getRequestUrl());

            // Check if the URL has already been crawled
            if (urlFingerprints.contains(urlFingerprint)) {
                return;
            }

            // If not, add its fingerprint to the set of URL fingerprints
            urlFingerprints.add(urlFingerprint);
        }

        CrawlCandidateBuilder builder;

        if (!isCrawlSeed) {
            int crawlDepthLimit = config.getMaxCrawlDepth();
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
     * @return True if there are candidates in the queue, false otherwise
     */
    public boolean hasNextCandidate() {
        return !candidates.isEmpty();
    }

    /**
     * Gets the next candidate from the queue.
     *
     * @return The next candidate
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
    private String getFingerprintForUrl(final URL url) {
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
     * @return A new PriorityQueue instance for CrawlRequests using the given
     * comparator
     */
    private PriorityQueue<CrawlCandidate> getPriorityQueue() {
        switch (config.getCrawlStrategy()) {
            case BREADTH_FIRST:
                return new PriorityQueue<>(Comparator.comparing((Function<CrawlCandidate, Integer> & Serializable) CrawlCandidate::getCrawlDepth)
                        .thenComparing((Function<CrawlCandidate, Integer> & Serializable) CrawlCandidate::getPriority, reverseOrder()));
            case DEPTH_FIRST:
                return new PriorityQueue<>(Comparator.comparing((Function<CrawlCandidate, Integer> & Serializable) CrawlCandidate::getCrawlDepth, reverseOrder())
                        .thenComparing((Function<CrawlCandidate, Integer> & Serializable) CrawlCandidate::getPriority, reverseOrder()));
        }

        throw new IllegalArgumentException("Unsupported crawl strategy.");
    }
}
