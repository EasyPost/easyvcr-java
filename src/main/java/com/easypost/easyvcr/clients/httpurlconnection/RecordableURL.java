package com.easypost.easyvcr.clients.httpurlconnection;

import com.easypost.easyvcr.AdvancedSettings;
import com.easypost.easyvcr.Cassette;
import com.easypost.easyvcr.Mode;
import com.easypost.easyvcr.RecordingExpirationException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLStreamHandler;

/**
 * A recordable URL, wrapper for RecordableHttpURLConnection and RecordableHttpsURLConnection.
 */
public final class RecordableURL {
    /**
     * The URL used by this recordable URL.
     */
    private final URL url;
    /**
     * The cassette used by this recordable URL.
     */
    private final Cassette cassette;
    /**
     * The VCR mode used by this recordable URL.
     */
    private final Mode mode;
    /**
     * The advanced settings used by this recordable URL.
     */
    private final AdvancedSettings advancedSettings;

    //CHECKSTYLE.OFF: ParameterNumber

    /**
     * Constructs a new recordable URL.
     *
     * @param protocol         The protocol of the URL.
     * @param host             The host of the URL.
     * @param port             The port of the URL.
     * @param file             The file of the URL.
     * @param handler          The URLStreamHandler of the URL.
     * @param cassette         The cassette used by this recordable URL.
     * @param mode             The VCR mode used by this recordable URL.
     * @param advancedSettings The advanced settings used by this recordable URL.
     * @throws MalformedURLException If the URL is malformed.
     */
    public RecordableURL(String protocol, String host, int port, String file, URLStreamHandler handler,
                         Cassette cassette, Mode mode, AdvancedSettings advancedSettings) throws MalformedURLException {
        this.url = new URL(protocol, host, port, file, handler);
        this.cassette = cassette;
        this.mode = mode;
        this.advancedSettings = advancedSettings;
    }
    //CHECKSTYLE.ON: ParameterNumber

    /**
     * Constructs a new recordable URL.
     *
     * @param protocol The protocol of the URL.
     * @param host     The host of the URL.
     * @param port     The port of the URL.
     * @param file     The file of the URL.
     * @param handler  The URLStreamHandler of the URL.
     * @param cassette The cassette used by this recordable URL.
     * @param mode     The VCR mode used by this recordable URL.
     * @throws MalformedURLException If the URL is malformed.
     */
    public RecordableURL(String protocol, String host, int port, String file, URLStreamHandler handler,
                         Cassette cassette, Mode mode) throws MalformedURLException {
        this(protocol, host, port, file, handler, cassette, mode, new AdvancedSettings());
    }

    /**
     * Constructs a new recordable URL.
     *
     * @param protocol         The protocol of the URL.
     * @param host             The host of the URL.
     * @param port             The port of the URL.
     * @param file             The file of the URL.
     * @param cassette         The cassette used by this recordable URL.
     * @param mode             The VCR mode used by this recordable URL.
     * @param advancedSettings The advanced settings used by this recordable URL.
     * @throws MalformedURLException If the URL is malformed.
     */
    public RecordableURL(String protocol, String host, int port, String file, Cassette cassette, Mode mode,
                         AdvancedSettings advancedSettings) throws MalformedURLException {
        this(protocol, host, port, file, null, cassette, mode, advancedSettings);
    }

    /**
     * Constructs a new recordable URL.
     *
     * @param protocol The protocol of the URL.
     * @param host     The host of the URL.
     * @param port     The port of the URL.
     * @param file     The file of the URL.
     * @param cassette The cassette used by this recordable URL.
     * @param mode     The VCR mode used by this recordable URL.
     * @throws MalformedURLException If the URL is malformed.
     */
    public RecordableURL(String protocol, String host, int port, String file, Cassette cassette, Mode mode)
            throws MalformedURLException {
        this(protocol, host, port, file, cassette, mode, new AdvancedSettings());
    }

    /**
     * Constructs a new recordable URL.
     *
     * @param protocol         The protocol of the URL.
     * @param host             The host of the URL.
     * @param file             The file of the URL.
     * @param cassette         The cassette used by this recordable URL.
     * @param mode             The VCR mode used by this recordable URL.
     * @param advancedSettings The advanced settings used by this recordable URL.
     * @throws MalformedURLException If the URL is malformed.
     */
    public RecordableURL(String protocol, String host, String file, Cassette cassette, Mode mode,
                         AdvancedSettings advancedSettings) throws MalformedURLException {
        this(protocol, host, -1, file, cassette, mode, advancedSettings);
    }

    /**
     * Constructs a new recordable URL.
     *
     * @param protocol The protocol of the URL.
     * @param host     The host of the URL.
     * @param file     The file of the URL.
     * @param cassette The cassette used by this recordable URL.
     * @param mode     The VCR mode used by this recordable URL.
     * @throws MalformedURLException If the URL is malformed.
     */
    public RecordableURL(String protocol, String host, String file, Cassette cassette, Mode mode)
            throws MalformedURLException {
        this(protocol, host, file, cassette, mode, new AdvancedSettings());
    }

    /**
     * Constructs a new recordable URL.
     *
     * @param context          The URL of the URL.
     * @param spec             The spec of the URL.
     * @param handler          The URLStreamHandler of the URL.
     * @param cassette         The cassette used by this recordable URL.
     * @param mode             The VCR mode used by this recordable URL.
     * @param advancedSettings The advanced settings used by this recordable URL.
     * @throws MalformedURLException If the URL is malformed.
     */
    public RecordableURL(URL context, String spec, URLStreamHandler handler, Cassette cassette, Mode mode,
                         AdvancedSettings advancedSettings) throws MalformedURLException {
        this.url = new URL(context, spec, handler);
        this.cassette = cassette;
        this.mode = mode;
        this.advancedSettings = advancedSettings != null ? advancedSettings : new AdvancedSettings();
        if (this.cassette == null) {
            throw new IllegalArgumentException("Cassette cannot be null");
        }
    }

