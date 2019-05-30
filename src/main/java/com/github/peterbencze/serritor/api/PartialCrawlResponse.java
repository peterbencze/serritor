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

package com.github.peterbencze.serritor.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import net.lightbody.bmp.core.har.HarResponse;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.message.BasicHeader;

/**
 * Represents a partial response that only contains HTTP header information.
 */
public class PartialCrawlResponse {

    private final int statusCode;
    private final String statusText;
    private final List<Header> headers;

    /**
     * Creates a {@link PartialCrawlResponse} instance from an HTTP response message.
     *
     * @param httpResponse the HTTP response message
     */
    public PartialCrawlResponse(final HttpResponse httpResponse) {
        statusCode = httpResponse.getStatusLine().getStatusCode();
        statusText = httpResponse.getStatusLine().getReasonPhrase();
        headers = Arrays.asList(httpResponse.getAllHeaders());
    }

    /**
     * Creates a {@link PartialCrawlResponse} instance from an HAR capture.
     *
     * @param harResponse the HAR capture
     */
    public PartialCrawlResponse(final HarResponse harResponse) {
        statusCode = harResponse.getStatus();
        statusText = harResponse.getStatusText();
        headers = new ArrayList<>();
        harResponse.getHeaders()
                .forEach(header -> headers.add(new BasicHeader(header.getName(),
                        header.getValue())));
    }

    /**
     * Returns the HTTP status code of the response.
     *
     * @return the HTTP status code
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * Returns the status message corresponding to the status code of the response.
     *
     * @return the status message
     */
    public String getStatusText() {
        return statusText;
    }

    /**
     * Returns all the headers of the response.
     *
     * @return all the headers
     */
    public List<Header> getAllHeaders() {
        return headers;
    }

    /**
     * Returns all the headers with the specified name of the response.
     *
     * @param name the name of the headers
     *
     * @return all the headers with the specified name
     */
    public List<Header> getHeaders(final String name) {
        return headers.stream()
                .filter(header -> name.equals(header.getName()))
                .collect(Collectors.toList());
    }

    /**
     * Returns the first header with the specified name of the response.
     *
     * @param name the name of the header
     *
     * @return the first header with the specified name
     */
    public Optional<Header> getFirstHeader(final String name) {
        return headers.stream()
                .filter(header -> name.equals(header.getName()))
                .findFirst();
    }
}
