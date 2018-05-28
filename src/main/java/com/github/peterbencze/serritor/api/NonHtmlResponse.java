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
import org.apache.commons.io.FileUtils;

/**
 * Represents a non-HTML response.
 *
 * @author Peter Bencze
 */
public final class NonHtmlResponse extends CallbackParameter {

    /**
     * Creates a {@link NonHtmlResponse} instance.
     *
     * @param crawlCandidate The crawled {@link CrawlCandidate} instance
     */
    public NonHtmlResponse(final CrawlCandidate crawlCandidate) {
        super(crawlCandidate);
    }

    /**
     * Downloads the file specified by the request URL.
     *
     * @param destination The destination {@link File} instance
     * @throws IOException If the URL cannot be opened or I/O error occurs while
     * downloading the file
     */
    public void downloadFile(final File destination) throws IOException {
        FileUtils.copyURLToFile(getCrawlCandidate().getCandidateUrl().toURL(), destination);
    }
}
