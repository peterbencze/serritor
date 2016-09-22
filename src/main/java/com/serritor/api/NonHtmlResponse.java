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

/**
 * Represents a non-HTML response.
 * 
 * @author Peter Bencze
 */
public final class NonHtmlResponse extends CallbackParameter {

    private final URL responseUrl;
    private final HttpHeadResponse httpHeadResponse;

    private NonHtmlResponse(NonHtmlResponseBuilder builder) {
        super(builder.crawlDepth, builder.refererUrl);

        this.responseUrl = builder.responseUrl;
        this.httpHeadResponse = builder.httpHeadResponse;
    }

    /**
     * Returns the URL of the non-HTML response.
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

    public static class NonHtmlResponseBuilder {

        private int crawlDepth;
        private URL refererUrl;
        private URL responseUrl;
        private HttpHeadResponse httpHeadResponse;

        public NonHtmlResponseBuilder() {
        }

        public NonHtmlResponseBuilder setCrawlDepth(int crawlDepth) {
            this.crawlDepth = crawlDepth;
            return this;
        }

        public NonHtmlResponseBuilder setRefererUrl(URL refererUrl) {
            this.refererUrl = refererUrl;
            return this;
        }

        public NonHtmlResponseBuilder setResponseUrl(URL responseUrl) {
            this.responseUrl = responseUrl;
            return this;
        }

        public NonHtmlResponseBuilder setHttpHeadResponse(HttpHeadResponse httpHeadResponse) {
            this.httpHeadResponse = httpHeadResponse;
            return this;
        }

        public NonHtmlResponse build() {
            return new NonHtmlResponse(this);
        }
    }
}
