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

package com.github.peterbencze.serritor.internal.web;

import com.github.peterbencze.serritor.api.web.ServerConfiguration;
import com.github.peterbencze.serritor.api.web.WebApiException;
import com.github.peterbencze.serritor.api.web.http.HttpHandler;
import com.github.peterbencze.serritor.api.web.http.HttpMethod;
import com.github.peterbencze.serritor.api.web.socket.WebSocketHandler;
import com.github.peterbencze.serritor.internal.web.http.HttpServlet;
import com.github.peterbencze.serritor.internal.web.socket.WebSocketFactory;
import com.github.peterbencze.serritor.internal.web.socket.WebSocketHandlerWrapper;
import com.github.peterbencze.serritor.internal.web.socket.WebSocketSessionManager;
import java.util.EnumSet;
import java.util.Set;
import javax.servlet.DispatcherType;
import javax.servlet.ServletException;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.server.WebSocketUpgradeFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A web API implementation that allows users to register HTTP and WebSocket endpoints that can be
 * used to interact with the crawler while it is running.
 */
public class WebApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebApi.class);

    private final ServerConfiguration serverConfig;
    private final Server server;
    private final ServletContextHandler contextHandler;
    private final WebSocketUpgradeFilter wsUpgradeFilter;
    private final WebSocketSessionManager webSocketSessionManager;

    /**
     * Creates a {@link WebApi} instance.
     *
     * @param serverConfig the configuration of the web server
     */
    public WebApi(final ServerConfiguration serverConfig) {
        this.serverConfig = serverConfig;

        contextHandler = new ServletContextHandler();
        contextHandler.setContextPath("/");

        FilterHolder crossOriginFilter =
                contextHandler.addFilter(CrossOriginFilter.class, "/*",
                        EnumSet.of(DispatcherType.REQUEST));
        crossOriginFilter.setInitParameter(CrossOriginFilter.ALLOWED_ORIGINS_PARAM,
                String.join(",", serverConfig.getCorsAllowedOrigins()));
        crossOriginFilter.setInitParameter(CrossOriginFilter.ALLOWED_METHODS_PARAM,
                String.join(",", serverConfig.getCorsAllowedMethods()));
        crossOriginFilter.setInitParameter(CrossOriginFilter.ALLOWED_HEADERS_PARAM,
                String.join(",", serverConfig.getCorsAllowedMethods()));
        crossOriginFilter.setInitParameter(CrossOriginFilter.EXPOSED_HEADERS_PARAM,
                String.join(",", serverConfig.getCorsExposedHeaders()));

        try {
            wsUpgradeFilter = WebSocketUpgradeFilter.configureContext(contextHandler);
        } catch (ServletException e) {
            LOGGER.error("Error while adding WebSocket upgrade filter", e);
            throw new WebApiException(e);
        }

        webSocketSessionManager = new WebSocketSessionManager();

        server = createServer(serverConfig);
        server.setHandler(contextHandler);
        server.setErrorHandler(new JsonErrorHandler());
    }

    /**
     * Returns the configuration of the web server.
     *
     * @return the configuration of the web server
     */
    public ServerConfiguration getServerConfiguration() {
        return serverConfig;
    }

    /**
     * Starts the web server.
     */
    public void start() {
        try {
            server.start();
        } catch (Exception e) {
            LOGGER.error("Error while running web server", e);
            throw new WebApiException(e);
        }
    }

    /**
     * Stops the web server.
     */
    public void stop() {
        try {
            server.stop();
        } catch (Exception e) {
            LOGGER.error("Error while stopping web server", e);
            throw new WebApiException(e);
        }
    }

    /**
     * Returns a set of open WebSocket sessions that represent connections to the specific
     * endpoint.
     *
     * @param socketHandlerClass the runtime class of the WebSocket endpoint handler
     *
     * @return a set of open WebSocket sessions that represent connections to the specific endpoint
     */
    public Set<Session> getOpenWebSocketSessions(
            final Class<? extends WebSocketHandler> socketHandlerClass) {
        return webSocketSessionManager.getOpenSessions(socketHandlerClass);
    }

    /**
     * Adds an HTTP endpoint to the web API.
     *
     * @param httpMethod the HTTP method of the endpoint
     * @param path       the path of the endpoint
     * @param handler    the handler of the endpoint
     */
    public void addHttpEndpoint(
            final HttpMethod httpMethod,
            final String path,
            final HttpHandler handler) {
        HttpServlet servlet = new HttpServlet(httpMethod, handler);
        contextHandler.addServlet(new ServletHolder(servlet), path);
    }

    /**
     * Adds a WebSocket endpoint to the web API.
     *
     * @param path    the path of the endpoint
     * @param handler the handler of the endpoint
     */
    public void addWebSocketEndpoint(final String path, final WebSocketHandler handler) {
        WebSocketHandlerWrapper handlerWrapper =
                new WebSocketHandlerWrapper(webSocketSessionManager, handler);
        WebSocketFactory socketFactory =
                new WebSocketFactory(serverConfig.getCorsAllowedOrigins(), handlerWrapper);

        wsUpgradeFilter.addMapping(path, socketFactory);
    }

    /**
     * Returns the Jetty HTTP servlet server.
     *
     * @return the Jetty HTTP servlet server
     */
    protected Server getServer() {
        return server;
    }

    /**
     * Returns the servlet context.
     *
     * @return the servlet context
     */
    protected ServletContextHandler getContextHandler() {
        return contextHandler;
    }

    /**
     * Creates and configures a Jetty HTTP servlet server.
     *
     * @param serverConfig the configuration of the web server
     *
     * @return the configured Jetty HTTP servlet server
     */
    private static Server createServer(final ServerConfiguration serverConfig) {
        Server server = new Server(new QueuedThreadPool(250, 8, 60_000));

        ServerConnector serverConnector = serverConfig.getSslContextConfiguration()
                .map(sslContextConfig -> {
                    SslContextFactory sslContextFactory = new SslContextFactory.Server();
                    sslContextFactory.setKeyStorePath(sslContextConfig.getKeyStorePath());
                    sslContextFactory.setKeyStorePassword(sslContextConfig.getKeyStorePassword());
                    sslContextConfig.getKeyManagerPassword()
                            .ifPresent(sslContextFactory::setKeyManagerPassword);

                    return new ServerConnector(server, sslContextFactory);
                })
                .orElseGet(() -> new ServerConnector(server));
        serverConnector.setPort(serverConfig.getPort());

        server.addConnector(serverConnector);

        return server;
    }
}
