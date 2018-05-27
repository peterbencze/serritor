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
import java.net.URI;
import org.openqa.selenium.WebDriver;

/**
 * Represents an HTML response.
 *
 * @author Peter Bencze
 */
public final class HtmlResponse extends CallbackParameter {

    private final WebDriver webDriver;

    /**
     * Constructs a <code>HtmlResponse</code> instance.
     * 
     * @param refererUrl The referer URL
     * @param crawlDepth The current crawl depth
     * @param crawlRequest The processed crawl request
     * @param webDriver The <code>WebDriver</code> instance
     */
    public HtmlResponse(final URI refererUrl, final int crawlDepth, final CrawlRequest crawlRequest, final WebDriver webDriver) {
        super(refererUrl, crawlDepth, crawlRequest);

        this.webDriver = webDriver;
    }

    /**
     * Returns the <code>WebDriver</code> instance for the browser.
     *
     * @return The <code>WebDriver</code> instance
     */
    public WebDriver getWebDriver() {
        return webDriver;
    }
}
