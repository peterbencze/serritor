/*
 * Copyright 2018 Peter Bencze.
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

import org.apache.commons.lang3.StringUtils;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.openqa.selenium.Cookie;

/**
 * Converts Selenium cookies to HTTP client ones.
 */
public final class CookieConverter {

    private static final String HTTP_ONLY_ATTRIBUTE = "httponly";

    /**
     * Private constructor to hide the implicit public one.
     */
    private CookieConverter() {
    }

    /**
     * Converts a Selenium cookie to a HTTP client one.
     *
     * @param seleniumCookie the browser cookie to be converted
     *
     * @return the converted HTTP client cookie
     */
    public static BasicClientCookie convertToHttpClientCookie(final Cookie seleniumCookie) {
        BasicClientCookie httpClientCookie
                = new BasicClientCookie(seleniumCookie.getName(), seleniumCookie.getValue());
        httpClientCookie.setDomain(seleniumCookie.getDomain());
        httpClientCookie.setPath(seleniumCookie.getPath());
        httpClientCookie.setExpiryDate(seleniumCookie.getExpiry());
        httpClientCookie.setSecure(seleniumCookie.isSecure());

        if (seleniumCookie.isHttpOnly()) {
            httpClientCookie.setAttribute(HTTP_ONLY_ATTRIBUTE, StringUtils.EMPTY);
        }

        return httpClientCookie;
    }
}
