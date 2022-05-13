package com.easypost.easyvcr.requestelements;

/**
 * Represents a status of an HTTP request tracked by EasyVCR.
 */
public final class Status {
    /**
     * The status code of the HTTP request.
     */
    private int code;

    /**
     * The status description of the HTTP request.
     */
    private String message;

    /**
     * Constructs a new Status object. object.
     *
     * @param code    The status code of the HTTP request.
     * @param message The status description of the HTTP request.
     */
    public Status(int code, String message) {
        setCode(code);
        setMessage(message);
    }

    /**
     * Returns the status code of the HTTP request.
     *
     * @return The status code of the HTTP request.
     */
    public int getCode() {
        return this.code;
    }

    /**
     * Sets the status code of the HTTP request.
     *
     * @param code The status code of the HTTP request.
     */
    public void setCode(int code) {
        this.code = code;
    }

    /**
     * Returns the status description of the HTTP request.
     *
     * @return The status description of the HTTP request.
     */
    public String getMessage() {
        return this.message;
    }

    /**
     * Sets the status description of the HTTP request.
     *
     * @param message The status description of the HTTP request.
     */
    public void setMessage(String message) {
        this.message = message;
    }
}
