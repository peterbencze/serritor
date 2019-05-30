/*
 * Copyright 2019 Peter Bencze.
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
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.github.peterbencze.serritor.internal.stats.DurationSerializer;
import com.github.peterbencze.serritor.internal.stats.StatsCounterSnapshot;
import java.time.Duration;
import java.util.Optional;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.lang3.time.DurationFormatUtils;

/**
 * Summary statistics about the crawl progress.
 */
@JsonPropertyOrder({
        "runDuration",
        "crawlRate",
        "remainingDurationEstimate",
        "remainingCrawlCandidateCount",
        "processedCrawlCandidateCount",
        "responseSuccessCount",
        "pageLoadTimeoutCount",
        "requestRedirectCount",
        "nonHtmlResponseCount",
        "responseErrorCount",
        "networkErrorCount",
        "filteredDuplicateRequestCount",
        "filteredOffsiteRequestCount",
        "filteredCrawlDepthLimitExceedingRequestCount"
})
public final class CrawlStats {

    private final Duration runDuration;
    private final StatsCounterSnapshot statsCounterSnapshot;

    // Derived stats
    private final double crawlRate;
    private final Duration remainingDurationEstimate;

    /**
     * Creates a {@link CrawlStats} instance.
     *
     * @param runDuration          the current run duration
     * @param statsCounterSnapshot a snapshot of the stats counter values
     */
    public CrawlStats(final Duration runDuration, final StatsCounterSnapshot statsCounterSnapshot) {
        this.runDuration = runDuration;
        this.statsCounterSnapshot = statsCounterSnapshot;

        // Calculate derived stats
        crawlRate = calculateCrawlRate(runDuration, getProcessedCrawlCandidateCount());

        // Remaining duration can only be calculated when at least one crawl candidate has been
        // processed
        if (getProcessedCrawlCandidateCount() > 0) {
            remainingDurationEstimate = calculateRemainingDurationEstimate(crawlRate,
                    getRemainingCrawlCandidateCount());
        } else {
            remainingDurationEstimate = null;
        }
    }

    /**
     * Returns the current run duration.
     *
     * @return the current run duration
     */
    @JsonSerialize(using = DurationSerializer.class)
    public Duration getRunDuration() {
        return runDuration;
    }

    /**
     * Returns the number of crawl candidates processed per minute.
     *
     * @return the number of crawl candidates processed per minute
     */
    public double getCrawlRate() {
        return crawlRate;
    }

    /**
     * Returns the remaining duration estimate.
     *
     * <p>Note: At least one crawl candidate needs to be processed before it is possible to
     * calculate an estimate.
     *
     * @return the remaining duration estimate
     */
    @JsonSerialize(contentUsing = DurationSerializer.class)
    public Optional<Duration> getRemainingDurationEstimate() {
        return Optional.ofNullable(remainingDurationEstimate);
    }

    /**
     * Returns the number of remaining crawl candidates.
     *
     * @return the number of remaining crawl candidates
     */
    public int getRemainingCrawlCandidateCount() {
        return statsCounterSnapshot.getRemainingCrawlCandidateCount();
    }

    /**
     * Returns the number of processed crawl candidates.
     *
     * @return the number of processed crawl candidates
     */
    public int getProcessedCrawlCandidateCount() {
        return statsCounterSnapshot.getProcessedCrawlCandidateCount();
    }

    /**
     * Returns the number of responses received during the crawl, whose HTTP status code indicated
     * success (2xx).
     *
     * @return the number of responses received during the crawl, whose HTTP status code indicated
     *         success (2xx)
     */
    public int getResponseSuccessCount() {
        return statsCounterSnapshot.getResponseSuccessCount();
    }

    /**
     * Returns the number of page load timeouts that occurred during the crawl.
     *
     * @return the number of page load timeouts that occurred during the crawl
     */
    public int getPageLoadTimeoutCount() {
        return statsCounterSnapshot.getPageLoadTimeoutCount();
    }

    /**
     * Returns the number of request redirects that occurred during the crawl.
     *
     * @return the number of request redirects that occurred during the crawl.
     */
    public int getRequestRedirectCount() {
        return statsCounterSnapshot.getRequestRedirectCount();
    }

