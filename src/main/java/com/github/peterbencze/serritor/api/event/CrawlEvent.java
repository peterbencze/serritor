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

/**
 * Represents events occurred during the crawling.
 *
 * @author Peter Bencze
 */
public enum CrawlEvent {

    /**
     * Event which gets triggered when the browser loads the page.
     */
    PAGE_LOAD,
    /**
     * Event which gets triggered when the MIME type of the response is not "text/html".
     */
    NON_HTML_CONTENT,
    /**
     * Event which gets triggered when a page does not load in the browser within the timeout
     * period.
     */
    PAGE_LOAD_TIMEOUT,
    /**
     * Event which gets triggered when a request is redirected.
     */
    REQUEST_REDIRECT,
    /**
     * Event which gets triggered when a request error occurs.
     */
    REQUEST_ERROR
}
