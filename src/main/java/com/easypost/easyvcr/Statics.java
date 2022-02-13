package com.easypost.easyvcr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Statics {
    /**
     * Default headers to censor (credential-related headers).
     */
    static final List<String> defaultCredentialHeadersToHide =
            new ArrayList<>(Collections.singletonList("Authorization"));

    /**
     * Default parameters to censor (credential-related parameters).
     */
    static final List<String> defaultCredentialParametersToHide = new ArrayList<>(
            Arrays.asList("api_key", "apiKey", "key", "api_token", "apiToken", "token", "access_token", "client_id",
                    "client_secret", "password", "secret", "username"));

    /**
     * Default string to use to censor sensitive information.
     */
    static final String defaultCensorText = "*****";

    static public final String viaRecordingHeaderKey = "X-Via-EasyVCR-Recording";

    public static Map<String, String> getReplayHeaders() {
        Map<String, String> replayHeaders = new HashMap<String, String>();
        replayHeaders.put(viaRecordingHeaderKey, "true");
        return replayHeaders;
    }
}
