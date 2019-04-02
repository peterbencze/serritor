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
import java.time.Duration;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.openqa.selenium.JavascriptExecutor;

/**
 * Test cases for {@link AdaptiveCrawlDelayMechanism}.
 */
public final class AdaptiveCrawlDelayMechanismTest {

    private static final long LOWER_DELAY_DURATION_IN_MILLIS = Duration.ZERO.toMillis();
    private static final long MINIMUM_DELAY_DURATION_IN_MILLIS = Duration.ofSeconds(1).toMillis();
    private static final long IN_RANGE_DELAY_DURATION_IN_MILLIS = Duration.ofSeconds(2).toMillis();
    private static final long MAXIMUM_DELAY_DURATION_IN_MILLIS = Duration.ofSeconds(3).toMillis();
    private static final long HIGHER_DELAY_DURATION_IN_MILLIS = Duration.ofSeconds(4).toMillis();

    private CrawlerConfiguration mockedConfig;
    private JavascriptExecutor mockedJsExecutor;
    private AdaptiveCrawlDelayMechanism crawlDelayMechanism;

    @Before
    public void initialize() {
        mockedConfig = Mockito.mock(CrawlerConfiguration.class);
        Mockito.when(mockedConfig.getMinimumCrawlDelayDurationInMillis())
                .thenReturn(MINIMUM_DELAY_DURATION_IN_MILLIS);
        Mockito.when(mockedConfig.getMaximumCrawlDelayDurationInMillis())
                .thenReturn(MAXIMUM_DELAY_DURATION_IN_MILLIS);

        mockedJsExecutor = Mockito.mock(JavascriptExecutor.class);

        // Mock browser compatibility check
        Mockito.when(mockedJsExecutor.executeScript(Mockito.anyString()))
                .thenReturn(true);

        crawlDelayMechanism = new AdaptiveCrawlDelayMechanism(mockedConfig, mockedJsExecutor);
    }

    @Test
    public void testDelayLowerThanMinimum() {
        Mockito.when(mockedJsExecutor.executeScript(Mockito.anyString()))
                .thenReturn(LOWER_DELAY_DURATION_IN_MILLIS);

        Assert.assertEquals(mockedConfig.getMinimumCrawlDelayDurationInMillis(),
                crawlDelayMechanism.getDelay());
    }

    @Test
    public void testDelayHigherThanMaximum() {
        Mockito.when(mockedJsExecutor.executeScript(Mockito.anyString()))
                .thenReturn(HIGHER_DELAY_DURATION_IN_MILLIS);

        Assert.assertEquals(mockedConfig.getMaximumCrawlDelayDurationInMillis(),
                crawlDelayMechanism.getDelay());
    }

    @Test
    public void testDelayBetweenRange() {
        Mockito.when(mockedJsExecutor.executeScript(Mockito.anyString()))
                .thenReturn(IN_RANGE_DELAY_DURATION_IN_MILLIS);

        Assert.assertEquals(IN_RANGE_DELAY_DURATION_IN_MILLIS, crawlDelayMechanism.getDelay());
    }
}
