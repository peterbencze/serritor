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

package com.github.peterbencze.serritor.internal.web.http.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A DTO that is used by the client to transfer user authentication credentials to the web server.
 */
public final class LoginDto {

    private final String username;
    private final String password;

    /**
     * Creates a {@link LoginDto} instance.
     *
     * @param username the username provided by the user
     * @param password the password provided by the user
     */
    public LoginDto(
            @JsonProperty(value = "username", required = true) final String username,
            @JsonProperty(value = "password", required = true) final String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * Returns the username provided by the user.
     *
     * @return the username provided by the user
     */
    public String getUsername() {
        return username;
    }

    /**
     * Returns the password provided by the user.
     *
     * @return the password provided by the user
     */
    public String getPassword() {
        return password;
    }
}
