package com.serritor;

import java.net.URL;
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
public class HttpHeadResponse {
    
    private final URL url;
    private final HttpResponse response;
    
    public HttpHeadResponse(URL url, HttpResponse response) {
        this.url = url;
        this.response = response;
    }
    
    /**
     * Returns the URL of the response.
     * 
     * @return The URL
     */
    public URL getUrl() {
        return url;
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
     * @return  The last header with a specified name
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
