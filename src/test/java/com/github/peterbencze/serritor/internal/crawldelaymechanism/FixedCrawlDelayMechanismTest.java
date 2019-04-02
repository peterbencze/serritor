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
import com.github.peterbencze.serritor.api.CrawlerConfiguration.CrawlerConfigurationBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test cases for {@link FixedCrawlDelayMechanism}.
 */
public class FixedCrawlDelayMechanismTest {

    private CrawlerConfiguration config;
    private FixedCrawlDelayMechanism crawlDelayMechanism;

    @Before
    public void initialize() {
        config = Mockito.spy(new CrawlerConfigurationBuilder().build());
        crawlDelayMechanism = new FixedCrawlDelayMechanism(config);
    }

    @Test
    public void testGetDelay() {
        Assert.assertEquals(config.getFixedCrawlDelayDurationInMillis(),
                crawlDelayMechanism.getDelay());
    }
}
