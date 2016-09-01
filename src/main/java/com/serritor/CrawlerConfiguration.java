package com.serritor;

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
    private final List<String> seeds;

    private boolean debugMode;
    private boolean runInBackground;
    private CrawlerDriver crawlerDriver;
    private String driverPath;
    private CrawlingStrategy crawlingStrategy;

    public CrawlerConfiguration() {
        crawlerDriver = CrawlerDriver.HTML_UNIT_DRIVER;
        desiredCapabilities = new HashMap<>();    
        seeds = new ArrayList<>();
        crawlingStrategy = CrawlingStrategy.BREADTH_FIRST;
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
    
    public List<String> getSeeds() {
        return seeds;
    }
    
    public void addSeed(String seed) {
        seeds.add(seed);
    }

    public void addSeeds(List<String> seeds) {
        this.seeds.addAll(seeds);
    }

    public CrawlingStrategy getCrawlingStrategy() {
        return crawlingStrategy;
    }

    public void setCrawlingStrategy(CrawlingStrategy crawlingStrategy) {
        this.crawlingStrategy = crawlingStrategy;
    }
}
