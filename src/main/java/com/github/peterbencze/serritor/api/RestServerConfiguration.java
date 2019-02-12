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

/**
 * Contains the settings of the REST server.
 *
 * @author Peter Bencze
 */
public final class RestServerConfiguration {

    private final int port;

    private RestServerConfiguration(final RestServerConfigurationBuilder builder) {
        port = builder.port;
    }

    /**
     * Creates a default configuration for the REST server.
     *
     * @return a default configuration for the REST server
     */
    public static RestServerConfiguration createDefault() {
        return new RestServerConfigurationBuilder().build();
    }

    /**
     * Returns the port of the REST server.
     *
     * @return the port of the REST server
     */
    public int getPort() {
        return port;
    }

    /**
     * Builds {@link RestServerConfiguration} instances.
     */
    public static final class RestServerConfigurationBuilder {

        private static final int DEFAULT_PORT = 8080;

        private int port;

        /**
         * Creates a {@link RestServerConfigurationBuilder} instance.
         */
        public RestServerConfigurationBuilder() {
            port = DEFAULT_PORT;
        }

        /**
         * Sets the port of the REST server.
         *
         * @param port the port number
         *
         * @return the <code>RestServerConfigurationBuilder</code> instance
         */
        public RestServerConfigurationBuilder setPort(final int port) {
            this.port = port;
            return this;
        }

        /**
         * Builds the configured <code>RestServerConfiguration</code> instance.
         *
         * @return the configured <code>RestServerConfiguration</code> instance
         */
        public RestServerConfiguration build() {
            return new RestServerConfiguration(this);
        }
    }
}
