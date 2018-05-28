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
package com.github.peterbencze.serritor.api;

import com.github.peterbencze.serritor.internal.CallbackParameter;
import java.io.IOException;

/**
 * Represents an unsuccessful request.
 *
 * @author Peter Bencze
 */
public final class UnsuccessfulRequest extends CallbackParameter {

    private final IOException exception;

    /**
     * Creates an {@link UnsuccessfulRequest} instance.
     *
     * @param crawlCandidate The crawled {@link CrawlCandidate} instance
     * @param exception The exception that was thrown while trying to fulfill
     * the request
     */
    public UnsuccessfulRequest(final CrawlCandidate crawlCandidate, final IOException exception) {
        super(crawlCandidate);

        this.exception = exception;
    }

    /**
     * Returns the exception which was thrown while trying to fulfill the
     * request.
     *
     * @return The thrown {@link IOException} instance
     */
    public IOException getException() {
        return exception;
    }
}
