package com.serritor;

import java.util.Comparator;

/**
 * Defines a comparator for CrawlRequests to decide the next URL to crawl.
 * Using this implementation, the first element in the ordering is the request with the least depth (equals to breadth-first search).
 * Reversing the comparator will result in depth-first search.
 * 
 * @author Krisztian Mozsi
 */
public class CrawlRequestComparator implements Comparator<CrawlRequest> {
    
    @Override
    public int compare(CrawlRequest request1, CrawlRequest request2)
    {
        if (request1.getCrawlDepth() < request2.getCrawlDepth())
            return 1;
        
        if (request1.getCrawlDepth() > request2.getCrawlDepth())
            return -1;
        
        return 0;
    }
    
}
