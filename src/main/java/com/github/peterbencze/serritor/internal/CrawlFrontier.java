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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages crawl requests and provides crawl candidates to the crawler.
 */
public final class CrawlFrontier implements Serializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(CrawlFrontier.class);
    private static final int INITIAL_CRAWL_DEPTH = 1;

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
    }

    /**
     * Feeds a crawl request to the frontier.
     *
     * @param request     the crawl request
     * @param isCrawlSeed indicates if the request is a crawl seed
     */
    public void feedRequest(final CrawlRequest request, final boolean isCrawlSeed) {
        LOGGER.debug("Feeding request: {}", request);

        if (config.isOffsiteRequestFilterEnabled()) {
            boolean inCrawlDomain = config.getAllowedCrawlDomains()
                    .stream()
                    .anyMatch(crawlDomain -> crawlDomain.contains(request.getDomain()));

            if (!inCrawlDomain) {
                LOGGER.debug("Filtering offsite request");

                statsCounter.recordOffsiteRequest();
                return;
            }
        }

        if (config.isDuplicateRequestFilterEnabled()) {
            String urlFingerprint = createFingerprintForUrl(request.getRequestUrl());
            if (urlFingerprints.contains(urlFingerprint)) {
                LOGGER.debug("Filtering duplicate request");

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
                LOGGER.debug("Filtering crawl depth limit exceeding request");

                statsCounter.recordCrawlDepthLimitExceedingRequest();
                return;
            }

            builder.setRefererUrl(currentCandidate.getRequestUrl())
                    .setCrawlDepth(nextCrawlDepth);
        } else {
            builder.setCrawlDepth(INITIAL_CRAWL_DEPTH);
        }

        LOGGER.debug("Adding request to the list of crawl candidates");
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
        LOGGER.debug("Setting crawl frontier to its initial state");

        urlFingerprints.clear();
        candidates.clear();
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
    private PriorityQueue<CrawlCandidate> createPriorityQueue() {
        Function<CrawlCandidate, Integer> crawlDepthGetter =
                (Function<CrawlCandidate, Integer> & Serializable) CrawlCandidate::getCrawlDepth;
        Function<CrawlCandidate, Integer> priorityGetter =
                (Function<CrawlCandidate, Integer> & Serializable) CrawlCandidate::getPriority;

        switch (config.getCrawlStrategy()) {
            case BREADTH_FIRST:
                Comparator<CrawlCandidate> breadthFirstComparator =
                        Comparator.comparing(crawlDepthGetter)
                                .thenComparing(priorityGetter, Comparator.reverseOrder());

                return new PriorityQueue<>(breadthFirstComparator);
            case DEPTH_FIRST:
                Comparator<CrawlCandidate> depthFirstComparator =
                        Comparator.comparing(crawlDepthGetter, Comparator.reverseOrder())
                                .thenComparing(priorityGetter, Comparator.reverseOrder());

                return new PriorityQueue<>(depthFirstComparator);
            default:
                throw new IllegalArgumentException("Unsupported crawl strategy");
        }
    }
}
