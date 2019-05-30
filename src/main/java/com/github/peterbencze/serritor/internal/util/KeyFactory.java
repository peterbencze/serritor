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

package com.github.peterbencze.serritor.internal.util;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.KeyGenerator;

/**
 * A helper class used for creating secure random keys.
 */
public final class KeyFactory {

    /**
     * Private constructor to hide the implicit public one.
     */
    private KeyFactory() {
    }

    /**
     * Creates a key using the specific algorithm.
     *
     * @param algorithm the algorithm to use for key generation
     *
     * @return the generated key
     */
    public static String createKey(final String algorithm) {
        try {
            return new String(KeyGenerator.getInstance(algorithm).generateKey().getEncoded());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Creates a secure random key.
     *
     * @param length the key size in bytes
     *
     * @return the generated key
     */
    public static String createKey(final int length) {
        SecureRandom secureRandom = new SecureRandom();
        byte[] randomBytes = new byte[length];

        secureRandom.nextBytes(randomBytes);

        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
}
