/*
 * Copyright 2017 Peter Bencze.
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

package com.github.peterbencze.serritor.api.event;

import com.github.peterbencze.serritor.api.CrawlCandidate;
import com.github.peterbencze.serritor.internal.EventObject;
import java.io.IOException;

/**
 * Event which gets delivered when a request error occurs.
 *
 * @author Peter Bencze
 */
public final class RequestErrorEvent extends EventObject {

    private final IOException exception;

    /**
     * Creates a {@link RequestErrorEvent} instance.
     *
     * @param crawlCandidate the current crawl candidate
     * @param exception      the thrown exception
     */
    public RequestErrorEvent(final CrawlCandidate crawlCandidate, final IOException exception) {
        super(crawlCandidate);

        this.exception = exception;
    }

    /**
     * Returns the thrown exception.
     *
     * @return the thrown exception
     */
    public IOException getException() {
        return exception;
    }
}
