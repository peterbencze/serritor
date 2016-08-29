package com.serritor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides an interface to specify properties to be used by the crawler.
 * 
 * @author Krisztian Mozsi
 * @author Peter Bencze
 */
public class CrawlerConfiguration {
    
    private boolean runInBackground;
    
    private CrawlerDriver crawlerDriver;
    private String driverPath;
    private Map<String, Object> desiredCapabilities;
    
    private List<String> seeds;

    
    public CrawlerConfiguration() {
        runInBackground = false;
        
        crawlerDriver = CrawlerDriver.HTML_UNIT_DRIVER;
        desiredCapabilities = new HashMap<>();
        
        seeds = new ArrayList<>();
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
    
    public void setDesiredCapabilities(Map<String, Object> desiredCapabilities) {
        this.desiredCapabilities = desiredCapabilities;
    }
    
    public List<String> getSeeds() {
        return seeds;
    }
    
    public void setSeed(String seed) {
        seeds.add(seed);
    }

    public void setSeeds(List<String> seeds) {
        this.seeds = seeds;
    }
}
