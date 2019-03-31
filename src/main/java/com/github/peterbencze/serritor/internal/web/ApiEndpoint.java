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

package com.github.peterbencze.serritor.internal.web;

import io.javalin.security.Role;
import java.util.Collections;
import java.util.Set;

/**
 * Represents an endpoint of the web API.
 */
public enum ApiEndpoint {

    LOGIN(HttpMethod.POST, "api/auth", Collections.singleton(UserRole.UNAUTHENTICATED)),
    STOP_CRAWLER(HttpMethod.DELETE, "api/crawler", Collections.singleton(UserRole.AUTHENTICATED)),
    GET_CONFIG(HttpMethod.GET, "api/crawler/config", Collections.singleton(UserRole.AUTHENTICATED)),
    GET_STATS(HttpMethod.GET, "api/crawler/stats", Collections.singleton(UserRole.AUTHENTICATED));

    private final HttpMethod httpMethod;
    private final String path;
    private final Set<Role> userRoles;

    ApiEndpoint(final HttpMethod httpMethod, final String path, final Set<Role> userRoles) {
        this.httpMethod = httpMethod;
        this.path = path;
        this.userRoles = userRoles;
    }

    /**
     * Returns the HTTP method associated with the endpoint.
     *
     * @return the HTTP method associated with the endpoint
     */
    public HttpMethod getHttpMethod() {
        return httpMethod;
    }

    /**
     * Returns the path of the endpoint.
     *
     * @return the path of the endpoint
     */
    public String getPath() {
        return path;
    }

    /**
     * Returns the user roles associated with the endpoint.
     *
     * @return the user roles associated with the endpoint
     */
    public Set<Role> getUserRoles() {
        return userRoles;
    }
}
