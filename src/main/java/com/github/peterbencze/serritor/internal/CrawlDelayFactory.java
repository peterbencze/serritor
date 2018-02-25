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

import com.github.peterbencze.serritor.api.CrawlDelayStrategy;
import org.openqa.selenium.JavascriptExecutor;

/**
 * Factory class which is used to construct the required crawl delay instance
 * specified in the configuration.
 *
 * @author Peter Bencze
 */
public final class CrawlDelayFactory {

    private final CrawlerConfiguration config;
    private final JavascriptExecutor javascriptExecutor;

    /**
     * Constructs a new <code>CrawlDelayFactory</code> instance.
     *
     * @param config A <code>CrawlerConfiguration</code> instance which
     * specifies the minimum and maximum delay.
     * @param javascriptExecutor A <code>WebDriver</code> instance which is
     * capable of executing JavaScript.
     */
    public CrawlDelayFactory(final CrawlerConfiguration config, final JavascriptExecutor javascriptExecutor) {
        this.config = config;
        this.javascriptExecutor = javascriptExecutor;
    }

    /**
     * Constructs the specific crawl delay instance determined by the strategy.
     *
     * @param crawlDelayStrategy The crawl delay strategy
     * @return The specific crawl delay instance
     */
    public CrawlDelay getInstanceOf(final CrawlDelayStrategy crawlDelayStrategy) {
        switch (crawlDelayStrategy) {
            case FIXED:
                return new FixedCrawlDelay(config);
            case RANDOM:
                return new RandomCrawlDelay(config);
            case ADAPTIVE:
                AdaptiveCrawlDelay adaptiveCrawlDelay = new AdaptiveCrawlDelay(config, javascriptExecutor);
                if (!adaptiveCrawlDelay.isBrowserCompatible()) {
                    throw new UnsupportedOperationException("The Navigation Timing API is not supported by the browser.");
                }

                return adaptiveCrawlDelay;
        }

        throw new IllegalArgumentException("Unsupported crawl delay strategy.");
    }
}
