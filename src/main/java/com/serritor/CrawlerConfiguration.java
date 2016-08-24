package com.serritor;

import java.util.HashMap;
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
    
    
    public CrawlerConfiguration() {
        runInBackground = false;
        crawlerDriver = CrawlerDriver.HTML_UNIT_DRIVER;
        desiredCapabilities = new HashMap<>();
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
    
    public boolean getRunInBackground() {
        return runInBackground;
    }
    
    public void setRunInBackground(boolean runInBackground) {
        this.runInBackground = runInBackground;
    }
}
