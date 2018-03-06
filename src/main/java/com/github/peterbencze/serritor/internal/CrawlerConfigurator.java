/* 
 * Copyright 2018 Peter Bencze.
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
import com.google.common.base.Preconditions;
import java.time.Duration;
import java.util.List;

/**
 * This class provides an interface for the user to configure the crawler.
 *
 * @author Peter Bencze
 */
public final class CrawlerConfigurator {

    private final CrawlerConfiguration configuration;

    public CrawlerConfigurator(CrawlerConfiguration configuration) {
        this.configuration = configuration;
    }

    /**
     * Appends a crawl request to the list of crawl seeds.
     *
     * @param request The <code>CrawlRequest</code> instance which represents
     * the crawl seed
     */
    public void addCrawlSeed(final CrawlRequest request) {
        configuration.addCrawlSeed(Preconditions.checkNotNull(request));
    }

    /**
     * Appends a list of crawl requests to the list of crawl seeds.
     *
     * @param requests The list of <code>CrawlRequest</code> instances which
     * represent the crawl seeds
     */
    public void addCrawlSeeds(final List<CrawlRequest> requests) {
        requests.forEach(this::addCrawlSeed);
    }

    /**
     * Sets the crawl strategy to be used by the crawler. Breadth-first strategy
     * orders crawl requests by the lowest crawl depth, whereas depth-first
     * orders them by the highest crawl depth.
     *
     * @param crawlStrategy The crawl strategy
     */
    public void setCrawlStrategy(final CrawlStrategy crawlStrategy) {
        configuration.setCrawlStrategy(Preconditions.checkNotNull(crawlStrategy));
    }

    /**
     * Enables or disables duplicate request filtering.
     *
     * @param filterDuplicateRequests <code>true</code> means enabled,
     * <code>false</code> means disabled
     */
    public void setDuplicateRequestFiltering(final boolean filterDuplicateRequests) {
        configuration.setDuplicateRequestFiltering(filterDuplicateRequests);
    }

    /**
     * Enables or disables offsite request filtering.
     *
     * @param filterOffsiteRequests <code>true</code> means enabled,
     * <code>false</code> means disabled
     */
    public void setOffsiteRequestFiltering(final boolean filterOffsiteRequests) {
        configuration.setOffsiteRequestFiltering(filterOffsiteRequests);
    }

    /**
     * Sets the maximum possible crawl depth. It should be a non-negative number
     * where 0 means there is no limit.
     *
     * @param maxCrawlDepth The maximum crawl depth
     */
    public void setMaximumCrawlDepth(final int maxCrawlDepth) {
        Preconditions.checkArgument(maxCrawlDepth >= 0, "The maximum crawl depth cannot be negative.");

        configuration.setMaximumCrawlDepth(maxCrawlDepth);
    }

    /**
     * Sets the crawl delay strategy to be used by the crawler.
     *
     * @param crawlDelayStrategy The crawl delay strategy
     */
    public void setCrawlDelayStrategy(final CrawlDelayStrategy crawlDelayStrategy) {
        configuration.setCrawlDelayStrategy(Preconditions.checkNotNull(crawlDelayStrategy));
    }

    /**
     * Sets the exact duration of delay between each request.
     *
     * @param fixedCrawlDelayDuration The duration of delay
     */
    public void setFixedCrawlDelayDuration(final Duration fixedCrawlDelayDuration) {
        configuration.setFixedCrawlDelayDurationInMillis(fixedCrawlDelayDuration.toMillis());
    }

    /**
     * Sets the minimum duration of delay between each request.
     *
     * @param minCrawlDelayDuration The minimum duration of delay
     */
    public void setMinimumCrawlDelayDuration(final Duration minCrawlDelayDuration) {
        Preconditions.checkArgument(!minCrawlDelayDuration.isNegative(), "The minimum crawl delay cannot be negative.");

        long minCrawlDelayDurationInMillis = minCrawlDelayDuration.toMillis();
        long maxCrawlDelayInMillis = configuration.getMaximumCrawlDelayDurationInMillis();

        Preconditions.checkArgument(minCrawlDelayDurationInMillis < maxCrawlDelayInMillis, "The minimum crawl delay should be less than the maximum.");

        configuration.setMinimumCrawlDelayDurationInMillis(minCrawlDelayDurationInMillis);
    }

    /**
     * Sets the maximum duration of delay between each request.
     *
     * @param maxCrawlDelayDuration The maximum duration of delay
     */
    public void setMaximumCrawlDelayDuration(final Duration maxCrawlDelayDuration) {
        long minCrawlDelayDurationInMillis = configuration.getMinimumCrawlDelayDurationInMillis();
        long maxCrawlDelayDurationInMillis = maxCrawlDelayDuration.toMillis();

        Preconditions.checkArgument(maxCrawlDelayDurationInMillis > minCrawlDelayDurationInMillis, "The maximum crawl delay should be higher than the minimum.");

        configuration.setMaximumCrawlDelayDuration(maxCrawlDelayDurationInMillis);
    }
}
