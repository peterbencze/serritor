package com.serritor;

import org.openqa.selenium.WebDriver;

/**
 * Provides a skeletal implementation of a crawler to minimize the effort for
 * users to implement their own.
 * 
 * @author Peter Bencze
 */
public abstract class BaseCrawler {
    
    protected final CrawlerConfiguration config;
    
    private Thread crawlerThread;
    private WebDriver driver;
    
    public BaseCrawler() {
        // Create default configuration
        config = new CrawlerConfiguration();
    }
   
    /**
     * Starts the crawler.
     */
    public final void start() {
        if (crawlerThread != null)
            throw new IllegalStateException("The crawler is already started.");
        
        driver = WebDriverFactory.getDriver(config);
        
        if (config.getRunInBackground()) {
            crawlerThread = new Thread() {
                @Override
                public void run() {
                    BaseCrawler.this.run();
                }
            };

            crawlerThread.start();
        } else {
            run();
        }
    }
    
    /**
     * Contains the workflow of the crawler.
     */
    private void run() {
        driver.quit();
    }
}
