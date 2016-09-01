package com.serritor;

import java.util.PriorityQueue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;

/**
 * Test cases for CrawlRequestComparator.
 * 
 * @author Krisztian Mozsi
 */
public class CrawlRequestComparatorTest {
    
    private final static String URL = "http://root-url.com";
    private PriorityQueue<CrawlRequest> crawlRequests;
    private CrawlRequestComparator comparator;
        
    @Test
    public void breadthFirstSearchTest() {
        comparator = new CrawlRequestComparator();
        crawlRequests = new PriorityQueue<>(comparator);
        priorityQueueInitialize();
        
        CrawlRequest currentRequest = crawlRequests.poll();
        assertEquals(0, currentRequest.getCrawlDepth());
        
        crawlRequests.add(new CrawlRequest(URL, 1));
        currentRequest = crawlRequests.poll();
        assertEquals(0, currentRequest.getCrawlDepth());
        
        crawlRequests.add(new CrawlRequest(URL, 2));
        crawlRequests.add(new CrawlRequest(URL, 1));
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
    public void depthFirstSearchTest() {
        comparator = new CrawlRequestComparator();
        crawlRequests = new PriorityQueue<>(comparator.reversed());
        priorityQueueInitialize();
        
        CrawlRequest currentRequest = crawlRequests.poll();
        assertEquals(0, currentRequest.getCrawlDepth());
        
        crawlRequests.add(new CrawlRequest(URL, 1));
        currentRequest = crawlRequests.poll();
        assertEquals(1, currentRequest.getCrawlDepth());
        
        currentRequest = crawlRequests.poll();
        assertEquals(0, currentRequest.getCrawlDepth());
        
        crawlRequests.add(new CrawlRequest(URL, 2));
        crawlRequests.add(new CrawlRequest(URL, 1));
        currentRequest = crawlRequests.poll();
        assertEquals(2, currentRequest.getCrawlDepth());
        
        currentRequest = crawlRequests.poll();
        assertEquals(1, currentRequest.getCrawlDepth());
        
        currentRequest = crawlRequests.poll();
        assertNull(currentRequest);
    }
    
    private void priorityQueueInitialize() {
        crawlRequests.add(new CrawlRequest(URL, 0));
        crawlRequests.add(new CrawlRequest(URL, 0));
    }
    
}
