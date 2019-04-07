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

import io.javalin.Context;
import io.javalin.Handler;
import io.javalin.security.AccessManager;
import io.javalin.security.Role;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A no-operation access manager that is used when access control is disabled.
 */
public final class NoopAccessManager implements AccessManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(NoopAccessManager.class);

    /**
     * Simply lets the request pass through without credential checking.
     *
     * @param handler        the request handler
     * @param ctx            the context object
     * @param permittedRoles a set of permitted roles
     */
    @Override
    public void manage(
            final Handler handler,
            final Context ctx,
            final Set<Role> permittedRoles) throws Exception {
        LOGGER.debug("Incoming request from {} to path {}", ctx.ip(), ctx.path());
        LOGGER.debug("Letting request through");
        handler.handle(ctx);
    }
}
