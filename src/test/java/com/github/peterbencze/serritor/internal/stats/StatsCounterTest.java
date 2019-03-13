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
    public void testRecordPageLoad() {
        statsCounter.recordRemainingCrawlCandidate();

        int remainingCrawlCandidateCountBefore = statsCounter.getRemainingCrawlCandidateCount();
        int pageLoadCountBefore = statsCounter.getPageLoadCount();
        int processedCrawlCandidateCountBefore = statsCounter.getProcessedCrawlCandidateCount();

        statsCounter.recordPageLoad();

        Assert.assertEquals(remainingCrawlCandidateCountBefore - 1,
                statsCounter.getRemainingCrawlCandidateCount());
        Assert.assertEquals(pageLoadCountBefore + 1, statsCounter.getPageLoadCount());
        Assert.assertEquals(processedCrawlCandidateCountBefore + 1,
                statsCounter.getProcessedCrawlCandidateCount());
    }

    @Test
    public void testRecordPageLoadTimeout() {
        statsCounter.recordRemainingCrawlCandidate();

        int remainingCrawlCandidateCountBefore = statsCounter.getRemainingCrawlCandidateCount();
        int pageLoadTimeoutCountBefore = statsCounter.getPageLoadTimeoutCount();
        int processedCrawlCandidateCountBefore = statsCounter.getProcessedCrawlCandidateCount();

        statsCounter.recordPageLoad();

        Assert.assertEquals(remainingCrawlCandidateCountBefore - 1,
                statsCounter.getRemainingCrawlCandidateCount());
        Assert.assertEquals(pageLoadTimeoutCountBefore + 1, statsCounter.getPageLoadCount());
        Assert.assertEquals(processedCrawlCandidateCountBefore + 1,
                statsCounter.getProcessedCrawlCandidateCount());
    }

    @Test
    public void testRecordRequestRedirect() {
        statsCounter.recordRemainingCrawlCandidate();

        int remainingCrawlCandidateCountBefore = statsCounter.getRemainingCrawlCandidateCount();
        int requestRedirectCountBefore = statsCounter.getRequestRedirectCount();
        int processedCrawlCandidateCountBefore = statsCounter.getProcessedCrawlCandidateCount();

        statsCounter.recordPageLoad();

        Assert.assertEquals(remainingCrawlCandidateCountBefore - 1,
                statsCounter.getRemainingCrawlCandidateCount());
        Assert.assertEquals(requestRedirectCountBefore + 1, statsCounter.getPageLoadCount());
        Assert.assertEquals(processedCrawlCandidateCountBefore + 1,
                statsCounter.getProcessedCrawlCandidateCount());
    }

    @Test
    public void testRecordNonHtmlContent() {
        statsCounter.recordRemainingCrawlCandidate();

        int remainingCrawlCandidateCountBefore = statsCounter.getRemainingCrawlCandidateCount();
        int nonHtmlContentCount = statsCounter.getNonHtmlContentCount();
        int processedCrawlCandidateCountBefore = statsCounter.getProcessedCrawlCandidateCount();

        statsCounter.recordPageLoad();

        Assert.assertEquals(remainingCrawlCandidateCountBefore - 1,
                statsCounter.getRemainingCrawlCandidateCount());
        Assert.assertEquals(nonHtmlContentCount + 1, statsCounter.getPageLoadCount());
        Assert.assertEquals(processedCrawlCandidateCountBefore + 1,
                statsCounter.getProcessedCrawlCandidateCount());
    }

    @Test
    public void testRecordRequestError() {
        statsCounter.recordRemainingCrawlCandidate();

        int remainingCrawlCandidateCountBefore = statsCounter.getRemainingCrawlCandidateCount();
        int requestErrorCount = statsCounter.getRequestErrorCount();
        int processedCrawlCandidateCountBefore = statsCounter.getProcessedCrawlCandidateCount();

        statsCounter.recordPageLoad();

        Assert.assertEquals(remainingCrawlCandidateCountBefore - 1,
                statsCounter.getRemainingCrawlCandidateCount());
        Assert.assertEquals(requestErrorCount + 1, statsCounter.getPageLoadCount());
        Assert.assertEquals(processedCrawlCandidateCountBefore + 1,
                statsCounter.getProcessedCrawlCandidateCount());
    }

    @Test
    public void testRecordNetworkError() {
        statsCounter.recordRemainingCrawlCandidate();

        int remainingCrawlCandidateCountBefore = statsCounter.getRemainingCrawlCandidateCount();
        int networkErrorCount = statsCounter.getNetworkErrorCount();
        int processedCrawlCandidateCountBefore = statsCounter.getProcessedCrawlCandidateCount();

        statsCounter.recordPageLoad();

        Assert.assertEquals(remainingCrawlCandidateCountBefore - 1,
                statsCounter.getRemainingCrawlCandidateCount());
        Assert.assertEquals(networkErrorCount + 1, statsCounter.getPageLoadCount());
        Assert.assertEquals(processedCrawlCandidateCountBefore + 1,
                statsCounter.getProcessedCrawlCandidateCount());
    }
}
