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

package com.github.peterbencze.serritor.api.event;

import com.github.peterbencze.serritor.api.CrawlCandidate;
import com.github.peterbencze.serritor.internal.CrawlEvent;

/**
 * Event which gets delivered when a network error occurs.
 */
public final class NetworkErrorEvent extends CrawlEvent {

    private final String errorMessage;

    /**
     * Creates a {@link NetworkErrorEvent} instance.
     *
     * @param crawlCandidate the current crawl candidate
     * @param errorMessage   the network error message
     */
    public NetworkErrorEvent(final CrawlCandidate crawlCandidate, final String errorMessage) {
        super(crawlCandidate);

        this.errorMessage = errorMessage;
    }

    /**
     * Returns the network error message.
     *
     * @return the network error message
     */
    public String getErrorMessage() {
        return errorMessage;
    }
}
