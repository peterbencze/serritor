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

import java.util.Date;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Cookie;

/**
 * Test cases for {@link CookieConverter}.
 */
public final class CookieConverterTest {

    private static final String NAME = "test-name";
    private static final String VALUE = "test-value";
    private static final String DOMAIN = "test-domain";
    private static final String PATH = "test-path";
    private static final Date EXPIRY_DATE = new Date();
    private static final boolean IS_SECURE = true;
    private static final boolean IS_HTTP_ONLY = true;
    private static final String HTTP_ONLY_ATTRIBUTE = "httponly";

    private Cookie seleniumCookie;

    @Before
    public void before() {
        seleniumCookie = new Cookie(NAME, VALUE, DOMAIN, PATH, EXPIRY_DATE, IS_SECURE,
                IS_HTTP_ONLY);
    }

    @Test
    public void convertToHttpClientCookieTest() {
        BasicClientCookie convertedCookie
                = CookieConverter.convertToHttpClientCookie(seleniumCookie);

        Assert.assertEquals(convertedCookie.getName(), seleniumCookie.getName());
        Assert.assertEquals(convertedCookie.getValue(), seleniumCookie.getValue());
        Assert.assertEquals(convertedCookie.getDomain(), seleniumCookie.getDomain());
        Assert.assertEquals(convertedCookie.getPath(), seleniumCookie.getPath());
        Assert.assertEquals(convertedCookie.getExpiryDate(), seleniumCookie.getExpiry());
        Assert.assertEquals(convertedCookie.isSecure(), seleniumCookie.isSecure());
        Assert.assertEquals(convertedCookie.getAttribute(HTTP_ONLY_ATTRIBUTE) != null,
                seleniumCookie.isHttpOnly());
    }
}
