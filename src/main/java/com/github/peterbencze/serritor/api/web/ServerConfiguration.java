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

import com.github.peterbencze.serritor.api.web.http.HttpMethod;
import com.google.common.collect.ImmutableSet;
import com.google.common.net.HttpHeaders;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Configuration for the web server.
 */
public final class ServerConfiguration {

    private final int port;
    private final Set<String> corsAllowedOrigins;
    private final Set<String> corsAllowedMethods;
    private final Set<String> corsAllowedHeaders;
    private final Set<String> corsExposedHeaders;
    private final SslContextConfiguration sslContextConfig;

    private ServerConfiguration(final ServerConfigurationBuilder builder) {
        port = builder.port;
        corsAllowedOrigins = builder.corsAllowedOrigins;
        corsAllowedMethods = builder.corsAllowedMethods;
        corsAllowedHeaders = builder.corsAllowedHeaders;
        corsExposedHeaders = builder.corsExposedHeaders;
        sslContextConfig = builder.sslContextConfig;
    }

    /**
     * Creates the default configuration of the web server.
     *
     * @return the default configuration of the web server
     */
    public static ServerConfiguration createDefault() {
        return new ServerConfigurationBuilder().build();
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
     * Returns the set of origins that are allowed to access the resources.
     *
     * @return the set of origins that are allowed to access the resources
     */
    public Set<String> getCorsAllowedOrigins() {
        return corsAllowedOrigins;
    }

    /**
     * Returns the set of HTTP methods that are allowed to be used when accessing the resources.
     *
     * @return the set of HTTP methods that are allowed to be used when accessing the resources
     */
    public Set<String> getCorsAllowedMethods() {
        return corsAllowedMethods;
    }

    /**
     * Returns the set of HTTP headers that are allowed to be specified when accessing the
     * resources.
     *
     * @return the set of HTTP headers that are allowed to be specified when accessing the
     *         resources
     */
    public Set<String> getCorsAllowedHeaders() {
        return corsAllowedHeaders;
    }

    /**
     * Returns the set of HTTP headers that are allowed to be exposed on the client.
     *
     * @return the set of HTTP headers that are allowed to be exposed on the client
     */
    public Set<String> getCorsExposedHeaders() {
        return corsExposedHeaders;
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
     * Returns a string representation of this web server configuration.
     *
     * @return a string representation of this web server configuration
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("port", port)
                .append("corsAllowedOrigins", corsAllowedOrigins)
                .append("corsAllowedMethods", corsAllowedMethods)
                .append("corsAllowedHeaders", corsAllowedHeaders)
                .append("corsExposedHeaders", corsExposedHeaders)
                .append("sslContextConfiguration", sslContextConfig)
                .toString();
    }

    /**
     * Builder for {@link ServerConfiguration}.
     */
    public static final class ServerConfigurationBuilder {

        private static final int DEFAULT_PORT = 8080;

        private static final Set<String> DEFAULT_CORS_ALLOWED_ORIGINS = ImmutableSet.of("*");
        private static final Set<String> DEFAULT_CORS_ALLOWED_METHODS =
                ImmutableSet.of(HttpMethod.GET.toString(), HttpMethod.POST.toString(),
                        HttpMethod.HEAD.toString());
        private static final Set<String> DEFAULT_CORS_ALLOWED_HEADERS =
                ImmutableSet.of(HttpHeaders.X_REQUESTED_WITH, HttpHeaders.CONTENT_TYPE,
                        HttpHeaders.ACCEPT, HttpHeaders.ORIGIN);
        private static final Set<String> DEFAULT_CORS_EXPOSED_HEADERS = Collections.emptySet();

        private int port;
        private Set<String> corsAllowedOrigins;
        private Set<String> corsAllowedMethods;
        private Set<String> corsAllowedHeaders;
        private Set<String> corsExposedHeaders;
        private SslContextConfiguration sslContextConfig;

        /**
         * Creates a {@link ServerConfigurationBuilder} instance.
         */
        public ServerConfigurationBuilder() {
            port = DEFAULT_PORT;
            corsAllowedOrigins = DEFAULT_CORS_ALLOWED_ORIGINS;
            corsAllowedMethods = DEFAULT_CORS_ALLOWED_METHODS;
            corsAllowedHeaders = DEFAULT_CORS_ALLOWED_HEADERS;
            corsExposedHeaders = DEFAULT_CORS_EXPOSED_HEADERS;
        }

        /**
         * Sets the port number to be used by the web server.
         *
         * @param port the port number to use
         *
         * @return the <code>ServerConfigurationBuilder</code> instance
         */
        public ServerConfigurationBuilder setPort(final int port) {
            this.port = port;
            return this;
        }

        /**
         * Sets the set of origins that are allowed to access the resources. All origins (*) are
         * allowed by default.
         *
         * @param allowedOrigins the set of allowed origins
         *
         * @return the <code>ServerConfigurationBuilder</code> instance
         */
        public ServerConfigurationBuilder setCorsAllowedOrigins(final Set<String> allowedOrigins) {
            Validate.notEmpty(allowedOrigins,
                    "The allowedOrigins parameter cannot be null or empty");
            Validate.noNullElements(allowedOrigins,
                    "The allowedOrigins parameter cannot contain null elements");

            corsAllowedOrigins = allowedOrigins;
            return this;
        }

        /**
         * Sets the set of HTTP methods that are allowed to be used when accessing the resources.
         * The default methods are: GET, POST and HEAD.
         *
         * @param allowedMethods the set of allowed HTTP methods
         *
         * @return the <code>ServerConfigurationBuilder</code> instance
         */
        public ServerConfigurationBuilder setCorsAllowedMethods(final Set<String> allowedMethods) {
            Validate.notEmpty(allowedMethods,
                    "The allowedMethods parameter cannot be null or empty");
            Validate.noNullElements(allowedMethods,
                    "The allowedMethods parameter cannot contain null elements");

            corsAllowedMethods = allowedMethods;
            return this;
        }

        /**
         * Sets the set of HTTP headers that are allowed to be specified when accessing the
         * resources. The default headers are X-Requested-With, Content-Type, Accept and Origin.
         *
         * @param allowedHeaders the set of allowed HTTP headers
         *
         * @return the <code>ServerConfigurationBuilder</code> instance
         */
        public ServerConfigurationBuilder setCorsAllowedHeaders(final Set<String> allowedHeaders) {
            Validate.notNull(allowedHeaders,
                    "The allowedHeaders parameter cannot be null or empty");
            Validate.noNullElements(allowedHeaders,
                    "The allowedHeaders parameter cannot contain null elements");

            corsAllowedHeaders = allowedHeaders;
            return this;
        }

        /**
         * Sets the set of HTTP headers that are allowed to be exposed on the client. No headers are
         * exposed by default.
         *
         * @param exposedHeaders the set of exposed HTTP headers
         *
         * @return the <code>ServerConfigurationBuilder</code> instance
         */
        public ServerConfigurationBuilder setCorsExposedHeaders(final Set<String> exposedHeaders) {
            Validate.notEmpty(exposedHeaders,
                    "The exposedHeaders parameter cannot be null or empty");
            Validate.noNullElements(exposedHeaders,
                    "The exposedHeaders parameter cannot contain null elements");

            corsExposedHeaders = exposedHeaders;
            return this;
        }

        /**
         * Enables the use of SSL.
         *
         * @param sslContextConfig the SSL context configuration
         *
         * @return the <code>ServerConfigurationBuilder</code> instance
         */
        public ServerConfigurationBuilder withSsl(final SslContextConfiguration sslContextConfig) {
            Validate.notNull(sslContextConfig, "The sslContextConfig parameter cannot be null");

            this.sslContextConfig = sslContextConfig;
            return this;
        }

        /**
         * Builds the configured <code>ServerConfiguration</code> instance.
         *
         * @return the configured <code>ServerConfiguration</code> instance
         */
        public ServerConfiguration build() {
            return new ServerConfiguration(this);
        }
    }
}
