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

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Configuration of the access management.
 */
public final class AccessControlConfiguration {

    private final String authenticationPath;
    private final List<User> users;
    private final String secretKey;
    private final boolean isCookieAuthenticationEnabled;
    private final Duration jwtExpirationDuration;

    private AccessControlConfiguration(final AccessControlConfigurationBuilder builder) {
        authenticationPath = builder.authenticationPath;
        users = builder.users;
        secretKey = builder.secretKey;
        isCookieAuthenticationEnabled = builder.isCookieAuthenticationEnabled;
        jwtExpirationDuration = builder.jwtExpirationDuration;
    }

    /**
     * Returns the authentication path.
     *
     * @return the authentication path
     */
    public String getAuthenticationPath() {
        return authenticationPath;
    }

    /**
     * Returns the list of users who have access to the web API.
     *
     * @return the list of users who have access to the web API
     */
    public List<User> getUsers() {
        return users;
    }

    /**
     * Returns the secret key to be used for the JWT signing algorithm.
     *
     * @return the secret key to be used for the JWT signing algorithm
     */
    public Optional<String> getSecretKey() {
        return Optional.ofNullable(secretKey);
    }

    /**
     * Indicates if the JWT can be stored in a cookie.
     *
     * @return <code>true</code> if the JWT can be stored in a cookie, <code>false</code> otherwise
     */
    public boolean isCookieAuthenticationEnabled() {
        return isCookieAuthenticationEnabled;
    }

    /**
     * Returns the expiration duration of the JWT which is used to authenticate.
     *
     * @return the expiration duration of the JWT which is used to authenticate
     */
    public Duration getJwtExpirationDuration() {
        return jwtExpirationDuration;
    }

    /**
     * Returns a string representation of this access control configuration instance.
     *
     * @return a string representation of this access control configuration instance
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("authenticationPath", authenticationPath)
                .append("users", users)
                .append("isCookieAuthenticationEnabled", isCookieAuthenticationEnabled)
                .append("jwtExpirationDuration", jwtExpirationDuration)
                .toString();
    }

    /**
     * Builder for {@link AccessControlConfiguration}.
     */
    public static final class AccessControlConfigurationBuilder {

        private static final String DEFAULT_AUTHENTICATION_PATH = "/api/auth";
        private static final Duration DEFAULT_JWT_EXPIRATION_DURATION = Duration.ofHours(1);

        private final List<User> users;

        private String authenticationPath;
        private String secretKey;
        private boolean isCookieAuthenticationEnabled;
        private Duration jwtExpirationDuration;

        /**
         * Creates a {@link AccessControlConfigurationBuilder} instance.
         *
         * @param rootUser the root user
         */
        public AccessControlConfigurationBuilder(final User rootUser) {
            Validate.notNull(rootUser, "The rootUser parameter cannot be null");

            users = new ArrayList<>();
            users.add(rootUser);

            authenticationPath = DEFAULT_AUTHENTICATION_PATH;
            jwtExpirationDuration = DEFAULT_JWT_EXPIRATION_DURATION;
        }

        /**
         * Sets the authentication path. The default path is /api/auth.
         *
         * @param authenticationPath the authentication path
         *
         * @return the <code>AccessControlConfigurationBuilder</code> instance
         */
        public AccessControlConfigurationBuilder setAuthenticationPath(
                final String authenticationPath) {
            Validate.notBlank(authenticationPath,
                    "The authenticationPath parameter cannot be null or blank");

            this.authenticationPath = authenticationPath;
            return this;
        }

        /**
         * Adds a user to the list of users who have access to the web API.
         *
         * @param newUser the user to add
         *
         * @return the <code>AccessControlConfigurationBuilder</code> instance
         */
        public AccessControlConfigurationBuilder addUser(final User newUser) {
            Validate.notNull(newUser, "The newUser parameter cannot be null");
            Validate.isTrue(isUniqueUsername(newUser.getUsername()), "Username must be unique");

            users.add(newUser);
            return this;
        }

        /**
         * Sets the secret key to be used for the JWT signing algorithm. If it is not specified, a
         * default will be generated.
         *
         * @param secretKey the secret key
         *
         * @return the <code>AccessControlConfigurationBuilder</code> instance
         */
        public AccessControlConfigurationBuilder setSecretKey(final String secretKey) {
            Validate.notBlank(secretKey, "The secretKey parameter cannot be null or blank");

            this.secretKey = secretKey;
            return this;
        }

        /**
         * If enabled, the JWT will be stored in a cookie.
         *
         * @param isCookieAuthenticationEnabled <code>true</code> enables, <code>false</code>
         *                                      disables cookie authentication
         *
         * @return the <code>AccessControlConfigurationBuilder</code> instance
         */
        public AccessControlConfigurationBuilder setCookieAuthenticationEnabled(
                final boolean isCookieAuthenticationEnabled) {
            this.isCookieAuthenticationEnabled = isCookieAuthenticationEnabled;
            return this;
        }

        /**
         * Sets the expiration duration of the JWT which is used to authenticate.
         *
         * @param jwtExpirationDuration the expiration duration of the JWT
         *
         * @return the <code>AccessControlConfigurationBuilder</code> instance
         */
        public AccessControlConfigurationBuilder setJwtExpirationDuration(
                final Duration jwtExpirationDuration) {
            Validate.notNull(jwtExpirationDuration,
                    "The jwtExpirationDuration parameter cannot be null");
            Validate.isTrue(!jwtExpirationDuration.isZero(),
                    "The JWT expiration duration cannot be zero");
            Validate.isTrue(!jwtExpirationDuration.isNegative(),
                    "The JWT expiration duration cannot be negative");

            this.jwtExpirationDuration = jwtExpirationDuration;
            return this;
        }

        /**
         * Builds the configured <code>AccessControlConfiguration</code> instance.
         *
         * @return the configured <code>AccessControlConfiguration</code> instance
         */
        public AccessControlConfiguration build() {
            return new AccessControlConfiguration(this);
        }

        /**
         * Indicates if the provided username has not yet been associated with a previously added
         * user.
         *
         * @param username the username to check
         *
         * @return <code>true</code> if the username is free, <code>false</code> otherwise
         */
        private boolean isUniqueUsername(final String username) {
            return users.stream()
                    .noneMatch(user -> StringUtils.equalsIgnoreCase(user.getUsername(), username));
        }
    }
}
