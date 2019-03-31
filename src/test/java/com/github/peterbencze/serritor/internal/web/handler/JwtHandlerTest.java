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

import io.javalin.Context;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test cases for {@link JwtHandler}.
 */
public final class JwtHandlerTest {

    private static final String JWT = "foo.bar.baz";

    private Context contextMock;

    private JwtHandler jwtHandler;

    @Before
    public void before() {
        contextMock = Mockito.mock(Context.class);

        jwtHandler = new JwtHandler();
    }

    @Test
    public void testHandleWhenJwtIsPresentInHeader() throws Exception {
        Mockito.when(contextMock.header(Mockito.eq("Authorization"))).thenReturn("Bearer " + JWT);

        jwtHandler.handle(contextMock);

        Mockito.verify(contextMock).attribute(JwtHandler.CONTEXT_ATTRIBUTE_NAME, JWT);
    }

    @Test
    public void testHandleWhenJwtIsPresentInCookie() throws Exception {
        Mockito.when(contextMock.cookie(JwtHandler.COOKIE_NAME)).thenReturn(JWT);

        jwtHandler.handle(contextMock);

        Mockito.verify(contextMock).attribute(JwtHandler.CONTEXT_ATTRIBUTE_NAME, JWT);
    }
}
