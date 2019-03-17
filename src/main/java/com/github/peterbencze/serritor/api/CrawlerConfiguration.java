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

package com.github.peterbencze.serritor.api;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.github.peterbencze.serritor.internal.CrawlDomain;
import com.google.common.net.InternetDomainName;
import java.io.Serializable;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Contains the settings of the crawler.
 */
@JsonPropertyOrder({
        "crawlSeeds",
        "crawlStrategy",
        "maximumCrawlDepth",
        "duplicateRequestFilterEnabled",
        "offsiteRequestFilterEnabled",
        "allowedCrawlDomains",
        "crawlDelayStrategy",
        "fixedCrawlDelayDurationInMillis",
        "minimumCrawlDelayDurationInMillis",
        "maximumCrawlDelayDurationInMillis"
})
public final class CrawlerConfiguration implements Serializable {

    public static final long DEFAULT_PAGE_LOAD_TIMEOUT_IN_MILLIS = Duration.ofMinutes(3).toMillis();

    private final Set<CrawlDomain> allowedCrawlDomains;
    private final Set<CrawlRequest> crawlSeeds;
    private final CrawlStrategy crawlStrategy;
    private final boolean isDuplicateRequestFilterEnabled;
    private final boolean isOffsiteRequestFilterEnabled;
    private final int maxCrawlDepth;
    private final CrawlDelayStrategy crawlDelayStrategy;
    private final long fixedCrawlDelayDurationInMillis;
    private final long minCrawlDelayDurationInMillis;
    private final long maxCrawlDelayDurationInMillis;

    private CrawlerConfiguration(final CrawlerConfigurationBuilder builder) {
        allowedCrawlDomains = builder.allowedCrawlDomains;
        crawlSeeds = builder.crawlSeeds;
        crawlStrategy = builder.crawlStrategy;
        isDuplicateRequestFilterEnabled = builder.isDuplicateRequestFilterEnabled;
        isOffsiteRequestFilterEnabled = builder.isOffsiteRequestFilterEnabled;
        maxCrawlDepth = builder.maxCrawlDepth;
        crawlDelayStrategy = builder.crawlDelayStrategy;
        fixedCrawlDelayDurationInMillis = builder.fixedCrawlDelayDurationInMillis;
        minCrawlDelayDurationInMillis = builder.minCrawlDelayDurationInMillis;
        maxCrawlDelayDurationInMillis = builder.maxCrawlDelayDurationInMillis;
    }

    /**
     * Returns the set of allowed crawl domains.
     *
     * @return the set of allowed crawl domains
     */
    public Set<CrawlDomain> getAllowedCrawlDomains() {
        return allowedCrawlDomains;
    }

    /**
     * Returns the set of crawl seeds.
     *
     * @return the set of crawl seeds
     */
    public Set<CrawlRequest> getCrawlSeeds() {
        return crawlSeeds;
    }

    /**
     * Returns the crawl strategy of the crawler.
     *
     * @return the crawl strategy of the crawler
     */
    public CrawlStrategy getCrawlStrategy() {
        return crawlStrategy;
    }

    /**
     * Indicates if the duplicate request filter is enabled.
     *
     * @return <code>true</code> if enabled, <code>false</code> otherwise
     */
    public boolean isDuplicateRequestFilterEnabled() {
        return isDuplicateRequestFilterEnabled;
    }

    /**
     * Indicates if the offsite request filter is enabled.
     *
     * @return <code>true</code> if enabled, <code>false</code> otherwise
     */
    public boolean isOffsiteRequestFilterEnabled() {
        return isOffsiteRequestFilterEnabled;
    }

    /**
     * Returns the maximum crawl depth.
     *
     * @return the maximum crawl depth
     */
    public int getMaximumCrawlDepth() {
        return maxCrawlDepth;
    }

