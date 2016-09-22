package com.serritor.internal;

import com.serritor.internal.CrawlerConfiguration;
import com.serritor.internal.CrawlFrontier;
import com.serritor.internal.CrawlRequest;
import com.serritor.internal.CrawlRequest.CrawlRequestBuilder;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
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

    private static final CrawlRequestBuilder CRAWL_REQUEST_BUILDER = new CrawlRequestBuilder();

    private final static URL ROOT_URL1;
    private final static CrawlRequest ROOT_URL1_CRAWL_REQUEST;

    private final static URL ROOT_URL2;

    private final static String ROOT_URL_DOMAIN = "root-url.com";
    private final static String ROOT_URL2_DOMAIN = "root-url2.com";

    private final static URL CHILD_URL1;
    private final static CrawlRequest CHILD_URL1_CRAWL_REQUEST;

    private final static URL CHILD_URL2;
    private final static CrawlRequest CHILD_URL2_CRAWL_REQUEST;

    private final static URL CHILD_URL3;
    private final static CrawlRequest CHILD_URL3_CRAWL_REQUEST;

    private final static URL OFFSITE_URL;
    private final static CrawlRequest OFFSITE_URL_CRAWL_REQUEST;
    private final static String OFFSITE_URL_DOMAIN = "offsite-url.com";

    private CrawlerConfiguration config;
    private CrawlFrontier frontier;

    static {
        try {
            ROOT_URL1 = new URL("http://root-url.com");
            ROOT_URL2 = new URL("http://root-url2.com");

            CHILD_URL1 = new URL("http://root-url.com/child1.html");
            CHILD_URL2 = new URL("http://root-url.com/child2.html");
            CHILD_URL3 = new URL("http://root-url2.com/child3.html");

            OFFSITE_URL = new URL("http://offsite-url.com");
        } catch (MalformedURLException ex) {
            throw new Error(ex);
        }

        ROOT_URL1_CRAWL_REQUEST = CRAWL_REQUEST_BUILDER.setRefererUrl(ROOT_URL1)
                .setRequestUrl(ROOT_URL1)
                .setTopPrivateDomain(ROOT_URL_DOMAIN)
                .setCrawlDepth(1)
                .build();

        CHILD_URL1_CRAWL_REQUEST = CRAWL_REQUEST_BUILDER.setRefererUrl(ROOT_URL1)
                .setRequestUrl(CHILD_URL1)
                .setTopPrivateDomain(ROOT_URL_DOMAIN)
                .setCrawlDepth(1)
                .build();

        CHILD_URL2_CRAWL_REQUEST = CRAWL_REQUEST_BUILDER.setRefererUrl(ROOT_URL2)
                .setRequestUrl(CHILD_URL2)
                .setTopPrivateDomain(ROOT_URL2_DOMAIN)
                .build();

        CHILD_URL3_CRAWL_REQUEST = CRAWL_REQUEST_BUILDER.setRefererUrl(ROOT_URL2)
                .setRequestUrl(CHILD_URL3)
                .setTopPrivateDomain(ROOT_URL2_DOMAIN)
                .setCrawlDepth(2)
                .build();

        OFFSITE_URL_CRAWL_REQUEST = CRAWL_REQUEST_BUILDER.setRefererUrl(ROOT_URL1)
                .setRequestUrl(OFFSITE_URL)
                .setTopPrivateDomain(OFFSITE_URL_DOMAIN)
                .setCrawlDepth(1)
                .build();
    }

    @Before
    public void initialize() {
        config = new CrawlerConfiguration();
        config.addSeeds(Arrays.asList(ROOT_URL1, ROOT_URL2));
        config.setFilterOffsiteRequests(true);

        frontier = new CrawlFrontier(config);
    }

    @Test
    public void hasNextRequestTest() {
        assertTrue(frontier.hasNextRequest());

        frontier.getNextRequest();
        assertTrue(frontier.hasNextRequest());

        frontier.getNextRequest();
        assertFalse(frontier.hasNextRequest());

        frontier.feedRequest(CHILD_URL1_CRAWL_REQUEST);
        frontier.feedRequest(CHILD_URL2_CRAWL_REQUEST);
        assertTrue(frontier.hasNextRequest());

        frontier.getNextRequest();
        assertTrue(frontier.hasNextRequest());

        frontier.getNextRequest();
        assertFalse(frontier.hasNextRequest());
    }

    @Test
    public void hasNextRequestEmptyFrontierTest() {
        frontier = new CrawlFrontier(new CrawlerConfiguration());
        assertFalse(frontier.hasNextRequest());
    }

    @Test
    public void getNextRequestTest() {
        CrawlRequest currentRequest = frontier.getNextRequest();
        assertEquals(0, currentRequest.getCrawlDepth());

        // feed child URLs
        frontier.feedRequest(CHILD_URL1_CRAWL_REQUEST);
        frontier.feedRequest(CHILD_URL2_CRAWL_REQUEST);

        currentRequest = frontier.getNextRequest();
        assertEquals(0, currentRequest.getCrawlDepth());

        currentRequest = frontier.getNextRequest();
        assertEquals(1, currentRequest.getCrawlDepth());

        frontier.feedRequest(CHILD_URL3_CRAWL_REQUEST);

        currentRequest = frontier.getNextRequest();
        assertEquals(1, currentRequest.getCrawlDepth());

        currentRequest = frontier.getNextRequest();
        assertEquals(CHILD_URL3.toString(), currentRequest.getRequestUrl().toString());
        assertEquals(2, currentRequest.getCrawlDepth());
        assertEquals(ROOT_URL2_DOMAIN, currentRequest.getTopPrivateDomain());

        currentRequest = frontier.getNextRequest();
        assertNull(currentRequest);
    }

    @Test
    public void getNextRequestWithDuplicateRequestFilterTest() {
        CrawlRequest currentRequest = frontier.getNextRequest();
        assertEquals(0, currentRequest.getCrawlDepth());
        currentRequest = frontier.getNextRequest();
        assertEquals(0, currentRequest.getCrawlDepth());

        // feed a duplicate request
        frontier.feedRequest(ROOT_URL1_CRAWL_REQUEST);
        currentRequest = frontier.getNextRequest();
        assertNull(currentRequest);
    }

    @Test
    public void getNextRequestWithOffsiteRequestFilterTest() {
        CrawlRequest currentRequest = frontier.getNextRequest();
        assertEquals(0, currentRequest.getCrawlDepth());
        currentRequest = frontier.getNextRequest();
        assertEquals(0, currentRequest.getCrawlDepth());

        // feed an offsite request
        frontier.feedRequest(OFFSITE_URL_CRAWL_REQUEST);
        currentRequest = frontier.getNextRequest();
        assertNull(currentRequest);
    }

    @Test
    public void getNextRequestWithoutDuplicateRequestFilterTest() {
        config.setFilterDuplicateRequests(false);

        CrawlRequest currentRequest = frontier.getNextRequest();
        assertEquals(0, currentRequest.getCrawlDepth());
        currentRequest = frontier.getNextRequest();
        assertEquals(0, currentRequest.getCrawlDepth());

        // feed a duplicate request
        frontier.feedRequest(ROOT_URL1_CRAWL_REQUEST);
        currentRequest = frontier.getNextRequest();
        assertEquals(ROOT_URL1.toString(), currentRequest.getRequestUrl().toString());
        assertEquals(ROOT_URL_DOMAIN, currentRequest.getTopPrivateDomain());
        assertEquals(1, currentRequest.getCrawlDepth());
    }

    @Test
    public void getNextRequestWithoutOffsiteRequestFilterTest() {
        config.setFilterOffsiteRequests(false);

        CrawlRequest currentRequest = frontier.getNextRequest();
        assertEquals(0, currentRequest.getCrawlDepth());
        currentRequest = frontier.getNextRequest();
        assertEquals(0, currentRequest.getCrawlDepth());

        // feed an offsite request
        frontier.feedRequest(OFFSITE_URL_CRAWL_REQUEST);
        currentRequest = frontier.getNextRequest();
        assertEquals(OFFSITE_URL.toString(), currentRequest.getRequestUrl().toString());
        assertEquals(OFFSITE_URL_DOMAIN, currentRequest.getTopPrivateDomain());
        assertEquals(1, currentRequest.getCrawlDepth());
    }
}
