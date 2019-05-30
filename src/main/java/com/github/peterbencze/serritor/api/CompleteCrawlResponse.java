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

package com.github.peterbencze.serritor.api;

import net.lightbody.bmp.core.har.HarResponse;
import org.openqa.selenium.WebDriver;

/**
 * Represents a complete crawl response that provides access to the HTTP header information and the
 * {@link WebDriver} instance to interact with the browser.
 */
public final class CompleteCrawlResponse extends PartialCrawlResponse {

    private final WebDriver webDriver;

    /**
     * Creates a {@link CompleteCrawlResponse} instance from an HAR capture.
     *
     * @param harResponse the har capture
     * @param webDriver   the <code>WebDriver</code> instance
     */
    public CompleteCrawlResponse(final HarResponse harResponse, final WebDriver webDriver) {
        super(harResponse);

        this.webDriver = webDriver;
    }

    /**
     * Returns the <code>WebDriver</code> instance to interact with the browser.
     *
     * @return the <code>WebDriver</code> instance
     */
    public WebDriver getWebDriver() {
        return webDriver;
    }
}
