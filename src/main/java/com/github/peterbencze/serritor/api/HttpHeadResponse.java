/* 
 * Copyright 2016 Peter Bencze.
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

import java.util.Locale;
import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;

/**
 * Represents a response of a HTTP HEAD request.
 *
 * @author Peter Bencze
 */
public final class HttpHeadResponse {

    private final HttpResponse response;

    public HttpHeadResponse(HttpResponse response) {
        this.response = response;
    }

    /**
     * Checks if a certain header is present in this message.
     *
     * @param name The name of the header
     * @return True if it is present, false otherwise
     */
    public boolean containsHeader(String name) {
        return response.containsHeader(name);
    }

    /**
     * Returns all the headers of this response.
     *
     * @return All the headers
     */
    public Header[] getAllHeaders() {
        return response.getAllHeaders();
    }

    /**
     * Returns the first header with a specified name of this response.
     *
     * @param name The name of the header
     * @return The first header with the specified name
     */
    public Header getFirstHeader(String name) {
        return response.getFirstHeader(name);
    }

    /**
     * Returns all the headers with a specified name of this response.
     *
     * @param name The name of the headers
     * @return All the headers
     */
    public Header[] getHeaders(String name) {
        return response.getHeaders(name);
    }

    /**
     * Returns the last header with a specified name of this response.
     *
     * @param name The name of the header
     * @return The last header with a specified name
     */
    public Header getLastHeader(String name) {
        return response.getLastHeader(name);
    }

    /**
     * Returns the protocol version this response is compatible with.
     *
     * @return The compatible protocol version
     */
    public ProtocolVersion getProtocolVersion() {
        return response.getProtocolVersion();
    }

    /**
     * Returns an iterator of all the headers.
     *
     * @return An iterator of all the headers
     */
    public HeaderIterator headerIterator() {
        return response.headerIterator();
    }

    /**
     * Returns an iterator of the headers with a given name.
     *
     * @param name The name of the headers
     * @return An iterator of the headers with a given name
     */
    public HeaderIterator headerIterator(String name) {
        return response.headerIterator(name);
    }

    /**
     * Obtains the locale of this response.
     *
     * @return The locale of this response
     */
    public Locale getLocale() {
        return response.getLocale();
    }

    /**
     * Obtains the status line of this response.
     *
     * @return The status line of this response
     */
    public StatusLine getStatusLine() {
        return response.getStatusLine();
    }
}
