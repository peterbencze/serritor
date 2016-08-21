package com.serritor;

import java.util.Map;

/**
 * Implements a configuration class to get and set 
 * all the necessary options for the crawler.
 * 
 * @author Krisztian Mozsi
 */
public class CrawlerConfiguration {
    
    private WebDriverType webDriverType;
    private Map<String, String> webDriverOptions;
    private boolean runInBackground;
    
    public WebDriverType getWebDriverType() {
        return webDriverType;
    }
    
    public void setWebDriverType(WebDriverType webDriverType) {
        this.webDriverType = webDriverType;
    }
    
    public Map<String, String> getWebDriverOptions() {
        return webDriverOptions;
    }
    
    public void setWebDriverOptions(Map<String, String> webDriverOptions) {
        this.webDriverOptions = webDriverOptions;
    }
    
    public boolean getRunInBackground() {
        return runInBackground;
    }
    
    public void setRunInBackground(boolean runInBackground) {
        this.runInBackground = runInBackground;
    }
    
}
