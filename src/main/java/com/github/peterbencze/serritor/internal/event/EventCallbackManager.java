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

    private final Map<Class<? extends EventObject>,
            Consumer<? extends EventObject>> defaultCallbacks;
    private final Map<Class<? extends EventObject>,
            List<PatternMatchingCallback<? extends EventObject>>> customCallbacks;

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
     * @param <T>        the type of the input to the operation
     * @param eventClass the runtime class of the event for which the callback should be invoked
     * @param callback   the operation to be performed
     */
    public <T extends EventObject> void setDefaultEventCallback(
            final Class<T> eventClass,
            final Consumer<T> callback) {
        defaultCallbacks.put(eventClass, callback);
    }

    /**
     * Associates a pattern matching callback with the specific event.
     *
     * @param <T>        the type of the input to the operation
     * @param eventClass the runtime class of the event for which the callback should be invoked
     * @param callback   the pattern matching callback to invoke
     */
    public <T extends EventObject> void addCustomEventCallback(
            final Class<T> eventClass,
            final PatternMatchingCallback<T> callback) {
        customCallbacks.computeIfAbsent(eventClass, key -> new ArrayList<>()).add(callback);
    }

    /**
     * Invokes the default callback for the specific event, if no custom callbacks are registered
     * for it. Otherwise, it calls all the associated callbacks whose pattern matches the URL of the
     * request.
     *
     * @param <T>         the type of the input to the operation
     * @param eventClass  the runtime class of the event for which the callback should be invoked
     * @param eventObject the input parameter for the callback
     */
    @SuppressWarnings("unchecked")
    public <T extends EventObject> void call(final Class<T> eventClass, final T eventObject) {
        if (!customCallbacks.containsKey(eventClass)) {
            ((Consumer<T>) defaultCallbacks.get(eventClass)).accept(eventObject);
            return;
        }

        String requestUrl = eventObject.getCrawlCandidate().getRequestUrl().toString();
        List<PatternMatchingCallback<? extends EventObject>> applicableCallbacks =
                customCallbacks.get(eventClass)
                        .stream()
                        .filter(callback -> callback.getUrlPattern().matcher(requestUrl).matches())
                        .collect(Collectors.toList());

        if (applicableCallbacks.isEmpty()) {
            ((Consumer<T>) defaultCallbacks.get(eventClass)).accept(eventObject);
            return;
        }

        applicableCallbacks.stream()
                .map(PatternMatchingCallback::getCallback)
                .forEach(op -> ((Consumer<T>) op).accept(eventObject));
    }
}
