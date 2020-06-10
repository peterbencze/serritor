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
import com.github.peterbencze.serritor.api.CrawlRequest;
import com.github.peterbencze.serritor.api.CrawlRequest.CrawlRequestBuilder;
import com.github.peterbencze.serritor.api.CrawlStrategy;
import com.github.peterbencze.serritor.api.CrawlerConfiguration;
import com.github.peterbencze.serritor.api.CrawlerConfiguration.CrawlerConfigurationBuilder;
import com.github.peterbencze.serritor.internal.stats.StatsCounter;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test cases for {@link CrawlFrontier}.
 */
public final class CrawlFrontierTest {

    // Allowed crawl domains
    private static final String ROOT_URL_0_DOMAIN = "root-url-0.com";
    private static final String ROOT_URL_1_DOMAIN = "root-url-1.com";
    private static final List<String> ALLOWED_CRAWL_DOMAINS
            = Arrays.asList(ROOT_URL_0_DOMAIN, ROOT_URL_1_DOMAIN);

    // Root URLs
    private static final URI ROOT_URL_0
            = URI.create("http://root-url-0.com/?param1=foo&param2=bar#fragment");
    private static final URI DUPLICATE_ROOT_URL_0
            = URI.create("HTTP://ROOT-URL-0.COM/?param2=bar&param1=foo");
    private static final URI ROOT_URL_1 = URI.create("http://root-url-1.com/");

    // Root URL crawl depth
    private static final int ROOT_URL_CRAWL_DEPTH = 1;

    // Root URL priorities
    private static final int ROOT_URL_0_PRIORITY = 0;
    private static final int ROOT_URL_1_PRIORITY = 1;

    // Root URL crawl requests
    private static final CrawlRequest ROOT_URL_0_CRAWL_REQUEST
            = new CrawlRequestBuilder(ROOT_URL_0).setPriority(ROOT_URL_0_PRIORITY).build();
    private static final CrawlRequest DUPLICATE_ROOT_URL_0_CRAWL_REQUEST
            = CrawlRequest.createDefault(DUPLICATE_ROOT_URL_0);
    private static final CrawlRequest ROOT_URL_1_CRAWL_REQUEST
            = new CrawlRequestBuilder(ROOT_URL_1).setPriority(ROOT_URL_1_PRIORITY).build();
    private static final List<CrawlRequest> CRAWL_SEEDS
            = Arrays.asList(ROOT_URL_0_CRAWL_REQUEST, ROOT_URL_1_CRAWL_REQUEST);

    // Child URL path
    private static final String CHILD_URL_PATH = "/child";

    // Child URLs
    private static final URI CHILD_URL_0
            = URI.create(String.format("http://root-url-0.com%s-0", CHILD_URL_PATH));
    private static final URI CHILD_URL_1
            = URI.create(String.format("http://root-url-0.com%s-1", CHILD_URL_PATH));
    private static final URI CHILD_URL_2
            = URI.create(String.format("http://root-url-1.com%s-0", CHILD_URL_PATH));

    // Child URL crawl depth
    private static final int CHILD_URL_CRAWL_DEPTH = 2;

    // Child URL priorities
    private static final int CHILD_URL_0_PRIORITY = 0;
    private static final int CHILD_URL_1_PRIORITY = CHILD_URL_0_PRIORITY;
    private static final int CHILD_URL_2_PRIORITY = 1;

    // Child URL crawl requests
    private static final CrawlRequest CHILD_URL_0_CRAWL_REQUEST
            = new CrawlRequestBuilder(CHILD_URL_0).setPriority(CHILD_URL_0_PRIORITY).build();
    private static final CrawlRequest CHILD_URL_1_CRAWL_REQUEST
            = new CrawlRequestBuilder(CHILD_URL_1).setPriority(CHILD_URL_1_PRIORITY).build();
    private static final CrawlRequest CHILD_URL_2_CRAWL_REQUEST
            = new CrawlRequestBuilder(CHILD_URL_2).setPriority(CHILD_URL_2_PRIORITY).build();

    // Offsite URL
    private static final URI OFFSITE_URL = URI.create("http://offsite-url.com/");

    // Offsite URL priority
    private static final int OFFSITE_URL_PRIORITY = 0;

