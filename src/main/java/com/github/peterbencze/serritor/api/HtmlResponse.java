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

    private final HttpHeadResponse httpHeadResponse;
    private final WebDriver webDriver;

    private HtmlResponse(final HtmlResponseBuilder builder) {
        super(builder);

        httpHeadResponse = builder.httpHeadResponse;
        webDriver = builder.webDriver;
    }

    /**
     * Returns the HTTP HEAD response.
     *
     * @return The HTTP HEAD response
     */
    public HttpHeadResponse getHttpHeadResponse() {
        return httpHeadResponse;
    }

    /**
     * Returns the <code>WebDriver</code> instance for the browser.
     *
     * @return The <code>WebDriver</code> instance
     */
    public WebDriver getWebDriver() {
        return webDriver;
    }

    public static final class HtmlResponseBuilder extends CallbackParameterBuilder {

        private HttpHeadResponse httpHeadResponse;
        private WebDriver webDriver;

        public HtmlResponseBuilder(final URI refererUrl, final int crawlDepth, final CrawlRequest crawlRequest) {
            super(refererUrl, crawlDepth, crawlRequest);
        }

        public HtmlResponseBuilder setHttpHeadResponse(final HttpHeadResponse httpHeadResponse) {
            this.httpHeadResponse = httpHeadResponse;
            return this;
        }

        public HtmlResponseBuilder setWebDriver(final WebDriver webDriver) {
            this.webDriver = webDriver;
            return this;
        }

        @Override
        public HtmlResponse build() {
            return new HtmlResponse(this);
        }
    }
}
