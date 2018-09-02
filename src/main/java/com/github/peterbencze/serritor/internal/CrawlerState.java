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

import java.io.Serializable;
import java.util.HashMap;

/**
 * Represents the state of a crawling session. More specifically, it contains a set of state objects
 * that can be later used to resume that session.
 *
 * @author Peter Bencze
 */
public final class CrawlerState implements Serializable {

    private final HashMap<Class<? extends Serializable>, Serializable> stateObjects;

    /**
     * Creates a {@link CrawlerState} instance.
     */
    public CrawlerState() {
        stateObjects = new HashMap<>();
    }

    /**
     * Inserts the specified state object and its corresponding runtime class into the internal map
     * used for storing these objects.
     *
     * @param stateObject the state object that is required for resuming the crawling session
     */
    public void putStateObject(final Serializable stateObject) {
        stateObjects.put(stateObject.getClass(), stateObject);
    }

    /**
     * Returns the state object specified by its class.
     *
     * @param <T>              the type of the state object
     * @param stateObjectClass the runtime class of the state object
     *
     * @return the state object specified by its class
     */
    public <T extends Serializable> T getStateObject(final Class<T> stateObjectClass) {
        return (T) stateObjects.get(stateObjectClass);
    }
}
