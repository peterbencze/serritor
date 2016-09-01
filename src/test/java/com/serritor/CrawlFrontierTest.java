package com.serritor;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertTrue;

/**
 * Test cases for CrawlFrontier.
 * 
 * @author Krisztian Mozsi
 */
public class CrawlFrontierTest {
    
    private CrawlFrontier frontier;
    private final static String ROOT_URL = "http://root-url.com";
    private final static String CHILD_URL = "http://root-url.com/child1.html";
    
    @Before
    public void initialize() {
        try {
            frontier = new CrawlFrontier(Arrays.asList(new URL(ROOT_URL)), CrawlingStrategy.BREADTH_FIRST);
        } catch (MalformedURLException ex) {
            Logger.getLogger(CrawlFrontierTest.class.getName()).log(Level.OFF, "Malformed root URL", ex);
        }
    }
    
    @Test
    public void hasNextRequestTest() {
        assertTrue(frontier.hasNextRequest());
    }
    
    @Test
    public void hasNextRequestEmptyFrontierTest() {
        frontier = new CrawlFrontier(new ArrayList<>(), CrawlingStrategy.BREADTH_FIRST);
        assertFalse(frontier.hasNextRequest());
    }
    
}