    /**
     * Returns the crawl delay strategy of the crawler.
     *
     * @return the crawl delay strategy of the crawler
     */
    public CrawlDelayStrategy getCrawlDelayStrategy() {
        return crawlDelayStrategy;
    }

    /**
     * Returns the exact duration of delay between each request.
     *
     * @return the duration of delay in milliseconds
     */
    public long getFixedCrawlDelayDurationInMillis() {
        return fixedCrawlDelayDurationInMillis;
    }

    /**
     * Returns the minimum duration of delay between each request.
     *
     * @return the minimum duration of delay in milliseconds
     */
    public long getMinimumCrawlDelayDurationInMillis() {
        return minCrawlDelayDurationInMillis;
    }

    /**
     * Returns the maximum duration of delay between each request.
     *
     * @return the maximum duration of delay in milliseconds
     */
    public long getMaximumCrawlDelayDurationInMillis() {
        return maxCrawlDelayDurationInMillis;
    }

    /**
     * Returns the string representation of this crawler configuration.
     *
     * @return the string representation of this crawler configuration
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("crawlSeeds", crawlSeeds)
                .append("crawlStrategy", crawlStrategy)
                .append("maximumCrawlDepth", maxCrawlDepth)
                .append("isDuplicateRequestFilterEnabled", isDuplicateRequestFilterEnabled)
                .append("isOffsiteRequestFilterEnabled", isOffsiteRequestFilterEnabled)
                .append("allowedCrawlDomains", allowedCrawlDomains)
                .append("crawlDelayStrategy", crawlDelayStrategy)
                .append("fixedCrawlDelayDurationInMillis", fixedCrawlDelayDurationInMillis)
                .append("minimumCrawlDelayDurationInMillis", minCrawlDelayDurationInMillis)
                .append("maximumCrawlDelayDurationInMillis", maxCrawlDelayDurationInMillis)
                .toString();
    }

    /**
     * Builds {@link CrawlerConfiguration} instances.
     */
    public static final class CrawlerConfigurationBuilder {

        private static final CrawlStrategy DEFAULT_CRAWL_STRATEGY = CrawlStrategy.BREADTH_FIRST;
        private static final boolean IS_DUPLICATE_REQUEST_FILTER_ENABLED_BY_DEFAULT = true;
        private static final boolean IS_OFFSITE_REQUEST_FILTER_ENABLED_BY_DEFAULT = false;
        private static final int DEFAULT_MAX_CRAWL_DEPTH = 0;
        private static final CrawlDelayStrategy DEFAULT_CRAWL_DELAY = CrawlDelayStrategy.FIXED;
        private static final long DEFAULT_FIXED_CRAWL_DELAY_IN_MILLIS
                = Duration.ZERO.toMillis();
        private static final long DEFAULT_MIN_CRAWL_DELAY_IN_MILLIS
                = Duration.ofSeconds(1).toMillis();
        private static final long DEFAULT_MAX_CRAWL_DELAY_IN_MILLIS
                = Duration.ofMinutes(1).toMillis();

        private final Set<CrawlDomain> allowedCrawlDomains;
        private final Set<CrawlRequest> crawlSeeds;

        private CrawlStrategy crawlStrategy;
        private boolean isDuplicateRequestFilterEnabled;
        private boolean isOffsiteRequestFilterEnabled;
        private int maxCrawlDepth;
        private CrawlDelayStrategy crawlDelayStrategy;
        private long fixedCrawlDelayDurationInMillis;
        private long minCrawlDelayDurationInMillis;
        private long maxCrawlDelayDurationInMillis;

