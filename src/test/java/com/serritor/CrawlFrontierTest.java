package com.serritor;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

/**
 * Test cases for CrawlFrontier.
 * 
 * @author Krisztian Mozsi
 * @author Peter Bencze
 */
public class CrawlFrontierTest {
    
    private final static String ROOT_URL = "http://root-url.com";
    private final static String ROOT_URL2 = "http://root-url2.com";
    
    private final static String ROOT_URL_DOMAIN = "root-url.com";
    private final static String ROOT_URL2_DOMAIN = "root-url2.com";
    
    private final static String CHILD_URL = "http://root-url.com/child1.html";
    private final static String CHILD_URL2 = "http://root-url.com/child2.html";
    private final static String CHILD_URL3 = "http://root-url2.com/child3.html";
    
    private CrawlFrontier frontier;
    
    @Before
    public void initialize() {
        try {
            frontier = new CrawlFrontier(new CrawlerConfiguration());
            
            frontier.feedRequest(new CrawlRequest(new URL(ROOT_URL), ROOT_URL_DOMAIN));
            frontier.feedRequest(new CrawlRequest(new URL(ROOT_URL2), ROOT_URL2_DOMAIN));
        } catch (MalformedURLException ex) {
            Logger.getLogger(CrawlFrontierTest.class.getName()).log(Level.OFF, null, ex);
        }
    }
    
    @Test
    public void hasNextRequestTest() {
        try {
            assertTrue(frontier.hasNextRequest());
            
            frontier.getNextRequest();
            assertTrue(frontier.hasNextRequest());
            
            frontier.getNextRequest();
            assertFalse(frontier.hasNextRequest());
            
            frontier.feedRequest(new CrawlRequest(new URL(CHILD_URL), ROOT_URL_DOMAIN, 1));
            frontier.feedRequest(new CrawlRequest(new URL(CHILD_URL2), ROOT_URL2_DOMAIN, 1));
            assertTrue(frontier.hasNextRequest());
            
            frontier.getNextRequest();
            assertTrue(frontier.hasNextRequest());
            
            frontier.getNextRequest();
            assertFalse(frontier.hasNextRequest());
        } catch (MalformedURLException ex) {
            Logger.getLogger(CrawlFrontierTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Test
    public void hasNextRequestEmptyFrontierTest() {
        frontier = new CrawlFrontier(new CrawlerConfiguration());
        assertFalse(frontier.hasNextRequest());
    }
    
    @Test
    public void getNextRequestTest() {
        try {
            CrawlRequest currentRequest = frontier.getNextRequest();
            assertEquals(0, currentRequest.getCrawlDepth());
            
            // feed child URLs
            frontier.feedRequest(new CrawlRequest(new URL(CHILD_URL), ROOT_URL_DOMAIN, 1));
            frontier.feedRequest(new CrawlRequest(new URL(CHILD_URL2), ROOT_URL2_DOMAIN, 1));
            
            currentRequest = frontier.getNextRequest();
            assertEquals(0, currentRequest.getCrawlDepth());
            
            currentRequest = frontier.getNextRequest();
            assertEquals(1, currentRequest.getCrawlDepth());
            
            frontier.feedRequest(new CrawlRequest(new URL(CHILD_URL3), ROOT_URL2_DOMAIN, 2));
            
            currentRequest = frontier.getNextRequest();
            assertEquals(1, currentRequest.getCrawlDepth());
            
            currentRequest = frontier.getNextRequest();
            assertEquals(2, currentRequest.getCrawlDepth());
            assertEquals(CHILD_URL3, currentRequest.getUrl());
            
            currentRequest = frontier.getNextRequest();
            assertNull(currentRequest);
        } catch (MalformedURLException ex) {
            Logger.getLogger(CrawlFrontierTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Test
    public void getNextRequestAddExistingTest() {
        try {
            CrawlRequest currentRequest = frontier.getNextRequest();
            assertEquals(0, currentRequest.getCrawlDepth());
            currentRequest = frontier.getNextRequest();
            assertEquals(0, currentRequest.getCrawlDepth());
            
            // add a URL again
            frontier.feedRequest(new CrawlRequest(new URL(ROOT_URL), ROOT_URL_DOMAIN, 1));
            currentRequest = frontier.getNextRequest();
            assertNull(currentRequest);
            
            frontier.feedRequest(new CrawlRequest(new URL(CHILD_URL), ROOT_URL_DOMAIN, 2));
            currentRequest = frontier.getNextRequest();
            assertEquals(2, currentRequest.getCrawlDepth());
            assertEquals(CHILD_URL, currentRequest.getUrl());
        } catch (MalformedURLException ex) {
            Logger.getLogger(CrawlFrontierTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
