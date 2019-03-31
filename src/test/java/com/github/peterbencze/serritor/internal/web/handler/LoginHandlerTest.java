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

import com.auth0.jwt.algorithms.Algorithm;
import com.github.peterbencze.serritor.api.web.AccessControlConfiguration;
import com.github.peterbencze.serritor.api.web.User;
import com.github.peterbencze.serritor.internal.web.dto.JwtDto;
import com.github.peterbencze.serritor.internal.web.dto.LoginDto;
import io.javalin.Context;
import io.javalin.UnauthorizedResponse;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test cases for {@link LoginHandler}.
 */
public final class LoginHandlerTest {

    private static final String INCORRECT_PASSWORD_HASH
            = "$2a$10$Jh4rXRRgeI6WDsb8X7XXpuOJlF1ntM6OJ4ObdNiEaI0AH6d4Lcmky";
    private static final String CORRECT_PASSWORD_HASH
            = "$2a$10$baEfqZy/tI3RoKlxQk6jGe9L5nf3NMTEOSWKasVArYH3Ki44pNSU2";

    private AccessControlConfiguration accessControlConfigMock;
    private Context contextMock;
    private User userMock;

    private LoginHandler loginHandler;

    @Before
    public void before() {
        accessControlConfigMock = Mockito.mock(AccessControlConfiguration.class);

        LoginDto loginDtoMock = Mockito.mock(LoginDto.class);
        Mockito.when(loginDtoMock.getUsername()).thenReturn("foo");
        Mockito.when(loginDtoMock.getPassword()).thenReturn("bar");

        contextMock = Mockito.mock(Context.class);
        Mockito.when(contextMock.bodyAsClass(LoginDto.class)).thenReturn(loginDtoMock);

        userMock = Mockito.mock(User.class);

        Algorithm signerAlgorithm = Mockito.spy(Algorithm.HMAC256("secret"));
        loginHandler = new LoginHandler(accessControlConfigMock, signerAlgorithm);
    }

    @Test(expected = UnauthorizedResponse.class)
    public void testHandleWhenUserDoesNotExist() throws Exception {
        Mockito.when(accessControlConfigMock.getUser(Mockito.anyString()))
                .thenReturn(Optional.empty());

        loginHandler.handle(contextMock);
    }

    @Test(expected = UnauthorizedResponse.class)
    public void testHandleWhenPasswordIsIncorrect() throws Exception {
        Mockito.when(userMock.getPasswordHash()).thenReturn(INCORRECT_PASSWORD_HASH);

        Mockito.when(accessControlConfigMock.getUser(Mockito.anyString()))
                .thenReturn(Optional.of(userMock));

        loginHandler.handle(contextMock);
    }

    @Test
    public void testHandleWhenPasswordIsCorrectAndCookieAuthenticationIsDisabled()
            throws Exception {
        Mockito.when(userMock.getPasswordHash()).thenReturn(CORRECT_PASSWORD_HASH);

        Mockito.when(accessControlConfigMock.getUser(Mockito.anyString()))
                .thenReturn(Optional.of(userMock));
        Mockito.when(accessControlConfigMock.isCookieAuthenticationEnabled()).thenReturn(false);

        loginHandler.handle(contextMock);

        Mockito.verify(contextMock).json(Mockito.any(JwtDto.class));
        Mockito.verify(contextMock, Mockito.never())
                .cookie(Mockito.eq(JwtHandler.COOKIE_NAME), Mockito.anyString(), Mockito.anyInt());
        Mockito.verify(contextMock, Mockito.never())
                .cookie(Mockito.eq(XsrfTokenHandler.COOKIE_NAME), Mockito.anyString(),
                        Mockito.anyInt());
    }

    @Test
    public void testHandleWhenPasswordIsCorrectAndCookieAuthenticationIsEnabled() throws Exception {
        Mockito.when(userMock.getPasswordHash()).thenReturn(CORRECT_PASSWORD_HASH);

        Mockito.when(accessControlConfigMock.getUser(Mockito.anyString()))
                .thenReturn(Optional.of(userMock));
        Mockito.when(accessControlConfigMock.isCookieAuthenticationEnabled()).thenReturn(true);

        loginHandler.handle(contextMock);

        Mockito.verify(contextMock, Mockito.never()).json(Mockito.any(JwtDto.class));
        Mockito.verify(contextMock)
                .cookie(Mockito.eq(JwtHandler.COOKIE_NAME), Mockito.anyString(), Mockito.anyInt());
        Mockito.verify(contextMock)
                .cookie(Mockito.eq(XsrfTokenHandler.COOKIE_NAME), Mockito.anyString(),
                        Mockito.anyInt());
    }
}
