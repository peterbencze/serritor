/* 
 * Copyright 2018 Peter Bencze.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.peterbencze.serritor.internal;

import org.openqa.selenium.JavascriptExecutor;

/**
 * A type of crawl delay, in which case the delay corresponds to the page
 * loading time, if it's between the specified range, otherwise the minimum or
 * maximum duration is used.
 *
 * @author Peter Bencze
 */
public final class AdaptiveCrawlDelay implements CrawlDelay {

    private final long minDelayInMillis;
    private final long maxDelayInMillis;
    private final JavascriptExecutor javascriptExecutor;

    /**
     * Constructs a new <code>AdaptiveCrawlDelay</code> instance.
     *
     * @param config A <code>CrawlerConfiguration</code> instance which
     * specifies the minimum and maximum delay.
     * @param javascriptExecutor A <code>WebDriver</code> instance which is
     * capable of executing JavaScript.
     */
    public AdaptiveCrawlDelay(final CrawlerConfiguration config, final JavascriptExecutor javascriptExecutor) {
        minDelayInMillis = config.getMinimumCrawlDelayDurationInMillis();
        maxDelayInMillis = config.getMaximumCrawlDelayDurationInMillis();
        this.javascriptExecutor = javascriptExecutor;
    }

    /**
     * Checks if the browser supports the Navigation Timing API.
     *
     * @return <code>true</code> if the browser is compatible,
     * <code>false</code> otherwise
     */
    public boolean isBrowserCompatible() {
        return (boolean) javascriptExecutor.executeScript("return ('performance' in window) && ('timing' in window.performance)");
    }

    /**
     * Calculates the page loading time and returns the delay accordingly,
     * between the specified min-max range. If the calculated delay is smaller
     * than the minimum, it returns the minimum delay. If the calculated delay
     * is higher than the maximum, it returns the maximum delay.
     *
     * @return The delay in milliseconds
     */
    @Override
    public long getDelay() {
        long delayInMillis = (long) javascriptExecutor.executeScript("return performance.timing.loadEventEnd - performance.timing.navigationStart;");

        if (delayInMillis < minDelayInMillis) {
            return minDelayInMillis;
        } else if (delayInMillis > maxDelayInMillis) {
            return maxDelayInMillis;
        }

        return delayInMillis;
    }
}
