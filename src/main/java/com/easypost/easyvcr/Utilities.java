package com.easypost.easyvcr;

import javax.net.ssl.HttpsURLConnection;
import java.net.HttpURLConnection;
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
}
