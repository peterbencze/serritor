/* 
 * Copyright 2016 Peter Bencze.
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

    private final HttpHeadResponse httpHeadResponse;
    private final WebDriver webDriver;

    private HtmlResponse(HtmlResponseBuilder builder) {
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
     * Returns the WebDriver instance for the browser.
     *
     * @return The WebDriver instance
     */
    public WebDriver getWebDriver() {
        return webDriver;
    }

    public static class HtmlResponseBuilder extends CallbackParameterBuilder<HtmlResponseBuilder> {

        private HttpHeadResponse httpHeadResponse;
        private WebDriver webDriver;

        public HtmlResponseBuilder setHttpHeadResponse(HttpHeadResponse httpHeadResponse) {
            this.httpHeadResponse = httpHeadResponse;
            return this;
        }

        public HtmlResponseBuilder setWebDriver(WebDriver webDriver) {
            this.webDriver = webDriver;
            return this;
        }

        @Override
        public HtmlResponse build() {
            return new HtmlResponse(this);
        }
    }
}
