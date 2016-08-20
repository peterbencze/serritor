package com.serritor;

/**
 * Provides a skeletal implementation of a crawler to minimize the effort for
 * users to implement their own.
 * 
 * @author Peter Bencze
 */
public abstract class BaseCrawler {
    
    private Thread crawlerThread;
   
    /**
     * Starts the crawler in a new thread.
     */
    public final void start() {
        if (crawlerThread != null)
            throw new IllegalStateException("The crawler is already started.");
        
        crawlerThread = new Thread() {
            @Override
            public void run() {
                BaseCrawler.this.run();
            }
        };
        
        crawlerThread.start();
    }
    
    /**
     * Contains the workflow of the crawler.
     */
    private void run() {
        
    }
}
