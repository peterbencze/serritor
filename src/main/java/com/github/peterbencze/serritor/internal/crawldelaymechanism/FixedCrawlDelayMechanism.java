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

/**
 * A crawl delay mechanism, in which case the delay is constant and equals to the duration specified
 * in the configuration.
 */
public final class FixedCrawlDelayMechanism implements CrawlDelayMechanism {

    private final long delayInMillis;

    /**
     * Creates a {@link FixedCrawlDelayMechanism} instance.
     *
     * @param config the crawler configuration which specifies the fixed delay duration
     */
    public FixedCrawlDelayMechanism(final CrawlerConfiguration config) {
        this.delayInMillis = config.getFixedCrawlDelayDurationInMillis();
    }

    /**
     * Returns the fixed delay specified in the configuration.
     *
     * @return the delay in milliseconds
     */
    @Override
    public long getDelay() {
        return delayInMillis;
    }
}
