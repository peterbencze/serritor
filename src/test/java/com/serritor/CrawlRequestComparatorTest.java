package com.serritor;

import java.util.PriorityQueue;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

/**
 * Test cases for CrawlRequestComparator.
 * 
 * @author Krisztian Mozsi
 */
public class CrawlRequestComparatorTest {
    
    private PriorityQueue<CrawlRequest> crawlRequests;
    private CrawlRequestComparator comparator;
    
    @Before
    public void initialize() {
        comparator = new CrawlRequestComparator();
        crawlRequests = new PriorityQueue<>(comparator);
    }
    
    @Test
    public void breadthFirstSearchTest() {
        assertEquals(0, 0);
    }
    
    @Test
    public void depthFirstSearchTest() {
        assertEquals(0, 0);
    }
    
}
