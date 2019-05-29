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
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.Future;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.http.MimeTypes.Type;
import org.eclipse.jetty.websocket.api.Session;

/**
 * A helper class that is intended to make it easier for users to read and write JSON structures to
 * HTTP or WebSocket streams.
 */
public final class JsonReaderWriter {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new Jdk8Module());

    /**
     * Private constructor to hide the implicit public one.
     */
    private JsonReaderWriter() {
    }

    /**
     * Returns the singleton object mapper instance that is used for reading and writing JSON.
     *
     * @return the singleton object mapper instance that is used for reading and writing JSON
     */
    public static ObjectMapper getObjectMapper() {
        return OBJECT_MAPPER;
    }

    /**
     * Deserializes the JSON structure contained by the HTTP request body into an instance of the
     * specified type.
     *
     * @param request    the HTTP request
     * @param objectType the runtime class of the object
     * @param <T>        the type of the object
     *
     * @return the deserialized object
     */
    public static <T> T readJsonRequest(
            final HttpServletRequest request,
            final Class<T> objectType) {
        try {
            return OBJECT_MAPPER.readValue(request.getInputStream(), objectType);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Serializes the specified object and writes the JSON structure to the response.
     *
     * @param response the HTTP response
     * @param object   the object to serialize
     */
    public static void writeJsonResponse(final HttpServletResponse response, final Object object) {
        response.setContentType(Type.APPLICATION_JSON_UTF_8.asString());

        try {
            OBJECT_MAPPER.writeValue(response.getOutputStream(), object);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Deserializes the JSON structure contained by the WebSocket message into an instance of the
     * specified type.
     *
     * @param message    the WebSocket message
     * @param objectType the runtime class of the object
     * @param <T>        the type of the object
     *
     * @return the deserialized object
     */
    public static <T> T readJsonMessage(final String message, final Class<T> objectType) {
        try {
            return OBJECT_MAPPER.readValue(message, objectType);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Serializes the specified object and writes the JSON structure to the WebSocket, blocking
     * until all bytes of the message have been transmitted.
     *
     * @param session the WebSocket session
     * @param object  the object to serialize
     */
    public static void writeJsonMessage(final Session session, final Object object) {
        try {
            session.getRemote().sendString(OBJECT_MAPPER.writeValueAsString(object));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Serializes the specified object and asynchronously writes the JSON structure to the
     * WebSocket. This method may return before the message is transmitted.
     *
     * @param session the WebSocket session
     * @param object  the object to serialize
     *
     * @return a Future object that can be used to track progress of the transmission
     */
    public static Future<Void> writeJsonMessageByFuture(
            final Session session,
            final Object object) {
        try {
            return session.getRemote().sendStringByFuture(OBJECT_MAPPER.writeValueAsString(object));
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }
}
