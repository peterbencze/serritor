package com.serritor;

import java.net.URL;
import org.apache.http.HttpStatus;

/**
 * Represents a response of a HTTP HEAD request with only the necessary properties.
 * 
 * @author Peter Bencze
 */
public class HttpHeadResponse {
    
    private final URL finalUrl;
    private final int statusCode;
    private final String contentType;
    
    public HttpHeadResponse(URL finalUrl, int statusCode, String contentType) {
        this.finalUrl = finalUrl;
        this.statusCode = statusCode;
        this.contentType = contentType;
    }
    
    /**
     * Returns the URL of the response.
     * 
     * @return The URL
     */
    public URL getUrl() {
        return finalUrl;
    }

    /**
     * Indicates if the HTTP status code of the response is 200 (OK).
     * 
     * @return True if the status code is 200 (OK), false otherwise
     */
    public boolean isStatusOk() {
        return statusCode == HttpStatus.SC_OK;
    }

    /**
     * Indicates if the content type of the response is text/html.
     * 
     * @return True if the content type is text/html, false otherwise
     */
    public boolean isHtmlContent() {
        // In case the server did not send the Content-Type header
        if (contentType == null)
            return false;
        
        return contentType.contains("text/html");
    }
}
