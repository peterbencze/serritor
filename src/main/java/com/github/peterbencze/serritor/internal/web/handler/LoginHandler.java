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

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.github.peterbencze.serritor.api.web.AccessControlConfiguration;
import com.github.peterbencze.serritor.api.web.User;
import com.github.peterbencze.serritor.internal.web.dto.JwtDto;
import com.github.peterbencze.serritor.internal.web.dto.LoginDto;
import io.javalin.Context;
import io.javalin.Handler;
import io.javalin.UnauthorizedResponse;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import org.mindrot.jbcrypt.BCrypt;

/**
 * A handler that is used to verify the authentication credentials of the user.
 */
public final class LoginHandler implements Handler {

    private final AccessControlConfiguration accessControlConfig;
    private final Algorithm signerAlgorithm;

    /**
     * Creates a {@link LoginHandler} instance.
     *
     * @param accessControlConfig the access control configuration
     * @param signerAlgorithm     the algorithm used for signing JWTs
     */
    public LoginHandler(
            final AccessControlConfiguration accessControlConfig,
            final Algorithm signerAlgorithm) {
        this.accessControlConfig = accessControlConfig;
        this.signerAlgorithm = signerAlgorithm;
    }

    /**
     * Verifies the authentication credentials of the user.
     *
     * @param ctx the context object
     */
    @Override
    public void handle(final Context ctx) throws Exception {
        LoginDto loginDto = ctx.bodyAsClass(LoginDto.class);

        User user = accessControlConfig.getUser(loginDto.getUsername())
                .orElseThrow(UnauthorizedResponse::new);

        if (!BCrypt.checkpw(loginDto.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedResponse();
        }

        Duration tokenValidDuration = Duration.ofHours(1);
        Date expiryDate = Date.from(Instant.now().plus(tokenValidDuration));
        String jwt = JWT.create()
                .withExpiresAt(expiryDate)
                .withClaim("username", user.getUsername())
                .sign(signerAlgorithm);

        if (accessControlConfig.isCookieAuthenticationEnabled()) {
            int cookieAgeInSeconds = Math.toIntExact(tokenValidDuration.getSeconds());

            ctx.cookie(JwtHandler.COOKIE_NAME, jwt, cookieAgeInSeconds);
            ctx.cookie(XsrfTokenHandler.COOKIE_NAME, generateXsrfToken(), cookieAgeInSeconds);
        } else {
            ctx.json(new JwtDto(user.getUsername(), expiryDate, jwt));
        }
    }

    /**
     * Generates a random 128-bit XSRF token.
     *
     * @return the generated XSRF token
     */
    private static String generateXsrfToken() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] randomBytes = new byte[16];

        secureRandom.nextBytes(randomBytes);

        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
}
