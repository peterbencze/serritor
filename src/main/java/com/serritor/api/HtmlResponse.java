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
package com.serritor.api;

import com.serritor.internal.CallbackParameter;
import java.net.URL;
import org.openqa.selenium.WebDriver;

/**
 * Represents an HTML response.
 * 
 * @author Peter Bencze
 */
public final class HtmlResponse extends CallbackParameter {

    private final URL responseUrl;
    private final HttpHeadResponse httpHeadResponse;
    private final WebDriver webDriver;

    private HtmlResponse(HtmlResponseBuilder builder) {
        super(builder.crawlDepth, builder.refererUrl);

        this.responseUrl = builder.responseUrl;
        this.httpHeadResponse = builder.httpHeadResponse;
        this.webDriver = builder.webDriver;
    }

    /**
     * Returns the URL of the response.
     * 
     * @return The URL of the response
     */
    public URL getUrl() {
        return responseUrl;
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

    public static class HtmlResponseBuilder {

        private int crawlDepth;
        private URL refererUrl;
        private URL responseUrl;
        private HttpHeadResponse httpHeadResponse;
        private WebDriver webDriver;

        public HtmlResponseBuilder setCrawlDepth(int crawlDepth) {
            this.crawlDepth = crawlDepth;
            return this;
        }

        public HtmlResponseBuilder setRefererUrl(URL refererUrl) {
            this.refererUrl = refererUrl;
            return this;
        }

        public HtmlResponseBuilder setResponseUrl(URL responseUrl) {
            this.responseUrl = responseUrl;
            return this;
        }

        public HtmlResponseBuilder setHttpHeadResponse(HttpHeadResponse httpHeadResponse) {
            this.httpHeadResponse = httpHeadResponse;
            return this;
        }

        public HtmlResponseBuilder setWebDriver(WebDriver webDriver) {
            this.webDriver = webDriver;
            return this;
        }

        public HtmlResponse build() {
            return new HtmlResponse(this);
        }
    }
}
