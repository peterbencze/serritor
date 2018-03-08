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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class contains the settings of the crawler.
 *
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

    private final Set<CrawlDomain> allowedCrawlDomains;
    private final List<CrawlRequest> crawlSeeds;

    private CrawlStrategy crawlStrategy;
    private boolean filterDuplicateRequests;
    private boolean filterOffsiteRequests;
    private int maxCrawlDepth;
    private CrawlDelayStrategy crawlDelayStrategy;
    private long fixedCrawlDelayDurationInMillis;
    private long minCrawlDelayDurationInMillis;
    private long maxCrawlDelayDurationInMillis;

    public CrawlerConfiguration() {
        // Initialize configuration with default values

        allowedCrawlDomains = new HashSet<>();
        crawlSeeds = new ArrayList<>();
        crawlStrategy = DEFAULT_CRAWL_STRATEGY;
        filterDuplicateRequests = FILTER_DUPLICATE_REQUESTS_BY_DEFAULT;
        filterOffsiteRequests = FILTER_OFFSITE_REQUESTS_BY_DEFAULT;
        maxCrawlDepth = DEFAULT_MAX_CRAWL_DEPTH;
        crawlDelayStrategy = DEFAULT_CRAWL_DELAY;
        fixedCrawlDelayDurationInMillis = DEFAULT_FIXED_CRAWL_DELAY_IN_MILLIS;
        minCrawlDelayDurationInMillis = DEFAULT_MIN_CRAWL_DELAY_IN_MILLIS;
        maxCrawlDelayDurationInMillis = DEFAULT_MAX_CRAWL_DELAY_IN_MILLIS;
    }

    /**
     * Returns the set of allowed crawl domains.
     *
     * @return The set of allowed crawl domains
     */
    public Set<CrawlDomain> getAllowedCrawlDomains() {
        return allowedCrawlDomains;
    }

    /**
     * Appends a crawl domain to the list of allowed ones.
     *
     * @param allowedCrawlDomain The <code>CrawlDomain</code> instance which
     * represents the allowed crawl domain
     */
    public void addAllowedCrawlDomain(CrawlDomain allowedCrawlDomain) {
        allowedCrawlDomains.add(allowedCrawlDomain);
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
     * @param request The <code>CrawlRequest</code> instance which represents
     * the crawl seed
     */
    public void addCrawlSeed(final CrawlRequest request) {
        crawlSeeds.add(request);
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
     * Sets the crawl strategy to be used by the crawler. Breadth-first strategy
     * orders crawl requests by the lowest crawl depth, whereas depth-first
     * orders them by the highest crawl depth.
     *
     * @param crawlStrategy The crawl strategy
     */
    public void setCrawlStrategy(final CrawlStrategy crawlStrategy) {
        this.crawlStrategy = crawlStrategy;
    }

    /**
     * Indicates if duplicate request filtering is enabled or not.
     *
     * @return <code>true</code> if enabled, <code>false</code> otherwise
     */
    public boolean isDuplicateRequestFilteringEnabled() {
        return filterDuplicateRequests;
    }

    /**
     * Enables or disables duplicate request filtering.
     *
     * @param filterDuplicateRequests <code>true</code> means enabled,
     * <code>false</code> means disabled
     */
    public void setDuplicateRequestFiltering(final boolean filterDuplicateRequests) {
        this.filterDuplicateRequests = filterDuplicateRequests;
    }

    /**
     * Indicates if offsite request filtering is enabled or not.
     *
     * @return <code>true</code> if enabled, <code>false</code> otherwise
     */
    public boolean isOffsiteRequestFilteringEnabled() {
        return filterOffsiteRequests;
    }

    /**
     * Enables or disables offsite request filtering.
     *
     * @param filterOffsiteRequests <code>true</code> means enabled,
     * <code>false</code> means disabled
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
     * Sets the maximum possible crawl depth. It should be a non-negative number
     * where 0 means there is no limit.
     *
     * @param maxCrawlDepth The maximum crawl depth
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
     * @return The crawl delay strategy
     */
    public CrawlDelayStrategy getCrawlDelayStrategy() {
        return crawlDelayStrategy;
    }

    /**
     * Sets the exact duration of delay between each request.
     *
     * @param fixedCrawlDelayDurationInMillis The duration of delay in
     * milliseconds
     */
    public void setFixedCrawlDelayDurationInMillis(final long fixedCrawlDelayDurationInMillis) {
        this.fixedCrawlDelayDurationInMillis = fixedCrawlDelayDurationInMillis;
    }

    /**
     * Returns the exact duration of delay between each request.
     *
     * @return The duration of delay in milliseconds
     */
    public long getFixedCrawlDelayDurationInMillis() {
        return fixedCrawlDelayDurationInMillis;
    }

    /**
     * Sets the minimum duration of delay between each request.
     *
     * @param minCrawlDelayDurationInMillis The minimum duration of delay in
     * milliseconds
     */
    public void setMinimumCrawlDelayDurationInMillis(final long minCrawlDelayDurationInMillis) {
        this.minCrawlDelayDurationInMillis = minCrawlDelayDurationInMillis;
    }

    /**
     * Returns the minimum duration of delay between each request.
     *
     * @return The minimum duration of delay in milliseconds
     */
    public long getMinimumCrawlDelayDurationInMillis() {
        return minCrawlDelayDurationInMillis;
    }

    /**
     * Sets the maximum duration of delay between each request.
     *
     * @param maxCrawlDelayDurationInMillis The maximum duration of delay in
     * milliseconds
     */
    public void setMaximumCrawlDelayDuration(final long maxCrawlDelayDurationInMillis) {
        this.maxCrawlDelayDurationInMillis = maxCrawlDelayDurationInMillis;
    }

    /**
     * Returns the maximum duration of delay between each request.
     *
     * @return The maximum duration of delay in milliseconds
     */
    public long getMaximumCrawlDelayDurationInMillis() {
        return maxCrawlDelayDurationInMillis;
    }
}
