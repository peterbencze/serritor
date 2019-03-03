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

package com.github.peterbencze.serritor.internal.stopwatch;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import org.apache.commons.lang3.Validate;

/**
 * A serializable stopwatch implementation that can be used to measure elapsed time.
 *
 * @author Peter Bencze
 */
public final class Stopwatch implements Serializable {

    private TimeSource timeSource;
    private Instant startTime;
    private Duration elapsedDuration;
    private boolean isRunning;

    /**
     * Creates a {@link Stopwatch} instance.
     *
     * @param timeSource a source providing access to the current instant
     */
    public Stopwatch(final TimeSource timeSource) {
        this.timeSource = timeSource;
        elapsedDuration = Duration.ZERO;
        isRunning = false;
    }

    /**
     * Creates a {@link Stopwatch} instance.
     */
    public Stopwatch() {
        this(new UtcTimeSource());
    }

    /**
     * Starts the stopwatch.
     */
    public void start() {
        Validate.validState(!isRunning, "The stopwatch is already running.");

        startTime = timeSource.getTime();
        isRunning = true;
    }

    /**
     * Indicates if the stopwatch is running.
     *
     * @return <code>true</code> if the stopwatch is running, <code>false</code> otherwise
     */
    public boolean isRunning() {
        return isRunning;
    }

    /**
     * Stops the stopwatch.
     */
    public void stop() {
        Validate.validState(isRunning, "The stopwatch is not running.");

        elapsedDuration = elapsedDuration.plus(Duration.between(startTime, timeSource.getTime()));
        isRunning = false;
    }

    /**
     * Returns the current elapsed duration.
     *
     * @return the current elapsed duration
     */
    public Duration getElapsedDuration() {
        if (isRunning) {
            return Duration.between(startTime, timeSource.getTime()).plus(elapsedDuration);
        }

        return elapsedDuration;
    }
}
