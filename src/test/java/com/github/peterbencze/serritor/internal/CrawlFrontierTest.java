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
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test cases for {@link CrawlFrontier}.
 *
 * @author Peter Bencze
 */
public final class CrawlFrontierTest {

    // Allowed crawl domains
    private static final String ALLOWED_CRAWL_DOMAIN_0 = "root-url-0.com";
    private static final String ALLOWED_CRAWL_DOMAIN_1 = "root-url-1.com";
    private static final List<String> ALLOWED_CRAWL_DOMAINS
            = Arrays.asList(ALLOWED_CRAWL_DOMAIN_0, ALLOWED_CRAWL_DOMAIN_1);

    // Root URLs
    private static final URI ROOT_URL_0
            = URI.create("http://root-url-0.com?param1=foo&param2=bar#fragment");
    private static final URI DUPLICATE_ROOT_URL_0
            = URI.create("https://root-url-0.com?param2=bar&param1=foo");
    private static final URI ROOT_URL_1 = URI.create("http://root-url-1.com");

    // Root URL crawl depth
    private static final int ROOT_URL_CRAWL_DEPTH = 0;

    // Root URL priorities
    private static final int ROOT_URL_0_PRIORITY = 0;
    private static final int ROOT_URL_1_PRIORITY = 1;

    // Root URL crawl requests
    private static final CrawlRequest ROOT_URL_0_CRAWL_REQUEST
            = new CrawlRequestBuilder(ROOT_URL_0).setPriority(ROOT_URL_0_PRIORITY).build();
    private static final CrawlRequest DUPLICATE_ROOT_URL_0_CRAWL_REQUEST
            = new CrawlRequestBuilder(DUPLICATE_ROOT_URL_0).build();
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
    private static final int CHILD_URL_CRAWL_DEPTH = 1;

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
    private static final URI OFFSITE_URL = URI.create("http://offsite-url.com");

    // Offsite URL priority
    private static final int OFFSITE_URL_PRIORITY = 0;

    // Offsite URL crawl request
    private static final CrawlRequest OFFSITE_URL_CRAWL_REQUEST
            = new CrawlRequestBuilder(OFFSITE_URL).setPriority(OFFSITE_URL_PRIORITY).build();

    // Max crawl depth
    private static final int MAX_CRAWL_DEPTH = 1;

    private CrawlerConfiguration config;
    private CrawlFrontier frontier;

    @Before
    public void initialize() {
        config = Mockito.spy(new CrawlerConfigurationBuilder().setOffsiteRequestFiltering(true)
                .addAllowedCrawlDomains(ALLOWED_CRAWL_DOMAINS)
                .addCrawlSeeds(CRAWL_SEEDS)
                .build());

        frontier = new CrawlFrontier(config);
    }

    @Test
    public void testHasNextCandidateWithCandidatesInQueue() {
        Assert.assertTrue(frontier.hasNextCandidate());

        frontier.getNextCandidate();

        Assert.assertTrue(frontier.hasNextCandidate());

        frontier.getNextCandidate();

        Assert.assertFalse(frontier.hasNextCandidate());

        frontier.feedRequest(CHILD_URL_0_CRAWL_REQUEST, false);
        frontier.feedRequest(CHILD_URL_1_CRAWL_REQUEST, false);

        Assert.assertTrue(frontier.hasNextCandidate());

        frontier.getNextCandidate();

        Assert.assertTrue(frontier.hasNextCandidate());

        frontier.getNextCandidate();

        Assert.assertFalse(frontier.hasNextCandidate());
    }

    @Test
    public void testHasNextCandidateWithEmptyQueue() {
        Mockito.when(config.getCrawlSeeds())
                .thenReturn(Collections.EMPTY_SET);

        // Create crawl frontier without crawl seeds
        frontier = new CrawlFrontier(config);

        Assert.assertFalse(frontier.hasNextCandidate());
    }

    @Test
    public void testEnabledDuplicateRequestFiltering() {
        clearCrawlCandidateQueue();

        frontier.feedRequest(DUPLICATE_ROOT_URL_0_CRAWL_REQUEST, false);

        Assert.assertFalse(frontier.hasNextCandidate());
    }

    @Test
    public void testDisabledDuplicateRequestFiltering() {
        // Disable duplicate request filtering
        Mockito.when(config.isDuplicateRequestFilteringEnabled())
                .thenReturn(false);

        clearCrawlCandidateQueue();

        frontier.feedRequest(DUPLICATE_ROOT_URL_0_CRAWL_REQUEST, true);

        Assert.assertTrue(frontier.hasNextCandidate());
        Assert.assertEquals(DUPLICATE_ROOT_URL_0, frontier.getNextCandidate().getRequestUrl());
    }

    @Test
    public void testEnabledOffsiteRequestFiltering() {
        clearCrawlCandidateQueue();

        frontier.feedRequest(OFFSITE_URL_CRAWL_REQUEST, false);

        Assert.assertFalse(frontier.hasNextCandidate());
    }

    @Test
    public void testDisabledOffsiteRequestFiltering() {
        // Disable offsite request filtering
        Mockito.when(config.isOffsiteRequestFilteringEnabled())
                .thenReturn(false);

        clearCrawlCandidateQueue();

        frontier.feedRequest(OFFSITE_URL_CRAWL_REQUEST, false);

        Assert.assertTrue(frontier.hasNextCandidate());
        Assert.assertEquals(OFFSITE_URL.toString(),
                frontier.getNextCandidate().getRequestUrl().toString());
    }

