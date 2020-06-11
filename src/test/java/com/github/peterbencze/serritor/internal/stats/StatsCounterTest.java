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

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test cases for {@link StatsCounter}.
 */
public final class StatsCounterTest {

    private StatsCounter statsCounter;

    @Before
    public void before() {
        statsCounter = new StatsCounter();
    }

    @Test
    public void testRecordResponseSuccess() {
        statsCounter.recordRemainingCrawlCandidate();

        int remainingCrawlCandidateCountBefore = statsCounter.getRemainingCrawlCandidateCount();
        int responseSuccessCountBefore = statsCounter.getResponseSuccessCount();
        int processedCrawlCandidateCountBefore = statsCounter.getProcessedCrawlCandidateCount();

        statsCounter.recordResponseSuccess();

        Assert.assertEquals(remainingCrawlCandidateCountBefore - 1,
                statsCounter.getRemainingCrawlCandidateCount());
        Assert.assertEquals(responseSuccessCountBefore + 1, statsCounter.getResponseSuccessCount());
        Assert.assertEquals(processedCrawlCandidateCountBefore + 1,
                statsCounter.getProcessedCrawlCandidateCount());
    }

    @Test
    public void testRecordPageLoadTimeout() {
        statsCounter.recordRemainingCrawlCandidate();

        int remainingCrawlCandidateCountBefore = statsCounter.getRemainingCrawlCandidateCount();
        int pageLoadTimeoutCountBefore = statsCounter.getPageLoadTimeoutCount();
        int processedCrawlCandidateCountBefore = statsCounter.getProcessedCrawlCandidateCount();

        statsCounter.recordResponseSuccess();

        Assert.assertEquals(remainingCrawlCandidateCountBefore - 1,
                statsCounter.getRemainingCrawlCandidateCount());
        Assert.assertEquals(pageLoadTimeoutCountBefore + 1, statsCounter.getResponseSuccessCount());
        Assert.assertEquals(processedCrawlCandidateCountBefore + 1,
                statsCounter.getProcessedCrawlCandidateCount());
    }

    @Test
    public void testRecordRequestRedirect() {
        statsCounter.recordRemainingCrawlCandidate();

        int remainingCrawlCandidateCountBefore = statsCounter.getRemainingCrawlCandidateCount();
        int requestRedirectCountBefore = statsCounter.getRequestRedirectCount();
        int processedCrawlCandidateCountBefore = statsCounter.getProcessedCrawlCandidateCount();

        statsCounter.recordResponseSuccess();

        Assert.assertEquals(remainingCrawlCandidateCountBefore - 1,
                statsCounter.getRemainingCrawlCandidateCount());
        Assert.assertEquals(requestRedirectCountBefore + 1, statsCounter.getResponseSuccessCount());
        Assert.assertEquals(processedCrawlCandidateCountBefore + 1,
                statsCounter.getProcessedCrawlCandidateCount());
    }

    @Test
    public void testRecordNonHtmlResponse() {
        statsCounter.recordRemainingCrawlCandidate();

        int remainingCrawlCandidateCountBefore = statsCounter.getRemainingCrawlCandidateCount();
        int nonHtmlResponseCount = statsCounter.getNonHtmlResponseCount();
        int processedCrawlCandidateCountBefore = statsCounter.getProcessedCrawlCandidateCount();

        statsCounter.recordResponseSuccess();

        Assert.assertEquals(remainingCrawlCandidateCountBefore - 1,
                statsCounter.getRemainingCrawlCandidateCount());
        Assert.assertEquals(nonHtmlResponseCount + 1, statsCounter.getResponseSuccessCount());
        Assert.assertEquals(processedCrawlCandidateCountBefore + 1,
                statsCounter.getProcessedCrawlCandidateCount());
    }

    @Test
    public void testRecordResponseError() {
        statsCounter.recordRemainingCrawlCandidate();

        int remainingCrawlCandidateCountBefore = statsCounter.getRemainingCrawlCandidateCount();
        int responseErrorCount = statsCounter.getResponseErrorCount();
        int processedCrawlCandidateCountBefore = statsCounter.getProcessedCrawlCandidateCount();

        statsCounter.recordResponseSuccess();

        Assert.assertEquals(remainingCrawlCandidateCountBefore - 1,
                statsCounter.getRemainingCrawlCandidateCount());
        Assert.assertEquals(responseErrorCount + 1, statsCounter.getResponseSuccessCount());
        Assert.assertEquals(processedCrawlCandidateCountBefore + 1,
                statsCounter.getProcessedCrawlCandidateCount());
    }

    @Test
    public void testRecordNetworkError() {
        statsCounter.recordRemainingCrawlCandidate();

        int remainingCrawlCandidateCountBefore = statsCounter.getRemainingCrawlCandidateCount();
        int networkErrorCount = statsCounter.getNetworkErrorCount();
        int processedCrawlCandidateCountBefore = statsCounter.getProcessedCrawlCandidateCount();

        statsCounter.recordResponseSuccess();

        Assert.assertEquals(remainingCrawlCandidateCountBefore - 1,
                statsCounter.getRemainingCrawlCandidateCount());
        Assert.assertEquals(networkErrorCount + 1, statsCounter.getResponseSuccessCount());
        Assert.assertEquals(processedCrawlCandidateCountBefore + 1,
                statsCounter.getProcessedCrawlCandidateCount());
    }

    @Test
    public void testReset() {
        statsCounter.recordRemainingCrawlCandidate();
        statsCounter.recordRemainingCrawlCandidate();
        statsCounter.recordRemainingCrawlCandidate();
        statsCounter.recordRemainingCrawlCandidate();
        statsCounter.recordRemainingCrawlCandidate();
        statsCounter.recordRemainingCrawlCandidate();
        statsCounter.recordRemainingCrawlCandidate();
        statsCounter.recordResponseSuccess();
        statsCounter.recordPageLoadTimeout();
        statsCounter.recordRequestRedirect();
        statsCounter.recordNonHtmlResponse();
        statsCounter.recordResponseError();
        statsCounter.recordNetworkError();
        statsCounter.recordDuplicateRequest();
        statsCounter.recordOffsiteRequest();
        statsCounter.recordCrawlDepthLimitExceedingRequest();

        statsCounter.reset();

        Assert.assertThat(statsCounter.getRemainingCrawlCandidateCount(), Matchers.is(0));
        Assert.assertThat(statsCounter.getProcessedCrawlCandidateCount(), Matchers.is(0));
        Assert.assertThat(statsCounter.getResponseSuccessCount(), Matchers.is(0));
        Assert.assertThat(statsCounter.getPageLoadTimeoutCount(), Matchers.is(0));
        Assert.assertThat(statsCounter.getRequestRedirectCount(), Matchers.is(0));
        Assert.assertThat(statsCounter.getNonHtmlResponseCount(), Matchers.is(0));
        Assert.assertThat(statsCounter.getResponseErrorCount(), Matchers.is(0));
        Assert.assertThat(statsCounter.getNetworkErrorCount(), Matchers.is(0));
        Assert.assertThat(statsCounter.getFilteredDuplicateRequestCount(), Matchers.is(0));
        Assert.assertThat(statsCounter.getFilteredOffsiteRequestCount(), Matchers.is(0));
        Assert.assertThat(statsCounter.getFilteredCrawlDepthLimitExceedingRequestCount(),
                Matchers.is(0));
    }
}
