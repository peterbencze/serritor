package com.serritor;

import com.google.common.net.InternetDomainName;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides an interface to configure the crawler.
  * 
 * @author Krisztian Mozsi
 * @author Peter Bencze
 */
public class CrawlerConfiguration {

    private final Map<String, Object> desiredCapabilities;
    private final List<CrawlRequest> seeds;

    private boolean debugMode;
    private boolean runInBackground;
    private CrawlerDriver crawlerDriver;
    private String driverPath;
    private CrawlingStrategy crawlingStrategy;
    private boolean filterDuplicateRequests;
    private boolean filterOffsiteRequests;

    public CrawlerConfiguration() {
        crawlerDriver = CrawlerDriver.HTML_UNIT_DRIVER;
        desiredCapabilities = new HashMap<>();    
        seeds = new ArrayList<>();
        crawlingStrategy = CrawlingStrategy.BREADTH_FIRST;
        filterDuplicateRequests = true;
    }

    public boolean getDebugMode() {
        return debugMode;
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }
    
    public boolean getRunInBackground() {
        return runInBackground;
    }
    
    public void setRunInBackground(boolean runInBackground) {
        this.runInBackground = runInBackground;
    }
    
    public CrawlerDriver getCrawlerDriver() {
        return crawlerDriver;
    }
    
    public void setCrawlerDriver(CrawlerDriver crawlerDriver) {
        this.crawlerDriver = crawlerDriver;
    }
    
    public String getDriverPath() {
        return driverPath;
    }

    public void setDriverPath(String driverPath) {
        this.driverPath = driverPath;
    }
    
    public Map<String, Object> getDesiredCapabilities() {
        return desiredCapabilities;
    }

    public void addDesiredCapability(String key, Object value) {
        desiredCapabilities.put(key, value);
    }
    
    public void addDesiredCapabilities(Map<String, Object> desiredCapabilities) {
        this.desiredCapabilities.putAll(desiredCapabilities);
    }
    
    public List<CrawlRequest> getSeeds() {
        return seeds;
    }
    
    public void addSeed(String seed) {
        try {
            URL requestUrl = new URL(seed);
            String topPrivateDomain = InternetDomainName.from(requestUrl.getHost())
                    .topPrivateDomain()
                    .toString();
            
            seeds.add(new CrawlRequest(requestUrl, topPrivateDomain));
        } catch (MalformedURLException | IllegalStateException ex) {
            throw new IllegalArgumentException(ex.getMessage());
        }
    }

    public void addSeeds(List<String> seeds) {
        seeds.stream().forEach(this::addSeed);
    }

    public CrawlingStrategy getCrawlingStrategy() {
        return crawlingStrategy;
    }

    public void setCrawlingStrategy(CrawlingStrategy crawlingStrategy) {
        this.crawlingStrategy = crawlingStrategy;
    }
    
    public boolean getFilterDuplicateRequests() {
        return filterDuplicateRequests;
    }

    public void setFilterDuplicateRequests(boolean filterDuplicateRequests) {
        this.filterDuplicateRequests = filterDuplicateRequests;
    }

    public boolean getFilterOffsiteRequests() {
        return filterOffsiteRequests;
    }

    public void setFilterOffsiteRequests(boolean filterOffsiteRequests) {
        this.filterOffsiteRequests = filterOffsiteRequests;
    }
}
