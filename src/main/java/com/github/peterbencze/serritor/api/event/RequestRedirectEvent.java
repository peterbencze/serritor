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
package com.github.peterbencze.serritor.api.event;

import com.github.peterbencze.serritor.api.CrawlCandidate;
import com.github.peterbencze.serritor.api.CrawlRequest;
import com.github.peterbencze.serritor.internal.EventObject;

/**
 * Event which gets delivered when a request is redirected.
 *
 * @author Peter Bencze
 */
public final class RequestRedirectEvent extends EventObject {

    private final CrawlRequest redirectedCrawlRequest;

    /**
     * Creates a {@link RequestRedirectEvent} instance.
     *
     * @param crawlCandidate the current crawl candidate
     * @param redirectedCrawlRequest the crawl request for the redirected URL
     */
    public RequestRedirectEvent(final CrawlCandidate crawlCandidate, final CrawlRequest redirectedCrawlRequest) {
        super(crawlCandidate);

        this.redirectedCrawlRequest = redirectedCrawlRequest;
    }

    /**
     * Returns the crawl request for the redirected URL.
     *
     * @return the crawl request for the redirected URL
     */
    public CrawlRequest getRedirectedCrawlRequest() {
        return redirectedCrawlRequest;
    }
}
