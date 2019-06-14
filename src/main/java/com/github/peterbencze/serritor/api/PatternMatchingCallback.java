/*
 * Copyright 2018 Peter Bencze.
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

import com.github.peterbencze.serritor.internal.CrawlEvent;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import org.apache.commons.lang3.Validate;

/**
 * Represents an operation which is invoked when the specified regex pattern matches the request
 * URL.
 *
 * @param <T> the type of the input to the operation
 */
public final class PatternMatchingCallback<T extends CrawlEvent> {

    private final Pattern urlPattern;
    private final Consumer<T> callback;

    /**
     * Creates a {@link PatternMatchingCallback} instance.
     *
     * @param urlPattern the regex pattern used for matching on request URLs
     * @param callback   the operation to be performed when the pattern matches
     */
    public PatternMatchingCallback(
            final Pattern urlPattern,
            final Consumer<T> callback) {
        Validate.notNull(urlPattern, "The urlPattern parameter cannot be null.");
        Validate.notNull(callback, "The callback parameter cannot be null.");

        this.urlPattern = urlPattern;
        this.callback = callback;
    }

    /**
     * Returns the regex pattern used for matching on request URLs.
     *
     * @return the regex pattern used for matching
     */
    public Pattern getUrlPattern() {
        return urlPattern;
    }

    /**
     * Returns the operation to be performed when the pattern matches.
     *
     * @return the operation to be performed when the pattern matches
     */
    public Consumer<T> getCallback() {
        return callback;
    }
}
