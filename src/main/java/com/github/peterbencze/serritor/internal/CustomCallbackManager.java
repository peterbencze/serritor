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

package com.github.peterbencze.serritor.internal;

import com.github.peterbencze.serritor.api.PatternMatchingCallback;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages custom callbacks associated with events.
 */
public final class CustomCallbackManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomCallbackManager.class);

    private final Map<Class<? extends CrawlEvent>,
            List<PatternMatchingCallback<? extends CrawlEvent>>> customCallbacks;

    /**
     * Creates a {@link CustomCallbackManager} instance.
     */
    public CustomCallbackManager() {
        customCallbacks = new HashMap<>();
    }

    /**
     * Associates a pattern matching callback with the specific event.
     *
     * @param <T>        the type of the input to the operation
     * @param eventClass the runtime class of the event for which the callback should be invoked
     * @param callback   the pattern matching callback to invoke
     */
    public <T extends CrawlEvent> void addCustomCallback(
            final Class<T> eventClass,
            final PatternMatchingCallback<T> callback) {
        LOGGER.debug("Adding custom callback for event {} with URL pattern {}",
                eventClass.getSimpleName(), callback.getUrlPattern());

        customCallbacks.computeIfAbsent(eventClass, key -> new ArrayList<>()).add(callback);
    }

    /**
     * Invokes all the custom callbacks associated with the event whose pattern matches the request
     * URL. If no custom callbacks are registered for the event, it calls the provided default
     * callback instead.
     *
     * @param <T>             the type of the input to the operation
     * @param eventClass      the runtime class of the event for which the callback should be
     *                        invoked
     * @param eventObject     the input parameter for the callback
     * @param defaultCallback the default callback for the event
     */
    @SuppressWarnings("unchecked")
    public <T extends CrawlEvent> void callCustomOrDefault(
            final Class<T> eventClass,
            final T eventObject,
            final Consumer<T> defaultCallback) {
        String requestUrl = eventObject.getCrawlCandidate().getRequestUrl().toString();
        List<PatternMatchingCallback<? extends CrawlEvent>> applicableCustomCallbacks =
                customCallbacks.getOrDefault(eventClass, Collections.emptyList())
                        .stream()
                        .filter(callback -> callback.getUrlPattern().matcher(requestUrl).find())
                        .collect(Collectors.toList());

        if (!applicableCustomCallbacks.isEmpty()) {
            applicableCustomCallbacks.forEach(callback -> {
                LOGGER.debug("Calling custom callback for event {} with URL pattern {}",
                        eventClass.getSimpleName(), callback.getUrlPattern());

                ((Consumer<T>) callback.getCallback()).accept(eventObject);
            });
        } else {
            LOGGER.debug("Calling default callback for event {}", eventClass.getSimpleName());

            defaultCallback.accept(eventObject);
        }
    }
}
