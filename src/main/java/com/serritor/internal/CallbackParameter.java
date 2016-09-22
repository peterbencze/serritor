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
package com.serritor.internal;

import java.net.URL;

/**
 * The base class that all callback parameters inherit from.
 * 
 * @author Peter Bencze
 */
public abstract class CallbackParameter {

    private final int crawlDepth;
    private final URL referer;

    protected CallbackParameter(int crawlDepth, URL referer) {
        this.crawlDepth = crawlDepth;
        this.referer = referer;
    }

    /**
     * Returns the crawl depth of the current request or response.
     * 
     * @return The crawl depth of the current request or response
     */
    public final int getCrawlDepth() {
        return crawlDepth;
    }

    /**
     * Returns the referer URL of the current request or response.
     * 
     * @return The referer URL of the current request or response
     */
    public final URL getRefererUrl() {
        return referer;
    }
}
