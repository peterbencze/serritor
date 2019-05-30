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

package com.github.peterbencze.serritor.internal.web.http;

import com.github.peterbencze.serritor.api.web.http.HttpHandler;
import com.github.peterbencze.serritor.api.web.http.HttpMethod;
import java.io.IOException;
import javax.servlet.GenericServlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public final class HttpServlet extends GenericServlet {

    private final HttpMethod httpMethod;
    private final HttpHandler handler;

    public HttpServlet(final HttpMethod httpMethod, final HttpHandler handler) {
        this.httpMethod = httpMethod;
        this.handler = handler;
    }

    @Override
    public void service(final ServletRequest servletRequest, final ServletResponse servletResponse)
            throws ServletException, IOException {
        if (!(servletRequest instanceof HttpServletRequest
                && servletResponse instanceof HttpServletResponse)) {
            throw new ServletException("Non-HTTP request or response");
        }

        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;

        service(httpServletRequest, httpServletResponse);
    }

    private void service(final HttpServletRequest request, final HttpServletResponse response)
            throws IOException {
        if (httpMethod.toString().equals(request.getMethod())) {
            handler.handle(request, response);
        } else {
            String protocol = request.getProtocol();

            if (protocol.endsWith("1.1")) {
                response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            }
        }
    }
}
