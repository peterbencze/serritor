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

import java.io.IOException;
import java.util.Set;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A factory that is used to create WebSockets.
 */
public final class WebSocketFactory implements WebSocketCreator {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketFactory.class);

    private final Set<String> allowedOrigins;
    private final WebSocketHandlerWrapper handlerWrapper;

    /**
     * Creates a {@link WebSocketFactory} instance.
     *
     * @param allowedOrigins the set of allowed origins
     * @param handlerWrapper the WebSocket handler wrapper
     */
    public WebSocketFactory(
            final Set<String> allowedOrigins,
            final WebSocketHandlerWrapper handlerWrapper) {
        this.allowedOrigins = allowedOrigins;
        this.handlerWrapper = handlerWrapper;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object createWebSocket(
            final ServletUpgradeRequest request,
            final ServletUpgradeResponse response) {
        String origin = request.getHeader(HttpHeader.ORIGIN.toString());
        if (origin != null) {
            LOGGER.info("WebSocket upgrade request from origin {}", origin);

            if (isAllowedOrigin(origin)) {
                LOGGER.info("Origin is allowed");
                return handlerWrapper;
            }

            LOGGER.info("Origin is not allowed");

            try {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                        HttpStatus.getMessage(HttpServletResponse.SC_UNAUTHORIZED));
            } catch (IOException e) {
                LOGGER.error("Error while sending response", e);
            }

            return null;
        }

        LOGGER.info("No Origin header present in request");
        return null;
    }

    /**
     * Indicates whether the specific origin is allowed to access the resource or not.
     *
     * @param origin the request origin
     *
     * @return <code>true</code> if the origin is allowed, <code>false</code> otherwise
     */
    private boolean isAllowedOrigin(final String origin) {
        return allowedOrigins.stream()
                .anyMatch(allowedOrigin ->
                        "*".equals(allowedOrigin) || origin.startsWith(allowedOrigin));
    }
}
