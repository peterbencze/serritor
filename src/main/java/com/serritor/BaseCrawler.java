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
     * Starts the crawler.
     * 
     * @param inNewThread If the crawler should run in a background thread.
     */
    public final void start(boolean inNewThread) {
        if (crawlerThread != null)
            throw new IllegalStateException("The crawler is already started.");
        
        if (inNewThread) {
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
