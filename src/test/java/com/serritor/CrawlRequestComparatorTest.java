package com.serritor;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.PriorityQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    
    private final static String URL = "http://root-url.com";
    private final static String DOMAIN = "root-url.com";
    
    private PriorityQueue<CrawlRequest> crawlRequests;
    private CrawlRequestComparator comparator;
        
    @Test
    public void breadthFirstSearchTest() {
        try {
            comparator = new CrawlRequestComparator();
            crawlRequests = new PriorityQueue<>(comparator);
            priorityQueueInitialize();
            
            CrawlRequest currentRequest = crawlRequests.poll();
            assertEquals(0, currentRequest.getCrawlDepth());
            
            crawlRequests.add(new CrawlRequest(new URL(URL), DOMAIN, 1));
            currentRequest = crawlRequests.poll();
            assertEquals(0, currentRequest.getCrawlDepth());
            
            crawlRequests.add(new CrawlRequest(new URL(URL), DOMAIN, 2));
            crawlRequests.add(new CrawlRequest(new URL(URL), DOMAIN, 1));
            currentRequest = crawlRequests.poll();
            assertEquals(1, currentRequest.getCrawlDepth());
            
            currentRequest = crawlRequests.poll();
            assertEquals(1, currentRequest.getCrawlDepth());
            
            currentRequest = crawlRequests.poll();
            assertEquals(2, currentRequest.getCrawlDepth());
            
            currentRequest = crawlRequests.poll();
            assertNull(currentRequest);
        } catch (MalformedURLException ex) {
            Logger.getLogger(CrawlRequestComparatorTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Test
    public void depthFirstSearchTest() {
        try {
            comparator = new CrawlRequestComparator();
            crawlRequests = new PriorityQueue<>(comparator.reversed());
            priorityQueueInitialize();
            
            CrawlRequest currentRequest = crawlRequests.poll();
            assertEquals(0, currentRequest.getCrawlDepth());
            
            crawlRequests.add(new CrawlRequest(new URL(URL), DOMAIN, 1));
            currentRequest = crawlRequests.poll();
            assertEquals(1, currentRequest.getCrawlDepth());
            
            currentRequest = crawlRequests.poll();
            assertEquals(0, currentRequest.getCrawlDepth());
            
            crawlRequests.add(new CrawlRequest(new URL(URL), DOMAIN, 2));
            crawlRequests.add(new CrawlRequest(new URL(URL), DOMAIN, 1));
            currentRequest = crawlRequests.poll();
            assertEquals(2, currentRequest.getCrawlDepth());
            
            currentRequest = crawlRequests.poll();
            assertEquals(1, currentRequest.getCrawlDepth());
            
            currentRequest = crawlRequests.poll();
            assertNull(currentRequest);
        } catch (MalformedURLException ex) {
            Logger.getLogger(CrawlRequestComparatorTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void priorityQueueInitialize() {
        try {
            crawlRequests.add(new CrawlRequest(new URL(URL), DOMAIN));
            crawlRequests.add(new CrawlRequest(new URL(URL), DOMAIN));
        } catch (MalformedURLException ex) {
            Logger.getLogger(CrawlRequestComparatorTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
