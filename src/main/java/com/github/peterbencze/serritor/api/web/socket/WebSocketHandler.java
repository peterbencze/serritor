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

package com.github.peterbencze.serritor.api.web.socket;

import java.io.IOException;
import org.eclipse.jetty.websocket.api.Session;

/**
 * Common interface of the WebSocket endpoint handlers.
 */
public interface WebSocketHandler {

    /**
     * Called when a client connects.
     *
     * @param session the WebSocket session
     *
     * @throws IOException if an I/O error occurs in the handler
     */
    default void onConnect(Session session) throws IOException {
    }

    /**
     * Called when a client sends a text message.
     *
     * @param session the WebSocket session
     * @param message the message
     *
     * @throws IOException if an I/O error occurs in the handler
     */
    default void onMessage(Session session, String message) throws IOException {
    }

    /**
     * Called when a client sends a binary message.
     *
     * @param session the WebSocket session
     * @param payload the raw payload array
     * @param offset  the offset in the payload array where the data starts
     * @param length  the length of bytes in the payload
     *
     * @throws IOException if an I/O error occurs in the handler
     */
    default void onMessage(
            Session session,
            byte[] payload,
            int offset,
            int length) throws IOException {
    }

    /**
     * Called when a client disconnects.
     *
     * @param session    the WebSocket session
     * @param statusCode the close status code
     * @param reason     the optional reason for the close
     *
     * @throws IOException if an I/O error occurs in the handler
     */
    default void onClose(Session session, int statusCode, String reason) throws IOException {
    }

    /**
     * Called when a WebSocket error occurs.
     *
     * @param session the WebSocket session
     * @param cause   the cause of the error
     *
     * @throws IOException if an I/O error occurs in the handler
     */
    default void onError(Session session, Throwable cause) throws IOException {
    }
}
