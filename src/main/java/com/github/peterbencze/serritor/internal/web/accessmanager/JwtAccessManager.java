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

package com.github.peterbencze.serritor.internal.web.accessmanager;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.github.peterbencze.serritor.internal.web.UserRole;
import com.github.peterbencze.serritor.internal.web.handler.JwtHandler;
import io.javalin.Context;
import io.javalin.Handler;
import io.javalin.UnauthorizedResponse;
import io.javalin.security.AccessManager;
import io.javalin.security.Role;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A JWT-based access manager.
 */
public final class JwtAccessManager implements AccessManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtAccessManager.class);

    private final Algorithm signerAlgorithm;

    /**
     * Creates a {@link JwtAccessManager} instance.
     *
     * @param signerAlgorithm the algorithm used for signing JWTs
     */
    public JwtAccessManager(final Algorithm signerAlgorithm) {
        this.signerAlgorithm = signerAlgorithm;
    }

    /**
     * Checks if the user is allowed to access the specific endpoint.
     *
     * @param handler        the request handler
     * @param ctx            the context object
     * @param permittedRoles the set of permitted roles
     */
    @Override
    public void manage(
            final Handler handler,
            final Context ctx,
            final Set<Role> permittedRoles) throws Exception {
        LOGGER.debug("Incoming request from {} to path {}", ctx.ip(), ctx.path());

        if (!permittedRoles.contains(UserRole.UNAUTHENTICATED)) {
            LOGGER.debug("Checking JWT");

            String jwt = ctx.attribute(JwtHandler.CONTEXT_ATTRIBUTE_NAME);
            if (jwt == null) {
                LOGGER.debug("Returning unauthorized response: no JWT present in context");

                throw new UnauthorizedResponse();
            }

            JWTVerifier verifier = JWT.require(signerAlgorithm).build();
            try {
                verifier.verify(jwt);
                LOGGER.debug("JWT verified");
            } catch (JWTVerificationException e) {
                LOGGER.debug("Returning unauthorized response: JWT verification failed");

                throw new UnauthorizedResponse();
            }
        }

        LOGGER.debug("Letting request through");
        handler.handle(ctx);
    }
}
