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

package com.github.peterbencze.serritor.internal.web.accessmanager;

import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.github.peterbencze.serritor.internal.web.UserRole;
import com.github.peterbencze.serritor.internal.web.handler.JwtHandler;
import io.javalin.Context;
import io.javalin.Handler;
import io.javalin.UnauthorizedResponse;
import io.javalin.security.Role;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test cases for {@link JwtAccessManager}.
 */
public final class JwtAccessManagerTest {

    private Handler handlerMock;
    private Context contextMock;
    private Set<Role> permittedRolesMock;

    private Algorithm signerAlgorithm;
    private JwtAccessManager jwtAccessManager;

    @Before
    public void before() {
        handlerMock = Mockito.mock(Handler.class);
        contextMock = Mockito.mock(Context.class);
        permittedRolesMock = Mockito.mock(Set.class);

        signerAlgorithm = Mockito.spy(Algorithm.HMAC256("secret"));
        jwtAccessManager = new JwtAccessManager(signerAlgorithm);
    }

    @Test(expected = UnauthorizedResponse.class)
    public void testManageWhenEndpointIsRestrictedAndJwtIsNotPresent() throws Exception {
        Mockito.when(permittedRolesMock.contains(Mockito.any(UserRole.class))).thenReturn(false);
        Mockito.when(contextMock.attribute(JwtHandler.CONTEXT_ATTRIBUTE_NAME)).thenReturn(null);

        jwtAccessManager.manage(handlerMock, contextMock, permittedRolesMock);
    }

    @Test(expected = UnauthorizedResponse.class)
    public void testManageWhenEndpointIsRestrictedAndJwtIsInvalid() throws Exception {
        Mockito.when(permittedRolesMock.contains(Mockito.any(UserRole.class))).thenReturn(false);
        Mockito.when(contextMock.attribute(JwtHandler.CONTEXT_ATTRIBUTE_NAME))
                .thenReturn("eyJhbGciOiJIUzI1NiJ9.e30.XmNK3GpH3Ys_7wsYBfq4C3M6goz71I7dTgUkuIa5lyQ");
        Mockito.doThrow(SignatureVerificationException.class).when(signerAlgorithm)
                .verify(Mockito.any(DecodedJWT.class));

        jwtAccessManager.manage(handlerMock, contextMock, permittedRolesMock);
    }

    @Test
    public void testManageWhenEndpointIsNotRestricted() throws Exception {
        Mockito.when(permittedRolesMock.contains(Mockito.any(UserRole.class))).thenReturn(true);

        jwtAccessManager.manage(handlerMock, contextMock, permittedRolesMock);

        Mockito.verify(handlerMock).handle(Mockito.eq(contextMock));
    }
}
