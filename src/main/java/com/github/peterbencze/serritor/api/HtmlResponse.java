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
import org.openqa.selenium.WebDriver;

/**
 * Represents an HTML response.
 *
 * @author Peter Bencze
 */
public final class HtmlResponse extends CallbackParameter {

    private final WebDriver webDriver;

    /**
     * Creates an {@link HtmlResponse} instance.
     *
     * @param crawlCandidate The crawled {@link CrawlCandidate} instance
     * @param webDriver The {@link WebDriver} instance
     */
    public HtmlResponse(final CrawlCandidate crawlCandidate, final WebDriver webDriver) {
        super(crawlCandidate);

        this.webDriver = webDriver;
    }

    /**
     * Returns the {@link WebDriver} instance of the browser.
     *
     * @return The {@link WebDriver} instance of the browser
     */
    public WebDriver getWebDriver() {
        return webDriver;
    }
}
