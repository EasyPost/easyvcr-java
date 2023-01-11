package com.easypost.easyvcr.requestelements;

import com.easypost.easyvcr.Statics;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Represents an HTTP response tracked by EasyVCR.
 */
@SuppressWarnings("deprecation")
public final class Response extends HttpElement {
    /**
     * The body of the response.
     */
    private String body;

    /**
     * The HTTP version of the response.
     */
    private HttpVersion httpVersion;

    /**
     * The headers of the response.
     */
    private Map<String, List<String>> headers;

    /**
     * The status of the response.
     */
    private Status status;

    /**
     * The URI of the response.
     */
    private URI uri;

    /**
     * Returns the body of the response.
     *
     * @return the body of the response
     */
    public String getBody() {
        return this.body;
    }

    /**
     * Sets the body of the response.
     *
     * @param body the body of the response
     */
    public void setBody(String body) {
        this.body = body;
    }

    /**
     * Returns the HttpVersion of the response.
     *
     * @return the HttpVersion of the response
     */
    public HttpVersion getHttpVersion() {
        return this.httpVersion;
    }

    /**
     * Sets the HTTP version of the response from a String.
     *
     * @param version the HTTP version of the response as a String
     */
    public void setHttpVersion(String version) {
        this.httpVersion = new HttpVersion(version);
    }

    /**
     * Returns the headers of the response.
     *
     * @return the headers of the response
     */
    public Map<String, List<String>> getHeaders() {
        return this.headers;
    }

    /**
     * Sets the headers of the response.
     *
     * @param headers the headers of the response
     */
    public void setHeaders(Map<String, List<String>> headers) {
        this.headers = headers;
    }

    /**
     * Add the EasyVCR headers to the response.
     */
    public void addReplayHeaders() {
        // add default replay headers
        Map<String, String> replayHeaders = Statics.getReplayHeaders();
        for (Map.Entry<String, String> entry : replayHeaders.entrySet()) {
            this.headers.put(entry.getKey(), Collections.singletonList(entry.getValue()));
        }
    }

    /**
     * Returns the status of the response.
     *
     * @return the status of the response
     */
    public Status getStatus() {
        return this.status;
    }

    /**
     * Sets the status of the response.
     *
     * @param status the status of the response
     */
    public void setStatus(Status status) {
        this.status = status;
    }

    /**
     * Returns the URI of the response.
     *
     * @return the URI of the response
     */
    public URI getUri() {
        return this.uri;
    }

    /**
     * Sets the URI of the response.
     *
     * @param uri the URI of the response
     */
    public void setUri(URI uri) {
        this.uri = uri;
    }

    /**
     * Returns the URI of the response as a string.
     *
     * @return the URI of the response as a string
     */
    public String getUriString() {
        return this.uri.toString();
    }

    /**
     * Sets the URI of the response from a string.
     *
     * @param uriString the URI of the response as a string
     */
    public void setUriString(String uriString) {
        this.uri = URI.create(uriString);
    }
}
