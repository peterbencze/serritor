package com.serritor;

/**
 * Provides a skeletal implementation of a crawler to minimize the effort for
 * users to implement their own.
 * 
 * @author Peter Bencze
 */
public abstract class BaseCrawler {
    
    protected final CrawlerConfiguration config = new CrawlerConfiguration();
    
    private Thread crawlerThread;
   
    /**
     * Starts the crawler.
     */
    public final void start() {
        if (crawlerThread != null)
            throw new IllegalStateException("The crawler is already started.");
        
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
        
    }
}
