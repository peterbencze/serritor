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
import java.net.URL;

/**
 * Represents a non-HTML response.
 *
 * @author Peter Bencze
 */
public final class NonHtmlResponse extends CallbackParameter {

    private final HttpHeadResponse httpHeadResponse;

    private NonHtmlResponse(final NonHtmlResponseBuilder builder) {
        super(builder);

        httpHeadResponse = builder.httpHeadResponse;
    }

    /**
     * Returns the HTTP HEAD response.
     *
     * @return The HTTP HEAD response
     */
    public HttpHeadResponse getHttpHeadResponse() {
        return httpHeadResponse;
    }

    public static final class NonHtmlResponseBuilder extends CallbackParameterBuilder<NonHtmlResponseBuilder> {

        private HttpHeadResponse httpHeadResponse;

        public NonHtmlResponseBuilder(URL refererUrl, int crawlDepth, CrawlRequest crawlRequest) {
            super(refererUrl, crawlDepth, crawlRequest);
        }

        public NonHtmlResponseBuilder setHttpHeadResponse(final HttpHeadResponse httpHeadResponse) {
            this.httpHeadResponse = httpHeadResponse;
            return this;
        }

        @Override
        public NonHtmlResponse build() {
            return new NonHtmlResponse(this);
        }
    }
}
