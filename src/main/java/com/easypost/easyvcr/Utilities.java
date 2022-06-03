package com.easypost.easyvcr;

import javax.net.ssl.HttpsURLConnection;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;

public class Utilities {

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
}
