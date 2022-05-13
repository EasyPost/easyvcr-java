package com.easypost.easyvcr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Statics {
    public static final String VIA_RECORDING_HEADER_KEY = "X-Via-EasyVCR-Recording";
    /**
     * Default headers to censor (credential-related headers).
     */
    public static final List<String> DEFAULT_CREDENTIAL_HEADERS_TO_HIDE =
            new ArrayList<>(Collections.singletonList("Authorization"));
    /**
     * Default parameters to censor (credential-related parameters).
     */
    public static final List<String> DEFAULT_CREDENTIAL_PARAMETERS_TO_HIDE = new ArrayList<>(
            Arrays.asList("api_key", "apiKey", "key", "api_token", "apiToken", "token", "access_token", "client_id",
                    "client_secret", "password", "secret", "username"));
    /**
     * Default string to use to censor sensitive information.
     */
    public static final String DEFAULT_CENSOR_TEXT = "*****";

    /**
     * Get a map of the EasyVCR replay headers.
     * @return a map of the EasyVCR replay headers.
     */
    public static Map<String, String> getReplayHeaders() {
        Map<String, String> replayHeaders = new HashMap<String, String>();
        replayHeaders.put(VIA_RECORDING_HEADER_KEY, "true");
        return replayHeaders;
    }
}