        /**
         * Creates a {@link CrawlerConfigurationBuilder} instance.
         */
        public CrawlerConfigurationBuilder() {
            // Initialize with default values
            allowedCrawlDomains = new HashSet<>();
            crawlSeeds = new HashSet<>();
            crawlStrategy = DEFAULT_CRAWL_STRATEGY;
            isDuplicateRequestFilterEnabled = IS_DUPLICATE_REQUEST_FILTER_ENABLED_BY_DEFAULT;
            isOffsiteRequestFilterEnabled = IS_OFFSITE_REQUEST_FILTER_ENABLED_BY_DEFAULT;
            maxCrawlDepth = DEFAULT_MAX_CRAWL_DEPTH;
            crawlDelayStrategy = DEFAULT_CRAWL_DELAY;
            fixedCrawlDelayDurationInMillis = DEFAULT_FIXED_CRAWL_DELAY_IN_MILLIS;
            minCrawlDelayDurationInMillis = DEFAULT_MIN_CRAWL_DELAY_IN_MILLIS;
            maxCrawlDelayDurationInMillis = DEFAULT_MAX_CRAWL_DELAY_IN_MILLIS;
        }

        /**
         * Appends an internet domain to the list of allowed crawl domains.
         *
         * @param allowedCrawlDomain a well-formed internet domain name
         *
         * @return the <code>CrawlerConfigurationBuilder</code> instance
         */
        public CrawlerConfigurationBuilder addAllowedCrawlDomain(final String allowedCrawlDomain) {
            InternetDomainName domain = InternetDomainName.from(allowedCrawlDomain);

            Validate.isTrue(domain.isUnderPublicSuffix(),
                    String.format("The domain (\"%s\") is not under public suffix.",
                            allowedCrawlDomain));

            allowedCrawlDomains.add(new CrawlDomain(domain));
            return this;
        }

        /**
         * Appends a list of internet domains to the list of allowed crawl domains.
         *
         * @param allowedCrawlDomains a list of well-formed internet domain names
         *
         * @return the <code>CrawlerConfigurationBuilder</code> instance
         */
        public CrawlerConfigurationBuilder addAllowedCrawlDomains(
                final List<String> allowedCrawlDomains) {
            allowedCrawlDomains.forEach(this::addAllowedCrawlDomain);
            return this;
        }

        /**
         * Appends a crawl request to the set of crawl seeds.
         *
         * @param request the crawl request which represents a crawl seed
         *
         * @return the <code>CrawlerConfigurationBuilder</code> instance
         */
        public CrawlerConfigurationBuilder addCrawlSeed(final CrawlRequest request) {
            Validate.notNull(request, "The request parameter cannot be null.");

            crawlSeeds.add(request);
            return this;
        }

        /**
         * Appends a list of crawl requests to the set of crawl seeds.
         *
         * @param requests the list of crawl requests which represent crawl seeds
         *
         * @return the <code>CrawlerConfigurationBuilder</code> instance
         */
        public CrawlerConfigurationBuilder addCrawlSeeds(final List<CrawlRequest> requests) {
            requests.forEach(this::addCrawlSeed);
            return this;
        }

        /**
         * Sets the crawl strategy to be used by the crawler. Breadth-first strategy orders crawl
         * requests by the lowest crawl depth, whereas depth-first orders them by the highest crawl
         * depth.
         *
         * @param strategy the crawl strategy
         *
         * @return the <code>CrawlerConfigurationBuilder</code> instance
         */
        public CrawlerConfigurationBuilder setCrawlStrategy(final CrawlStrategy strategy) {
            Validate.notNull(strategy, "The strategy parameter cannot be null.");

            crawlStrategy = strategy;
            return this;
        }

        /**
         * Enables or disables the duplicate request filter.
         *
         * @param filterEnabled <code>true</code> enables, <code>false</code> disables the filter
         *
         * @return the <code>CrawlerConfigurationBuilder</code> instance
         */
        public CrawlerConfigurationBuilder setDuplicateRequestFilterEnabled(
                final boolean filterEnabled) {
            this.isDuplicateRequestFilterEnabled = filterEnabled;
            return this;
        }