    /**
     * Returns the number of responses received with non-HTML content.
     *
     * @return the number of responses received with non-HTML content
     */
    public int getNonHtmlResponseCount() {
        return statsCounterSnapshot.getNonHtmlResponseCount();
    }

    /**
     * Returns the number of responses received during the crawl, whose HTTP status code indicated
     * error (4xx or 5xx).
     *
     * @return the number of responses received during the crawl, whose HTTP status code indicated
     *         error (4xx or 5xx)
     */
    public int getResponseErrorCount() {
        return statsCounterSnapshot.getResponseErrorCount();
    }

    /**
     * Returns the number of network errors that occurred during the crawl.
     *
     * @return the number of network errors that occurred during the crawl
     */
    public int getNetworkErrorCount() {
        return statsCounterSnapshot.getNetworkErrorCount();
    }

    /**
     * Returns the number of filtered duplicate requests.
     *
     * @return the number of filtered duplicate requests
     */
    public int getFilteredDuplicateRequestCount() {
        return statsCounterSnapshot.getFilteredDuplicateRequestCount();
    }

    /**
     * Returns the number of filtered offsite requests.
     *
     * @return the number of filtered offsite requests
     */
    public int getFilteredOffsiteRequestCount() {
        return statsCounterSnapshot.getFilteredOffsiteRequestCount();
    }

    /**
     * Returns the number of filtered crawl depth limit exceeding requests.
     *
     * @return the number of filtered crawl depth limit exceeding requests
     */
    public int getFilteredCrawlDepthLimitExceedingRequestCount() {
        return statsCounterSnapshot.getFilteredCrawlDepthLimitExceedingRequestCount();
    }

    /**
     * Returns a string representation of the statistics.
     *
     * @return a string representation of the statistics
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("runDuration",
                        DurationFormatUtils.formatDurationWords(runDuration.toMillis(), true, true))
                .append("crawlRate", crawlRate)
                .append("remainingDurationEstimate",
                        DurationFormatUtils.formatDurationWords(
                                remainingDurationEstimate.toMillis(), true, true))
                .append("remainingCrawlCandidateCount", getRemainingCrawlCandidateCount())
                .append("processedCrawlCandidateCount", getProcessedCrawlCandidateCount())
                .append("responseSuccessCount", getResponseSuccessCount())
                .append("pageLoadTimeoutCount", getPageLoadTimeoutCount())
                .append("requestRedirectCount", getRequestRedirectCount())
                .append("nonHtmlResponseCount", getNonHtmlResponseCount())
                .append("responseErrorCount", getResponseErrorCount())
                .append("networkErrorCount", getNetworkErrorCount())
                .append("filteredDuplicateRequestCount", getFilteredDuplicateRequestCount())
                .append("filteredOffsiteRequestCount", getFilteredOffsiteRequestCount())
                .append("filteredCrawlDepthLimitExceedingRequestCount",
                        getFilteredCrawlDepthLimitExceedingRequestCount())
                .toString();
    }

    /**
     * Calculates the number of crawl candidates processed per minute.
     *
     * @param processedCrawlCandidateCount the number of processed crawl candidates
     * @param runDuration                  the current run duration
     *
     * @return the number of crawl candidates processed per minute
     */
    private static double calculateCrawlRate(
            final Duration runDuration,
            final int processedCrawlCandidateCount) {
        long runDurationInMinutes = runDuration.toMinutes();
        if (runDurationInMinutes == 0) {
            return processedCrawlCandidateCount;
        }

        return (double) processedCrawlCandidateCount / runDurationInMinutes;
    }

    /**
     * Calculates the remaining duration estimate.
     *
     * @param remainingCrawlCandidateCount the number of remaining crawl candidates
     * @param crawlRate                    the number of crawl candidates processed per minute
     *
     * @return the remaining duration estimate
     */
    private static Duration calculateRemainingDurationEstimate(
            final double crawlRate,
            final int remainingCrawlCandidateCount) {
        Validate.finite(crawlRate, "The crawlRate parameter must be finite.");
        Validate.isTrue(crawlRate > 0, "The crawlRate parameter must be larger than 0.");

        return Duration.ofMinutes((long) Math.ceil(remainingCrawlCandidateCount / crawlRate));
    }
}
