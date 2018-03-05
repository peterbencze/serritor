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

/**
 * A type of crawl delay, in which case the delay is constant and equals to the
 * duration specified in the configuration.
 *
 * @author Peter Bencze
 */
public final class FixedCrawlDelay implements CrawlDelay {

    private final long delayInMillis;

    /**
     * Constructs a new <code>FixedCrawlDelay</code> instance.
     * 
     * @param config A <code>CrawlerConfiguration</code> instance which specifies the fixed delay
     */
    public FixedCrawlDelay(final CrawlerConfiguration config) {
        delayInMillis = config.getFixedCrawlDelayDurationInMillis();
    }

    /**
     * Returns the fixed delay specified in the configuration.
     * 
     * @return The delay in milliseconds
     */
    @Override
    public long getDelay() {
        return delayInMillis;
    }
}
