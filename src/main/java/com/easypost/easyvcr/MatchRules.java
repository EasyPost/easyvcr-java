package com.easypost.easyvcr;

import com.easypost.easyvcr.internal.Utilities;
import com.easypost.easyvcr.requestelements.Request;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Rule set for matching requests against recorded requests.
 */
public final class MatchRules {
    private final List<BiFunction<Request, Request, Boolean>> rules;

    /**
     * Construct a new MatchRules factory.
     */
    public MatchRules() {
        rules = new ArrayList<>();
    }

    /**
     * Default rule is to match on the method and URL.
     *
     * @return Default MatchRules object.
     */
    public static MatchRules regular() {
        return new MatchRules().byMethod().byFullUrl();
    }

    /**
     * Default strict rule is to match on the method, URL and body.
     *
     * @return Default strict MatchRules object.
     */
    public static MatchRules strict() {
        return new MatchRules().byMethod().byFullUrl().byBody();
    }

    private void by(BiFunction<Request, Request, Boolean> rule) {
        rules.add(rule);
    }

    /**
     * Add a rule to compare the base URLs of the requests.
     *
     * @return This MatchRules factory.
     */
    public MatchRules byBaseUrl() {
        by((received, recorded) -> {
            String receivedUri = getBaseUrl(received.getUri());
            String recordedUri = getBaseUrl(recorded.getUri());
            return receivedUri.equalsIgnoreCase(recordedUri);
        });
        return this;
    }

    private static String getBaseUrl(URI url) {

        String baseUrl = url.getScheme() + "://" + url.getHost();
        if (url.getPort() != -1) {
            baseUrl += ":" + url.getPort();
        }
        if (url.getPath() != null) {
            baseUrl += url.getPath();
        }
        return baseUrl;
    }

    /**
     * Add a rule to compare the bodies of the requests.
     *
     * @param ignoredElements List of body elements to ignore when comparing the requests.
     * @return This MatchRules factory.
     */
    public MatchRules byBody(List<CensorElement> ignoredElements) {
        by((received, recorded) -> {
            String receivedBody = received.getBody();
            String recordedBody = recorded.getBody();

            if (receivedBody == null && recordedBody == null) {
                // both have null bodies, so they match
                return true;
            }

            if (receivedBody == null || recordedBody == null) {
                // one has a null body, so they don't match
                return false;
            }

            // remove ignored elements from the body
            receivedBody = Utilities.removeJsonElements(receivedBody, ignoredElements);
            recordedBody = Utilities.removeJsonElements(recordedBody, ignoredElements);

            // convert body to base64string to assist comparison by removing special characters
            receivedBody = Utilities.toBase64String(receivedBody);
            recordedBody = Utilities.toBase64String(recordedBody);
            return receivedBody.equalsIgnoreCase(recordedBody);
        });
        return this;
    }

    /**
     * Add a rule to compare the bodies of the requests.
     *
     * @return This MatchRules factory.
     */
    public MatchRules byBody() {
        return byBody(null);
    }

    /**
     * Add a rule to compare the entire requests.
     * Note, this rule is very strict, and will fail if the requests are not identical (including duration).
     * It is highly recommended to use the other rules to compare the requests.
     *
     * @return This MatchRules factory.
     */
    public MatchRules byEverything() {
        by((received, recorded) -> {
            String receivedRequest = received.toJson();
            String recordedRequest = recorded.toJson();
            return receivedRequest.equalsIgnoreCase(recordedRequest);
        });
        return this;
    }

    /**
     * Add a rule to compare the full URLs (including query parameters) of the requests.
     * Default: query parameter order does not matter.
     *
     * @return This MatchRules factory.
     */
    public MatchRules byFullUrl() {
        return byFullUrl(false);
    }

    /**
     * Add a rule to compare the full URLs (including query parameters) of the requests.
     *
     * @param exact If true, query parameters must be in the same exact order to match.
     *              If false, query parameter order doesn't matter.
     * @return This MatchRules factory.
     */
    public MatchRules byFullUrl(boolean exact) {
        if (exact) {
            by((received, recorded) -> {
                String receivedUri = Utilities.toBase64String(received.getUriString());
                String recordedUri = Utilities.toBase64String(recorded.getUriString());
                return receivedUri.equalsIgnoreCase(recordedUri);
            });
        } else {
            byBaseUrl();
            by((received, recorded) -> {
                Map<String, String> receivedQuery = Utilities.queryParametersToMap(received.getUri());
                Map<String, String> recordedQuery = Utilities.queryParametersToMap(recorded.getUri());
                if (receivedQuery.size() != recordedQuery.size()) {
                    return false;
                }
                for (Map.Entry<String, String> entry : receivedQuery.entrySet()) {
                    if (!recordedQuery.containsKey(entry.getKey())) {
                        return false;
                    }
                }
                return true;
            });
        }

        return this;
    }

    /**
     * Add a rule to compare a specific header of the requests.
     *
     * @param name Key of the header to compare.
     * @return This MatchRules factory.
     */
    public MatchRules byHeader(String name) {
        by((received, recorded) -> {
            Map<String, List<String>> receivedHeaders = received.getHeaders();
            Map<String, List<String>> recordedHeaders = recorded.getHeaders();
            if (!receivedHeaders.containsKey(name) || !recordedHeaders.containsKey(name)) {
                return false;
            }
            List<String> receivedHeader = receivedHeaders.get(name);
            List<String> recordedHeader = recordedHeaders.get(name);
            return receivedHeader.equals(recordedHeader);
        });
        return this;
    }

    /**
     * Add a rule to compare the headers of the requests.
     * Default: headers strictness is set to false.
     *
     * @return This MatchRules factory.
     */
    public MatchRules byHeaders() {
        return byHeaders(false);
    }

    /**
     * Add a rule to compare the headers of the requests.
     *
     * @param exact If true, both requests must have the exact same headers.
     *              If false, as long as the evaluated request has all the headers of the matching request
     *              (and potentially more), the match is considered valid.
     * @return This MatchRules factory.
     */
    public MatchRules byHeaders(boolean exact) {
        if (exact) {
            // first, we'll check that there are the same number of headers in both requests.
            // If they are, then the second check is guaranteed to compare all headers.
            by((received, recorded) -> received.getHeaders().size() == recorded.getHeaders().size());
        }

        by((received, recorded) -> {
            Map<String, List<String>> receivedHeaders = received.getHeaders();
            Map<String, List<String>> recordedHeaders = recorded.getHeaders();
            for (String headerName : receivedHeaders.keySet()) {
                if (!recordedHeaders.containsKey(headerName)) {
                    return false;
                }
                if (!receivedHeaders.get(headerName).equals(recordedHeaders.get(headerName))) {
                    return false;
                }
            }
            return true;
        });
        return this;
    }

    /**
     * Add a rule to compare the HTTP methods of the requests.
     *
     * @return This MatchRules factory.
     */
    public MatchRules byMethod() {
        by((received, recorded) -> received.getMethod().equalsIgnoreCase(recorded.getMethod()));
        return this;
    }

    /**
     * Execute rules to determine if the received request matches the recorded request.
     *
     * @param receivedRequest Request to find a match for.
     * @param recordedRequest Request to compare against.
     * @return True if the received request matches the recorded request, false otherwise.
     */
    public boolean requestsMatch(Request receivedRequest, Request recordedRequest) {
        if (rules.size() == 0) {
            return true;
        }

        for (BiFunction<Request, Request, Boolean> rule : rules) {
            if (!rule.apply(receivedRequest, recordedRequest)) {
                return false;
            }
        }
        return true;
    }
}
