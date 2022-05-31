package com.easypost.easyvcr;

import com.easypost.easyvcr.internalutilities.Tools;
import com.easypost.easyvcr.internalutilities.json.Serialization;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Censoring capabilities for EasyVCR.
 */
public final class Censors {
    /**
     * The body parameters to censor.
     */
    private final List<String> bodyParamsToCensor;
    /**
     * Whether censor keys are case sensitive.
     */
    private final boolean caseSensitive;
    /**
     * The string to replace censored data with.
     */
    private final String censorText;
    /**
     * The headers to censor.
     */
    private final List<String> headersToCensor;
    /**
     * The query parameters to censor.
     */
    private final List<String> queryParamsToCensor;

    /**
     * Initialize a new instance of the Censors factory, using default censor string.
     */
    public Censors() {
        this(Statics.DEFAULT_CENSOR_TEXT);
    }

    /**
     * Initialize a new instance of the Censors factory.
     *
     * @param censorString The string to use to censor sensitive information.
     */
    public Censors(String censorString) {
        this(censorString, false);
    }

    /**
     * Initialize a new instance of the Censors factory.
     *
     * @param censorString  The string to use to censor sensitive information.
     * @param caseSensitive Whether to use case sensitive censoring.
     */
    public Censors(String censorString, boolean caseSensitive) {
        this.queryParamsToCensor = new ArrayList<>();
        this.bodyParamsToCensor = new ArrayList<>();
        this.headersToCensor = new ArrayList<>();
        this.censorText = censorString;
        this.caseSensitive = caseSensitive;
    }

    /**
     * Default censors is to not censor anything.
     *
     * @return Default set of censors.
     */
    public static Censors regular() {
        return new Censors();
    }

    /**
     * Default sensitive censors is to censor common private information (i.e. API keys, auth tokens, etc.)
     *
     * @return Default sensitive set of censors.
     */
    public static Censors strict() {
        Censors censors = new Censors();
        censors.hideHeaders(Statics.DEFAULT_CREDENTIAL_HEADERS_TO_HIDE);
        censors.hideBodyParameters(Statics.DEFAULT_CREDENTIAL_PARAMETERS_TO_HIDE);
        censors.hideQueryParameters(Statics.DEFAULT_CREDENTIAL_PARAMETERS_TO_HIDE);
        return censors;
    }

    /**
     * Add a rule to censor specified body parameters.
     * Note: Only top-level pairs can be censored.
     *
     * @param parameterKeys Keys of body parameters to censor.
     * @return This Censors factory.
     */
    public Censors hideBodyParameters(List<String> parameterKeys) {
        bodyParamsToCensor.addAll(parameterKeys);
        return this;
    }

    /**
     * Add a rule to censor specified header keys.
     * Note: This will censor the header keys in both the request and response.
     *
     * @param headerKeys Keys of headers to censor.
     * @return This Censors factory.
     */
    public Censors hideHeaders(List<String> headerKeys) {
        headersToCensor.addAll(headerKeys);
        return this;
    }

    /**
     * Add a rule to censor specified query parameters.
     *
     * @param parameterKeys Keys of query parameters to censor.
     * @return This Censors factory.
     */
    public Censors hideQueryParameters(List<String> parameterKeys) {
        queryParamsToCensor.addAll(parameterKeys);
        return this;
    }

    /**
     * Censor the appropriate body parameters.
     *
     * @param body String representation of request body to apply censors to.
     * @return Censored string representation of request body.
     */
    public String censorBodyParameters(String body) {
        if (body == null || body.length() == 0) {
            // short circuit if body is null or empty
            return body;
        }

        if (bodyParamsToCensor.size() == 0) {
            // short circuit if there are no censors to apply
            return body;
        }

        // TODO: Future different content type support here, only JSON is supported currently
        return censorJsonBodyParameters(body);
    }

    /**
     * Censor the appropriate headers.
     *
     * @param headers Map of headers to apply censors to.
     * @return Censored map of headers.
     */
    public Map<String, List<String>> censorHeaders(Map<String, List<String>> headers) {
        if (headers == null || headers.size() == 0) {
            // short circuit if there are no headers to censor
            return headers;
        }

        if (headersToCensor.size() == 0) {
            // short circuit if there are no censors to apply
            return headers;
        }

        final Map<String, List<String>> headersCopy = new HashMap<>(headers);

        for (String headerKey : headersToCensor) {
            if (headersCopy.containsKey(headerKey)) {
                headersCopy.put(headerKey, Collections.singletonList(censorText));
            }
        }
        return headersCopy;
    }

