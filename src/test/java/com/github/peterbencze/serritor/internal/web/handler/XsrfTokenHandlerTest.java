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
import io.javalin.UnauthorizedResponse;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test cases for {@link XsrfTokenHandler}.
 */
public final class XsrfTokenHandlerTest {

    private Context contextMock;

    private XsrfTokenHandler xsrfTokenHandler;

    @Before
    public void before() {
        contextMock = Mockito.mock(Context.class);
        Mockito.when(contextMock.method()).thenReturn("POST");
        Mockito.when(contextMock.cookie(XsrfTokenHandler.COOKIE_NAME)).thenReturn("foo");

        xsrfTokenHandler = new XsrfTokenHandler();
    }

    @Test(expected = UnauthorizedResponse.class)
    public void testHandleWhenHeaderIsNotPresent() throws Exception {
        xsrfTokenHandler.handle(contextMock);
    }

    @Test(expected = UnauthorizedResponse.class)
    public void testHandleWhenHeaderContainsInvalidToken() throws Exception {
        Mockito.when(contextMock.header(XsrfTokenHandler.HEADER_NAME)).thenReturn("bar");

        xsrfTokenHandler.handle(contextMock);
    }
}
