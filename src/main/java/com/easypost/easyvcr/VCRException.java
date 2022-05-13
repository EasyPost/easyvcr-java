package com.easypost.easyvcr;

/**
 * Custom exception for EasyVCR.
 */
public final class VCRException extends Exception {

    /**
     * Constructs a new VCRException with the specified detail message.
     *
     * @param message the error message.
     */
    public VCRException(String message) {
        super(message);
    }
}
