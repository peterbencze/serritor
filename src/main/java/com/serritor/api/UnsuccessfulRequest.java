package com.serritor.api;

import com.serritor.internal.CallbackParameter;
import java.io.IOException;
import java.net.URL;

/**
 * Represents an unsuccessful request.
 * 
 * @author Peter Bencze
 */
public final class UnsuccessfulRequest extends CallbackParameter {

    private final URL requestUrl;
    private final IOException exception;

    private UnsuccessfulRequest(UnsuccessfulRequestBuilder builder) {
        super(builder.crawlDepth, builder.referer);

        this.requestUrl = builder.requestUrl;
        this.exception = builder.exception;
    }

    /**
     * Returns the URL of the request.
     * 
     * @return The URL of the request
     */
    public URL getUrl() {
        return requestUrl;
    }

    /**
     * Returns the exception that was thrown.
     * 
     * @return The thrown exception
     */
    public IOException getException() {
        return exception;
    }

    public static class UnsuccessfulRequestBuilder {

        private int crawlDepth;
        private URL referer;
        private URL requestUrl;
        private IOException exception;

        public UnsuccessfulRequestBuilder() {
        }

        public UnsuccessfulRequestBuilder setCrawlDepth(int crawlDepth) {
            this.crawlDepth = crawlDepth;
            return this;
        }

        public UnsuccessfulRequestBuilder setReferer(URL referer) {
            this.referer = referer;
            return this;
        }

        public UnsuccessfulRequestBuilder setRequestUrl(URL requestUrl) {
            this.requestUrl = requestUrl;
            return this;
        }

        public UnsuccessfulRequestBuilder setException(IOException exception) {
            this.exception = exception;
            return this;
        }

        public UnsuccessfulRequest build() {
            return new UnsuccessfulRequest(this);
        }
    }
}
