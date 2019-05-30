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

import com.github.peterbencze.serritor.api.web.http.HttpMethod;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A filter that is used to prevent CSRF. If a cookie with the name of XSRF-TOKEN is present in the
 * request, it will look for a header with the name of X-XSRF-TOKEN and check if these values match
 * each other.
 */
public final class CsrfFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(CsrfFilter.class);

    public static final String CSRF_COOKIE_NAME = "XSRF-TOKEN";
    public static final String CSRF_HEADER_NAME = "X-XSRF-TOKEN";

    private static final List<HttpMethod> SAFE_HTTP_METHODS =
            Arrays.asList(HttpMethod.HEAD, HttpMethod.GET, HttpMethod.OPTIONS, HttpMethod.TRACE);

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(FilterConfig filterConfig) {
        // Do nothing
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doFilter(
            final ServletRequest servletRequest,
            final ServletResponse servletResponse,
            final FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;

        if (!SAFE_HTTP_METHODS.contains(HttpMethod.valueOf(httpServletRequest.getMethod()))) {
            Optional<String> tokenInCookieOpt = extractTokenFromCookie(httpServletRequest);
            if (tokenInCookieOpt.isPresent()) {
                LOGGER.info("CSRF cookie is present in request, checking token in header");

                // Cannot use ifPresent because sendError throws IOException
                String tokenInCookie = tokenInCookieOpt.get();

                if (!tokenInCookie.equals(httpServletRequest.getHeader(CSRF_HEADER_NAME))) {
                    LOGGER.info("Missing or incorrect CSRF token");
                    httpServletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                            "Missing or incorrect CSRF token");
                    return;
                }
            }
        }

        chain.doFilter(httpServletRequest, httpServletResponse);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy() {
        // Do nothing
    }

    /**
     * Extracts the CSRF token from the CSRF cookie.
     *
     * @param request the HTTP request
     *
     * @return the CSRF token in the CSRF cookie
     */
    private static Optional<String> extractTokenFromCookie(final HttpServletRequest request) {
        return Optional.ofNullable(request.getCookies())
                .map(Stream::of)
                .orElseGet(Stream::empty)
                .filter(cookie -> CSRF_COOKIE_NAME.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst();
    }
}
