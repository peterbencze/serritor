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

package com.github.peterbencze.serritor.internal.web.dto;

import java.util.Date;

/**
 * A DTO that is used to send back the generated JWT to the user.
 */
public final class JwtDto {

    private final String username;
    private final Date expiryDate;
    private final String jwt;

    /**
     * Creates a {@link JwtDto} instance.
     *
     * @param username   the username of the authenticated user
     * @param expiryDate the expiry date of the JWT
     * @param jwt        the generated JWT
     */
    public JwtDto(final String username, final Date expiryDate, final String jwt) {
        this.username = username;
        this.expiryDate = expiryDate;
        this.jwt = jwt;
    }

    /**
     * Returns the username of the authenticated user.
     *
     * @return the username of the authenticated user
     */
    public String getUsername() {
        return username;
    }

    /**
     * Returns the expiry date of the JWT.
     *
     * @return the expiry date of the JWT
     */
    public Date getExpiryDate() {
        return expiryDate;
    }

    /**
     * Returns the generated JWT.
     *
     * @return the generated JWT
     */
    public String getJwt() {
        return jwt;
    }
}
