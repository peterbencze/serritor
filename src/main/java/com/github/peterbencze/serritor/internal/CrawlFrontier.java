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
 * Manages crawl requests and provides crawl candidates to the crawler.
 *
 * @author Peter Bencze
 */
public final class CrawlFrontier implements Serializable {

    private final CrawlerConfiguration config;
    private final Set<String> urlFingerprints;
    private final Queue<CrawlCandidate> candidates;

    private CrawlCandidate currentCandidate;

    /**
     * Creates a {@link CrawlFrontier} instance.
     *
     * @param config the crawler configuration
     */
    public CrawlFrontier(final CrawlerConfiguration config) {
        this.config = config;
        urlFingerprints = new HashSet<>();
        candidates = createPriorityQueue();

        config.getCrawlSeeds()
                .forEach((CrawlRequest request) -> {
                    feedRequest(request, true);
                });
    }

    /**
     * Feeds a crawl request to the frontier.
     *
     * @param request     the crawl request
     * @param isCrawlSeed indicates if the request is a crawl seed
     */
    public void feedRequest(final CrawlRequest request, final boolean isCrawlSeed) {
        if (config.isOffsiteRequestFilteringEnabled()) {
            boolean inCrawlDomain = false;

            for (CrawlDomain allowedCrawlDomain : config.getAllowedCrawlDomains()) {
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
            String urlFingerprint = createFingerprintForUrl(request.getRequestUrl());

            if (urlFingerprints.contains(urlFingerprint)) {
                return;
            }

            urlFingerprints.add(urlFingerprint);
        }

        CrawlCandidateBuilder builder = new CrawlCandidateBuilder(request);

        if (!isCrawlSeed) {
            int crawlDepthLimit = config.getMaximumCrawlDepth();
            int nextCrawlDepth = currentCandidate.getCrawlDepth() + 1;

            if (crawlDepthLimit != 0 && nextCrawlDepth > crawlDepthLimit) {
                return;
            }

            builder = builder
                    .setRefererUrl(currentCandidate.getRequestUrl())
                    .setCrawlDepth(nextCrawlDepth);
        }

        candidates.add(builder.build());
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
     * Creates the fingerprint of the given URL. If the URL contains query parameters, it sorts
     * them. This way URLs with different order of query parameters get the same fingerprint.
     *
     * @param url the URL for which the fingerprint is created
     *
     * @return the fingerprint of the URL
     */
    private static String createFingerprintForUrl(final URI url) {
        StringBuilder truncatedUrl = new StringBuilder(url.getHost()).append(url.getPath());

        String query = url.getQuery();
        if (query != null) {
            truncatedUrl.append("?");

            String[] queryParams = url.getQuery().split("&");

            List<String> queryParamList = Arrays.asList(queryParams);
            queryParamList.stream()
                    .sorted(String::compareToIgnoreCase)
                    .forEachOrdered(truncatedUrl::append);
        }

        return DigestUtils.sha256Hex(truncatedUrl.toString());
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
