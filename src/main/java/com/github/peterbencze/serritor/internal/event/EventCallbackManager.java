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

package com.github.peterbencze.serritor.internal.event;

import com.github.peterbencze.serritor.api.PatternMatchingCallback;
import com.github.peterbencze.serritor.api.event.CrawlEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Manages the default and custom callbacks associated with specific events. When an event occurs,
 * it calls the default callback for it, or the associated custom ones whose pattern matches the URL
 * of the request.
 *
 * @author Peter Bencze
 */
public final class EventCallbackManager {

    private final Map<CrawlEvent, Consumer<? extends EventObject>> defaultCallbacks;
    private final Map<CrawlEvent, List<PatternMatchingCallback>> customCallbacks;

    /**
     * Creates an {@link EventCallbackManager} instance.
     */
    public EventCallbackManager() {
        defaultCallbacks = new HashMap<>();
        customCallbacks = new HashMap<>();
    }

    /**
     * Sets the default callback for the specific event.
     *
     * @param <T>      the type of the input to the operation
     * @param event    the event for which the callback should be invoked
     * @param callback the operation to be performed
     */
    public <T extends EventObject> void setDefaultEventCallback(
            final CrawlEvent event,
            final Consumer<T> callback) {
        defaultCallbacks.put(event, callback);
    }

    /**
     * Associates a pattern matching callback with the specific event.
     *
     * @param event    the event for which the callback should be invoked
     * @param callback the pattern matching callback to invoke
     */
    public void addCustomEventCallback(
            final CrawlEvent event,
            final PatternMatchingCallback callback) {
        customCallbacks.computeIfAbsent(event, key -> new ArrayList<>()).add(callback);
    }

    /**
     * Invokes the default callback for the specific event, if no custom callbacks are registered
     * for it. Otherwise, it calls all the associated callbacks whose pattern matches the URL of the
     * request.
     *
     * @param <T>         the type of the input to the operation
     * @param event       the event for which the callback should be invoked
     * @param eventObject the input parameter for the callback
     */
    public <T extends EventObject> void call(final CrawlEvent event, final T eventObject) {
        if (!customCallbacks.containsKey(event)) {
            ((Consumer<T>) defaultCallbacks.get(event)).accept(eventObject);
            return;
        }

        String requestUrl = eventObject.getCrawlCandidate().getRequestUrl().toString();
        List<PatternMatchingCallback> applicableCallbacks = customCallbacks.get(event)
                .stream()
                .filter(callback -> callback.getUrlPattern().matcher(requestUrl).matches())
                .collect(Collectors.toList());

        if (applicableCallbacks.isEmpty()) {
            ((Consumer<T>) defaultCallbacks.get(event)).accept(eventObject);
            return;
        }

        applicableCallbacks.stream()
                .map(PatternMatchingCallback::getCallback)
                .forEach(op -> op.accept(eventObject));
    }
}
