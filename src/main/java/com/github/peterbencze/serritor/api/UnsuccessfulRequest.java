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
import java.io.IOException;
import java.net.URI;

/**
 * Represents an unsuccessful request.
 *
 * @author Peter Bencze
 */
public final class UnsuccessfulRequest extends CallbackParameter {

    private final IOException exception;

    private UnsuccessfulRequest(final UnsuccessfulRequestBuilder builder) {
        super(builder);

        exception = builder.exception;
    }

    /**
     * Returns the exception that was thrown while trying to fulfill the
     * request.
     *
     * @return The <code>IOException</code> instance
     */
    public IOException getException() {
        return exception;
    }

    public static final class UnsuccessfulRequestBuilder extends CallbackParameterBuilder {

        private IOException exception;

        public UnsuccessfulRequestBuilder(final URI refererUrl, final int crawlDepth, final CrawlRequest crawlRequest) {
            super(refererUrl, crawlDepth, crawlRequest);
        }

        public UnsuccessfulRequestBuilder setException(final IOException exception) {
            this.exception = exception;
            return this;
        }

        @Override
        public UnsuccessfulRequest build() {
            return new UnsuccessfulRequest(this);
        }
    }
}
