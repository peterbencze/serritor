package com.serritor;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * Provides an interface for the crawler to manage requests while crawling.
  * 
 * @author Peter Bencze
 * @author Krisztian Mozsi
 */
public class CrawlFrontier {

    private final CrawlerConfiguration config;

    private final HashSet<String> topPrivateDomains;
    private final HashSet<String> urlFingerprints;
    private final PriorityQueue<CrawlRequest> requests;

    private final Predicate<CrawlRequest> duplicateRequestFilter;
    private final Predicate<CrawlRequest> offsiteRequestFilter;

    public CrawlFrontier(CrawlerConfiguration config, List<CrawlRequest> seeds) {
        this.config = config;

        urlFingerprints = new HashSet<>();
        requests = getPriorityQueue(config.getCrawlingStrategy());
        
        topPrivateDomains = new HashSet<>();
        
        duplicateRequestFilter = request -> {
            return !urlFingerprints.contains(getFingerprintForUrl(request.getUrl()));
        };

        offsiteRequestFilter = request -> {
            return topPrivateDomains.contains(request.getTopPrivateDomain());
        };
        
        seeds.stream()
            .filter(duplicateRequestFilter)
            .forEach(request -> {
                topPrivateDomains.add(request.getTopPrivateDomain());
                addRequest(request);
            });
    }
    
    /**
     * Method for the crawler to add requests to the frontier.
     * 
     * @param requests A list of requests
     */
    public void addRequests(List<CrawlRequest> requests) {
        List<CrawlRequest> filteredRequests = requests.stream()
            .filter(duplicateRequestFilter)
            .collect(Collectors.toList());

        if (!config.getAllowOffsiteRequests()) {
            filteredRequests = filteredRequests.stream()
                .filter(offsiteRequestFilter)
                .collect(Collectors.toList());
        }

        filteredRequests.forEach(request -> addRequest(request));
    }
    
    /**
     * Adds a request to the queue and stores its fingerprint.
      * 
     * @param request The request to be added to the queue
     */
    private void addRequest(CrawlRequest request) {
        urlFingerprints.add(getFingerprintForUrl(request.getUrl()));
        requests.add(request);
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
     * Indicates if there are any requests left in the queue.
      * 
     * @return True if there are requests in the queue, false otherwise
     */
    public boolean hasNextRequest() {
        return !requests.isEmpty();
    }
    
    /**
     * Gets the next request from the queue.
      * 
     * @return The next request
     */
    public CrawlRequest getNextRequest() {
        return requests.poll();
    }
    
}
