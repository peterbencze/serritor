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

package com.github.peterbencze.serritor.internal;

import com.github.peterbencze.serritor.api.CrawlCandidate;

/**
 * Base class from which all crawl event classes shall be derived.
 */
public abstract class CrawlEvent {

    private final CrawlCandidate crawlCandidate;

    /**
     * Base constructor of all crawl event classes.
     *
     * @param crawlCandidate the current crawl candidate
     */
    protected CrawlEvent(final CrawlCandidate crawlCandidate) {
        this.crawlCandidate = crawlCandidate;
    }

    /**
     * Returns the current crawl candidate.
     *
     * @return the current crawl candidate
     */
    public final CrawlCandidate getCrawlCandidate() {
        return crawlCandidate;
    }
}
