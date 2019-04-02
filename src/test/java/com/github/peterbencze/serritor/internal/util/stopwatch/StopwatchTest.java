/*
 * Copyright 2019 Peter Bencze.
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

package com.github.peterbencze.serritor.internal.util.stopwatch;

import java.time.Duration;
import java.time.Instant;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test cases for {@link Stopwatch}.
 */
public final class StopwatchTest {

    private TimeSource timeSourceMock;
    private Stopwatch stopwatch;

    @Before
    public void before() {
        timeSourceMock = Mockito.mock(UtcTimeSource.class);
        Mockito.doCallRealMethod().when(timeSourceMock).getTime();

        stopwatch = new Stopwatch(timeSourceMock);
    }

    @Test
    public void testStartWhenStopwatchIsNotRunning() {
        stopwatch.start();

        Assert.assertTrue(stopwatch.isRunning());
    }

    @Test(expected = IllegalStateException.class)
    public void testStartWhenStopwatchIsAlreadyRunning() {
        stopwatch.start();
        stopwatch.start();
    }

    @Test
    public void testStopWhenStopwatchIsRunning() {
        stopwatch.start();
        stopwatch.stop();

        Assert.assertFalse(stopwatch.isRunning());
    }

    @Test(expected = IllegalStateException.class)
    public void testStopWhenStopwatchIsNotRunning() {
        stopwatch.stop();
    }

    @Test
    public void testGetElapsedDurationWhenStopwatchHasNotYetBeenStarted() {
        Assert.assertTrue(stopwatch.getElapsedDuration().isZero());
    }

    @Test
    public void testGetElapsedDurationWhenStopwatchIsRunning() {
        Instant now = Instant.now();
        Instant oneMinuteLater = now.plus(Duration.ofMinutes(1));
        Instant twoMinutesLater = now.plus(Duration.ofMinutes(2));

        Mockito.when(timeSourceMock.getTime()).thenReturn(now, oneMinuteLater, twoMinutesLater);

        stopwatch.start();

        Assert.assertEquals(Duration.ofMinutes(1), stopwatch.getElapsedDuration());
        Assert.assertEquals(Duration.ofMinutes(2), stopwatch.getElapsedDuration());
    }

    @Test
    public void testGetElapsedDurationWhenStopwatchIsStopped() {
        Instant now = Instant.now();
        Instant oneMinuteLater = now.plus(Duration.ofMinutes(1));

        Mockito.when(timeSourceMock.getTime()).thenReturn(now, oneMinuteLater);

        stopwatch.start();
        stopwatch.stop();

        Assert.assertEquals(Duration.ofMinutes(1), stopwatch.getElapsedDuration());
        Assert.assertEquals(Duration.ofMinutes(1), stopwatch.getElapsedDuration());
    }
}
