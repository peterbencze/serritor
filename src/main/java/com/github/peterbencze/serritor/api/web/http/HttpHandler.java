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

package com.github.peterbencze.serritor.api.web.http;

import java.io.IOException;
import java.io.Serializable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Common interface of the HTTP endpoint handlers.
 */
@FunctionalInterface
public interface HttpHandler extends Serializable {

    /**
     * Handles the incoming HTTP request.
     *
     * @param request  an object to provide client request information to the handler
     * @param response an object to assist the handler in sending a response to the client
     *
     * @throws IOException if an error occurs in the handler
     */
    void handle(HttpServletRequest request, HttpServletResponse response) throws IOException;
}
