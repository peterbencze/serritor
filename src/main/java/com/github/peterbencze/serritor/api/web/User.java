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

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Represents a user of the web API.
 */
public final class User {

    private final String username;
    private final String passwordHash;

    /**
     * Creates a {@link User} instance.
     *
     * @param username     the unique username of the user
     * @param passwordHash the BCrypt hash of the user's password
     */
    public User(final String username, final String passwordHash) {
        Validate.notBlank(username, "The username parameter cannot be null or blank");
        Validate.notBlank(passwordHash, "The passwordHash parameter cannot be null or blank");
        Validate.isTrue(isSupportedSaltVersion(passwordHash),
                "Unsupported BCrypt salt version (only $2$ or $2a$ are supported)");

        this.username = username;
        this.passwordHash = passwordHash;
    }

    /**
     * Returns the username of the user.
     *
     * @return the username of the user
     */
    public String getUsername() {
        return username;
    }

    /**
     * Returns the BCrypt hash of the user's password.
     *
     * @return the BCrypt hash of the user's password
     */
    public String getPasswordHash() {
        return passwordHash;
    }

    /**
     * Returns a string representation of this user instance.
     *
     * @return a string representation of this user instance
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("username", username)
                .toString();
    }

    /**
     * Determines if the version of the BCrypt algorithm used to create the hash is supported.
     *
     * @param passwordHash the BCrypt hash
     *
     * @return <code>true</code> if the version is supported, <code>false</code> otherwise
     */
    private static boolean isSupportedSaltVersion(final String passwordHash) {
        return passwordHash.startsWith("$2$") || passwordHash.startsWith("$2a$");
    }
}
