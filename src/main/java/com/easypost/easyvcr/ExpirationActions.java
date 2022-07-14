package com.easypost.easyvcr;

/**
 * Enums representing different actions to take when a recording is expired.
 */
public enum ExpirationActions {
    /**
     * Warn that the recorded interaction is expired, but proceed as normal.
     */
    Warn,
    /**
     * Throw an exception that the recorded interaction is expired.
     */
    Throw_Exception,
    /**
     * Automatically re-record the recorded interaction. This cannot be used with {@link Mode#Replay}.
     */
    Record_Again,
}