    @Test
    public void testGetNextCandidateUsingBreadthFirstCrawlStrategy() {
        CrawlCandidate nextCandidate = frontier.getNextCandidate();

        Assert.assertEquals(ROOT_URL_1, nextCandidate.getRequestUrl());
        Assert.assertEquals(ROOT_URL_CRAWL_DEPTH, nextCandidate.getCrawlDepth());
        Assert.assertEquals(ROOT_URL_1_PRIORITY, nextCandidate.getPriority());

        frontier.feedRequest(CHILD_URL_2_CRAWL_REQUEST, false);

        nextCandidate = frontier.getNextCandidate();

        Assert.assertEquals(ROOT_URL_0, nextCandidate.getRequestUrl());
        Assert.assertEquals(ROOT_URL_CRAWL_DEPTH, nextCandidate.getCrawlDepth());
        Assert.assertEquals(ROOT_URL_0_PRIORITY, nextCandidate.getPriority());

        frontier.feedRequest(CHILD_URL_0_CRAWL_REQUEST, false);
        frontier.feedRequest(CHILD_URL_1_CRAWL_REQUEST, false);

        nextCandidate = frontier.getNextCandidate();

        Assert.assertEquals(CHILD_URL_2.toString(), nextCandidate.getRequestUrl().toString());
        Assert.assertEquals(CHILD_URL_CRAWL_DEPTH, nextCandidate.getCrawlDepth());
        Assert.assertEquals(CHILD_URL_2_PRIORITY, nextCandidate.getPriority());

        // a priority queue doesn't ensure FIFO order when elements have the same depth and priority
        nextCandidate = frontier.getNextCandidate();

        Assert.assertTrue(nextCandidate.getRequestUrl().toString().contains(CHILD_URL_PATH));
        Assert.assertEquals(CHILD_URL_CRAWL_DEPTH, nextCandidate.getCrawlDepth());

        int previousChildCandidatePriority = nextCandidate.getPriority();

        nextCandidate = frontier.getNextCandidate();

        Assert.assertTrue(nextCandidate.getRequestUrl().toString().contains(CHILD_URL_PATH));
        Assert.assertEquals(CHILD_URL_CRAWL_DEPTH, nextCandidate.getCrawlDepth());
        Assert.assertEquals(previousChildCandidatePriority, nextCandidate.getPriority());
        Assert.assertFalse(frontier.hasNextCandidate());
    }

    @Test
    public void testGetNextCandidateUsingDepthFirstCrawlStrategy() {
        Mockito.when(config.getCrawlStrategy())
                .thenReturn(CrawlStrategy.DEPTH_FIRST);

        // Create frontier with depth-first crawl strategy
        frontier = new CrawlFrontier(config);

        CrawlCandidate nextCandidate = frontier.getNextCandidate();

        Assert.assertEquals(ROOT_URL_1, nextCandidate.getRequestUrl());
        Assert.assertEquals(ROOT_URL_CRAWL_DEPTH, nextCandidate.getCrawlDepth());
        Assert.assertEquals(ROOT_URL_1_PRIORITY, nextCandidate.getPriority());

        frontier.feedRequest(CHILD_URL_2_CRAWL_REQUEST, false);

        // a priority queue doesn't ensure FIFO order when elements have the same depth and priority
        nextCandidate = frontier.getNextCandidate();

        Assert.assertTrue(nextCandidate.getRequestUrl().toString().contains(CHILD_URL_PATH));
        Assert.assertEquals(CHILD_URL_CRAWL_DEPTH, nextCandidate.getCrawlDepth());
        Assert.assertEquals(CHILD_URL_2_PRIORITY, nextCandidate.getPriority());

        nextCandidate = frontier.getNextCandidate();

        Assert.assertEquals(ROOT_URL_0, nextCandidate.getRequestUrl());
        Assert.assertEquals(ROOT_URL_CRAWL_DEPTH, nextCandidate.getCrawlDepth());
        Assert.assertEquals(ROOT_URL_0_PRIORITY, nextCandidate.getPriority());

        frontier.feedRequest(CHILD_URL_0_CRAWL_REQUEST, false);
        frontier.feedRequest(CHILD_URL_1_CRAWL_REQUEST, false);

        nextCandidate = frontier.getNextCandidate();

        Assert.assertEquals(CHILD_URL_0.toString(), nextCandidate.getRequestUrl().toString());
        Assert.assertEquals(CHILD_URL_CRAWL_DEPTH, nextCandidate.getCrawlDepth());
        Assert.assertEquals(CHILD_URL_0_PRIORITY, nextCandidate.getPriority());

        nextCandidate = frontier.getNextCandidate();

        Assert.assertEquals(CHILD_URL_1.toString(), nextCandidate.getRequestUrl().toString());
        Assert.assertEquals(CHILD_URL_CRAWL_DEPTH, nextCandidate.getCrawlDepth());
        Assert.assertEquals(CHILD_URL_1_PRIORITY, nextCandidate.getPriority());
        Assert.assertFalse(frontier.hasNextCandidate());
    }

    @Test
    public void testCrawlDepthLimitation() {
        Mockito.when(config.getMaximumCrawlDepth())
                .thenReturn(MAX_CRAWL_DEPTH);

        clearCrawlCandidateQueue();

        frontier.feedRequest(CHILD_URL_0_CRAWL_REQUEST, false);

        CrawlCandidate nextCandidate = frontier.getNextCandidate();

        Assert.assertTrue(nextCandidate.getCrawlDepth() <= MAX_CRAWL_DEPTH);

        frontier.feedRequest(CHILD_URL_1_CRAWL_REQUEST, false);

        Assert.assertFalse(frontier.hasNextCandidate());
    }

    private void clearCrawlCandidateQueue() {
        while (frontier.hasNextCandidate()) {
            frontier.getNextCandidate();
        }
    }
}
