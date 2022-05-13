package com.easypost.easyvcr.requestelements;

import java.time.Instant;

/**
 * Represents an HTTP request-response pair tracked by EasyVCR.
 */
public final class HttpInteraction extends HttpElement {
    /**
     * Timestamp of when the interaction was recorded.
     */
    private long recordedAt;

    /**
     * The HTTP request.
     */
    private Request request;

    /**
     * The HTTP response.
     */
    private Response response;

    /**
     * The duration of the request in milliseconds.
     */
    private long duration = 0;

    /**
     * Constructs a new HTTPInteraction object.
     *
     * @param request  The HTTP request.
     * @param response The HTTP response.
     * @param duration The duration of the request in milliseconds.
     */
    public HttpInteraction(Request request, Response response, long duration) {
        this.request = request;
        this.response = response;
        this.recordedAt = Instant.now().getEpochSecond();
        this.duration = duration;
    }

    /**
     * Returns the timestamp of when the interaction was recorded.
     *
     * @return The timestamp of when the interaction was recorded.
     */
    public long getRecordedAt() {
        return this.recordedAt;
    }

    /**
     * Set the timestamp of when the interaction was recorded.
     *
     * @param recordedAt The timestamp of when the interaction was recorded.
     */
    public void setRecordedAt(final long recordedAt) {
        this.recordedAt = recordedAt;
    }

    /**
     * Returns the HTTP request.
     *
     * @return The HTTP request.
     */
    public Request getRequest() {
        return this.request;
    }

    /**
     * Sets the HTTP request.
     *
     * @param request The HTTP request.
     */
    public void setRequest(final Request request) {
        this.request = request;
    }

    /**
     * Returns the HTTP response.
     *
     * @return The HTTP response.
     */
    public Response getResponse() {
        return this.response;
    }

    /**
     * Sets the HTTP response.
     *
     * @param response The HTTP response.
     */
    public void setResponse(final Response response) {
        this.response = response;
    }

    /**
     * Returns the duration of the request in milliseconds.
     *
     * @return The duration of the request in milliseconds.
     */
    public long getDuration() {
        return this.duration;
    }

    /**
     * Sets the duration of the request in milliseconds.
     *
     * @param duration The duration of the request in milliseconds.
     */
    public void setDuration(final int duration) {
        this.duration = duration;
    }
}
