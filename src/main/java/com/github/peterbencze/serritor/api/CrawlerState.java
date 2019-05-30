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

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Represents the current state of the crawling session. More specifically, it contains a set of
 * state objects that can be later reused to resume that particular session.
 */
public final class CrawlerState implements Serializable {

    private final Map<Class<? extends Serializable>, Serializable> stateObjects;

    /**
     * Creates a {@link CrawlerState} instance.
     *
     * @param stateObj the state object that is required to restore the state of the crawler
     */
    public CrawlerState(final Serializable stateObj) {
        this(Collections.singletonList(stateObj));
    }

    /**
     * Creates a {@link CrawlerState} instance.
     *
     * @param stateObjs the list of state objects that are required to restore the state of the
     *                  crawler
     */
    public CrawlerState(final List<Serializable> stateObjs) {
        this.stateObjects = stateObjs.stream()
                .collect(Collectors.toMap(Serializable::getClass, stateObj -> stateObj));
    }

    /**
     * Returns the state object specified by its class.
     *
     * @param <T>              the type of the state object
     * @param stateObjectClass the runtime class of the state object
     *
     * @return the state object specified by its class
     */
    public <T extends Serializable> Optional<T> getStateObject(final Class<T> stateObjectClass) {
        return Optional.ofNullable((T) stateObjects.get(stateObjectClass));
    }
}
