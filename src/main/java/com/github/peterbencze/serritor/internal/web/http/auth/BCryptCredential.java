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

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.util.security.Credential;
import org.mindrot.jbcrypt.BCrypt;

/**
 * A BCrypt credential implementation.
 */
public final class BCryptCredential extends Credential {

    public static final String PREFIX = "BCRYPT:";

    private final String passwordHash;

    /**
     * Creates a {@link BCryptCredential} instance.
     *
     * @param credential the BCrypt hash of the user's password (with the prefix)
     */
    public BCryptCredential(final String credential) {
        this.passwordHash = StringUtils.removeStart(credential, PREFIX);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean check(final Object credentials) {
        return BCrypt.checkpw(credentials.toString(), passwordHash);
    }
}