    // Offsite URL crawl request
    private static final CrawlRequest OFFSITE_URL_CRAWL_REQUEST
            = new CrawlRequestBuilder(OFFSITE_URL).setPriority(OFFSITE_URL_PRIORITY).build();

    private CrawlerConfiguration configMock;
    private StatsCounter statsCounterMock;
    private CrawlFrontier crawlFrontier;

    @Before
    public void before() {
        configMock = Mockito.spy(new CrawlerConfigurationBuilder()
                .setOffsiteRequestFilterEnabled(true)
                .addAllowedCrawlDomains(ALLOWED_CRAWL_DOMAINS)
                .addCrawlSeeds(CRAWL_SEEDS)
                .build());

        statsCounterMock = Mockito.mock(StatsCounter.class);

        crawlFrontier = new CrawlFrontier(configMock, statsCounterMock);
    }

    @Test
    public void testFeedRequestWhenOffsiteRequestFilterIsDisabledAndRequestIsOffsite() {
        Mockito.when(configMock.isOffsiteRequestFilterEnabled()).thenReturn(false);

        crawlFrontier.feedRequest(OFFSITE_URL_CRAWL_REQUEST, true);

        Assert.assertThat(crawlFrontier.hasNextCandidate(), Matchers.is(true));
        Assert.assertThat(crawlFrontier.getNextCandidate(), Matchers.notNullValue());
        Mockito.verify(statsCounterMock).recordRemainingCrawlCandidate();
    }

    @Test
    public void testFeedRequestWhenOffsiteRequestFilterIsEnabledAndRequestDomainIsNotAllowed() {
        crawlFrontier.feedRequest(OFFSITE_URL_CRAWL_REQUEST, true);

        Assert.assertThat(crawlFrontier.hasNextCandidate(), Matchers.is(false));
        Assert.assertThat(crawlFrontier.getNextCandidate(), Matchers.nullValue());
        Mockito.verify(statsCounterMock).recordOffsiteRequest();
    }

    @Test
    public void testFeedRequestWhenDuplicateRequestFilterIsDisabledAndRequestIsADuplicate() {
        Mockito.when(configMock.isDuplicateRequestFilterEnabled()).thenReturn(false);
        crawlFrontier.feedRequest(ROOT_URL_0_CRAWL_REQUEST, true);
        crawlFrontier.getNextCandidate();

        crawlFrontier.feedRequest(DUPLICATE_ROOT_URL_0_CRAWL_REQUEST, false);

        Assert.assertThat(crawlFrontier.hasNextCandidate(), Matchers.is(true));
        Assert.assertThat(crawlFrontier.getNextCandidate(), Matchers.notNullValue());
        Mockito.verify(statsCounterMock, Mockito.times(2)).recordRemainingCrawlCandidate();
    }

    @Test
    public void testFeedRequestWhenDuplicateRequestFilterIsEnabledAndRequestIsADuplicate() {
        crawlFrontier.feedRequest(ROOT_URL_0_CRAWL_REQUEST, true);
        crawlFrontier.getNextCandidate();

        crawlFrontier.feedRequest(DUPLICATE_ROOT_URL_0_CRAWL_REQUEST, false);

        Assert.assertThat(crawlFrontier.hasNextCandidate(), Matchers.is(false));
        Assert.assertThat(crawlFrontier.getNextCandidate(), Matchers.nullValue());
        Mockito.verify(statsCounterMock).recordDuplicateRequest();
    }

    @Test
    public void testFeedRequestWhenCrawlDepthLimitIsSetAndRequestExceedsLimit() {
        Mockito.when(configMock.getMaximumCrawlDepth()).thenReturn(1);
        crawlFrontier.feedRequest(ROOT_URL_0_CRAWL_REQUEST, true);
        crawlFrontier.getNextCandidate();

        crawlFrontier.feedRequest(CHILD_URL_0_CRAWL_REQUEST, false);

        Assert.assertThat(crawlFrontier.hasNextCandidate(), Matchers.is(false));
        Assert.assertThat(crawlFrontier.getNextCandidate(), Matchers.nullValue());
        Mockito.verify(statsCounterMock).recordCrawlDepthLimitExceedingRequest();
    }

