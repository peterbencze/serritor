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

package com.github.peterbencze.serritor.api.helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.Future;
import javax.servlet.ServletResponse;
import org.eclipse.jetty.http.MimeTypes.Type;
import org.eclipse.jetty.websocket.api.Session;

/**
 * A helper class that can be used to send JSON objects as HTTP response or WebSocket message.
 */
public final class JsonSender {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * Private constructor to hide the implicit public one.
     */
    private JsonSender() {
    }

    /**
     * Returns the singleton object mapper that is used for reading and writing JSON.
     *
     * @return the singleton object mapper that is used for reading and writing JSON
     */
    public static ObjectMapper getObjectMapper() {
        return OBJECT_MAPPER;
    }

    /**
     * Sends a JSON HTTP response.
     *
     * @param response the response
     * @param object   the object to send as JSON
     */
    public static void sendJsonResponse(final ServletResponse response, final Object object) {
        response.setContentType(Type.APPLICATION_JSON_UTF_8.asString());

        try {
            OBJECT_MAPPER.writeValue(response.getOutputStream(), object);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Sends a JSON WebSocket message, blocking until all bytes of the message has been
     * transmitted.
     *
     * @param session the WebSocket session
     * @param object  the object to send as JSON
     */
    public static void sendJsonMessage(final Session session, final Object object) {
        try {
            session.getRemote().sendString(OBJECT_MAPPER.writeValueAsString(object));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Initiates the asynchronous transmission of a JSON WebSocket message. This method may return
     * before the message is transmitted.
     *
     * @param session the WebSocket session
     * @param object  the object to send as JSON
     *
     * @return a Future object that can be used to track progress of the transmission
     */
    public static Future<Void> sendJsonMessageByFuture(final Session session, final Object object) {
        try {
            return session.getRemote().sendStringByFuture(OBJECT_MAPPER.writeValueAsString(object));
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }
}
