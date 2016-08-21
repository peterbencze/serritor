package com.serritor;

import java.util.Map;

/**
 *
 * @author Krisztian Mozsi
 */
public class CrawlerConfiguration {
    
    private Map<String, String> webDriverOptions;
    private boolean runInBackground;
    
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
