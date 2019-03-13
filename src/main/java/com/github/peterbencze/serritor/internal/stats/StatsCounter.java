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

package com.github.peterbencze.serritor.internal.stats;

import com.github.peterbencze.serritor.internal.util.FunctionalReentrantReadWriteLock;
import java.io.Serializable;
import org.apache.commons.lang3.Validate;

/**
 * Accumulates statistics during the operation of the crawler.
 */
public final class StatsCounter implements Serializable {

    private final FunctionalReentrantReadWriteLock lock;

    private int remainingCrawlCandidateCount;
    private int processedCrawlCandidateCount;
    private int pageLoadCount;
    private int pageLoadTimeoutCount;
    private int requestRedirectCount;
    private int nonHtmlContentCount;
    private int requestErrorCount;
    private int networkErrorCount;
    private int filteredDuplicateRequestCount;
    private int filteredOffsiteRequestCount;
    private int filteredCrawlDepthLimitExceedingRequestCount;

    /**
     * Creates a {@link StatsCounter} instance.
     */
    public StatsCounter() {
        lock = new FunctionalReentrantReadWriteLock();
    }

    /**
     * Returns the number of remaining crawl candidates.
     *
     * @return the number of remaining crawl candidates
     */
    public int getRemainingCrawlCandidateCount() {
        return lock.readWithLock(() -> remainingCrawlCandidateCount);
    }

    /**
     * Records an added crawl candidate. This should be called when a crawl candidate is added to
     * the crawl frontier.
     */
    public void recordRemainingCrawlCandidate() {
        lock.writeWithLock(() -> ++remainingCrawlCandidateCount);
    }

    /**
     * Returns the number of processed crawl candidates.
     *
     * @return the number of processed crawl candidates
     */
    public int getProcessedCrawlCandidateCount() {
        return lock.readWithLock(() -> processedCrawlCandidateCount);
    }

    /**
     * Returns the number of successful page loads that occurred during the crawl.
     *
     * @return the number of successful page loads that occurred during the crawl
     */
    public int getPageLoadCount() {
        return lock.readWithLock(() -> pageLoadCount);
    }

    /**
     * Records a successful page load. This should be called when the status code of the response is
     * successful.
     */
    public void recordPageLoad() {
        lock.writeWithLock(() -> {
            decrementRemainingCrawlCandidateCount();

            ++pageLoadCount;
            incrementProcessedCrawlCandidateCount();
        });
    }

    /**
     * Returns the number of page load timeouts that occurred during the crawl.
     *
     * @return the number of page load timeouts that occurred during the crawl
     */
    public int getPageLoadTimeoutCount() {
        return lock.readWithLock(() -> pageLoadTimeoutCount);
    }

    /**
     * Records a page load timeout. This should be called when a page does not load in the browser
     * within the timeout period.
     */
    public void recordPageLoadTimeout() {
        lock.writeWithLock(() -> {
            decrementRemainingCrawlCandidateCount();

            ++pageLoadTimeoutCount;
            incrementProcessedCrawlCandidateCount();
        });
    }

    /**
     * Returns the number of request redirects that occurred during the crawl.
     *
     * @return the number of request redirects that occurred during the crawl.
     */
    public int getRequestRedirectCount() {
        return lock.readWithLock(() -> requestRedirectCount);
    }

    /**
     * Records a request redirect. This should be called when a request is redirected.
     */
    public void recordRequestRedirect() {
        lock.writeWithLock(() -> {
            decrementRemainingCrawlCandidateCount();

            ++requestRedirectCount;
            incrementProcessedCrawlCandidateCount();
        });
    }

    /**
     * Returns the number of responses with non-HTML content that occurred during the crawl.
     *
     * @return the number of responses with non-HTML content that occurred during the crawl
     */
    public int getNonHtmlContentCount() {
        return lock.readWithLock(() -> nonHtmlContentCount);
    }

    /**
     * Records a response with non-HTML content. This should be called when the MIME type of a
     * response is not text/html.
     */
    public void recordNonHtmlContent() {
        lock.writeWithLock(() -> {
            decrementRemainingCrawlCandidateCount();

            ++nonHtmlContentCount;
            incrementProcessedCrawlCandidateCount();
        });
    }

    /**
     * Returns the number of request errors that occurred during the crawl.
     *
     * @return the number of request errors that occurred during the crawl
     */
    public int getRequestErrorCount() {
        return lock.readWithLock(() -> requestErrorCount);
    }

    /**
     * Records an error response. This should be called when the status code of the response is 4xx
     * or 5xx.
     */
    public void recordRequestError() {
        lock.writeWithLock(() -> {
            decrementRemainingCrawlCandidateCount();

            ++requestErrorCount;
            incrementProcessedCrawlCandidateCount();
        });
    }

    /**
     * Returns the number of network errors that occurred during the crawl.
     *
     * @return the number of network errors that occurred during the crawl
     */
    public int getNetworkErrorCount() {
        return lock.readWithLock(() -> networkErrorCount);
    }

    /**
     * Records a network error. This should be called when a network error occurs while trying to
     * fulfill a request.
     */
    public void recordNetworkError() {
        lock.writeWithLock(() -> {
            decrementRemainingCrawlCandidateCount();

            ++networkErrorCount;
            incrementProcessedCrawlCandidateCount();
        });
    }

    /**
     * Returns the number of filtered duplicate requests.
     *
     * @return the number of filtered duplicate requests
     */
    public int getFilteredDuplicateRequestCount() {
        return lock.readWithLock(() -> filteredDuplicateRequestCount);
    }

    /**
     * Records a duplicate request. This should be called when the duplicate request filter is
     * enabled and a duplicate request is encountered.
     */
    public void recordDuplicateRequest() {
        lock.writeWithLock(() -> ++filteredDuplicateRequestCount);
    }

    /**
     * Returns the number of filtered offsite requests.
     *
     * @return the number of filtered offsite requests
     */
    public int getFilteredOffsiteRequestCount() {
        return lock.readWithLock(() -> filteredOffsiteRequestCount);
    }

    /**
     * Records an offsite request. This should be called when the offsite request filter is enabled
     * and an offsite request is encountered.
     */
    public void recordOffsiteRequest() {
        lock.writeWithLock(() -> ++filteredOffsiteRequestCount);
    }

    /**
     * Returns the number of filtered crawl depth limit exceeding requests.
     *
     * @return the number of filtered crawl depth limit exceeding requests
     */
    public int getFilteredCrawlDepthLimitExceedingRequestCount() {
        return lock.readWithLock(() -> filteredCrawlDepthLimitExceedingRequestCount);
    }

    /**
     * Records a crawl depth limit exceeding request. This should be called when a crawl depth limit
     * is set and the request's crawl depth exceeds this limit.
     */
    public void recordCrawlDepthLimitExceedingRequest() {
        lock.writeWithLock(() -> ++filteredCrawlDepthLimitExceedingRequestCount);
    }

    /**
     * Returns a snapshot of this counter's values.
     *
     * @return a snapshot of this counter's values
     */
    public StatsCounterSnapshot getSnapshot() {
        return lock.readWithLock(() -> new StatsCounterSnapshot(this));
    }

    /**
     * Increments the number of processed crawl candidates.
     */
    private void incrementProcessedCrawlCandidateCount() {
        ++processedCrawlCandidateCount;
    }

    /**
     * Decrements the number of remaining crawl candidates. This number cannot be negative.
     */
    private void decrementRemainingCrawlCandidateCount() {
        Validate.validState(remainingCrawlCandidateCount > 0,
                "The number of remaining crawl candidates cannot be negative.");

        --remainingCrawlCandidateCount;
    }
}