    /**
     * Constructs a new recordable URL.
     *
     * @param context  The URL of the URL.
     * @param spec     The spec of the URL.
     * @param handler  The URLStreamHandler of the URL.
     * @param cassette The cassette used by this recordable URL.
     * @param mode     The VCR mode used by this recordable URL.
     * @throws MalformedURLException If the URL is malformed.
     */
    public RecordableURL(URL context, String spec, URLStreamHandler handler, Cassette cassette, Mode mode)
            throws MalformedURLException {
        this(context, spec, handler, cassette, mode, new AdvancedSettings());
    }

    /**
     * Constructs a new recordable URL.
     *
     * @param spec             The spec of the URL.
     * @param cassette         The cassette used by this recordable URL.
     * @param mode             The VCR mode used by this recordable URL.
     * @param advancedSettings The advanced settings used by this recordable URL.
     * @throws MalformedURLException If the URL is malformed.
     */
    public RecordableURL(String spec, Cassette cassette, Mode mode, AdvancedSettings advancedSettings)
            throws MalformedURLException {
        this(null, spec, cassette, mode, advancedSettings);
    }

    /**
     * Constructs a new recordable URL.
     *
     * @param spec     The spec of the URL.
     * @param cassette The cassette used by this recordable URL.
     * @param mode     The VCR mode used by this recordable URL.
     * @throws MalformedURLException If the URL is malformed.
     */
    public RecordableURL(String spec, Cassette cassette, Mode mode) throws MalformedURLException {
        this(spec, cassette, mode, new AdvancedSettings());
    }

    /**
     * Constructs a new recordable URL.
     *
     * @param context          The URL of the URL.
     * @param spec             The spec of the URL.
     * @param cassette         The cassette used by this recordable URL.
     * @param mode             The VCR mode used by this recordable URL.
     * @param advancedSettings The advanced settings used by this recordable URL.
     * @throws MalformedURLException If the URL is malformed.
     */
    public RecordableURL(URL context, String spec, Cassette cassette, Mode mode, AdvancedSettings advancedSettings)
            throws MalformedURLException {
        this(context, spec, null, cassette, mode, advancedSettings);
    }

    /**
     * Constructs a new recordable URL.
     *
     * @param context  The URL of the URL.
     * @param spec     The spec of the URL.
     * @param cassette The cassette used by this recordable URL.
     * @param mode     The VCR mode used by this recordable URL.
     * @throws MalformedURLException If the URL is malformed.
     */
    public RecordableURL(URL context, String spec, Cassette cassette, Mode mode) throws MalformedURLException {
        this(context, spec, cassette, mode, new AdvancedSettings());
    }

    /**
     * Constructs a new recordable URL.
     *
     * @param context          The URL of the URL.
     * @param cassette         The cassette used by this recordable URL.
     * @param mode             The VCR mode used by this recordable URL.
     * @param advancedSettings The advanced settings used by this recordable URL.
     * @throws MalformedURLException If the URL is malformed.
     */
    public RecordableURL(URL context, Cassette cassette, Mode mode, AdvancedSettings advancedSettings)
            throws MalformedURLException {
        this.url = context;
        this.cassette = cassette;
        this.mode = mode;
        this.advancedSettings = advancedSettings != null ? advancedSettings : new AdvancedSettings();
    }

    /**
     * Constructs a new recordable URL.
     *
     * @param context  The URL of the URL.
     * @param cassette The cassette used by this recordable URL.
     * @param mode     The VCR mode used by this recordable URL.
     * @throws MalformedURLException If the URL is malformed.
     */
    public RecordableURL(URL context, Cassette cassette, Mode mode) throws MalformedURLException {
        this(context, cassette, mode, new AdvancedSettings());
    }

    /**
     * Open an HTTP connection to the URL.
     *
     * @return a RecordableHttpURLConnection instance.
     * @throws IOException if an I/O error occurs.
     */
    public RecordableHttpURLConnection openConnection() throws IOException, RecordingExpirationException {
        return new RecordableHttpURLConnection(this.url, this.cassette, this.mode, this.advancedSettings);
    }

    /**
     * Open an HTTP connection to the URL.
     *
     * @param proxy the proxy to use.
     * @return a RecordableHttpURLConnection instance.
     * @throws IOException if an I/O error occurs.
     */
    public RecordableHttpURLConnection openConnection(Proxy proxy) throws IOException, RecordingExpirationException {
        return new RecordableHttpURLConnection(this.url, proxy, this.cassette, this.mode, this.advancedSettings);
    }

    /**
     * Open an HTTPS connection to the URL.
     *
     * @return a RecordableHttpsURLConnection instance.
     * @throws IOException if an I/O error occurs.
     */
    public RecordableHttpsURLConnection openConnectionSecure() throws IOException, RecordingExpirationException {
        return new RecordableHttpsURLConnection(this.url, this.cassette, this.mode, this.advancedSettings);
    }

    /**
     * Open an HTTPS connection to the URL.
     *
     * @param proxy the proxy to use.
     * @return a RecordableHttpsURLConnection instance.
     * @throws IOException if an I/O error occurs.
     */
    public RecordableHttpsURLConnection openConnectionSecure(Proxy proxy)
            throws IOException, RecordingExpirationException {
        return new RecordableHttpsURLConnection(this.url, proxy, this.cassette, this.mode, this.advancedSettings);
    }
}