    @Test
    public void testFeedRequestWhenRequestIsNotDuplicateAndIsNotOffsiteAndIsInCrawlDepthLimit() {
        Mockito.when(configMock.getMaximumCrawlDepth()).thenReturn(1);

        crawlFrontier.feedRequest(ROOT_URL_0_CRAWL_REQUEST, true);

        Assert.assertThat(crawlFrontier.hasNextCandidate(), Matchers.is(true));
        CrawlCandidate candidate = crawlFrontier.getNextCandidate();
        Assert.assertThat(candidate.getRequestUrl(), Matchers.is(ROOT_URL_0));
        Assert.assertThat(candidate.getCrawlDepth(), Matchers.is(ROOT_URL_CRAWL_DEPTH));
        Assert.assertThat(candidate.getPriority(), Matchers.is(ROOT_URL_0_PRIORITY));
        Assert.assertThat(candidate.getDomain().toString(), Matchers.is(ROOT_URL_0_DOMAIN));
        Assert.assertThat(candidate.getRefererUrl(), Matchers.nullValue());
        Assert.assertThat(candidate.getMetadata(), Matchers.is(Optional.empty()));
        Mockito.verify(statsCounterMock).recordRemainingCrawlCandidate();
    }

    @Test
    public void testHasNextCandidateWhenCandidateQueueIsEmpty() {
        Assert.assertThat(crawlFrontier.hasNextCandidate(), Matchers.is(false));
    }

    @Test
    public void testHashNextCandidateWhenCandidateQueueIsNotEmpty() {
        crawlFrontier.feedRequest(ROOT_URL_0_CRAWL_REQUEST, true);

        Assert.assertThat(crawlFrontier.hasNextCandidate(), Matchers.is(true));
    }

    @Test
    public void testGetNextCandidateWhenCandidateQueueIsEmpty() {
        Assert.assertThat(crawlFrontier.getNextCandidate(), Matchers.nullValue());
    }

    @Test
    public void testGetNextCandidateWhenUsingBreadthFirstCrawlStrategy() {
        crawlFrontier.feedRequest(ROOT_URL_0_CRAWL_REQUEST, true);
        crawlFrontier.feedRequest(ROOT_URL_1_CRAWL_REQUEST, true);

        CrawlCandidate nextCandidate = crawlFrontier.getNextCandidate();
        Assert.assertThat(nextCandidate.getRequestUrl(), Matchers.is(ROOT_URL_1));
        Assert.assertThat(nextCandidate.getCrawlDepth(), Matchers.is(ROOT_URL_CRAWL_DEPTH));
        Assert.assertThat(nextCandidate.getPriority(), Matchers.is(ROOT_URL_1_PRIORITY));

        crawlFrontier.feedRequest(CHILD_URL_2_CRAWL_REQUEST, false);

        nextCandidate = crawlFrontier.getNextCandidate();
        Assert.assertThat(nextCandidate.getRequestUrl(), Matchers.is(ROOT_URL_0));
        Assert.assertThat(nextCandidate.getCrawlDepth(), Matchers.is(ROOT_URL_CRAWL_DEPTH));
        Assert.assertThat(nextCandidate.getPriority(), Matchers.is(ROOT_URL_0_PRIORITY));

        crawlFrontier.feedRequest(CHILD_URL_0_CRAWL_REQUEST, false);
        crawlFrontier.feedRequest(CHILD_URL_1_CRAWL_REQUEST, false);

        nextCandidate = crawlFrontier.getNextCandidate();
        Assert.assertThat(nextCandidate.getRequestUrl(), Matchers.is(CHILD_URL_2));
        Assert.assertThat(nextCandidate.getCrawlDepth(), Matchers.is(CHILD_URL_CRAWL_DEPTH));
        Assert.assertThat(nextCandidate.getPriority(), Matchers.is(CHILD_URL_2_PRIORITY));

        // A priority queue doesn't ensure FIFO order when elements have the same depth and priority
        nextCandidate = crawlFrontier.getNextCandidate();
        int previousChildCandidatePriority = nextCandidate.getPriority();
        Assert.assertThat(nextCandidate.getRequestUrl().getPath().contains(CHILD_URL_PATH),
                Matchers.is(true));
        Assert.assertThat(nextCandidate.getCrawlDepth(), Matchers.is(CHILD_URL_CRAWL_DEPTH));

        nextCandidate = crawlFrontier.getNextCandidate();
        Assert.assertThat(nextCandidate.getRequestUrl().getPath().contains(CHILD_URL_PATH),
                Matchers.is(true));
        Assert.assertThat(nextCandidate.getCrawlDepth(), Matchers.is(CHILD_URL_CRAWL_DEPTH));
        Assert.assertThat(previousChildCandidatePriority, Matchers.is(nextCandidate.getPriority()));

        Assert.assertThat(crawlFrontier.hasNextCandidate(), Matchers.is(false));
        Assert.assertThat(crawlFrontier.getNextCandidate(), Matchers.nullValue());
    }

