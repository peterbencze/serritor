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

import com.github.peterbencze.serritor.api.CompleteCrawlResponse;
import com.github.peterbencze.serritor.api.CrawlCandidate;
import com.github.peterbencze.serritor.internal.CrawlEvent;

/**
 * Event which gets delivered when the browser loads the page and the HTTP status code indicates
 * error (4xx or 5xx).
 */
public final class ResponseErrorEvent extends CrawlEvent {

    private final CompleteCrawlResponse completeCrawlResponse;

    /**
     * Creates a {@link ResponseErrorEvent} instance.
     *
     * @param crawlCandidate        the current crawl candidate
     * @param completeCrawlResponse the complete crawl response
     */
    public ResponseErrorEvent(
            final CrawlCandidate crawlCandidate,
            final CompleteCrawlResponse completeCrawlResponse) {
        super(crawlCandidate);

        this.completeCrawlResponse = completeCrawlResponse;
    }

    /**
     * Returns the complete crawl response.
     *
     * @return the complete crawl response
     */
    public CompleteCrawlResponse getCompleteCrawlResponse() {
        return completeCrawlResponse;
    }
}
