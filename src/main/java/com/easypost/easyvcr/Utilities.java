package com.easypost.easyvcr;

import javax.net.ssl.HttpsURLConnection;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;

public class Utilities {

    public static boolean responseCameFromRecording(HttpsURLConnection connection) {
        return responseCameFromRecording((HttpURLConnection) connection);
    }

    public static boolean responseCameFromRecording(HttpURLConnection connection) {
        Map<String, List<String>> headers = connection.getHeaderFields();
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            if (entry.getKey() != null && entry.getKey().equals(Statics.viaRecordingHeaderKey)) {
                return true;
            }
        }
        return false;
    }
}
