package com.easypost.easyvcr.requestelements;

import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * Represents an HTTP request tracked by EasyVCR.
 */
public final class Request extends HttpElement {

    /**
     * The body of the request.
     */
    private String body;

    /**
     * The method of the request.
     */
    private String method;

    /**
     * The headers of the request.
     */
    private Map<String, List<String>> headers;

    /**
     * The URI of the request.
     */
    private URI uri;

    /**
     * Returns the body of the request.
     *
     * @return the body of the request
     */
    public String getBody() {
        return body != null ? body : "";
    }

    /**
     * Sets the body of the request.
     *
     * @param body the body of the request
     */
    public void setBody(String body) {
        this.body = body;
    }

    /**
     * Returns the method of the request.
     *
     * @return the method of the request
     */
    public String getMethod() {
        return method;
    }

    /**
     * Sets the method of the request.
     *
     * @param method the method of the request
     */
    public void setMethod(String method) {
        this.method = method;
    }

    /**
     * Returns the headers of the request.
     *
     * @return the headers of the request
     */
    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    /**
     * Sets the headers of the request.
     *
     * @param headers the headers of the request
     */
    public void setHeaders(Map<String, List<String>> headers) {
        this.headers = headers;
    }

    /**
     * Returns the URI of the request.
     *
     * @return the URI of the request
     */
    public URI getUri() {
        return this.uri;
    }

    /**
     * Sets the URI of the request.
     *
     * @param uri the URI of the request
     */
    public void setUri(URI uri) {
        this.uri = uri;
    }

    /**
     * Returns the URI of the request as a string.
     *
     * @return the URI of the request as a string
     */
    public String getUriString() {
        return this.uri.toString();
    }

    /**
     * Sets the URI of the request from a string.
     *
     * @param uriString the URI of the request as a string
     */
    public void setUriString(String uriString) {
        this.uri = URI.create(uriString);
    }
}
