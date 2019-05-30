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

package com.github.peterbencze.serritor.internal.crawldelaymechanism;

import com.github.peterbencze.serritor.api.CrawlerConfiguration;
import org.apache.commons.lang3.Validate;
import org.openqa.selenium.JavascriptExecutor;

/**
 * A crawl delay mechanism, in which case the delay corresponds to the page loading time, if it is
 * between the specified range, otherwise the minimum or maximum duration is used.
 */
public final class AdaptiveCrawlDelayMechanism implements CrawlDelayMechanism {

    private static final String BROWSER_COMPATIBILITY_JS = "return ('performance' in window) && "
            + "('timing' in window.performance)";
    private static final String DELAY_CALCULATION_JS = "return performance.timing.loadEventEnd - "
            + "performance.timing.navigationStart;";

    private final long minDelayInMillis;
    private final long maxDelayInMillis;
    private final JavascriptExecutor jsExecutor;

    /**
     * Creates an {@link AdaptiveCrawlDelayMechanism} instance.
     *
     * @param config     the crawler configuration which specifies the minimum and maximum delay
     * @param jsExecutor the {@link org.openqa.selenium.WebDriver} instance which is capable of
     *                   executing JavaScript
     */
    public AdaptiveCrawlDelayMechanism(
            final CrawlerConfiguration config,
            final JavascriptExecutor jsExecutor) {
        Validate.isTrue(isBrowserCompatible(jsExecutor), "The Navigation Timing API is not "
                + "supported by the browser.");

        minDelayInMillis = config.getMinimumCrawlDelayDurationInMillis();
        maxDelayInMillis = config.getMaximumCrawlDelayDurationInMillis();
        this.jsExecutor = jsExecutor;
    }

    /**
     * Calculates the page loading time and returns the delay accordingly, between the specified
     * min-max range. If the calculated delay is smaller than the minimum, it returns the minimum
     * delay. If the calculated delay is higher than the maximum, it returns the maximum delay.
     *
     * @return the delay in milliseconds
     */
    @Override
    public long getDelay() {
        long delayInMillis = (long) jsExecutor.executeScript(DELAY_CALCULATION_JS);

        if (delayInMillis < minDelayInMillis) {
            return minDelayInMillis;
        } else if (delayInMillis > maxDelayInMillis) {
            return maxDelayInMillis;
        }

        return delayInMillis;
    }

    /**
     * Checks if the browser supports the Navigation Timing API.
     *
     * @param jsExecutor the {@link org.openqa.selenium.WebDriver} instance which is capable of
     *                   executing JavaScript
     *
     * @return <code>true</code> if the browser is compatible, <code>false</code> otherwise
     */
    private static boolean isBrowserCompatible(final JavascriptExecutor jsExecutor) {
        return (boolean) jsExecutor.executeScript(BROWSER_COMPATIBILITY_JS);
    }
}