        /**
         * Enables or disables the offsite request filter.
         *
         * @param filterEnabled <code>true</code> enables, <code>false</code> disables the filter
         *
         * @return the <code>CrawlerConfigurationBuilder</code> instance
         */
        public CrawlerConfigurationBuilder setOffsiteRequestFilterEnabled(
                final boolean filterEnabled) {
            this.isOffsiteRequestFilterEnabled = filterEnabled;
            return this;
        }

        /**
         * Sets the maximum crawl depth. It should be a non-negative number (0 means no limit).
         *
         * @param maxCrawlDepth the maximum crawl depth
         *
         * @return the <code>CrawlerConfigurationBuilder</code> instance
         */
        public CrawlerConfigurationBuilder setMaximumCrawlDepth(final int maxCrawlDepth) {
            Validate.isTrue(maxCrawlDepth >= 0, "The maximum crawl depth cannot be negative.");

            this.maxCrawlDepth = maxCrawlDepth;
            return this;
        }

        /**
         * Sets the crawl delay strategy to be used by the crawler. This strategy defines how the
         * delay between each request is determined.
         *
         * @param strategy the crawl delay strategy
         *
         * @return the <code>CrawlerConfigurationBuilder</code> instance
         */
        public CrawlerConfigurationBuilder setCrawlDelayStrategy(
                final CrawlDelayStrategy strategy) {
            Validate.notNull(strategy, "The strategy parameter cannot be null.");

            crawlDelayStrategy = strategy;
            return this;
        }

        /**
         * Sets the exact duration of delay between each request.
         *
         * @param fixedCrawlDelayDuration the duration of delay
         *
         * @return the <code>CrawlerConfigurationBuilder</code> instance
         */
        public CrawlerConfigurationBuilder setFixedCrawlDelayDuration(
                final Duration fixedCrawlDelayDuration) {
            Validate.notNull(fixedCrawlDelayDuration,
                    "The fixedCrawlDelayDuration parameter cannot be null.");

            fixedCrawlDelayDurationInMillis = fixedCrawlDelayDuration.toMillis();
            return this;
        }

        /**
         * Sets the minimum duration of delay between each request.
         *
         * @param minCrawlDelayDuration the minimum duration of delay
         *
         * @return the <code>CrawlerConfigurationBuilder</code> instance
         */
        public CrawlerConfigurationBuilder setMinimumCrawlDelayDuration(
                final Duration minCrawlDelayDuration) {
            Validate.notNull(minCrawlDelayDuration,
                    "The minCrawlDelayDuration parameter cannot be null.");
            Validate.isTrue(!minCrawlDelayDuration.isNegative(),
                    "The minimum crawl delay cannot be negative.");

            long minDelayDurationInMillis = minCrawlDelayDuration.toMillis();

            Validate.isTrue(minDelayDurationInMillis < maxCrawlDelayDurationInMillis,
                    "The minimum crawl delay should be less than the maximum.");

            minCrawlDelayDurationInMillis = minDelayDurationInMillis;
            return this;
        }

        /**
         * Sets the maximum duration of delay between each request.
         *
         * @param maxCrawlDelayDuration the maximum duration of delay
         *
         * @return the <code>CrawlerConfigurationBuilder</code> instance
         */
        public CrawlerConfigurationBuilder setMaximumCrawlDelayDuration(
                final Duration maxCrawlDelayDuration) {
            Validate.notNull(maxCrawlDelayDuration,
                    "The maxCrawlDelayDuration parameter cannot be null.");

            long maxDelayDurationInMillis = maxCrawlDelayDuration.toMillis();

            Validate.isTrue(maxDelayDurationInMillis > minCrawlDelayDurationInMillis,
                    "The maximum crawl delay should be higher than the minimum.");

            maxCrawlDelayDurationInMillis = maxDelayDurationInMillis;
            return this;
        }

        /**
         * Builds the configured <code>CrawlerConfiguration</code> instance.
         *
         * @return the configured <code>CrawlerConfiguration</code> instance
         */
        public CrawlerConfiguration build() {
            return new CrawlerConfiguration(this);
        }
    }
}
