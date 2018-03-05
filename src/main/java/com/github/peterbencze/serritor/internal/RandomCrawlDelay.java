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

import java.util.concurrent.ThreadLocalRandom;

/**
 * A type of crawl delay in which case the duration is randomized between the
 * specified minimum and maximum range.
 *
 * @author Peter Bencze
 */
public final class RandomCrawlDelay implements CrawlDelay {

    private final long origin;
    private final long bound;

    /**
     * Constructs a new <code>RandomCrawlDelay</code> instance.
     *
     * @param config A <code>CrawlerConfiguration</code> instance which
     * specifies the minimum and maximum delay.
     */
    public RandomCrawlDelay(final CrawlerConfiguration config) {
        origin = config.getMinimumCrawlDelayDurationInMillis();
        bound = config.getMaximumCrawlDelayDurationInMillis() + 1;
    }

    /**
     * Returns a random delay between the minimum and maximum range specified in
     * the configuration.
     *
     * @return The delay in milliseconds
     */
    @Override
    public long getDelay() {
        return ThreadLocalRandom.current().nextLong(origin, bound);
    }
}
