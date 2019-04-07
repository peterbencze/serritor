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

import io.javalin.Context;
import io.javalin.Handler;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A before-handler which extracts the JWT from the Authorization header or the cookie.
 */
public final class JwtHandler implements Handler {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtHandler.class);

    public static final String CONTEXT_ATTRIBUTE_NAME = "JWT";
    static final String COOKIE_NAME = "JWT";

    /**
     * Extracts JWT from the Authorization header or the cookie.
     *
     * @param ctx the context object
     */
    @Override
    public void handle(final Context ctx) {
        Optional<String> jwtFromHeaderOpt = extractJwtFromHeader(ctx);
        if (jwtFromHeaderOpt.isPresent()) {
            LOGGER.debug("JWT found in headers");

            ctx.attribute(CONTEXT_ATTRIBUTE_NAME, jwtFromHeaderOpt.get());
        } else {
            extractJwtFromCookie(ctx).ifPresent(jwt -> {
                LOGGER.debug("JWT found in cookies");

                ctx.attribute(CONTEXT_ATTRIBUTE_NAME, jwt);
            });
        }
    }

    /**
     * Returns the JWT from the Authorization header.
     *
     * @param ctx the context object
     *
     * @return the JWT from the Authorization header
     */
    private static Optional<String> extractJwtFromHeader(final Context ctx) {
        return Optional.ofNullable(ctx.header("Authorization"))
                .flatMap(header -> {
                    String[] headerValueParts = header.split(" ");
                    if (headerValueParts.length != 2 || !"Bearer".equals(headerValueParts[0])) {
                        return Optional.empty();
                    }

                    return Optional.of(headerValueParts[1]);
                });
    }

    /**
     * Returns the JWT from the cookie.
     *
     * @param ctx the context object
     *
     * @return the JWT from the cookie
     */
    private static Optional<String> extractJwtFromCookie(final Context ctx) {
        return Optional.ofNullable(ctx.cookie(COOKIE_NAME));
    }
}
