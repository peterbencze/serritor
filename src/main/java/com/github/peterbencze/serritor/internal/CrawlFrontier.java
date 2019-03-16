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

import com.github.peterbencze.serritor.api.CrawlCandidate;
import com.github.peterbencze.serritor.api.CrawlCandidate.CrawlCandidateBuilder;
import com.github.peterbencze.serritor.api.CrawlRequest;
import com.github.peterbencze.serritor.api.CrawlerConfiguration;
import com.github.peterbencze.serritor.internal.stats.StatsCounter;
import java.io.Serializable;
import java.net.URI;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;

/**
 * Manages crawl requests and provides crawl candidates to the crawler.
 */
public final class CrawlFrontier implements Serializable {

    private final CrawlerConfiguration config;
    private final StatsCounter statsCounter;
    private final Set<String> urlFingerprints;
    private final Queue<CrawlCandidate> candidates;

    private CrawlCandidate currentCandidate;

    /**
     * Creates a {@link CrawlFrontier} instance.
     *
     * @param config       the crawler configuration
     * @param statsCounter the stats counter which accumulates statistics during the operation of
     *                     the crawler
     */
    public CrawlFrontier(final CrawlerConfiguration config, final StatsCounter statsCounter) {
        this.config = config;
        this.statsCounter = statsCounter;
        urlFingerprints = new HashSet<>();
        candidates = createPriorityQueue();

        feedCrawlSeeds();
    }

    /**
     * Feeds a crawl request to the frontier.
     *
     * @param request     the crawl request
     * @param isCrawlSeed indicates if the request is a crawl seed
     */
    public void feedRequest(final CrawlRequest request, final boolean isCrawlSeed) {
        if (config.isOffsiteRequestFilterEnabled()) {
            boolean inCrawlDomain = config.getAllowedCrawlDomains()
                    .stream()
                    .anyMatch(crawlDomain -> crawlDomain.contains(request.getDomain()));

            if (!inCrawlDomain) {
                statsCounter.recordOffsiteRequest();
                return;
            }
        }

        if (config.isDuplicateRequestFilterEnabled()) {
            String urlFingerprint = createFingerprintForUrl(request.getRequestUrl());
            if (urlFingerprints.contains(urlFingerprint)) {
                statsCounter.recordDuplicateRequest();
                return;
            }

            urlFingerprints.add(urlFingerprint);
        }

        CrawlCandidateBuilder builder = new CrawlCandidateBuilder(request);

        if (!isCrawlSeed) {
            int crawlDepthLimit = config.getMaximumCrawlDepth();
            int nextCrawlDepth = currentCandidate.getCrawlDepth() + 1;

            if (crawlDepthLimit != 0 && nextCrawlDepth > crawlDepthLimit) {
                statsCounter.recordCrawlDepthLimitExceedingRequest();
                return;
            }

            builder.setRefererUrl(currentCandidate.getRequestUrl())
                    .setCrawlDepth(nextCrawlDepth);
        }

        candidates.add(builder.build());
        statsCounter.recordRemainingCrawlCandidate();
    }

    /**
     * Indicates if there are any candidates left in the queue.
     *
     * @return <code>true</code> if there are candidates in the queue, <code>false</code> otherwise
     */
    public boolean hasNextCandidate() {
        return !candidates.isEmpty();
    }

    /**
     * Returns the next crawl candidate from the queue.
     *
     * @return the next crawl candidate from the queue
     */
    public CrawlCandidate getNextCandidate() {
        currentCandidate = candidates.poll();
        return currentCandidate;
    }

    /**
     * Resets the crawl frontier to its initial state.
     */
    public void reset() {
        urlFingerprints.clear();
        candidates.clear();

        feedCrawlSeeds();
    }

    /**
     * Feeds all the crawl seeds to the crawl frontier.
     */
    private void feedCrawlSeeds() {
        config.getCrawlSeeds().forEach((CrawlRequest request) -> feedRequest(request, true));
    }

    /**
     * Creates the fingerprint of the given URL. If the URL contains query params, it sorts them by
     * key and value. This way URLs that have the same query params but in different order will have
     * the same fingerprint. Fragments are ignored.
     *
     * @param url the URL for which the fingerprint is created
     *
     * @return the fingerprint of the URL
     */
    private static String createFingerprintForUrl(final URI url) {
        URIBuilder builder = new URIBuilder(url);

        // Change scheme and host to lowercase
        builder.setScheme(builder.getScheme().toLowerCase())
                .setHost(builder.getHost().toLowerCase());

        // Sort query params by key and value
        List<NameValuePair> queryParams = builder.getQueryParams();
        queryParams.sort(Comparator.comparing(NameValuePair::getName)
                .thenComparing(NameValuePair::getValue));

        builder.setParameters(queryParams);

        // Remove fragment
        builder.setFragment(null);

        return DigestUtils.sha256Hex(builder.toString());
    }

    /**
     * Creates a priority queue using the strategy specified in the configuration.
     *
     * @return the priority queue using the strategy specified in the configuration
     */
    @SuppressWarnings("checkstyle:MissingSwitchDefault")
    private PriorityQueue<CrawlCandidate> createPriorityQueue() {
        Function crawlDepthGetter
                = (Function<CrawlCandidate, Integer> & Serializable) CrawlCandidate::getCrawlDepth;
        Function priorityGetter
                = (Function<CrawlCandidate, Integer> & Serializable) CrawlCandidate::getPriority;

        switch (config.getCrawlStrategy()) {
            case BREADTH_FIRST:
                Comparator breadthFirstComparator = Comparator.comparing(crawlDepthGetter)
                        .thenComparing(priorityGetter, Comparator.reverseOrder());

                return new PriorityQueue<>(breadthFirstComparator);
            case DEPTH_FIRST:
                Comparator depthFirstComparator
                        = Comparator.comparing(crawlDepthGetter, Comparator.reverseOrder())
                        .thenComparing(priorityGetter, Comparator.reverseOrder());

                return new PriorityQueue<>(depthFirstComparator);
        }

        throw new IllegalArgumentException("Unsupported crawl strategy.");
    }
}
