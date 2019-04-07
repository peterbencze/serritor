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

import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.github.peterbencze.serritor.api.web.AccessControlConfiguration;
import com.github.peterbencze.serritor.api.web.WebApiConfiguration;
import com.github.peterbencze.serritor.internal.web.ApiEndpoint;
import com.github.peterbencze.serritor.internal.web.accessmanager.JwtAccessManager;
import com.github.peterbencze.serritor.internal.web.accessmanager.NoopAccessManager;
import com.github.peterbencze.serritor.internal.web.handler.JwtHandler;
import com.github.peterbencze.serritor.internal.web.handler.LoginHandler;
import com.github.peterbencze.serritor.internal.web.handler.XsrfTokenHandler;
import io.javalin.Handler;
import io.javalin.Javalin;
import io.javalin.apibuilder.ApiBuilder;
import io.javalin.json.JavalinJackson;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import javax.crypto.KeyGenerator;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a skeletal implementation of a crawler to minimize the effort for users to implement
 * their own. It also exposes a RESTful web API that can be used to interact with the crawler while
 * it is running.
 */
public abstract class RestfulBaseCrawler extends BaseCrawler {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestfulBaseCrawler.class);

    private final WebApiConfiguration webApiConfig;
    private final Javalin webServer;

    /**
     * Base constructor which sets up the crawler with the provided configuration. The web API is
     * initialized with the default settings.
     *
     * @param crawlerConfig the configuration of the crawler
     */
    protected RestfulBaseCrawler(final CrawlerConfiguration crawlerConfig) {
        this(WebApiConfiguration.createDefault(), crawlerConfig);
    }

    /**
     * Base constructor which sets up the web API and the crawler with the provided configurations.
     *
     * @param webApiConfig  the configuration of the web API
     * @param crawlerConfig the configuration of the crawler
     */
    protected RestfulBaseCrawler(
            final WebApiConfiguration webApiConfig,
            final CrawlerConfiguration crawlerConfig) {
        this(webApiConfig, new CrawlerState(crawlerConfig));
    }

    /**
     * Base constructor which restores the crawler to the provided state. The web API is initialized
     * with the default settings.
     *
     * @param state the state to restore the crawler to
     */
    protected RestfulBaseCrawler(final CrawlerState state) {
        this(WebApiConfiguration.createDefault(), state);
    }

    /**
     * Base constructor which sets up the web API with the provided configuration and restores the
     * crawler to the provided state.
     *
     * @param webApiConfig the configuration of the web API
     * @param state        the state to restore the crawler to
     */
    protected RestfulBaseCrawler(final WebApiConfiguration webApiConfig, final CrawlerState state) {
        super(state);

        this.webApiConfig = webApiConfig;

        webServer = Javalin.create()
                .disableStartupBanner()
                .server(() -> createServer(webApiConfig))
                .routes(() -> {
                    registerEndpoint(ApiEndpoint.STOP_CRAWLER, ctx -> stop());
                    registerEndpoint(ApiEndpoint.GET_CONFIG,
                            ctx -> ctx.json(getCrawlerConfiguration()));
                    registerEndpoint(ApiEndpoint.GET_STATS, ctx -> ctx.json(getCrawlStats()));
                });

        webApiConfig.getCorsOrigins().forEach(webServer::enableCorsForOrigin);

        Optional<AccessControlConfiguration> accessControlConfigOpt
                = webApiConfig.getAccessControlConfiguration();
        if (accessControlConfigOpt.isPresent()) {
            AccessControlConfiguration accessControlConfig = accessControlConfigOpt.get();

            webServer.before(new JwtHandler());

            if (accessControlConfig.isCookieAuthenticationEnabled()) {
                webServer.before(new XsrfTokenHandler());
            }

            byte[] secretKey = accessControlConfig.getSecretKey()
                    .orElseGet(() -> {
                        LOGGER.debug("Generating secret key for signer algorithm");

                        try {
                            return KeyGenerator.getInstance("HmacSHA256")
                                    .generateKey()
                                    .getEncoded();
                        } catch (NoSuchAlgorithmException e) {
                            throw new IllegalStateException(e);
                        }
                    });
            Algorithm signerAlgorithm = Algorithm.HMAC256(secretKey);

            webServer.accessManager(new JwtAccessManager(signerAlgorithm));

            webServer.routes(() -> registerEndpoint(ApiEndpoint.LOGIN,
                    new LoginHandler(accessControlConfig, signerAlgorithm)));
        } else {
            webServer.accessManager(new NoopAccessManager());
        }

        JavalinJackson.configure(new ObjectMapper().registerModule(new Jdk8Module()));
    }

    /**
     * Returns the configuration of the web API.
     *
     * @return the configuration of the web API
     */
    public WebApiConfiguration getWebApiConfiguration() {
        return webApiConfig;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onStart() {
        super.onStart();

        LOGGER.debug("Starting web server");
        LOGGER.debug("Using configuration: {}", webApiConfig);
        webServer.start();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onStop() {
        super.onStop();

        LOGGER.debug("Stopping web server");
        webServer.stop();
    }

    /**
     * Creates and configures a Jetty HTTP servlet server.
     *
     * @param webApiConfig the configuration of the web API
     *
     * @return the configured Jetty HTTP servlet server
     */
    private static Server createServer(final WebApiConfiguration webApiConfig) {
        Server server = new Server(new QueuedThreadPool(250, 8, 60_000));

        ServerConnector serverConnector = webApiConfig.getSslContextConfiguration()
                .map(sslContextConfig -> {
                    SslContextFactory sslContextFactory = new SslContextFactory();
                    sslContextFactory.setKeyStorePath(sslContextConfig.getKeyStorePath());
                    sslContextFactory.setKeyStorePassword(sslContextConfig.getKeyStorePassword());
                    sslContextConfig.getKeyManagerPassword()
                            .ifPresent(sslContextFactory::setKeyManagerPassword);

                    return new ServerConnector(server, sslContextFactory);
                })
                .orElseGet(() -> new ServerConnector(server));
        serverConnector.setPort(webApiConfig.getPort());

        server.addConnector(serverConnector);

        return server;
    }

    /**
     * Adds an endpoint to the web API.
     *
     * @param apiEndpoint the endpoint
     * @param handler     the handler of the endpoint
     */
    private static void registerEndpoint(final ApiEndpoint apiEndpoint, final Handler handler) {
        switch (apiEndpoint.getHttpMethod()) {
            case HEAD:
                ApiBuilder.head(apiEndpoint.getPath(), handler, apiEndpoint.getUserRoles());
                break;
            case GET:
                ApiBuilder.get(apiEndpoint.getPath(), handler, apiEndpoint.getUserRoles());
                break;
            case POST:
                ApiBuilder.post(apiEndpoint.getPath(), handler, apiEndpoint.getUserRoles());
                break;
            case PUT:
                ApiBuilder.put(apiEndpoint.getPath(), handler, apiEndpoint.getUserRoles());
                break;
            case PATCH:
                ApiBuilder.patch(apiEndpoint.getPath(), handler, apiEndpoint.getUserRoles());
                break;
            case DELETE:
                ApiBuilder.delete(apiEndpoint.getPath(), handler, apiEndpoint.getUserRoles());
                break;
            default:
                throw new IllegalArgumentException("Unsupported HTTP method");
        }
    }
}
