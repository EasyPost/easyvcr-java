package com.easypost.easyvcr;

import com.easypost.easyvcr.internalutilities.Tools;
import com.easypost.easyvcr.internalutilities.json.Serialization;
import com.google.gson.JsonSyntaxException;
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
        queryParamsToCensor = new ArrayList<>();
        bodyParamsToCensor = new ArrayList<>();
        headersToCensor = new ArrayList<>();
        censorText = censorString;
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
        for (String key : Statics.DEFAULT_CREDENTIAL_HEADERS_TO_HIDE) {
            censors.hideHeader(key);
        }
        for (String key : Statics.DEFAULT_CREDENTIAL_PARAMETERS_TO_HIDE) {
            censors.hideQueryParameter(key);
            censors.hideBodyParameter(key);
        }
        return censors;
    }

    /**
     * Add a rule to censor a specified body parameter.
     * Note: Only top-level pairs can be censored.
     *
     * @param parameterKey Key of body parameter to censor.
     * @return This Censors factory.
     */
    public Censors hideBodyParameter(String parameterKey) {
        bodyParamsToCensor.add(parameterKey);
        return this;
    }

    /**
     * Add a rule to censor a specified header key.
     * Note: This will censor the header key in both the request and response.
     *
     * @param headerKey Key of header to censor.
     * @return This Censors factory.
     */
    public Censors hideHeader(String headerKey) {
        headersToCensor.add(headerKey);
        return this;
    }

    /**
     * Add a rule to censor a specified query parameter.
     *
     * @param parameterKey Key of query parameter to censor.
     * @return This Censors factory.
     */
    public Censors hideQueryParameter(String parameterKey) {
        queryParamsToCensor.add(parameterKey);
        return this;
    }

    /**
     * Censor the appropriate body parameters.
     *
     * @param body String representation of request body to apply censors to.
     * @return Censored string representation of request body.
     */
    public String applyBodyParametersCensors(String body) {
        if (body == null || body.length() == 0) {
            // short circuit if body is null or empty
            return body;
        }

        Map<String, Object> bodyParameters;
        try {
            bodyParameters = Serialization.convertJsonToObject(body, Map.class);
        } catch (JsonSyntaxException ignored) {
            // short circuit if body is not a JSON dictionary
            return body;
        }

        if (bodyParameters == null || bodyParameters.size() == 0) {
            // short circuit if there are no body parameters
            return body;
        }

        for (String parameterKey : bodyParamsToCensor) {
            if (bodyParameters.containsKey(parameterKey)) {
                bodyParameters.put(parameterKey, censorText);
            }
        }

        return Serialization.convertObjectToJson(bodyParameters);
    }

    /**
     * Censor the appropriate headers.
     *
     * @param headers Map of headers to apply censors to.
     * @return Censored map of headers.
     */
    public Map<String, List<String>> applyHeadersCensors(Map<String, List<String>> headers) {
        if (headers == null || headers.size() == 0) {
            // short circuit if there are no headers to censor
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
    public String applyQueryParametersCensors(String url) {
        if (url == null || url.length() == 0) {
            // short circuit if url is null
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
}