    @Test
    public void testGetNextCandidateWhenUsingDepthFirstCrawlStrategy() {
        Mockito.when(configMock.getCrawlStrategy()).thenReturn(CrawlStrategy.DEPTH_FIRST);
        CrawlFrontier crawlFrontier = new CrawlFrontier(configMock, statsCounterMock);
        crawlFrontier.feedRequest(ROOT_URL_0_CRAWL_REQUEST, true);
        crawlFrontier.feedRequest(ROOT_URL_1_CRAWL_REQUEST, true);

        CrawlCandidate nextCandidate = crawlFrontier.getNextCandidate();
        Assert.assertThat(nextCandidate.getRequestUrl(), Matchers.is(ROOT_URL_1));
        Assert.assertThat(nextCandidate.getCrawlDepth(), Matchers.is(ROOT_URL_CRAWL_DEPTH));
        Assert.assertThat(nextCandidate.getPriority(), Matchers.is(ROOT_URL_1_PRIORITY));

        crawlFrontier.feedRequest(CHILD_URL_2_CRAWL_REQUEST, false);

        // A priority queue doesn't ensure FIFO order when elements have the same depth and priority
        nextCandidate = crawlFrontier.getNextCandidate();
        Assert.assertThat(nextCandidate.getRequestUrl().getPath().contains(CHILD_URL_PATH),
                Matchers.is(true));
        Assert.assertThat(nextCandidate.getCrawlDepth(), Matchers.is(CHILD_URL_CRAWL_DEPTH));
        Assert.assertThat(nextCandidate.getPriority(), Matchers.is(CHILD_URL_2_PRIORITY));

        nextCandidate = crawlFrontier.getNextCandidate();
        Assert.assertThat(nextCandidate.getRequestUrl(), Matchers.is(ROOT_URL_0));
        Assert.assertThat(nextCandidate.getCrawlDepth(), Matchers.is(ROOT_URL_CRAWL_DEPTH));
        Assert.assertThat(nextCandidate.getPriority(), Matchers.is(ROOT_URL_0_PRIORITY));

        crawlFrontier.feedRequest(CHILD_URL_0_CRAWL_REQUEST, false);
        crawlFrontier.feedRequest(CHILD_URL_1_CRAWL_REQUEST, false);

        nextCandidate = crawlFrontier.getNextCandidate();
        Assert.assertThat(nextCandidate.getRequestUrl(), Matchers.is(CHILD_URL_0));
        Assert.assertThat(nextCandidate.getCrawlDepth(), Matchers.is(CHILD_URL_CRAWL_DEPTH));
        Assert.assertThat(nextCandidate.getPriority(), Matchers.is(CHILD_URL_0_PRIORITY));

        nextCandidate = crawlFrontier.getNextCandidate();
        Assert.assertThat(nextCandidate.getRequestUrl(), Matchers.is(CHILD_URL_1));
        Assert.assertThat(nextCandidate.getCrawlDepth(), Matchers.is(CHILD_URL_CRAWL_DEPTH));
        Assert.assertThat(nextCandidate.getPriority(), Matchers.is(CHILD_URL_1_PRIORITY));

        Assert.assertThat(crawlFrontier.hasNextCandidate(), Matchers.is(false));
        Assert.assertThat(crawlFrontier.getNextCandidate(), Matchers.nullValue());
    }

    @Test
    public void testResetWhenCandidateQueueIsNotEmpty() {
        crawlFrontier.feedRequest(ROOT_URL_0_CRAWL_REQUEST, true);

        crawlFrontier.reset();

        Assert.assertThat(crawlFrontier.hasNextCandidate(), Matchers.is(false));
        Assert.assertThat(crawlFrontier.getNextCandidate(), Matchers.nullValue());
    }
}
