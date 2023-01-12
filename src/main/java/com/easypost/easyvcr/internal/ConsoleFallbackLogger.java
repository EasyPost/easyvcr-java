package com.easypost.easyvcr.internal;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A logger wrapper that, if there's no proper internal logger, writes to the console instead.
 */
public final class ConsoleFallbackLogger {
    private final Logger logger;
    private final String name;

    /**
     * Constructor for ConsoleFallbackLogger.
     *
     * @param logger {@link Logger} logger to optionally use internally.
     * @param name   Name of the application.
     */
    public ConsoleFallbackLogger(Logger logger, String name) {
        this.logger = logger;
        this.name = name;
    }

    /**
     * Logs an error message to the logger or console.
     *
     * @param message The message to log.
     */
    public void error(String message) {
        if (logger != null) {
            logger.log(Level.INFO, makeMessage(message));
        } else {
            System.err.println(makeMessage(message));
        }
    }

    /**
     * Logs a warning message to the logger or console.
     *
     * @param message The message to log.
     */
    public void warning(String message) {
        if (logger != null) {
            logger.log(Level.WARNING, makeMessage(message));
        } else {
            System.out.println(makeMessage(message));
        }
    }

    private String makeMessage(String message) {
        return name + ": " + message;
    }
}
