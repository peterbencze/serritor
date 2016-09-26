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
package com.github.peterbencze.serritor.internal;

import java.net.URL;

/**
 * The base class that all callback parameters inherit from.
 *
 * @author Peter Bencze
 */
public abstract class CallbackParameter {

    private final int crawlDepth;
    private final URL refererUrl;
    private final URL currentUrl;

    protected CallbackParameter(CallbackParameterBuilder builder) {
        crawlDepth = builder.crawlDepth;
        refererUrl = builder.refererUrl;
        currentUrl = builder.currentUrl;
    }

    /**
     * Returns the current crawl depth.
     *
     * @return The current crawl depth
     */
    public final int getCrawlDepth() {
        return crawlDepth;
    }

    /**
     * Returns the referer URL.
     *
     * @return The referer URL
     */
    public final URL getRefererUrl() {
        return refererUrl;
    }

    /**
     * Returns the current URL.
     *
     * @return The current URL
     */
    public final URL getCurrentUrl() {
        return currentUrl;
    }

    public static abstract class CallbackParameterBuilder<T extends CallbackParameterBuilder<T>> {

        private int crawlDepth;
        private URL refererUrl;
        private URL currentUrl;

        public T setCrawlDepth(int crawlDepth) {
            this.crawlDepth = crawlDepth;
            return (T) this;
        }

        public T setRefererUrl(URL refererUrl) {
            this.refererUrl = refererUrl;
            return (T) this;
        }

        public T setCurrentUrl(URL currentUrl) {
            this.currentUrl = currentUrl;
            return (T) this;
        }

        public abstract CallbackParameter build();
    }
}