    /**
     * Censor the appropriate query parameters.
     *
     * @param url Full URL string to apply censors to.
     * @return Censored URL string.
     */
    public String censorQueryParameters(String url) {
        if (url == null || url.length() == 0) {
            // short circuit if url is null
            return url;
        }

        if (queryParamsToCensor.size() == 0) {
            // short circuit if there are no censors to apply
            return url;
        }

        URI uri = URI.create(url);
        Map<String, String> queryParameters = Tools.queryParametersToMap(uri);
        if (queryParameters.size() == 0) {
            // short circuit if there are no query parameters to censor
            return url;
        }

        for (String parameterKey : queryParamsToCensor) {
            if (queryParameters.containsKey(parameterKey)) {
                queryParameters.put(parameterKey, censorText);
            }
        }

        List<NameValuePair> censoredQueryParametersList = Tools.mapToQueryParameters(queryParameters);
        String formattedQueryParameters = URLEncodedUtils.format(censoredQueryParametersList, StandardCharsets.UTF_8);
        if (formattedQueryParameters.length() == 0) {
            // short circuit if there are no query parameters to censor
            return url;
        }

        return uri.getScheme() + "://" + uri.getHost() + uri.getPath() + "?" + formattedQueryParameters;
    }

    private List<Object> applyBodyCensors(List<Object> list) {
        if (list == null || list.size() == 0) {
            // short circuit if list is null or empty
            return list;
        }

        List<Object> censoredList = new ArrayList<>();

        for (Object object : list) {
            Object value = object;
            if (Utilities.isDictionary(value)) {
                // recursively censor inner dictionaries
                try {
                    // change the value if can be parsed as a dictionary
                    value = applyBodyCensors((Map<String, Object>) value);
                } catch (ClassCastException e) {
                    // otherwise, skip censoring
                }
            } else if (Utilities.isList(value)) {
                // recursively censor list elements
                try {
                    // change the value if can be parsed as a list
                    value = applyBodyCensors((List<Object>) value);
                } catch (ClassCastException e) {
                    // otherwise, skip censoring
                }
            }  // either a primitive or null, no censoring needed

            censoredList.add(value);
        }

        return censoredList;

    }

    private Map<String, Object> applyBodyCensors(Map<String, Object> dictionary) {
        if (dictionary == null || dictionary.size() == 0) {
            // short circuit if dictionary is null or empty
            return dictionary;
        }

        Map<String, Object> censoredBodyDictionary = new HashMap<>();

        for (Map.Entry<String, Object> entry : dictionary.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (keyShouldBeCensored(key, this.bodyParamsToCensor)) {
                if (value == null) {
                    // don't need to worry about censoring something that's null
                    // (don't replace null with the censor string)
                    continue;
                } else if (Utilities.isDictionary(value)) {
                    // replace with empty dictionary
                    censoredBodyDictionary.put(key, new HashMap<>());
                } else if (Utilities.isList(value)) {
                    // replace with empty array
                    censoredBodyDictionary.put(key, new ArrayList<>());
                } else {
                    // replace with censor text
                    censoredBodyDictionary.put(key, this.censorText);
                }
            } else {
                if (Utilities.isDictionary(value)) {
                    // recursively censor inner dictionaries
                    try {
                        // change the value if can be parsed as a dictionary
                        value = applyBodyCensors((Map<String, Object>) value);
                    } catch (ClassCastException e) {
                        // otherwise, skip censoring
                    }
                } else if (Utilities.isList(value)) {
                    // recursively censor list elements
                    try {
                        // change the value if can be parsed as a list
                        value = applyBodyCensors((List<Object>) value);
                    } catch (ClassCastException e) {
                        // otherwise, skip censoring
                    }
                }

                censoredBodyDictionary.put(key, value);
            }
        }

        return censoredBodyDictionary;
    }

    private String censorJsonBodyParameters(String body) {
        Map<String, Object> bodyDictionary;
        try {
            bodyDictionary = Serialization.convertJsonToObject(body, Map.class);
            Map<String, Object> censoredBodyDictionary = applyBodyCensors(bodyDictionary);
            return censoredBodyDictionary == null ? body : Serialization.convertObjectToJson(censoredBodyDictionary);
        } catch (Exception ex) {
            // body is not a JSON dictionary
            try {
                List<Object> bodyList = Serialization.convertJsonToObject(body, List.class);
                List<Object> censoredBodyList = applyBodyCensors(bodyList);
                return censoredBodyList == null ? body : Serialization.convertObjectToJson(censoredBodyList);
            } catch (Exception ex2) {
                // short circuit if body is not a JSON dictionary or JSON list
                return body;
            }
        }
    }

    private boolean keyShouldBeCensored(String foundKey, List<String> keysToCensor) {
        // keysToCensor are already cased as needed
        if (!this.caseSensitive) {
            foundKey = foundKey.toLowerCase();
        }

        return keysToCensor.contains(foundKey);
    }
}
