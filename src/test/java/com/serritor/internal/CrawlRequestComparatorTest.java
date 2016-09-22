package com.serritor.internal;

import com.serritor.internal.CrawlRequest;
import com.serritor.internal.CrawlRequestComparator;
import com.serritor.internal.CrawlRequest.CrawlRequestBuilder;
import java.util.PriorityQueue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;

/**
 * Test cases for CrawlRequestComparator.
 *
 * @author Krisztian Mozsi
 * @author Peter Bencze
 */
public class CrawlRequestComparatorTest {

    private static final CrawlRequest.CrawlRequestBuilder CRAWL_REQUEST_BUILDER = new CrawlRequestBuilder();

    private static final CrawlRequest CRAWL_REQUEST_WITH_0_DEPTH;
    private static final CrawlRequest CRAWL_REQUEST_WITH_1_DEPTH;
    private static final CrawlRequest CRAWL_REQUEST_WITH_2_DEPTH;

    private PriorityQueue<CrawlRequest> crawlRequests;
    private CrawlRequestComparator comparator;

    static {
        CRAWL_REQUEST_WITH_0_DEPTH = CRAWL_REQUEST_BUILDER.setCrawlDepth(0)
                .build();

        CRAWL_REQUEST_WITH_1_DEPTH = CRAWL_REQUEST_BUILDER.setCrawlDepth(1)
                .build();

        CRAWL_REQUEST_WITH_2_DEPTH = CRAWL_REQUEST_BUILDER.setCrawlDepth(2)
                .build();
    }

    @Test
    public void breadthFirstCrawlTest() {
        comparator = new CrawlRequestComparator();
        crawlRequests = new PriorityQueue<>(comparator);
        priorityQueueInitialize();

        CrawlRequest currentRequest = crawlRequests.poll();
        assertEquals(0, currentRequest.getCrawlDepth());

        crawlRequests.add(CRAWL_REQUEST_WITH_1_DEPTH);
        currentRequest = crawlRequests.poll();
        assertEquals(0, currentRequest.getCrawlDepth());

        crawlRequests.add(CRAWL_REQUEST_WITH_2_DEPTH);
        crawlRequests.add(CRAWL_REQUEST_WITH_1_DEPTH);
        currentRequest = crawlRequests.poll();
        assertEquals(1, currentRequest.getCrawlDepth());

        currentRequest = crawlRequests.poll();
        assertEquals(1, currentRequest.getCrawlDepth());

        currentRequest = crawlRequests.poll();
        assertEquals(2, currentRequest.getCrawlDepth());

        currentRequest = crawlRequests.poll();
        assertNull(currentRequest);
    }

    @Test
    public void depthFirstCrawlTest() {
        comparator = new CrawlRequestComparator();
        crawlRequests = new PriorityQueue<>(comparator.reversed());
        priorityQueueInitialize();

        CrawlRequest currentRequest = crawlRequests.poll();
        assertEquals(0, currentRequest.getCrawlDepth());

        crawlRequests.add(CRAWL_REQUEST_WITH_1_DEPTH);
        currentRequest = crawlRequests.poll();
        assertEquals(1, currentRequest.getCrawlDepth());

        currentRequest = crawlRequests.poll();
        assertEquals(0, currentRequest.getCrawlDepth());

        crawlRequests.add(CRAWL_REQUEST_WITH_2_DEPTH);
        crawlRequests.add(CRAWL_REQUEST_WITH_1_DEPTH);
        currentRequest = crawlRequests.poll();
        assertEquals(2, currentRequest.getCrawlDepth());

        currentRequest = crawlRequests.poll();
        assertEquals(1, currentRequest.getCrawlDepth());

        currentRequest = crawlRequests.poll();
        assertNull(currentRequest);
    }

    private void priorityQueueInitialize() {
        crawlRequests.add(CRAWL_REQUEST_WITH_0_DEPTH);
        crawlRequests.add(CRAWL_REQUEST_WITH_0_DEPTH);
    }
}
