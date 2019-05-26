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

import java.security.Principal;
import java.util.Set;
import javax.security.auth.Subject;
import org.eclipse.jetty.server.UserIdentity;

/**
 * A user object that encapsulates user identity.
 */
public final class JwtUserIdentity implements UserIdentity {

    private final Subject subject;
    private final Principal userPrincipal;
    private final Set<String> roles;

    /**
     * Creates a {@link JwtUserIdentity} instance.
     *
     * @param subject       the user subject
     * @param userPrincipal the user principal
     * @param roles         the roles of the user
     */
    public JwtUserIdentity(
            final Subject subject,
            final Principal userPrincipal,
            final Set<String> roles) {
        this.subject = subject;
        this.userPrincipal = userPrincipal;
        this.roles = roles;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Subject getSubject() {
        return subject;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Principal getUserPrincipal() {
        return userPrincipal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isUserInRole(final String role, final Scope scope) {
        return roles.contains(role);
    }
}
