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

import io.javalin.Javalin;
import io.javalin.apibuilder.ApiBuilder;

/**
 * Provides a skeletal implementation of a crawler to minimize the effort for users to implement
 * their own. It also exposes a REST API that can be used to interact with the crawler while it is
 * running.
 *
 * @author Peter Bencze
 */
public abstract class RestfulBaseCrawler extends BaseCrawler {

    private final RestServerConfiguration config;
    private final Javalin restServer;

    /**
     * Base constructor which sets up the crawler with the provided configuration. The REST server
     * is initialized with the default settings.
     *
     * @param config the configuration of the crawler
     */
    protected RestfulBaseCrawler(final CrawlerConfiguration config) {
        this(RestServerConfiguration.createDefault(), config);
    }

    /**
     * Base constructor that sets up the REST server and the crawler with the provided
     * configurations.
     *
     * @param restServerConfig the configuration of the REST server
     * @param crawlerConfig    the configuration of the crawler
     */
    protected RestfulBaseCrawler(final RestServerConfiguration restServerConfig,
                                 final CrawlerConfiguration crawlerConfig) {
        this(restServerConfig, new CrawlerState(crawlerConfig));
    }

    /**
     * Base constructor which restores the crawler to the provided state. The REST server is
     * initialized with the default settings.
     *
     * @param state the state to restore the crawler to
     */
    protected RestfulBaseCrawler(final CrawlerState state) {
        this(RestServerConfiguration.createDefault(), state);
    }

    /**
     * Base constructor that sets up the REST server with the provided configuration and restores
     * the crawler to the provided state.
     *
     * @param config the configuration of the REST server
     * @param state  the state to restore the crawler to
     */
    protected RestfulBaseCrawler(final RestServerConfiguration config, final CrawlerState state) {
        super(state);

        this.config = config;
        restServer = Javalin.create();

        configureRoutes();
    }

    /**
     * Returns the configuration of the REST server.
     *
     * @return the configuration of the REST server
     */
    public final RestServerConfiguration getRestServerConfiguration() {
        return config;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onStart() {
        super.onStart();

        restServer.start(config.getPort());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onStop() {
        super.onStop();

        restServer.stop();
    }

    /**
     * Sets up the routes.
     */
    private void configureRoutes() {
        restServer.routes(() -> {
            ApiBuilder.path("api", () -> {
                ApiBuilder.path("crawler", () -> {
                    ApiBuilder.delete(ctx -> stop());

                    ApiBuilder.get("config", ctx -> ctx.json(getCrawlerConfiguration()));
                });
            });
        });
    }
}
