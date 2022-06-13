package com.easypost.easyvcr;

import com.easypost.easyvcr.clients.httpurlconnection.RecordableHttpURLConnection;
import com.easypost.easyvcr.clients.httpurlconnection.RecordableHttpsURLConnection;
import com.easypost.easyvcr.clients.httpurlconnection.RecordableURL;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

/**
 * HttpClient singleton for EasyVCR.
 */
public abstract class HttpClients {

    /**
     * Get a new client configured to use cassettes.
     *
     * @param type             HttpClientType type of client to create.
     * @param url              String url to use when creating the client.
     * @param cassette         Cassette to use when creating the client.
     * @param mode             Mode to use when creating the client.
     * @param advancedSettings AdvancedSettings to use when creating the client.
     * @return Object client.
     * @throws URISyntaxException If the url is malformed.
     * @throws IOException        If there is an error creating the client.
     */
    public static Object newClient(HttpClientType type, String url, Cassette cassette, Mode mode,
                                   AdvancedSettings advancedSettings) throws URISyntaxException, IOException {
        switch (type) {
            case HttpUrlConnection:
                return newHttpURLConnection(url, cassette, mode, advancedSettings);
            case HttpsUrlConnection:
                return newHttpsURLConnection(url, cassette, mode, advancedSettings);
            default:
                throw new IllegalArgumentException("Unsupported HttpClientType: " + type);
        }
    }

    /**
     * Get a new client configured to use cassettes.
     *
     * @param type     HttpClientType type of client to create.
     * @param url      String url to use when creating the client.
     * @param cassette Cassette to use when creating the client.
     * @param mode     Mode to use when creating the client.
     * @return Object client.
     * @throws URISyntaxException If the url is malformed.
     * @throws IOException        If there is an error creating the client.
     */
    public static Object newClient(HttpClientType type, String url, Cassette cassette, Mode mode)
            throws URISyntaxException, IOException {
        return newClient(type, url, cassette, mode, null);
    }

    /**
     * Get a new RecordableURL configured to use cassettes.
     *
     * @param url              String url to use when creating the client.
     * @param cassette         Cassette to use when creating the client.
     * @param mode             Mode to use when creating the client.
     * @param advancedSettings AdvancedSettings to use when creating the client.
     * @return RecordableURL client.
     * @throws MalformedURLException If the url is malformed.
     */
    private static RecordableURL newRecordableURL(String url, Cassette cassette, Mode mode,
                                                  AdvancedSettings advancedSettings) throws MalformedURLException {
        return new RecordableURL(url, cassette, mode, advancedSettings);
    }

    /**
     * Get a new RecordableHttpURLConnection configured to use cassettes.
     *
     * @param url              String url to use when creating the client.
     * @param cassette         Cassette to use when creating the client.
     * @param mode             Mode to use when creating the client.
     * @param advancedSettings AdvancedSettings to use when creating the client.
     * @return RecordableHttpURLConnection client.
     * @throws IOException If there is an error creating the client.
     */
    public static RecordableHttpURLConnection newHttpURLConnection(String url, Cassette cassette, Mode mode,
                                                                   AdvancedSettings advancedSettings)
            throws IOException {
        return newRecordableURL(url, cassette, mode, advancedSettings).openConnection();
    }

    /**
     * Get a new RecordableHttpURLConnection configured to use cassettes.
     *
     * @param url      String url to use when creating the client.
     * @param cassette Cassette to use when creating the client.
     * @param mode     Mode to use when creating the client.
     * @return RecordableHttpURLConnection client.
     * @throws IOException If there is an error creating the client.
     */
    public static RecordableHttpURLConnection newHttpURLConnection(String url, Cassette cassette, Mode mode)
            throws IOException {
        return newRecordableURL(url, cassette, mode, null).openConnection();
    }

    /**
     * Get a new RecordableHttpsURLConnection configured to use cassettes.
     *
     * @param url              String url to use when creating the client.
     * @param cassette         Cassette to use when creating the client.
     * @param mode             Mode to use when creating the client.
     * @param advancedSettings AdvancedSettings to use when creating the client.
     * @return RecordableHttpsURLConnection client.
     * @throws IOException If there is an error creating the client.
     */
    public static RecordableHttpsURLConnection newHttpsURLConnection(String url, Cassette cassette, Mode mode,
                                                                     AdvancedSettings advancedSettings)
            throws IOException {
        return newRecordableURL(url, cassette, mode, advancedSettings).openConnectionSecure();
    }

    /**
     * Get a new RecordableHttpsURLConnection configured to use cassettes.
     *
     * @param url      String url to use when creating the client.
     * @param cassette Cassette to use when creating the client.
     * @param mode     Mode to use when creating the client.
     * @return RecordableHttpsURLConnection client.
     * @throws IOException If there is an error creating the client.
     */
    public static RecordableHttpsURLConnection newHttpsURLConnection(String url, Cassette cassette, Mode mode)
            throws IOException {
        return newRecordableURL(url, cassette, mode, null).openConnectionSecure();
    }
}
