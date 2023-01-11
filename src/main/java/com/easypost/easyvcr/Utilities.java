package com.easypost.easyvcr;

import javax.net.ssl.HttpsURLConnection;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.List;
import java.util.Map;

public abstract class Utilities {

    /**
     * Check if the connection came from an EasyVCR recording.
     * @param connection The connection to check.
     * @return True if the connection came from an EasyVCR recording.
     */
    public static boolean responseCameFromRecording(HttpsURLConnection connection) {
        return responseCameFromRecording((HttpURLConnection) connection);
    }

    /**
     * Check if the connection came from an EasyVCR recording.
     * @param connection The connection to check.
     * @return True if the connection came from an EasyVCR recording.
     */
    public static boolean responseCameFromRecording(HttpURLConnection connection) {
        Map<String, List<String>> headers = connection.getHeaderFields();
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            if (entry.getKey() != null && entry.getKey().equals(Statics.VIA_RECORDING_HEADER_KEY)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if the object is a dictionary.
     * DEPRECATED: Not intended for public use.
     *
     * @param obj The object to check.
     * @return True if the object is a dictionary.
     */
    @Deprecated
    public static boolean isDictionary(Object obj) {
        return com.easypost.easyvcr.internal.Utilities.isDictionary(obj);
    }

    /**
     * Check if the object is a list.
     * DEPRECATED: Not intended for public use.
     *
     * @param obj The object to check.
     * @return True if the object is a list.
     */
    @Deprecated
    public static boolean isList(Object obj) {
        return com.easypost.easyvcr.internal.Utilities.isList(obj);
    }

    /**
     * Remove elements from a JSON string.
     * DEPRECATED: Not intended for public use.
     *
     * @param json The JSON string to remove elements from.
     * @param elements The elements to remove.
     * @return The JSON string without the elements.
     */
    @Deprecated
    public static String removeJsonElements(String json, List<CensorElement> elements) {
        return com.easypost.easyvcr.internal.Utilities.removeJsonElements(json, elements);
    }

    /**
     * Convert a URI's query parameters to a Map.
     * DEPRECATED: Not intended for public use.
     *
     * @param uri The URI.
     * @return The Map of query parameters.
     */
    @Deprecated
    public static Map<String, String> queryParametersToMap(URI uri) {
        return com.easypost.easyvcr.internal.Utilities.queryParametersToMap(uri);
    }

    /**
     * Extract the path from a URI.
     * DEPRECATED: Not intended for public use.
     *
     * @param uri The URI to extract the path from.
     * @return The path.
     */
    @Deprecated
    public static String extractPathFromUri(URI uri) {
        return com.easypost.easyvcr.internal.Utilities.extractPathFromUri(uri);
    }
}
