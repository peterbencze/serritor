package com.serritor.api;

import com.serritor.internal.CallbackParameter;
import java.net.URL;

/**
 * Represents a non-HTML response.
 * 
 * @author Peter Bencze
 */
public final class NonHtmlResponse extends CallbackParameter {

    private final URL responseUrl;
    private final HttpHeadResponse httpHeadResponse;

    private NonHtmlResponse(NonHtmlResponseBuilder builder) {
        super(builder.crawlDepth, builder.refererUrl);

        this.responseUrl = builder.responseUrl;
        this.httpHeadResponse = builder.httpHeadResponse;
    }

    /**
     * Returns the URL of the non-HTML response.
     * 
     * @return The URL of the response
     */
    public URL getUrl() {
        return responseUrl;
    }

    /**
     * Returns the HTTP HEAD response.
     * 
     * @return The HTTP HEAD response
     */
    public HttpHeadResponse getHttpHeadResponse() {
        return httpHeadResponse;
    }

    public static class NonHtmlResponseBuilder {

        private int crawlDepth;
        private URL refererUrl;
        private URL responseUrl;
        private HttpHeadResponse httpHeadResponse;

        public NonHtmlResponseBuilder() {
        }

        public NonHtmlResponseBuilder setCrawlDepth(int crawlDepth) {
            this.crawlDepth = crawlDepth;
            return this;
        }

        public NonHtmlResponseBuilder setRefererUrl(URL refererUrl) {
            this.refererUrl = refererUrl;
            return this;
        }

        public NonHtmlResponseBuilder setResponseUrl(URL responseUrl) {
            this.responseUrl = responseUrl;
            return this;
        }

        public NonHtmlResponseBuilder setHttpHeadResponse(HttpHeadResponse httpHeadResponse) {
            this.httpHeadResponse = httpHeadResponse;
            return this;
        }

        public NonHtmlResponse build() {
            return new NonHtmlResponse(this);
        }
    }
}
