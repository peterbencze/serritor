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
import java.io.IOException;

/**
 * Represents an unsuccessful request.
 *
 * @author Peter Bencze
 */
public final class UnsuccessfulRequest extends CallbackParameter {

    private final IOException exception;

    private UnsuccessfulRequest(UnsuccessfulRequestBuilder builder) {
        super(builder);

        exception = builder.exception;
    }

    /**
     * Returns the exception that was thrown.
     *
     * @return The thrown exception
     */
    public IOException getException() {
        return exception;
    }

    public static class UnsuccessfulRequestBuilder extends CallbackParameterBuilder<UnsuccessfulRequestBuilder> {

        private IOException exception;

        public UnsuccessfulRequestBuilder setException(IOException exception) {
            this.exception = exception;
            return this;
        }

        @Override
        public UnsuccessfulRequest build() {
            return new UnsuccessfulRequest(this);
        }
    }
}
