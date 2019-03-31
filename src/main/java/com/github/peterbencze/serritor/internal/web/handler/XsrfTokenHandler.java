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

package com.github.peterbencze.serritor.internal.web.handler;

import com.github.peterbencze.serritor.internal.web.HttpMethod;
import io.javalin.Context;
import io.javalin.Handler;
import io.javalin.UnauthorizedResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * A before-handler that is responsible for the validation of the XSRF token header if an XSRF
 * cookie is present in the request.
 */
public final class XsrfTokenHandler implements Handler {

    static final String COOKIE_NAME = "XSRF-TOKEN";
    static final String HEADER_NAME = "X-XSRF-TOKEN";

    private static final List<HttpMethod> XSRF_SAFE_HTTP_METHODS
            = Arrays.asList(HttpMethod.HEAD, HttpMethod.GET, HttpMethod.OPTIONS, HttpMethod.TRACE);

    /**
     * Verifies that the XSRF token present in the cookie matches the one present in the header.
     *
     * @param ctx the context object
     */
    @Override
    public void handle(final Context ctx) throws Exception {
        HttpMethod requestMethod = HttpMethod.valueOf(ctx.method());
        if (XSRF_SAFE_HTTP_METHODS.contains(requestMethod)) {
            return;
        }

        Optional.ofNullable(ctx.cookie(COOKIE_NAME)).ifPresent(xsrfTokenInCookie -> {
            if (!xsrfTokenInCookie.equals(ctx.header(HEADER_NAME))) {
                throw new UnauthorizedResponse("XSRF token missing or incorrect");
            }
        });
    }
}
