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

package com.github.peterbencze.serritor.internal.web;

import com.github.peterbencze.serritor.api.helper.JsonReaderWriter;
import com.github.peterbencze.serritor.internal.web.http.dto.ErrorDto;
import java.io.IOException;
import java.io.Writer;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.MimeTypes.Type;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.ErrorHandler;

/**
 * An error handler that formats the response as JSON.
 */
public final class JsonErrorHandler extends ErrorHandler {

    /**
     * {@inheritDoc}
     */
    @Override
    protected void generateAcceptableResponse(
            final Request baseRequest,
            final HttpServletRequest request,
            final HttpServletResponse response,
            final int code,
            final String message,
            final String mimeType) throws IOException {
        baseRequest.setHandled(true);

        String errorMessage = Optional.ofNullable(message)
                // Ignore Jetty's default error message when the user is unauthorized
                .filter(msg -> !("!role".equals(msg) && code == HttpStatus.FORBIDDEN_403))
                .orElseGet(() -> HttpStatus.getMessage(code));

        Writer writer = getAcceptableWriter(baseRequest, request, response);
        if (writer != null) {
            response.setContentType(Type.APPLICATION_JSON.asString());
            handleErrorPage(request, writer, code, errorMessage);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void writeErrorPage(
            final HttpServletRequest request,
            final Writer writer,
            final int code,
            final String message,
            final boolean showStacks) throws IOException {
        JsonReaderWriter.getObjectMapper().writeValue(writer, new ErrorDto(code, message));
    }
}
