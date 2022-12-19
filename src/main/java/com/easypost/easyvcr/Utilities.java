package com.easypost.easyvcr;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import javax.net.ssl.HttpsURLConnection;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
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
     * @param obj The object to check.
     * @return True if the object is a dictionary.
     */
    public static boolean isDictionary(Object obj) {
        return obj instanceof Map;
    }

    /**
     * Check if the object is a list.
     * @param obj The object to check.
     * @return True if the object is a list.
     */
    public static boolean isList(Object obj) {
        return obj instanceof List;
    }

    /**
     * Remove elements from a JSON string.
     * @param json The JSON string to remove elements from.
     * @param elements The elements to remove.
     * @return The JSON string without the elements.
     */
    public static String removeJsonElements(String json, List<CensorElement> elements) {
        if (json == null || elements == null) {
            return json;
        }

        return Censors.censorJsonData(json, "FILTERED", elements);
    }

    /**
     * Convert a URI's query parameters to a Map.
     *
     * @param uri The URI.
     * @return The Map of query parameters.
     */
    public static Map<String, String> queryParametersToMap(URI uri) {
        List<NameValuePair> receivedQueryDict = URLEncodedUtils.parse(uri, StandardCharsets.UTF_8);
        if (receivedQueryDict == null || receivedQueryDict.size() == 0) {
            return Collections.emptyMap();
        }
        Map<String, String> queryDict = new java.util.Hashtable<>();
        for (NameValuePair pair : receivedQueryDict) {
            queryDict.put(pair.getName(), pair.getValue());
        }
        return queryDict;
    }

    /**
     * Extract the path from a URI.
     *
     * @param uri The URI to extract the path from.
     * @return The path.
     */
    public static String extractPathFromUri(URI uri) {
        String uriString = uri.toString();

        // strip the query parameters
        uriString = uriString.replace(uri.getQuery(), "");

        if (uriString.endsWith("?")) {
            uriString = uriString.substring(0, uriString.length() - 1);
        }

        // strip the scheme
        uriString = uriString.replace(uri.getScheme() + "://", "");

        return uriString;
    }
}
