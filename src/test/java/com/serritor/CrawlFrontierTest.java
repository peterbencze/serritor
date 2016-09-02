package com.serritor;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

/**
 * Test cases for CrawlFrontier.
 * 
 * @author Krisztian Mozsi
 */
public class CrawlFrontierTest {
    
    private CrawlFrontier frontier;
    private final static String ROOT_URL = "http://root-url.com";
    private final static String ROOT_URL2 = "http://root-url2.com";
    private final static String CHILD_URL = "http://root-url.com/child1.html";
    private final static String CHILD_URL2 = "http://root-url.com/child2.html";
    private final static String CHILD_URL3 = "http://root-url2.com/child3.html";
    
    @Before
    public void initialize() {
        try {
            frontier = new CrawlFrontier(Arrays.asList(new URL(ROOT_URL), new URL(ROOT_URL2)), CrawlingStrategy.BREADTH_FIRST);
        } catch (MalformedURLException ex) {
            Logger.getLogger(CrawlFrontierTest.class.getName()).log(Level.OFF, "Malformed root URL", ex);
        }
    }
    
    @Test
    public void hasNextRequestTest() {
        assertTrue(frontier.hasNextRequest());
        
        frontier.getNextRequest();
        assertTrue(frontier.hasNextRequest());
        
        frontier.getNextRequest();
        assertFalse(frontier.hasNextRequest());
        
        addExtractedUrlsToFrontier(Arrays.asList(CHILD_URL, CHILD_URL2), 1);
        assertTrue(frontier.hasNextRequest());
        
        frontier.getNextRequest();
        assertTrue(frontier.hasNextRequest());
        
        frontier.getNextRequest();
        assertFalse(frontier.hasNextRequest());
    }
    
    @Test
    public void hasNextRequestEmptyFrontierTest() {
        frontier = new CrawlFrontier(new ArrayList<>(), CrawlingStrategy.BREADTH_FIRST);
        assertFalse(frontier.hasNextRequest());
    }
    
    @Test
    public void getNextRequestTest() {
        CrawlRequest currentRequest = frontier.getNextRequest();
        assertEquals(0, currentRequest.getCrawlDepth());
        
        // add child URLs
        addExtractedUrlsToFrontier(Arrays.asList(CHILD_URL, CHILD_URL2), 1);
        
        currentRequest = frontier.getNextRequest();
        assertEquals(0, currentRequest.getCrawlDepth());
        
        currentRequest = frontier.getNextRequest();
        assertEquals(1, currentRequest.getCrawlDepth());
        
        addExtractedUrlsToFrontier(Arrays.asList(CHILD_URL3), 2);
        
        currentRequest = frontier.getNextRequest();
        assertEquals(1, currentRequest.getCrawlDepth());
        
        currentRequest = frontier.getNextRequest();
        assertEquals(2, currentRequest.getCrawlDepth());
        assertEquals(CHILD_URL3, currentRequest.getUrl());
        
        currentRequest = frontier.getNextRequest();
        assertNull(currentRequest);
    }
    
    @Test
    public void getNextRequestAddExistingTest() {
        CrawlRequest currentRequest = frontier.getNextRequest();
        assertEquals(0, currentRequest.getCrawlDepth());
        currentRequest = frontier.getNextRequest();
        assertEquals(0, currentRequest.getCrawlDepth());
        
        // add a URL again
        addExtractedUrlsToFrontier(Arrays.asList(ROOT_URL), 1);
        currentRequest = frontier.getNextRequest();
        assertNull(currentRequest);
        
        addExtractedUrlsToFrontier(Arrays.asList(CHILD_URL), 2);
        currentRequest = frontier.getNextRequest();
        assertEquals(2, currentRequest.getCrawlDepth());
        assertEquals(CHILD_URL, currentRequest.getUrl());
    }
    
    private void addExtractedUrlsToFrontier(List<String> extractedUrls, int crawlDepth) {
        // transform URL string list to URL list
        List<URL> urlList = extractedUrls.stream()
            .map(urlString -> {
                try {
                    return new URL(urlString);
                } catch (MalformedURLException ex) {
                    Logger.getLogger(CrawlFrontierTest.class.getName()).log(Level.OFF, "Malformed child URL", ex);
                    return null;
                }
            })
            .collect(Collectors.toList());

        // add as extracted URLs
        CrawlResponse response = new CrawlResponse(urlList, crawlDepth);
        frontier.addCrawlResponse(response);
    }
    
}
