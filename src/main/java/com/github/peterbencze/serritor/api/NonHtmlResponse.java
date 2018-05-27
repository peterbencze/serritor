/* 
 * Copyright 2017 Peter Bencze.
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

import com.github.peterbencze.serritor.internal.CallbackParameter;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import org.apache.commons.io.FileUtils;

/**
 * Represents a non-HTML response.
 *
 * @author Peter Bencze
 */
public final class NonHtmlResponse extends CallbackParameter {

    /**
     * Constructs a <code>NonHtmlResponse</code> instance.
     * 
     * @param refererUrl The referer URL
     * @param crawlDepth The current crawl depth
     * @param crawlRequest The processed crawl request
     */
    public NonHtmlResponse(final URI refererUrl, final int crawlDepth, final CrawlRequest crawlRequest) {
        super(refererUrl, crawlDepth, crawlRequest);
    }
    
    /**
     * Downloads the file specified by the request URL.
     * 
     * @param destination The destination <code>File</code> instance
     * @throws IOException If the URL cannot be opened or I/O error occurs while downloading the file
     */
    public void downloadFile(final File destination) throws IOException {
        FileUtils.copyURLToFile(getCrawlRequest().getRequestUrl().toURL(), destination);
    }
}
