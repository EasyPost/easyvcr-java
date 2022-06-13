package com.easypost.easyvcr.internalutilities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiPredicate;

import static java.lang.String.format;

public abstract class Utils {
    private static final boolean[] T_CHAR = new boolean[256];
    private static final boolean[] FIELD_V_CHAR = new boolean[256];

    private static final String HEADER_CONNECTION = "Connection";
    private static final String HEADER_UPGRADE = "Upgrade";

    private static final Set<String> DISALLOWED_HEADERS_SET = getDisallowedHeaders();

    public static final BiPredicate<String, String> ALLOWED_HEADERS =
            (header, unused) -> !DISALLOWED_HEADERS_SET.contains(header);

    public static final BiPredicate<String, String> VALIDATE_USER_HEADER = (name, value) -> {
        assert name != null : "null header name";
        assert value != null : "null header value";
        if (!isValidName(name)) {
            throw newIAE("invalid header name: \"%s\"", name);
        }
        if (!ALLOWED_HEADERS.test(name, null)) {
            throw newIAE("restricted header name: \"%s\"", name);
        }
        if (!isValidValue(value)) {
            throw newIAE("invalid header value for %s: \"%s\"", name, value);
        }
        return true;
    };

    private static Set<String> getDisallowedHeaders() {
        Set<String> headers = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        // create a collection with all strings
        Collection<String> headerKeys = new ArrayList<>();
        headerKeys.add("connection");
        headerKeys.add("content-length");
        headerKeys.add("expect");
        headerKeys.add("host");
        headerKeys.add("upgrade");
        headers.addAll(headerKeys);

        String v = null;
        if (v != null) {
            // any headers found are removed from set.
            String[] tokens = v.trim().split(",");
            for (String token : tokens) {
                headers.remove(token);
            }
            return Collections.unmodifiableSet(headers);
        } else {
            return Collections.unmodifiableSet(headers);
        }
    }

    /**
     * Validates a RFC 7230 field-name.
     *
     * @param token the field-name to validate
     * @return true if the field-name is valid
     */
    public static boolean isValidName(String token) {
        for (int i = 0; i < token.length(); i++) {
            char c = token.charAt(i);
            if (c > 255 || !T_CHAR[c]) {
                return false;
            }
        }
        return !token.isEmpty();
    }

    /**
     * Validates a RFC 7230 field-value.
     * <p>
     * "Obsolete line folding" rule
     * <p>
     * obs-fold = CRLF 1*( SP / HTAB )
     * <p>
     * is not permitted!
     *
     * @param token the field-value to validate
     * @return true if the field-value is valid
     */
    public static boolean isValidValue(String token) {
        for (int i = 0; i < token.length(); i++) {
            char c = token.charAt(i);
            if (c > 255) {
                return false;
            }
            if (c == ' ' || c == '\t') {
                continue;
            } else if (!FIELD_V_CHAR[c]) {
                return false; // forbidden byte
            }
        }
        return true;
    }

    /**
     * Throw an IllegalArgumentException with a formatted message.
     *
     * @param message the message to use in the exception
     * @param args    the arguments to use in the formatted message
     * @return IllegalArgumentException
     */
    public static IllegalArgumentException newIAE(String message, Object... args) {
        return new IllegalArgumentException(format(message, args));
    }
}
