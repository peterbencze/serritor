package com.serritor;


import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.NotImplementedException;

/**
 * Provides an interface for the crawler to manage URLs while crawling.
 * 
 * @author Peter Bencze
 */
public class CrawlFrontier {
    
    private final HashSet<String> urlFingerprints;
    
    public CrawlFrontier() {
        urlFingerprints = new HashSet<>();
    }
    
    public void addUrls(List<URL> urls) {
        for (URL url : urls) {
            String urlFingerprint = getFingerprintForUrl(url);
            
            if (!urlFingerprints.contains(urlFingerprint)) {
                // TODO: add to graph, check graph depth, etc.
                
                urlFingerprints.add(urlFingerprint);
            }
        }
    }
    
    /**
     * Creates the fingerprint of the given URL.
     * 
     * @param url The URL that the fingerprint will be created for
     * @return The fingerprint of the URL
     */
    public String getFingerprintForUrl(URL url) {  
        StringBuilder truncatedUrl = new StringBuilder(url.getHost())
                .append(url.getPath());
        
        String query = url.getQuery();
        if (query != null) {
            truncatedUrl.append("?");
            
            String[] queryParams = url.getQuery().split("&");
            List<String> queryParamList = new ArrayList(Arrays.asList(queryParams));
            Collections.sort(queryParamList);

            for (String param : queryParamList)
                truncatedUrl.append(param);
        }
        
        return DigestUtils.sha256Hex(truncatedUrl.toString());
    }
    
    /**
     * Indicates if there are any URL left to crawl.
     * 
     * @return True if there are URLs left to crawl, false otherwise
     */
    public boolean hasNextUrl() {
        throw new NotImplementedException("TODO");
    }
    
    /**
     * Gets the next URL to crawl.
     * 
     * @return The next URL to crawl
     */
    public String getNextUrl() {
        throw new NotImplementedException("TODO");
    }
}
