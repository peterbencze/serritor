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

import com.github.peterbencze.serritor.api.web.AccessControlConfiguration;
import com.github.peterbencze.serritor.api.web.ServerConfiguration;
import com.github.peterbencze.serritor.api.web.User;
import com.github.peterbencze.serritor.api.web.http.HttpHandler;
import com.github.peterbencze.serritor.api.web.http.HttpMethod;
import com.github.peterbencze.serritor.api.web.socket.WebSocketHandler;
import com.github.peterbencze.serritor.internal.web.http.CsrfFilter;
import com.github.peterbencze.serritor.internal.web.http.auth.JwtAuthenticator;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import javax.servlet.DispatcherType;
import org.eclipse.jetty.security.Authenticator;
import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.security.LoginService;
import org.eclipse.jetty.security.UserStore;
import org.eclipse.jetty.util.security.Constraint;
import org.eclipse.jetty.util.security.Credential;

/**
 * A secured web API implementation that allows users to register HTTP and WebSocket endpoints that
 * can be used to interact with the crawler while it is running. Users are required to authenticate
 * before they can access restricted endpoints (if they are authorized to do so).
 */
public final class SecuredWebApi extends WebApi {

    private final AccessControlConfiguration accessControlConfig;
    private final ConstraintSecurityHandler securityHandler;

    /**
     * Creates a {@link SecuredWebApi} instance.
     *
     * @param serverConfig        the configuration of the web server
     * @param accessControlConfig the access control configuration
     */
    public SecuredWebApi(
            final ServerConfiguration serverConfig,
            final AccessControlConfiguration accessControlConfig) {
        super(serverConfig);

        this.accessControlConfig = accessControlConfig;

        Authenticator jwtAuthenticator = new JwtAuthenticator(accessControlConfig);

        LoginService loginService = createLoginService(accessControlConfig.getUsers());
        getServer().addBean(loginService);

        securityHandler = createSecurityHandler(jwtAuthenticator, loginService);
        securityHandler.setHandler(getContextHandler());
        getServer().setHandler(securityHandler);

        if (accessControlConfig.isCookieAuthenticationEnabled()) {
            getContextHandler()
                    .addFilter(CsrfFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));
        }
    }

    /**
     * Returns the access control configuration.
     *
     * @return the access control configuration
     */
    public AccessControlConfiguration getAccessControlConfiguration() {
        return accessControlConfig;
    }

    /**
     * Adds an HTTP endpoint to the web API that is accessible to anyone (regardless of if they are
     * authenticated or not).
     *
     * @param httpMethod the HTTP method of the endpoint
     * @param path       the path of the endpoint
     * @param handler    the handler of the endpoint
     */
    @Override
    public void addHttpEndpoint(
            final HttpMethod httpMethod,
            final String path,
            final HttpHandler handler) {
        addHttpEndpoint(httpMethod, path, Collections.singleton(Constraint.ANY_AUTH), handler);
    }

    /**
     * Adds an HTTP endpoint to the web API that is only accessible for users who are authenticated
     * and have any of the roles specified.
     *
     * @param httpMethod   the HTTP method of the endpoint
     * @param path         the path of the endpoint
     * @param allowedRoles the set of allowed roles
     * @param handler      the handler of the endpoint
     */
    public void addHttpEndpoint(
            final HttpMethod httpMethod,
            final String path,
            final Set<String> allowedRoles,
            final HttpHandler handler) {
        super.addHttpEndpoint(httpMethod, path, handler);

        securityHandler.addConstraintMapping(createConstraintMapping(path, allowedRoles));
    }

    /**
     * Adds a WebSocket endpoint to the web API that is accessible to anyone (regardless of if they
     * are authenticated or not).
     *
     * @param path    the path of the endpoint
     * @param handler the handler of the endpoint
     */
    @Override
    public void addWebSocketEndpoint(final String path, final WebSocketHandler handler) {
        addWebSocketEndpoint(path, Collections.singleton(Constraint.ANY_AUTH), handler);
    }

    /**
     * Adds a WebSocket endpoint to the web API that is only accessible for users who are
     * authenticated and have any of the roles specified.
     *
     * @param path         the path of the endpoint
     * @param allowedRoles the set of allowed roles
     * @param handler      the handler of the endpoint
     */
    public void addWebSocketEndpoint(
            final String path,
            final Set<String> allowedRoles,
            final WebSocketHandler handler) {
        super.addWebSocketEndpoint(path, handler);

        securityHandler.addConstraintMapping(createConstraintMapping(path, allowedRoles));
    }

    /**
     * Creates and configures a login service.
     *
     * @param users the list of users
     *
     * @return the configured login service
     */
    private static LoginService createLoginService(List<User> users) {
        UserStore userStore = new UserStore();
        users.forEach(user -> {
            Credential userCredential = Credential.getCredential(user.getPassword());
            String[] userRoles = user.getRoles().toArray(new String[0]);

            userStore.addUser(user.getUsername(), userCredential, userRoles);
        });

        HashLoginService loginService = new HashLoginService();
        loginService.setUserStore(userStore);

        return loginService;
    }

    /**
     * Creates a handler that enforces security constraints.
     *
     * @param authenticator the authenticator
     * @param loginService  the login service
     *
     * @return the configured security handler
     */
    private static ConstraintSecurityHandler createSecurityHandler(
            final Authenticator authenticator,
            final LoginService loginService) {
        ConstraintSecurityHandler handler = new ConstraintSecurityHandler();
        handler.setAuthenticator(authenticator);
        handler.setLoginService(loginService);

        return handler;
    }

    /**
     * Creates a constraint mapping.
     *
     * @param path  the path
     * @param roles the allowed roles
     *
     * @return the constraint mapping
     */
    private static ConstraintMapping createConstraintMapping(
            final String path,
            final Set<String> roles) {
        Constraint constraint = new Constraint();
        constraint.setName("auth");
        constraint.setAuthenticate(true);
        constraint.setRoles(roles.toArray(new String[0]));

        ConstraintMapping mapping = new ConstraintMapping();
        mapping.setPathSpec(path);
        mapping.setConstraint(constraint);

        return mapping;
    }
}
