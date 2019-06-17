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

package com.github.peterbencze.serritor.internal.web.http.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.github.peterbencze.serritor.api.helper.JsonReaderWriter;
import com.github.peterbencze.serritor.api.web.AccessControlConfiguration;
import com.github.peterbencze.serritor.internal.util.KeyFactory;
import com.github.peterbencze.serritor.internal.web.http.CsrfFilter;
import com.github.peterbencze.serritor.internal.web.http.dto.JwtDto;
import com.github.peterbencze.serritor.internal.web.http.dto.LoginDto;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.security.auth.Subject;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.MimeTypes.Type;
import org.eclipse.jetty.security.AbstractLoginService.RolePrincipal;
import org.eclipse.jetty.security.Authenticator;
import org.eclipse.jetty.security.LoginService;
import org.eclipse.jetty.security.ServerAuthException;
import org.eclipse.jetty.security.UserAuthentication;
import org.eclipse.jetty.server.Authentication;
import org.eclipse.jetty.server.Authentication.User;
import org.eclipse.jetty.server.UserIdentity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An authentication implementation that authenticate users by JWTs.
 */
public final class JwtAuthenticator implements Authenticator {

    public static final String AUTH_COOKIE_NAME = "JWT";
    public static final int CSRF_TOKEN_BYTE_SIZE = 16; // 128-bit token

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtAuthenticator.class);

    private static final String AUTH_METHOD = "JWT";

    private static final String KEY_GENERATOR_ALGORITHM = "HmacSHA256";

    private static final String AUTH_QUERY_PARAM_NAME = "access_token";

    private static final String AUTH_HEADER_VALUE_PREFIX = "Bearer ";

    private final AccessControlConfiguration accessControlConfig;
    private final Pattern authPathPattern;
    private final Algorithm signerAlgorithm;

    private LoginService loginService;

    /**
     * Creates a {@link JwtAuthenticator} instance.
     *
     * @param accessControlConfig the access control configuration
     */
    public JwtAuthenticator(final AccessControlConfiguration accessControlConfig) {
        this.accessControlConfig = accessControlConfig;

        String regex = String.format("^%s[/?#]?.*$", accessControlConfig.getAuthenticationPath());
        this.authPathPattern = Pattern.compile(regex);

        String secretKey = accessControlConfig.getSecretKey()
                .orElseGet(() -> {
                    LOGGER.debug("Generating secret key for JWT signer algorithm");

                    return KeyFactory.createKey(KEY_GENERATOR_ALGORITHM);
                });
        signerAlgorithm = Algorithm.HMAC256(secretKey);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setConfiguration(final AuthConfiguration authConfig) {
        loginService = Optional.ofNullable(authConfig.getLoginService())
                .orElseThrow(() -> new IllegalStateException("Login service is not set"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAuthMethod() {
        return AUTH_METHOD;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void prepareRequest(final ServletRequest request) {
        // Nothing to do
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Authentication validateRequest(
            final ServletRequest servletRequest,
            final ServletResponse servletResponse,
            final boolean mandatory) throws ServerAuthException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;

        LOGGER.info("Incoming request from IP {} to access path {}",
                httpServletRequest.getRemoteAddr(), httpServletRequest.getRequestURI());

        try {
            if (isAuthenticationRequest(httpServletRequest.getRequestURI())) {
                return authenticateWithCredentials(httpServletRequest, httpServletResponse);
            }

            Optional<String> jwtOpt = extractJwtFromRequest(httpServletRequest,
                    accessControlConfig.isCookieAuthenticationEnabled());
            if (jwtOpt.isPresent()) {
                try {
                    return authenticateWithJwt(jwtOpt.get());
                } catch (JWTVerificationException e) {
                    LOGGER.info("Failed authentication: JWT verification error");

                    httpServletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    return Authentication.SEND_FAILURE;
                }
            } else {
                LOGGER.info("Failed authentication: no JWT found in request");

                httpServletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                return Authentication.SEND_FAILURE;
            }
        } catch (IOException e) {
            LOGGER.error("Error occurred during authentication: {}", e.getMessage());

            throw new ServerAuthException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean secureResponse(
            final ServletRequest request,
            final ServletResponse response,
            final boolean mandatory,
            final User validatedUser) {
        return true;
    }

    /**
     * Authenticates the user by credentials (username and password).
     *
     * @param request  the request
     * @param response the response
     *
     * @return an authentication
     * @throws IOException if an I/O error occurs during the authentication
     */
    private Authentication authenticateWithCredentials(
            final HttpServletRequest request,
            final HttpServletResponse response) throws IOException {
        try {
            LoginDto loginDto = JsonReaderWriter.readJsonRequest(request, LoginDto.class);

            String username = loginDto.getUsername();

            UserIdentity userIdentity = loginService.login(username, loginDto.getPassword(),
                    request);
            if (userIdentity == null) {
                LOGGER.info("Failed authentication for user {}: wrong credentials", username);

                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                return Authentication.SEND_FAILURE;
            }

            LOGGER.info("User {} successfully authenticated (with credentials)", username);

            Set<String> userRoles = userIdentity.getSubject()
                    .getPrincipals(RolePrincipal.class)
                    .stream()
                    .map(RolePrincipal::getName)
                    .collect(Collectors.toSet());

            Instant expiryInstant = Instant.now()
                    .plus(accessControlConfig.getJwtExpirationDuration())
                    .truncatedTo(ChronoUnit.SECONDS);
            Date expiryDate = Date.from(expiryInstant);

            String jwt = JWT.create()
                    .withExpiresAt(expiryDate)
                    .withClaim("name", username)
                    .withArrayClaim("roles", userRoles.toArray(new String[0]))
                    .sign(signerAlgorithm);

            if (accessControlConfig.isCookieAuthenticationEnabled()) {
                int maxAgeInSeconds =
                        Math.toIntExact(accessControlConfig.getJwtExpirationDuration()
                                .getSeconds());

                Cookie authCookie = new Cookie(AUTH_COOKIE_NAME, jwt);
                authCookie.setPath("/");
                authCookie.setMaxAge(maxAgeInSeconds);
                response.addCookie(authCookie);

                String csrfToken = KeyFactory.createKey(CSRF_TOKEN_BYTE_SIZE);
                Cookie csrfCookie = new Cookie(CsrfFilter.CSRF_COOKIE_NAME, csrfToken);
                csrfCookie.setPath("/");
                csrfCookie.setMaxAge(maxAgeInSeconds);
                response.addCookie(csrfCookie);
            }

            response.setContentType(Type.APPLICATION_JSON.asString());

            JwtDto jwtDto = new JwtDto(username, userRoles, expiryDate, jwt);
            JsonReaderWriter.writeJsonResponse(response, jwtDto);

            return new UserAuthentication(getAuthMethod(), userIdentity);
        } catch (JsonParseException | JsonMappingException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return Authentication.SEND_FAILURE;
        }
    }

    /**
     * Authenticates the user by JWT.
     *
     * @param jwt the JWT to authenticate with
     *
     * @return an authentication
     */
    private Authentication authenticateWithJwt(final String jwt) {
        JWTVerifier verifier = JWT.require(signerAlgorithm).build();
        DecodedJWT decodedJwt = verifier.verify(jwt);

        String username = decodedJwt.getClaim("name").asString();

        LOGGER.info("User {} successfully authenticated (with JWT)", username);

        JwtUserPrincipal principal = new JwtUserPrincipal(username);

        Subject subject = new Subject();
        subject.getPrincipals().add(principal);

        Set<String> roles = new HashSet<>(decodedJwt.getClaim("roles").asList(String.class));

        JwtUserIdentity userIdentity = new JwtUserIdentity(subject, principal, roles);

        return new UserAuthentication(getAuthMethod(), userIdentity);
    }

    /**
     * Indicates if the request is an an authentication request.
     *
     * @param requestUri the request URL
     *
     * @return <code>true</code> if the request is authentication request, <code>false</code>
     *         otherwise
     */
    private boolean isAuthenticationRequest(final String requestUri) {
        return authPathPattern.matcher(requestUri).matches();
    }

    /**
     * Extracts the JWT from the request, if present. If the request is a WebSocket upgrade request,
     * it extracts the token from the query parameters. Otherwise, it looks for the token in the
     * Authorization header. If the token is still not found, it also checks the cookies, if cookie
     * authentication is enabled.
     *
     * @param request the request
     *
     * @return the JWT from the request
     */
    private static Optional<String> extractJwtFromRequest(
            final HttpServletRequest request,
            final boolean isCookieAuthenticationEnabled) {
        Optional<String> jwtOpt;
        if (isWebSocketUpgradeRequest(request)) {
            jwtOpt = Optional.ofNullable(request.getParameter(AUTH_QUERY_PARAM_NAME));
        } else {
            jwtOpt = extractJwtFromHeader(request);
        }

        if (!jwtOpt.isPresent() && isCookieAuthenticationEnabled) {
            jwtOpt = extractJwtFromCookie(request);
        }

        return jwtOpt;
    }

    /**
     * Indicates if the request is a WebSocket upgrade request.
     *
     * @param request the request
     *
     * @return <code>true</code> if the request is a WebSocket upgrade request,
     *         <code>false</code> otherwise
     */
    private static boolean isWebSocketUpgradeRequest(final HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(HttpHeader.UPGRADE.toString()))
                .filter("websocket"::equals)
                .isPresent();
    }

    /**
     * Extracts the JWT from the Authorization header, if present.
     *
     * @param request the request
     *
     * @return the JWT from the Authorization header
     */
    private static Optional<String> extractJwtFromHeader(final HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(HttpHeader.AUTHORIZATION.toString()))
                .filter(headerValue -> headerValue.startsWith(AUTH_HEADER_VALUE_PREFIX))
                .map(headerValue -> headerValue.substring(AUTH_HEADER_VALUE_PREFIX.length()));
    }

    /**
     * Extracts the JWT from the cookies, if present.
     *
     * @param request the request
     *
     * @return the JWT from the cookies
     */
    private static Optional<String> extractJwtFromCookie(final HttpServletRequest request) {
        return Optional.ofNullable(request.getCookies())
                .map(Stream::of)
                .orElseGet(Stream::empty)
                .filter(cookie -> AUTH_COOKIE_NAME.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst();
    }
}
