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

import com.github.peterbencze.serritor.internal.web.http.auth.BCryptCredential;
import java.util.Collections;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Represents a user of the web API.
 */
public final class User {

    private final String username;
    private final String password;
    private final Set<String> roles;

    /**
     * Creates a {@link User} instance.
     *
     * @param username the unique username of the user
     * @param password the BCrypt hash of the user's password
     * @param roles    the roles associated with the user
     */
    public User(final String username, final String password, final Set<String> roles) {
        Validate.notBlank(username, "The username parameter cannot be null or blank");
        Validate.notBlank(password, "The password parameter cannot be null or blank");

        if (password.startsWith(BCryptCredential.PREFIX)) {
            Validate.isTrue(isSupportedSaltVersion(password),
                    "Unsupported BCrypt salt version (only $2$ or $2a$ are supported)");
        }

        Validate.noNullElements(roles,
                "The roles parameter cannot be null or contain null elements");

        this.username = username;
        this.password = password;
        this.roles = roles;
    }

    /**
     * Creates a {@link User} instance.
     *
     * @param username the unique username of the user
     * @param password the BCrypt hash of the user's password
     */
    public User(final String username, final String password) {
        this(username, password, Collections.emptySet());
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
    public String getPassword() {
        return password;
    }

    /**
     * Returns the roles of the user.
     *
     * @return the roles of the user
     */
    public Set<String> getRoles() {
        return roles;
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
                .append("roles", roles)
                .toString();
    }

    /**
     * Determines if the version of the BCrypt algorithm used to create the hash is supported.
     *
     * @param password the BCrypt hash
     *
     * @return <code>true</code> if the version is supported, <code>false</code> otherwise
     */
    private static boolean isSupportedSaltVersion(final String password) {
        String passwordWithoutPrefix = StringUtils.removeStart(password, BCryptCredential.PREFIX);
        return StringUtils.startsWithAny(passwordWithoutPrefix, "$2$", "$2a$");
    }
}
