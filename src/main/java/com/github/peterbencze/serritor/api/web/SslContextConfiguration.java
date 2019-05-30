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

package com.github.peterbencze.serritor.api.web;

import java.util.Optional;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Configuration of the SSL context.
 */
public final class SslContextConfiguration {

    private final String keyStorePath;
    private final String keyStorePassword;
    private final String keyManagerPassword;

    /**
     * Creates a {@link SslContextConfiguration} instance.
     *
     * @param keyStorePath       the path to the keystore file
     * @param keyStorePassword   the password for the keystore
     * @param keyManagerPassword the password for the key manager
     */
    public SslContextConfiguration(
            final String keyStorePath,
            final String keyStorePassword,
            final String keyManagerPassword) {
        Validate.notBlank(keyStorePath, "The keyStorePath parameter cannot be null or blank");
        Validate.notBlank(keyStorePassword,
                "The keyStorePassword parameter cannot be null or blank");

        this.keyStorePath = keyStorePath;
        this.keyStorePassword = keyStorePassword;
        this.keyManagerPassword = keyManagerPassword;
    }

    /**
     * Creates a {@link SslContextConfiguration} instance.
     *
     * @param keyStorePath     the path to the keystore file
     * @param keyStorePassword the password for the keystore
     */
    public SslContextConfiguration(final String keyStorePath, final String keyStorePassword) {
        this(keyStorePath, keyStorePassword, null);
    }

    /**
     * Returns the path to the keystore file.
     *
     * @return the path to the keystore file
     */
    public String getKeyStorePath() {
        return keyStorePath;
    }

    /**
     * Returns the password for the keystore.
     *
     * @return the password for the keystore
     */
    public String getKeyStorePassword() {
        return keyStorePassword;
    }

    /**
     * Returns the password for the key manager.
     *
     * @return the password for the key manager
     */
    public Optional<String> getKeyManagerPassword() {
        return Optional.ofNullable(keyManagerPassword);
    }

    /**
     * Returns a string representation of this SSL context configuration instance.
     *
     * @return a string representation of this SSL context configuration instance
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("keyStorePath", keyStorePath)
                .toString();
    }
}
