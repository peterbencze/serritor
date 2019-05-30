/*
 * Copyright 2019 Peter Bencze.
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

package com.github.peterbencze.serritor.internal.web.socket;

import com.github.peterbencze.serritor.api.web.socket.WebSocketHandler;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.eclipse.jetty.websocket.api.Session;

/**
 * A WebSocket session manager that tracks open sessions.
 */
public final class WebSocketSessionManager {

    private final Map<Class<? extends WebSocketHandler>, Set<Session>> sessionsBySocket;

    /**
     * Creates a {@link WebSocketSessionManager} instance.
     */
    public WebSocketSessionManager() {
        sessionsBySocket = new ConcurrentHashMap<>();
    }

    /**
     * Returns a set of open WebSocket sessions that represent connections to the specific
     * endpoint.
     *
     * @param socketHandlerClass the runtime class of the WebSocket endpoint handler
     *
     * @return a set of open WebSocket sessions that represent connections to the specific endpoint
     */
    public Set<Session> getOpenSessions(
            final Class<? extends WebSocketHandler> socketHandlerClass) {
        return sessionsBySocket.getOrDefault(socketHandlerClass, ConcurrentHashMap.newKeySet())
                .stream()
                .filter(Session::isOpen)
                .collect(Collectors.toSet());
    }

    /**
     * Adds a WebSocket session to the set of open sessions. This method is called when a client
     * connects to a WebSocket endpoint.
     *
     * @param socketHandlerClass the runtime class of the WebSocket endpoint handler
     * @param session            the open WebSocket session
     */
    public void addSession(
            final Class<? extends WebSocketHandler> socketHandlerClass,
            final Session session) {
        sessionsBySocket.computeIfAbsent(socketHandlerClass, key -> ConcurrentHashMap.newKeySet())
                .add(session);
    }

    /**
     * Removes a WebSocket session from the set of open sessions. This method is called when a
     * client disconnects from a WebSocket endpoint.
     *
     * @param socketHandlerClass the runtime class of the WebSocket endpoint handler
     * @param session            the no longer open WebSocket session
     */
    public void removeSession(
            final Class<? extends WebSocketHandler> socketHandlerClass,
            final Session session) {
        if (sessionsBySocket.containsKey(socketHandlerClass)) {
            sessionsBySocket.get(socketHandlerClass).remove(session);
        }
    }
}
