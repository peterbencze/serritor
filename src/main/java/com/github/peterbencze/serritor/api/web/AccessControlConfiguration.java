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
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

/**
 * Configuration of the access management.
 */
public final class AccessControlConfiguration {

    private final List<User> users;
    private final byte[] secretKey;
    private final boolean isCookieAuthenticationEnabled;

    private AccessControlConfiguration(final AccessControlConfigurationBuilder builder) {
        users = builder.users;
        secretKey = builder.secretKey;
        isCookieAuthenticationEnabled = builder.isCookieAuthenticationEnabled;
    }

    /**
     * Returns the user with the given username.
     *
     * @param username the username of the user
     *
     * @return the user with the given username
     */
    public Optional<User> getUser(final String username) {
        return users.stream()
                .filter(user -> StringUtils.equalsIgnoreCase(user.getUsername(), username))
                .findFirst();
    }

    /**
     * Returns the secret key to be used for the JWT signing algorithm.
     *
     * @return the secret key to be used for the JWT signing algorithm
     */
    public Optional<byte[]> getSecretKey() {
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
     * Builder for {@link AccessControlConfiguration}.
     */
    public static final class AccessControlConfigurationBuilder {

        private final List<User> users;

        private byte[] secretKey;
        private boolean isCookieAuthenticationEnabled;

        /**
         * Creates a {@link AccessControlConfigurationBuilder} instance.
         *
         * @param rootUser the root user
         */
        public AccessControlConfigurationBuilder(final User rootUser) {
            Validate.notNull(rootUser, "The rootUser parameter cannot be null");

            users = new ArrayList<>();
            users.add(rootUser);
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
        public AccessControlConfigurationBuilder setSecretKey(final Byte[] secretKey) {
            Validate.notEmpty(secretKey, "The secretKey parameter cannot be empty");
            this.secretKey = ArrayUtils.toPrimitive(secretKey);

            return this;
        }

        /**
         * If enabled, the JWT will be stored in a cookie.
         *
         * @return the <code>AccessControlConfigurationBuilder</code> instance
         */
        public AccessControlConfigurationBuilder enableCookieAuthentication() {
            isCookieAuthenticationEnabled = true;
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
