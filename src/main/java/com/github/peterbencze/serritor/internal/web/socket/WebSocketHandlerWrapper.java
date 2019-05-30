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
import java.io.IOException;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

/**
 * A WebSocket handler wrapper that notifies the WebSocket session manager when a client connects or
 * disconnects and also delegates the handling of the event to the user defined handler.
 */
@WebSocket
public final class WebSocketHandlerWrapper {

    private final WebSocketSessionManager sessionManager;
    private final WebSocketHandler delegateHandler;

    /**
     * Creates a {@link WebSocketHandlerWrapper} instance.
     *
     * @param sessionManager  the session manager which tracks the currently open WebSocket
     *                        sessions
     * @param delegateHandler the user defined handler which actually handles the event
     */
    public WebSocketHandlerWrapper(
            final WebSocketSessionManager sessionManager,
            final WebSocketHandler delegateHandler) {
        this.sessionManager = sessionManager;
        this.delegateHandler = delegateHandler;
    }

    /**
     * Called when a client connects.
     *
     * @param session the WebSocket session
     *
     * @throws IOException if an I/O error occurs in the handler
     */
    @OnWebSocketConnect
    public void onWebSocketConnect(final Session session) throws IOException {
        sessionManager.addSession(delegateHandler.getClass(), session);
        delegateHandler.onConnect(session);
    }

    /**
     * Called when a client sends a text message.
     *
     * @param session the WebSocket session
     * @param message the message
     *
     * @throws IOException if an I/O error occurs in the handler
     */
    @OnWebSocketMessage
    public void onWebSocketText(final Session session, final String message) throws IOException {
        delegateHandler.onMessage(session, message);
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
    @OnWebSocketMessage
    public void onWebSocketBinary(
            final Session session,
            final byte[] payload,
            final int offset,
            final int length) throws IOException {
        delegateHandler.onMessage(session, payload, offset, length);
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
    @OnWebSocketClose
    public void onWebSocketClose(
            final Session session,
            final int statusCode,
            final String reason) throws IOException {
        sessionManager.removeSession(delegateHandler.getClass(), session);
        delegateHandler.onClose(session, statusCode, reason);
    }

    /**
     * Called when a WebSocket error occurs.
     *
     * @param session the WebSocket session
     * @param cause   the cause of the error
     *
     * @throws IOException if an I/O error occurs in the handler
     */
    @OnWebSocketError
    public void onWebSocketError(final Session session, final Throwable cause) throws IOException {
        sessionManager.removeSession(delegateHandler.getClass(), session);
        delegateHandler.onError(session, cause);
    }
}
