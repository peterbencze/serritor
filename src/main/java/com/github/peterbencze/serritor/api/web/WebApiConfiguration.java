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

package com.github.peterbencze.serritor.api.web;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.Validate;

/**
 * Configuration for the web API.
 */
public final class WebApiConfiguration {

    private final int port;
    private final List<String> corsOrigins;
    private final SslContextConfiguration sslContextConfig;
    private final AccessControlConfiguration accessControlConfig;

    private WebApiConfiguration(final WebApiConfigurationBuilder builder) {
        port = builder.port;
        corsOrigins = builder.corsOrigins;
        sslContextConfig = builder.sslContextConfig;
        accessControlConfig = builder.accessControlConfig;
    }

    /**
     * Returns the default configuration of the web API.
     *
     * @return the default configuration of the web API
     */
    public static WebApiConfiguration createDefault() {
        return new WebApiConfigurationBuilder().build();
    }

    /**
     * Returns the port number used by the web server.
     *
     * @return the port number used by the web server
     */
    public int getPort() {
        return port;
    }

    /**
     * Returns the list of allowed CORS origins.
     *
     * @return the list of allowed CORS origins
     */
    public List<String> getCorsOrigins() {
        return corsOrigins;
    }

    /**
     * Returns the SSL context configuration.
     *
     * @return the SSL context configuration
     */
    public Optional<SslContextConfiguration> getSslContextConfiguration() {
        return Optional.ofNullable(sslContextConfig);
    }

    /**
     * Returns the access control configuration.
     *
     * @return the access control configuration
     */
    public Optional<AccessControlConfiguration> getAccessControlConfiguration() {
        return Optional.ofNullable(accessControlConfig);
    }

    /**
     * Builder for {@link WebApiConfiguration}.
     */
    public static final class WebApiConfigurationBuilder {

        private static final int DEFAULT_PORT = 8080;

        private final List<String> corsOrigins;

        private int port;
        private SslContextConfiguration sslContextConfig;
        private AccessControlConfiguration accessControlConfig;

        /**
         * Creates a {@link WebApiConfigurationBuilder} instance.
         */
        public WebApiConfigurationBuilder() {
            corsOrigins = new ArrayList<>();
            port = DEFAULT_PORT;
        }

        /**
         * Sets the port number to be used by the web server.
         *
         * @param port the port number to use
         *
         * @return the <code>WebApiConfigurationBuilder</code> instance
         */
        public WebApiConfigurationBuilder setPort(final int port) {
            this.port = port;
            return this;
        }

        /**
         * Configures the web server to accept cross origin requests for the specific origin. The
         * wildcard symbol "*" can be used to enable CORS for all origins.
         *
         * @param origin the origin from which the server should accept cross origin requests
         *
         * @return the <code>WebApiConfigurationBuilder</code> instance
         */
        public WebApiConfigurationBuilder enableCorsForOrigin(final String origin) {
            corsOrigins.add(origin);
            return this;
        }

        /**
         * Enables the use of SSL.
         *
         * @param sslContextConfig the SSL context configuration
         *
         * @return the <code>WebApiConfigurationBuilder</code> instance
         */
        public WebApiConfigurationBuilder withSsl(final SslContextConfiguration sslContextConfig) {
            Validate.notNull(sslContextConfig, "The sslContextConfig parameter cannot be null");

            this.sslContextConfig = sslContextConfig;
            return this;
        }

        /**
         * Enables access control.
         *
         * @param accessControlConfig the access control configuration
         *
         * @return the <code>WebApiConfigurationBuilder</code> instance
         */
        public WebApiConfigurationBuilder withAccessControl(
                final AccessControlConfiguration accessControlConfig) {
            Validate.notNull(accessControlConfig,
                    "The accessControlConfig parameter cannot be null");

            this.accessControlConfig = accessControlConfig;
            return this;
        }

        /**
         * Builds the configured <code>WebApiConfiguration</code> instance.
         *
         * @return the configured <code>WebApiConfiguration</code> instance
         */
        public WebApiConfiguration build() {
            return new WebApiConfiguration(this);
        }
    }
}
