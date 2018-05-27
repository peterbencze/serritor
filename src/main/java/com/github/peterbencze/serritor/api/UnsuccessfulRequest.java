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
import java.net.URI;

/**
 * Represents an unsuccessful request.
 *
 * @author Peter Bencze
 */
public final class UnsuccessfulRequest extends CallbackParameter {

    private final IOException exception;

    /**
     * Constructs a <code>UnsuccessfulRequest</code> instance.
     *
     * @param refererUrl The referer URL
     * @param crawlDepth The current crawl depth
     * @param crawlRequest The processed crawl request
     * @param exception The exception that was thrown while trying to fulfill
     * the request
     */
    public UnsuccessfulRequest(final URI refererUrl, final int crawlDepth, final CrawlRequest crawlRequest, final IOException exception) {
        super(refererUrl, crawlDepth, crawlRequest);

        this.exception = exception;
    }

    /**
     * Returns the exception that was thrown while trying to fulfill the
     * request.
     *
     * @return The <code>IOException</code> instance
     */
    public IOException getException() {
        return exception;
    }
}
