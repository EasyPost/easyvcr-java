package com.easypost.easyvcr;

import com.easypost.easyvcr.internalutilities.Tools;
import com.easypost.easyvcr.internalutilities.json.Serialization;
import com.google.gson.JsonParseException;
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
@SuppressWarnings("unchecked")
public final class Censors {
    /**
     * The body elements to censor.
     */
    private final List<CensorElement> bodyElementsToCensor;
    /**
     * The string to replace censored data with.
     */
    private final String censorText;
    /**
     * The headers to censor.
     */
    private final List<CensorElement> headersToCensor;
    /**
     * The query parameters to censor.
     */
    private final List<CensorElement> queryParamsToCensor;

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
        this.queryParamsToCensor = new ArrayList<>();
        this.bodyElementsToCensor = new ArrayList<>();
        this.headersToCensor = new ArrayList<>();
        this.censorText = censorString;
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
        censors.censorHeadersByKeys(Statics.DEFAULT_CREDENTIAL_HEADERS_TO_HIDE);
        censors.censorBodyElementsByKeys(Statics.DEFAULT_CREDENTIAL_PARAMETERS_TO_HIDE);
        censors.censorQueryParametersByKeys(Statics.DEFAULT_CREDENTIAL_PARAMETERS_TO_HIDE);
        return censors;
    }

    /**
     * Apply censors to a JSON list.
     *
     * @param list             JSON list to process.
     * @param censorText       Text to use when censoring an element.
     * @param elementsToCensor List of elements to find and censor.
     * @return Censored JSON list.
     */
    private static List<Object> applyJsonCensors(List<Object> list, String censorText,
                                                 List<CensorElement> elementsToCensor) {
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
                    value = applyJsonCensors((Map<String, Object>) value, censorText, elementsToCensor);
                } catch (ClassCastException e) {
                    // otherwise, skip censoring
                }
            } else if (Utilities.isList(value)) {
                // recursively censor list elements
                try {
                    // change the value if can be parsed as a list
                    value = applyJsonCensors((List<Object>) value, censorText, elementsToCensor);
                } catch (ClassCastException e) {
                    // otherwise, skip censoring
                }
            }  // either a primitive or null, no censoring needed

            censoredList.add(value);
        }

        return censoredList;
    }

    /**
     * Apply censors to a JSON dictionary.
     *
     * @param dictionary       JSON dictionary to process.
     * @param censorText       Text to use when censoring an element.
     * @param elementsToCensor List of elements to find and censor.
     * @return Censored JSON dicstionary.
     */
    private static Map<String, Object> applyJsonCensors(Map<String, Object> dictionary, String censorText,
                                                        List<CensorElement> elementsToCensor) {
        if (dictionary == null || dictionary.size() == 0) {
            // short circuit if dictionary is null or empty
            return dictionary;
        }

        Map<String, Object> censoredBodyDictionary = new HashMap<>();

        for (Map.Entry<String, Object> entry : dictionary.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (elementShouldBeCensored(key, elementsToCensor)) {
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
                    censoredBodyDictionary.put(key, censorText);
                }
            } else {
                if (Utilities.isDictionary(value)) {
                    // recursively censor inner dictionaries
                    try {
                        // change the value if can be parsed as a dictionary
                        value = applyJsonCensors((Map<String, Object>) value, censorText, elementsToCensor);
                    } catch (ClassCastException e) {
                        // otherwise, skip censoring
                    }
                } else if (Utilities.isList(value)) {
                    // recursively censor list elements
                    try {
                        // change the value if can be parsed as a list
                        value = applyJsonCensors((List<Object>) value, censorText, elementsToCensor);
                    } catch (ClassCastException e) {
                        // otherwise, skip censoring
                    }
                }

                censoredBodyDictionary.put(key, value);
            }
        }

        return censoredBodyDictionary;
    }

    /**
     * Apply censors to a JSON string.
     *
     * @param data             The JSON string to censor.
     * @param censorText       The string to use to censor sensitive information.
     * @param elementsToCensor The body elements to censor.
     * @return The censored JSON string.
     */
    public static String censorJsonData(String data, String censorText, List<CensorElement> elementsToCensor) {
        Map<String, Object> bodyDictionary;
        try {
            bodyDictionary = Serialization.convertJsonToObject(data, Map.class);
            Map<String, Object> censoredBodyDictionary = applyJsonCensors(bodyDictionary, censorText, elementsToCensor);
            return censoredBodyDictionary == null ? data : Serialization.convertObjectToJson(censoredBodyDictionary);
        } catch (Exception ignored) {
            // body is not a JSON dictionary
            try {
                List<Object> bodyList = Serialization.convertJsonToObject(data, List.class);
                List<Object> censoredBodyList = applyJsonCensors(bodyList, censorText, elementsToCensor);
                return censoredBodyList == null ? data : Serialization.convertObjectToJson(censoredBodyList);
            } catch (Exception notJsonData) {
                // body is not parsable as JSON, return original data
                return data;
            }
        }
    }

    /**
     * Check if the current JSON element should be censored.
     *
     * @param foundKey         Key of the JSON element to evaluate.
     * @param elementsToCensor List of censors to compare against.
     * @return True if the value should be censored, false otherwise.
     */
    private static boolean elementShouldBeCensored(String foundKey, List<CensorElement> elementsToCensor) {
        return elementsToCensor.stream().anyMatch(queryElement -> queryElement.matches(foundKey));
    }

    /**
     * Censor the appropriate body elements.
     *
     * @param body                 String representation of request body to apply censors to.
     * @param censorText           The string to use to censor sensitive information.
     * @param bodyElementsToCensor The body elements to censor.
     * @return Censored string representation of request body.
     */
    public static String applyBodyParameterCensors(String body, String censorText,
                                                   List<CensorElement> bodyElementsToCensor) {
        if (body == null || body.length() == 0) {
            // short circuit if body is null or empty
            return body;
        }

        if (bodyElementsToCensor.size() == 0) {
            // short circuit if there are no censors to apply
            return body;
        }

        // TODO: Future different content type support here, only JSON is supported currently
        return censorJsonData(body, censorText, bodyElementsToCensor);
    }

    /**
     * Censor the appropriate headers.
     *
     * @param headers         Map of headers to apply censors to.
     * @param censorText      The string to use to censor sensitive information.
     * @param headersToCensor The headers to censor.
     * @return Censored map of headers.
     */
    public static Map<String, List<String>> applyHeaderCensors(Map<String, List<String>> headers, String censorText,
                                                               List<CensorElement> headersToCensor) {
        if (headers == null || headers.size() == 0) {
            // short circuit if there are no headers to censor
            return headers;
        }

        if (headersToCensor.size() == 0) {
            // short circuit if there are no censors to apply
            return headers;
        }

        final Map<String, List<String>> headersCopy = new HashMap<>(headers);

        List<String> headerKeys = new ArrayList<>(headersCopy.keySet());
        for (String headerKey : headerKeys) {
            if (headerKey == null) {
                continue;
            }
            if (elementShouldBeCensored(headerKey, headersToCensor)) {
                headersCopy.put(headerKey, Collections.singletonList(censorText));
            }
        }

        return headersCopy;
    }

    /**
     * Censor the appropriate query parameters.
     *
     * @param url                 Full URL string to apply censors to.
     * @param censorText          The string to use to censor sensitive information.
     * @param queryParamsToCensor The query parameters to censor.
     * @return Censored URL string.
     */
    public static String applyQueryParameterCensors(String url, String censorText,
                                                    List<CensorElement> queryParamsToCensor) {
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

        List<String> queryKeys = new ArrayList<>(queryParameters.keySet());
        for (String queryKey : queryKeys) {
            if (elementShouldBeCensored(queryKey, queryParamsToCensor)) {
                queryParameters.put(queryKey, censorText);
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

    /**
     * Add a rule to censor specified body elements.
     *
     * @param elements List of body elements to censor.
     * @return This Censors factory.
     */
    public Censors censorBodyElements(List<CensorElement> elements) {
        bodyElementsToCensor.addAll(elements);
        return this;
    }

    /**
     * Add a rule to censor specified body elements by their keys.
     *
     * @param elementKeys   Keys of body elements to censor.
     * @param caseSensitive Whether to use case-sensitive censoring.
     * @return This Censors factory.
     */
    public Censors censorBodyElementsByKeys(List<String> elementKeys, boolean caseSensitive) {
        for (String elementKey : elementKeys) {
            bodyElementsToCensor.add(new CensorElement(elementKey, caseSensitive));
        }
        return this;
    }

    /**
     * Add a rule to censor specified body elements by their keys.
     *
     * @param elementKeys Keys of body elementKeys to censor.
     * @return This Censors factory.
     */
    public Censors censorBodyElementsByKeys(List<String> elementKeys) {
        return censorBodyElementsByKeys(elementKeys, false);
    }

    /**
     * Add a rule to censor specified headers.
     * Note: This will censor the header keys in both the request and response.
     *
     * @param headers List of Headers to censor.
     * @return This Censors factory.
     */
    public Censors censorHeaders(List<CensorElement> headers) {
        headersToCensor.addAll(headers);
        return this;
    }

    /**
     * Add a rule to censor specified headers by their keys.
     * Note: This will censor the header keys in both the request and response.
     *
     * @param headerKeys    Keys of headers to censor.
     * @param caseSensitive Whether to use case-sensitive censoring.
     * @return This Censors factory.
     */
    public Censors censorHeadersByKeys(List<String> headerKeys, boolean caseSensitive) {
        for (String headerKey : headerKeys) {
            headersToCensor.add(new CensorElement(headerKey, caseSensitive));
        }
        return this;
    }

    /**
     * Add a rule to censor specified headers by their keys.
     * Note: This will censor the header keys in both the request and response.
     *
     * @param headerKeys Keys of headers to censor.
     * @return This Censors factory.
     */
    public Censors censorHeadersByKeys(List<String> headerKeys) {
        return censorHeadersByKeys(headerKeys, false);
    }

    /**
     * Add a rule to censor specified query parameters.
     *
     * @param elements List of QueryElements to censor.
     * @return This Censors factory.
     */
    public Censors censorQueryParameters(List<CensorElement> elements) {
        queryParamsToCensor.addAll(elements);
        return this;
    }

    /**
     * Add a rule to censor specified query parameters by their keys.
     *
     * @param parameterKeys Keys of query parameters to censor.
     * @param caseSensitive Whether to use case-sensitive censoring.
     * @return This Censors factory.
     */
    public Censors censorQueryParametersByKeys(List<String> parameterKeys, boolean caseSensitive) {
        for (String parameterKey : parameterKeys) {
            queryParamsToCensor.add(new CensorElement(parameterKey, caseSensitive));
        }
        return this;
    }

    /**
     * Add a rule to censor specified query parameters by their keys.
     *
     * @param parameterKeys Keys of query parameters to censor.
     * @return This Censors factory.
     */
    public Censors censorQueryParametersByKeys(List<String> parameterKeys) {
        return censorQueryParametersByKeys(parameterKeys, false);
    }

    /**
     * Censor the appropriate body elements.
     *
     * @param body String representation of request body to apply censors to.
     * @return Censored string representation of request body.
     */
    public String applyBodyParameterCensors(String body) {
        return applyBodyParameterCensors(body, this.censorText, this.bodyElementsToCensor);
    }

    /**
     * Censor the appropriate headers.
     *
     * @param headers Map of headers to apply censors to.
     * @return Censored map of headers.
     */
    public Map<String, List<String>> applyHeaderCensors(Map<String, List<String>> headers) {
        return applyHeaderCensors(headers, this.censorText, this.headersToCensor);
    }

    /**
     * Censor the appropriate query parameters.
     *
     * @param url Full URL string to apply censors to.
     * @return Censored URL string.
     */
    public String applyQueryParameterCensors(String url) {
        return applyQueryParameterCensors(url, this.censorText, this.queryParamsToCensor);
    }
}
