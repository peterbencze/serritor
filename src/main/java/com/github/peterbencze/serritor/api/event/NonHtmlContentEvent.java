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

/**
 * Event which gets delivered when the MIME type of the response is not "text/html".
 *
 * @author Peter Bencze
 */
public final class NonHtmlContentEvent extends EventObject {

    private final String mimeType;

    /**
     * Creates a {@link NonHtmlContentEvent} instance.
     *
     * @param crawlCandidate the current crawl candidate
     * @param mimeType       the MIME type of the response
     */
    public NonHtmlContentEvent(final CrawlCandidate crawlCandidate, final String mimeType) {
        super(crawlCandidate);
        this.mimeType = mimeType;
    }

    /**
     * Returns the MIME type of the response.
     *
     * @return the MIME type of the response
     */
    public String getMimeType() {
        return mimeType;
    }
}
