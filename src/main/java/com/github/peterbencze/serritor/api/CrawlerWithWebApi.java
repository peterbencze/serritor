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

package com.github.peterbencze.serritor.api;

import com.github.peterbencze.serritor.api.web.ServerConfiguration;
import com.github.peterbencze.serritor.api.web.http.HttpHandler;
import com.github.peterbencze.serritor.api.web.http.HttpMethod;
import com.github.peterbencze.serritor.api.web.socket.WebSocketHandler;
import com.github.peterbencze.serritor.internal.web.WebApi;
import java.util.Set;
import org.apache.commons.lang3.Validate;
import org.eclipse.jetty.websocket.api.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A crawler implementation with web API support. It allows users to register HTTP and WebSocket
 * endpoints that can be used to interact with the crawler while it is running.
 */
public abstract class CrawlerWithWebApi extends Crawler {

    private static final Logger LOGGER = LoggerFactory.getLogger(CrawlerWithWebApi.class);

    private final WebApi webApi;

    /**
     * Base constructor which sets up the web server and the crawler with the provided
     * configurations.
     *
     * @param serverConfig  the configuration of the web server
     * @param crawlerConfig the configuration of the crawler
     */
    protected CrawlerWithWebApi(
            final ServerConfiguration serverConfig,
            final CrawlerConfiguration crawlerConfig) {
        this(serverConfig, new CrawlerState(crawlerConfig));
    }

    /**
     * Base constructor which sets up the web server with the provided configuration and restores
     * the crawler to the given state.
     *
     * @param serverConfig the configuration of the web server
     * @param state        the state to restore the crawler to
     */
    protected CrawlerWithWebApi(final ServerConfiguration serverConfig, final CrawlerState state) {
        super(state);

        webApi = new WebApi(serverConfig);
    }

    /**
     * Returns the configuration of the web server.
     *
     * @return the configuration of the web server
     */
    public final ServerConfiguration getServerConfiguration() {
        return webApi.getServerConfiguration();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onStart() {
        super.onStart();

        LOGGER.info("Starting web server");
        webApi.start();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onStop() {
        super.onStop();

        LOGGER.info("Stopping web server");
        webApi.stop();
    }

    /**
     * Returns a set of open WebSocket sessions that represent connections to the specific
     * endpoint.
     *
     * @param socketHandlerClass the runtime class of the WebSocket endpoint handler
     *
     * @return a set of open WebSocket sessions that represent connections to the specific endpoint
     */
    protected final Set<Session> getOpenWebSocketSessions(
            final Class<? extends WebSocketHandler> socketHandlerClass) {
        Validate.notNull(socketHandlerClass, "The socketHandlerClass parameter cannot be null");

        return webApi.getOpenWebSocketSessions(socketHandlerClass);
    }

    /**
     * Adds an HTTP endpoint to the web API.
     *
     * @param httpMethod the HTTP method of the endpoint
     * @param path       the path of the endpoint
     * @param handler    the handler of the endpoint
     */
    protected final void addHttpEndpoint(
            final HttpMethod httpMethod,
            final String path,
            final HttpHandler handler) {
        Validate.notNull(httpMethod, "The httpMethod parameter cannot be null");
        Validate.notBlank(path, "The path parameter cannot be null or blank");
        Validate.notNull(handler, "The handler parameter cannot be null");

        webApi.addHttpEndpoint(httpMethod, path, handler);
    }

    /**
     * Adds a WebSocket endpoint to the web API.
     *
     * @param path    the path of the endpoint
     * @param handler the handler of the endpoint
     */
    protected final void addWebSocketEndpoint(final String path, final WebSocketHandler handler) {
        Validate.notBlank(path, "The path parameter cannot be null or blank");
        Validate.notNull(handler, "The handler parameter cannot be null");

        webApi.addWebSocketEndpoint(path, handler);
    }
}
