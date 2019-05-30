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
 * A DTO that is used by the web server to transfer error details to the client.
 */
public final class ErrorDto {

    private final int code;
    private final String message;

    /**
     * Creates a {@link ErrorDto} instance.
     *
     * @param code    the HTTP status code
     * @param message the HTTP status message
     */
    public ErrorDto(
            @JsonProperty(value = "code", required = true) final int code,
            @JsonProperty(value = "message", required = true) final String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * Returns the HTTP status code.
     *
     * @return the HTTP status code
     */
    public int getCode() {
        return code;
    }

    /**
     * Returns the HTTP status message.
     *
     * @return the HTTP status message
     */
    public String getMessage() {
        return message;
    }
}
