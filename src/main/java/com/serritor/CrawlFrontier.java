package com.serritor;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.function.Predicate;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * Provides an interface for the crawler to manage URLs while crawling.
 * 
 * @author Peter Bencze
 * @author Krisztian Mozsi
 */
public class CrawlFrontier {
    
    private final HashSet<String> urlFingerprints;
    private final PriorityQueue<CrawlRequest> crawlRequests;
    private final Predicate<URL> urlFilter;
    
    public CrawlFrontier(List<URL> urlList, CrawlingStrategy strategy) {
        urlFingerprints = new HashSet<>();
        crawlRequests = getPriorityQueue(strategy);
        
        urlFilter = url -> {
            return !urlFingerprints.contains(getFingerprintForUrl(url));
        };
        
        urlList.stream()
            .filter(urlFilter)
            .forEach(url -> addCrawlRequest(url, 0));
    }
    
    /**
     * Method for the crawler to provide a response to a request.
     * 
     * @param response A response object that contains the extracted URLs to crawl and the crawl depth
     */
    public void addCrawlResponse(CrawlResponse response) {
        int crawlDepth = response.getCrawlDepth();
        response.getExtractedUrls().stream()
            .filter(urlFilter)
            .forEach(url -> addCrawlRequest(url, crawlDepth));
    }
    
    /**
     * Adds an URL to the crawl requests and registers its fingerprint if the URL has not already been visited.
     * 
     * @param url The URL to add to the crawl requests
     * @param crawlDepth Crawl depth of the URL
     */
    private void addCrawlRequest(URL url, int crawlDepth) {
        urlFingerprints.add(getFingerprintForUrl(url));
        crawlRequests.add(new CrawlRequest(url.toString(), crawlDepth));
    }
    
    /**
     * Creates the fingerprint of the given URL.
     * 
     * @param url The URL that the fingerprint will be created for
     * @return The fingerprint of the URL
     */
    private String getFingerprintForUrl(URL url) {  
        StringBuilder truncatedUrl = new StringBuilder(url.getHost())
                .append(url.getPath());
        
        String query = url.getQuery();
        if (query != null) {
            truncatedUrl.append("?");
            
            String[] queryParams = url.getQuery().split("&");
            List<String> queryParamList = new ArrayList(Arrays.asList(queryParams));
            Collections.sort(queryParamList);

            queryParamList.stream().forEach((param) -> {
                truncatedUrl.append(param);
            });
        }
        
        return DigestUtils.sha256Hex(truncatedUrl.toString());
    }
    
    /**
     * Creates a new priority queue using the given strategy related comparator.
     * 
     * @param strategy The URL traversal strategy
     * @return A new PriorityQueue instance for CrawlRequests using the given comparator
     */
    private PriorityQueue<CrawlRequest> getPriorityQueue(CrawlingStrategy strategy) {
        switch (strategy) {
            case BREADTH_FIRST:
                return new PriorityQueue<>(new CrawlRequestComparator());
            case DEPTH_FIRST:
                return new PriorityQueue<>(new CrawlRequestComparator().reversed());
        }
        
        throw new IllegalArgumentException("Not supported crawling strategy.");
    }
    
    /**
     * Indicates if there are any URL left to crawl.
     * 
     * @return True if there are URLs left to crawl, false otherwise
     */
    public boolean hasNextRequest() {
        return !crawlRequests.isEmpty();
    }
    
    /**
     * Gets the next URL to crawl.
     * 
     * @return The next URL to crawl
     */
    public CrawlRequest getNextRequest() {
        return crawlRequests.poll();
    }
    
}
