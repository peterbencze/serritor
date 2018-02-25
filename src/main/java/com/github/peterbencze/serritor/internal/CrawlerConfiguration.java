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

import com.github.peterbencze.serritor.api.CrawlDelayStrategy;
import com.github.peterbencze.serritor.api.CrawlRequest;
import com.github.peterbencze.serritor.api.CrawlStrategy;
import java.io.Serializable;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides an interface to configure the crawler.
 *
 * @author Krisztian Mozsi
 * @author Peter Bencze
 */
public final class CrawlerConfiguration implements Serializable {

    private static final CrawlStrategy DEFAULT_CRAWL_STRATEGY = CrawlStrategy.BREADTH_FIRST;
    private static final boolean FILTER_DUPLICATE_REQUESTS_BY_DEFAULT = true;
    private static final boolean FILTER_OFFSITE_REQUESTS_BY_DEFAULT = false;
    private static final int DEFAULT_MAX_CRAWL_DEPTH = 0;
    private static final CrawlDelayStrategy DEFAULT_CRAWL_DELAY = CrawlDelayStrategy.FIXED;
    private static final long DEFAULT_FIXED_CRAWL_DELAY_IN_MILLIS = Duration.ZERO.toMillis();
    private static final long DEFAULT_MIN_CRAWL_DELAY_IN_MILLIS = Duration.ofSeconds(1).toMillis();
    private static final long DEFAULT_MAX_CRAWL_DELAY_IN_MILLIS = Duration.ofMinutes(1).toMillis();

    private final List<CrawlRequest> crawlSeeds;

    private CrawlStrategy crawlStrategy;
    private boolean filterDuplicateRequests;
    private boolean filterOffsiteRequests;
    private int maxCrawlDepth;
    private CrawlDelayStrategy crawlDelayStrategy;
    private long fixedCrawlDelayInMillis;
    private long minCrawlDelayInMillis;
    private long maxCrawlDelayInMillis;

    public CrawlerConfiguration() {
        // Default configuration
        crawlSeeds = new ArrayList<>();
        crawlStrategy = DEFAULT_CRAWL_STRATEGY;
        filterDuplicateRequests = FILTER_DUPLICATE_REQUESTS_BY_DEFAULT;
        filterOffsiteRequests = FILTER_OFFSITE_REQUESTS_BY_DEFAULT;
        maxCrawlDepth = DEFAULT_MAX_CRAWL_DEPTH;
        crawlDelayStrategy = DEFAULT_CRAWL_DELAY;
        fixedCrawlDelayInMillis = DEFAULT_FIXED_CRAWL_DELAY_IN_MILLIS;
        minCrawlDelayInMillis = DEFAULT_MIN_CRAWL_DELAY_IN_MILLIS;
        maxCrawlDelayInMillis = DEFAULT_MAX_CRAWL_DELAY_IN_MILLIS;
    }

    /**
     * Returns the list of crawl seeds.
     *
     * @return The list of crawl seeds
     */
    public List<CrawlRequest> getCrawlSeeds() {
        return crawlSeeds;
    }

    /**
     * Appends a crawl request to the list of crawl seeds.
     *
     * @param request The crawl request
     */
    public void addCrawlSeed(final CrawlRequest request) {
        crawlSeeds.add(request);
    }

    /**
     * Appends a list of crawl requests to the list of crawl seeds.
     *
     * @param requests The list of crawl requests
     */
    public void addCrawlSeeds(final List<CrawlRequest> requests) {
        crawlSeeds.addAll(requests);
    }

    /**
     * Returns the crawl strategy of the crawler.
     *
     * @return The crawl strategy
     */
    public CrawlStrategy getCrawlStrategy() {
        return crawlStrategy;
    }

    /**
     * Sets the crawl strategy of the crawler.
     *
     * @param crawlStrategy The crawl strategy
     */
    public void setCrawlStrategy(final CrawlStrategy crawlStrategy) {
        this.crawlStrategy = crawlStrategy;
    }

    /**
     * Indicates if duplicate request filtering is enabled or not.
     *
     * @return True if it is enabled, false otherwise
     */
    public boolean isDuplicateRequestFilteringEnabled() {
        return filterDuplicateRequests;
    }

