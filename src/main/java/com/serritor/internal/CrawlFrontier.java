/* 
 * Copyright 2016 Peter Bencze.
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
package com.serritor.internal;

import com.serritor.api.CrawlingStrategy;
import com.serritor.internal.CrawlRequest;
import com.serritor.internal.CrawlRequestComparator;
import java.io.Serializable;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * Provides an interface for the crawler to manage crawl requests while crawling.
 *
 * @author Peter Bencze
 * @author Krisztian Mozsi
 */
public final class CrawlFrontier implements Serializable {

    private final CrawlerConfiguration config;

    private final Set<String> allowedDomains;
    private final Set<String> urlFingerprints;

    private final Queue<CrawlRequest> requests;

    public CrawlFrontier(CrawlerConfiguration config) {
        this.config = config;

        allowedDomains = new HashSet<>();
        urlFingerprints = new HashSet<>();

        requests = getPriorityQueue(config.getCrawlingStrategy());

        config.getSeeds().stream()
                .forEach((CrawlRequest request) -> {
                    if (config.getFilterOffsiteRequests()) {
                        allowedDomains.add(request.getTopPrivateDomain());
                    }

                    if (config.getFilterDuplicateRequests()) {
                        String urlFingerprint = getFingerprintForUrl(request.getRequestUrl());

                        if (!urlFingerprints.contains(urlFingerprint)) {
                            addRequest(request, urlFingerprint);
                        }
                    }
                });
    }

    /**
     * Method for the crawler to feed requests to the frontier.
     *
     * @param request The request to be fed
     */
    public void feedRequest(CrawlRequest request) {
        String urlFingerprint = getFingerprintForUrl(request.getRequestUrl());

        if (config.getFilterDuplicateRequests() && urlFingerprints.contains(urlFingerprint)) {
            return;
        }

        if (config.getFilterOffsiteRequests() && !allowedDomains.contains(request.getTopPrivateDomain())) {
            return;
        }

        addRequest(request, urlFingerprint);
    }

    /**
     * Indicates if there are any requests left in the queue.
     *
     * @return True if there are requests in the queue, false otherwise
     */
    public boolean hasNextRequest() {
        return !requests.isEmpty();
    }

    /**
     * Gets the next request from the queue.
     *
     * @return The next request
     */
    public CrawlRequest getNextRequest() {
        return requests.poll();
    }

    /**
     * Adds a request to the queue and stores its fingerprint.
     *
     * @param request The request to be added to the queue
     */
    private void addRequest(CrawlRequest request, String urlFingerprint) {
        urlFingerprints.add(urlFingerprint);
        requests.add(request);
    }

    /**
     * Creates the fingerprint of the given URL.
     *
     * @param url The URL that the fingerprint will be created for
     * @return The fingerprint of the URL
     */
    private String getFingerprintForUrl(URL url) {
        StringBuilder truncatedUrl = new StringBuilder(url.getHost());

        String path = url.getPath();
        if (path != null && !path.equals("/")) {
            truncatedUrl.append(path);
        }

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
     * Creates a new priority queue using the given strategy related comparator.
     *
     * @param strategy The URL traversal strategy
     * @return A new PriorityQueue instance for CrawlRequests using the given
     * comparator
     */
    private PriorityQueue<CrawlRequest> getPriorityQueue(CrawlingStrategy strategy) {
        switch (strategy) {
            case BREADTH_FIRST:
                return new PriorityQueue<>(new CrawlRequestComparator());
            case DEPTH_FIRST:
                return new PriorityQueue<>(new CrawlRequestComparator().reversed());
        }

        throw new IllegalArgumentException("Not supported crawling strategy.");
    }
}