    /**
     * Sets duplicate request filtering.
     *
     * @param filterDuplicateRequests True means enabled, false means disabled
     */
    public void setDuplicateRequestFiltering(final boolean filterDuplicateRequests) {
        this.filterDuplicateRequests = filterDuplicateRequests;
    }

    /**
     * Indicates if offsite request filtering is enabled or not.
     *
     * @return True if it is enabled, false otherwise
     */
    public boolean isOffsiteRequestFilteringEnabled() {
        return filterOffsiteRequests;
    }

    /**
     * Sets offsite request filtering.
     *
     * @param filterOffsiteRequests True means enabled, false means disabled
     */
    public void setOffsiteRequestFiltering(final boolean filterOffsiteRequests) {
        this.filterOffsiteRequests = filterOffsiteRequests;
    }

    /**
     * Returns the maximum possible crawl depth.
     *
     * @return The maximum crawl depth
     */
    public int getMaximumCrawlDepth() {
        return maxCrawlDepth;
    }

    /**
     * Sets the maximum possible crawl depth.
     *
     * @param maxCrawlDepth The maximum crawl depth, zero means no limit
     */
    public void setMaximumCrawlDepth(final int maxCrawlDepth) {
        this.maxCrawlDepth = maxCrawlDepth;
    }

    /**
     * Sets the crawl delay strategy to be used by the crawler.
     *
     * @param crawlDelayStrategy The crawl delay strategy
     */
    public void setCrawlDelayStrategy(final CrawlDelayStrategy crawlDelayStrategy) {
        this.crawlDelayStrategy = crawlDelayStrategy;
    }

    /**
     * Returns the crawl delay strategy used by the crawler.
     *
     * @return The crawl delay type
     */
    public CrawlDelayStrategy getCrawlDelayStrategy() {
        return crawlDelayStrategy;
    }

    /**
     * Sets the exact duration of delay between each request.
     *
     * @param fixedCrawlDelayDuration The duration of delay
     */
    public void setFixedCrawlDelayDuration(final Duration fixedCrawlDelayDuration) {
        try {
            fixedCrawlDelayInMillis = fixedCrawlDelayDuration.toMillis();
        } catch (ArithmeticException ex) {
            throw new IllegalArgumentException("The duration is too large.");
        }
    }

    /**
     * Returns the exact duration of delay between each request.
     *
     * @return The duration of delay in milliseconds
     */
    public long getFixedCrawlDelayInMillis() {
        return fixedCrawlDelayInMillis;
    }

    /**
     * Sets the minimum duration of delay between each request.
     *
     * @param minCrawlDelayDuration The minimum duration of delay
     */
    public void setMinimumCrawlDelayDuration(final Duration minCrawlDelayDuration) {
        if (minCrawlDelayDuration.isNegative()) {
            throw new IllegalArgumentException("The minimum crawl delay should be positive.");
        }

        try {
            long delayInMillis = minCrawlDelayDuration.toMillis();
            if (delayInMillis >= maxCrawlDelayInMillis) {
                throw new IllegalArgumentException("The minimum crawl delay should be less than the maximum.");
            }

            minCrawlDelayInMillis = delayInMillis;
        } catch (ArithmeticException ex) {
            throw new IllegalArgumentException("The duration is too large.");
        }
    }

    /**
     * Returns the minimum duration of delay between each request.
     *
     * @return The minimum duration of delay in milliseconds
     */
    public long getMinimumCrawlDelayInMillis() {
        return minCrawlDelayInMillis;
    }

    /**
     * Sets the maximum duration of delay between each request.
     *
     * @param maxCrawlDelayDuration The maximum duration of delay
     */
    public void setMaximumCrawlDelayDuration(final Duration maxCrawlDelayDuration) {
        try {
            long delayInMillis = maxCrawlDelayDuration.toMillis();
            if (delayInMillis <= minCrawlDelayInMillis) {
                throw new IllegalArgumentException("The maximum crawl delay should be higher than the minimum.");
            }

            maxCrawlDelayInMillis = delayInMillis;
        } catch (ArithmeticException ex) {
            throw new IllegalArgumentException("The duration is too large.");
        }
    }

    /**
     * Returns the maximum duration of delay between each request.
     *
     * @return The maximum duration of delay in milliseconds
     */
    public long getMaximumCrawlDelayInMillis() {
        return maxCrawlDelayInMillis;
    }
}
