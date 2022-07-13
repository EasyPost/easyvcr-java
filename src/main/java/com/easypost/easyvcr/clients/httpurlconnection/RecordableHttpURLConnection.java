package com.easypost.easyvcr.clients.httpurlconnection;

import com.easypost.easyvcr.AdvancedSettings;
import com.easypost.easyvcr.Cassette;
import com.easypost.easyvcr.Mode;
import com.easypost.easyvcr.VCRException;
import com.easypost.easyvcr.interactionconverters.HttpUrlConnectionInteractionConverter;
import com.easypost.easyvcr.requestelements.HttpInteraction;
import com.easypost.easyvcr.requestelements.Request;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpRetryException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.Proxy;
import java.net.SocketPermission;
import java.net.URL;
import java.net.UnknownServiceException;
import java.security.Permission;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static com.easypost.easyvcr.internalutilities.Tools.createInputStream;
import static com.easypost.easyvcr.internalutilities.Tools.simulateDelay;

public final class RecordableHttpURLConnection extends HttpURLConnection {

    // interaction is not actually recorded until you getX() from the result
    // connect() or any getX() function will cache the interaction
    // you cannot setX() after the interaction has been cached

    // TODO: ^ Eventually allow users to set after cache (update cache)

    /**
     * The internal HttpURLConnection that this class wraps.
     */
    private HttpURLConnection connection;
    /**
     * Stores the request body until the connection is made.
     */
    private final RecordableRequestBody requestBody;
    /**
     * The HttpUrlConnectionInteractionConverter that converts the HttpURLConnection to an HttpInteraction.
     */
    private final HttpUrlConnectionInteractionConverter converter;
    /**
     * The Cassette that this class is recording to and reading from.
     */
    private final Cassette cassette;
    /**
     * The VCR mode that this class is using.
     */
    private final Mode mode;
    /**
     * The AdvancedSettings that this class is using.
     */
    private final AdvancedSettings advancedSettings;
    /**
     * Internal cached HttpInteraction storing the request and response details.
     */
    private HttpInteraction cachedInteraction;

    /**
     * Constructor for the RecordableHttpURLConnection class.
     *
     * @param url              The URL to connect to.
     * @param proxy            The proxy to use.
     * @param cassette         The cassette to use.
     * @param mode             The mode to use.
     * @param advancedSettings The advanced settings to use.
     * @throws IOException If an error occurs.
     */
    public RecordableHttpURLConnection(URL url, Proxy proxy, Cassette cassette, Mode mode,
                                       AdvancedSettings advancedSettings) throws IOException {
        // this super is not used
        super(url);
        if (proxy == null) {
            this.connection = (HttpURLConnection) url.openConnection();
        } else {
            this.connection = (HttpURLConnection) url.openConnection(proxy);
        }
        this.requestBody = new RecordableRequestBody();
        this.cachedInteraction = null;
        this.cassette = cassette;
        this.mode = mode;
        this.advancedSettings = advancedSettings;
        this.converter = new HttpUrlConnectionInteractionConverter();
    }

    /**
     * Constructor for the RecordableHttpURLConnection class.
     *
     * @param url              The URL to connect to.
     * @param cassette         The cassette to use.
     * @param mode             The mode to use.
     * @param advancedSettings The advanced settings to use.
     * @throws IOException If an error occurs.
     */
    public RecordableHttpURLConnection(URL url, Cassette cassette, Mode mode, AdvancedSettings advancedSettings)
            throws IOException {
        this(url, null, cassette, mode, advancedSettings);
    }

    /**
     * Constructor for the RecordableHttpURLConnection class.
     *
     * @param url      The URL to connect to.
     * @param proxy    The proxy to use.
     * @param cassette The cassette to use.
     * @param mode     The mode to use.
     * @throws IOException If an error occurs.
     */
    public RecordableHttpURLConnection(URL url, Proxy proxy, Cassette cassette, Mode mode) throws IOException {
        this(url, proxy, cassette, mode, new AdvancedSettings());
    }

    /**
     * Constructor for the RecordableHttpURLConnection class.
     *
     * @param url      The URL to connect to.
     * @param cassette The cassette to use.
     * @param mode     The mode to use.
     * @throws IOException If an error occurs.
     */
    public RecordableHttpURLConnection(URL url, Cassette cassette, Mode mode) throws IOException {
        this(url, cassette, mode, new AdvancedSettings());
    }

    /**
     * Get an object from the cache.
     *
     * @param getter       Function to get the object from the cache.
     * @param defaultValue The default value to return if the object is not in the cache.
     * @return The object from the cache.
     * @throws VCRException If an error occurs.
     */
    private Object getObjectElementFromCache(Function<HttpInteraction, Object> getter, Object defaultValue)
            throws VCRException {
        if (this.cachedInteraction == null) {
            return defaultValue;
        }
        try {
            return getter.apply(this.cachedInteraction);
        } catch (Exception e) {
            throw new VCRException("Error getting string element from cache");
        }
    }

    /**
     * Get a string from the cache.
     *
     * @param getter       Function to get the string from the cache.
     * @param defaultValue The default value to return if the string is not in the cache.
     * @return The string from the cache.
     * @throws VCRException If an error occurs.
     */
    private String getStringElementFromCache(Function<HttpInteraction, String> getter, String defaultValue)
            throws VCRException {
        if (this.cachedInteraction == null) {
            return defaultValue;
        }
        try {
            return getter.apply(this.cachedInteraction);
        } catch (Exception e) {
            throw new VCRException("Error getting string element from cache");
        }
    }

    /**
     * Get an integer from the cache.
     *
     * @param getter       Function to get the integer from the cache.
     * @param defaultValue The default value to return if the integer is not in the cache.
     * @return The integer from the cache.
     * @throws VCRException If an error occurs.
     */
    private int getIntegerElementFromCache(Function<HttpInteraction, Integer> getter, int defaultValue)
            throws VCRException {
        if (this.cachedInteraction == null) {
            return defaultValue;
        }
        try {
            return getter.apply(this.cachedInteraction);
        } catch (Exception e) {
            throw new VCRException("Error getting integer element from cache");
        }
    }

    /**
     * Cache the current request and response to an in-memory HttpInteraction.
     * This is done once on the first attempt to retrieve request or response details.
     *
     * @param recordToCassette Whether to also record the cached interaction to the cassette.
     * @throws VCRException If an error occurs.
     */
    private void cacheInteraction(boolean recordToCassette) throws VCRException {
        // record this interaction
        // only need to execute this once, on the first getX(), since no more setX() is allowed at that point
        // so the request and response won't be changing
        // important to call directly on connection, rather than this.function() to avoid potential recursion
        this.cachedInteraction =
                this.converter.createInteraction(this.connection, this.requestBody, this.advancedSettings.censors);
        if (recordToCassette) {
            this.cassette.updateInteraction(this.cachedInteraction, this.advancedSettings.matchRules, false);
        }
    }

    /**
     * Load an existing interaction from the cassette and cache it.
     *
     * @return boolean indicating whether the interaction was found and cached successfully.
     * @throws VCRException         If an error occurs.
     * @throws InterruptedException If the thread is interrupted.
     */
    private boolean loadExistingInteraction() throws VCRException, InterruptedException {
        Request request =
                converter.createRecordedRequest(this.connection, this.requestBody, this.advancedSettings.censors);
        // null because couldn't be created
        if (request == null) {
            return false;
        }
        HttpInteraction matchingInteraction =
                converter.findMatchingInteraction(this.cassette, request, advancedSettings.matchRules);
        if (matchingInteraction == null) {
            return false;
        }
        simulateDelay(matchingInteraction, this.advancedSettings);
        this.cachedInteraction = matchingInteraction;
        this.cachedInteraction.getResponse().addReplayHeaders();
        return true;
    }

    /**
     * Build an in-memory cache of the current request and response details if needed.
     *
     * @throws VCRException If an error occurs.
     */
    private void buildCache() throws VCRException {
        // run every time a user attempts to getX()

        // can't setX() after the first getX(), so if cache has already been built, can't build again
        if (this.cachedInteraction != null) {
            return;
        }

        switch (mode) {
            case Record:
                cacheInteraction(true);
                break;
            case Replay:
                try {
                    loadExistingInteraction();
                } catch (VCRException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
                break;
            case Auto:
                try {
                    if (!loadExistingInteraction()) {
                        cacheInteraction(true);
                    }
                } catch (VCRException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
                break;
            case Bypass:
            default:
                break;
        }
    }

    /**
     * Clear the in-memory cache of the current request and response details.
     */
    private void clearCache() {
        this.cachedInteraction = null;
    }

    @Override
    public void connect() throws IOException {
        try {
            if (this.requestBody.hasData()) {
                setRequestProperty("Content-Type", "application/json"); // only supports JSON for now
                this.connection.setDoOutput(
                        true); // have to set this to true to allow the ability to get and write to output stream
                // have to write this at the last second, otherwise locks us out
                OutputStream output = null;
                try {
                    output = this.connection.getOutputStream();
                    byte[] jsonData = this.requestBody.getData();
                    output.write(jsonData);
                } catch (Exception e) {
                    throw new IOException(e);
                } finally {
                    if (output != null) {
                        output.close();
                    }
                }
            }
            buildCache(); // can't set anything after connecting, so might as well build the cache now
            // will establish connection as a result of caching, so need to disconnect afterwards
            this.connection.disconnect();
        } catch (VCRException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void disconnect() {
        this.connection.disconnect();
        clearCache();
    }

    @Override
    public boolean usingProxy() {
        return this.connection.usingProxy();
    }

    /**
     * Returns the key for the {@code n}<sup>th</sup> header field.
     * Some implementations may treat the {@code 0}<sup>th</sup>
     * header field as special, i.e. as the status line returned by the HTTP
     * server. In this case, {@link #getHeaderField(int) getHeaderField(0)} returns the status
     * line, but {@code getHeaderFieldKey(0)} returns null.
     *
     * @param n an index, where {@code n >=0}.
     * @return the key for the {@code n}<sup>th</sup> header field,
     * or {@code null} if the key does not exist.
     */
    @Override
    public String getHeaderFieldKey(int n) {
        if (mode == Mode.Bypass) {
            return this.connection.getHeaderFieldKey(n);
        }
        try {
            buildCache();
            return getStringElementFromCache(
                    (interaction) -> interaction.getResponse().getHeaders().keySet().toArray()[n].toString(), null);
        } catch (VCRException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * This method is used to enable streaming of a HTTP request body
     * without internal buffering, when the content length is known in
     * advance.
     * <p>
     * An exception will be thrown if the application
     * attempts to write more data than the indicated
     * content-length, or if the application closes the OutputStream
     * before writing the indicated amount.
     * <p>
     * When output streaming is enabled, authentication
     * and redirection cannot be handled automatically.
     * A HttpRetryException will be thrown when reading
     * the response if authentication or redirection are required.
     * This exception can be queried for the details of the error.
     * <p>
     * This method must be called before the URLConnection is connected.
     * <p>
     * <B>NOTE:</B> {@link #setFixedLengthStreamingMode(long)} is recommended
     * instead of this method as it allows larger content lengths to be set.
     *
     * @param contentLength The number of bytes which will be written
     *                      to the OutputStream.
     * @throws IllegalStateException    if URLConnection is already connected
     *                                  or if a different streaming mode is already enabled.
     * @throws IllegalArgumentException if a content length less than
     *                                  zero is specified.
     * @see #setChunkedStreamingMode(int)
     * @since 1.5
     */
    @Override
    public void setFixedLengthStreamingMode(int contentLength) {
        if (cachedInteraction != null) {
            throw new IllegalStateException("Cannot set anything after interaction has been cached");
        }
        this.connection.setFixedLengthStreamingMode(contentLength);
    }

    /**
     * This method is used to enable streaming of a HTTP request body
     * without internal buffering, when the content length is known in
     * advance.
     *
     * <P> An exception will be thrown if the application attempts to write
     * more data than the indicated content-length, or if the application
     * closes the OutputStream before writing the indicated amount.
     *
     * <P> When output streaming is enabled, authentication and redirection
     * cannot be handled automatically. A {@linkplain HttpRetryException} will
     * be thrown when reading the response if authentication or redirection
     * are required. This exception can be queried for the details of the
     * error.
     *
     * <P> This method must be called before the URLConnection is connected.
     *
     * <P> The content length set by invoking this method takes precedence
     * over any value set by {@link #setFixedLengthStreamingMode(int)}.
     *
     * @param contentLength The number of bytes which will be written to the OutputStream.
     * @throws IllegalStateException    if URLConnection is already connected or if a different
     *                                  streaming mode is already enabled.
     * @throws IllegalArgumentException if a content length less than zero is specified.
     * @since 1.7
     */
    @Override
    public void setFixedLengthStreamingMode(long contentLength) {
        if (cachedInteraction != null) {
            throw new IllegalStateException("Cannot set anything after interaction has been cached");
        }
        this.connection.setFixedLengthStreamingMode(contentLength);
    }

    /**
     * This method is used to enable streaming of a HTTP request body
     * without internal buffering, when the content length is <b>not</b>
     * known in advance. In this mode, chunked transfer encoding
     * is used to send the request body. Note, not all HTTP servers
     * support this mode.
     * <p>
     * When output streaming is enabled, authentication
     * and redirection cannot be handled automatically.
     * A HttpRetryException will be thrown when reading
     * the response if authentication or redirection are required.
     * This exception can be queried for the details of the error.
     * <p>
     * This method must be called before the URLConnection is connected.
     *
     * @param chunklen The number of bytes to write in each chunk.
     *                 If chunklen is less than or equal to zero, a default
     *                 value will be used.
     * @throws IllegalStateException if URLConnection is already connected
     *                               or if a different streaming mode is already enabled.
     * @see #setFixedLengthStreamingMode(int)
     * @since 1.5
     */
    @Override
    public void setChunkedStreamingMode(int chunklen) {
        if (cachedInteraction != null) {
            throw new IllegalStateException("Cannot set anything after interaction has been cached");
        }
        this.connection.setChunkedStreamingMode(chunklen);
    }

    /**
     * Returns the value for the {@code n}<sup>th</sup> header field.
     * Some implementations may treat the {@code 0}<sup>th</sup>
     * header field as special, i.e. as the status line returned by the HTTP
     * server.
     * <p>
     * This method can be used in conjunction with the
     * {@link #getHeaderFieldKey getHeaderFieldKey} method to iterate through all
     * the headers in the message.
     *
     * @param n an index, where {@code n>=0}.
     * @return the value of the {@code n}<sup>th</sup> header field,
     * or {@code null} if the value does not exist.
     * @see HttpURLConnection#getHeaderFieldKey(int)
     */
    @Override
    public String getHeaderField(int n) {
        if (mode == Mode.Bypass) {
            return this.connection.getHeaderField(n);
        }
        try {
            buildCache();
            return getStringElementFromCache(
                    (interaction) -> interaction.getResponse().getHeaders().values().toArray()[n].toString(), null);
        } catch (VCRException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the value of this {@code RecordableHttpUrlConnection}'s
     * {@code instanceFollowRedirects} field.
     *
     * @return the value of this {@code RecordableHttpUrlConnection}'s
     * {@code instanceFollowRedirects} field.
     * @see #setInstanceFollowRedirects(boolean)
     * @since 1.3
     */
    @Override
    public boolean getInstanceFollowRedirects() {
        // not in cassette, go to real connection
        return this.connection.getInstanceFollowRedirects();
    }

    /**
     * Sets whether HTTP redirects (requests with response code 3xx) should
     * be automatically followed by this {@code RecordableHttpUrlConnection}
     * instance.
     * <p>
     * The default value comes from followRedirects, which defaults to
     * true.
     *
     * @param followRedirects a {@code boolean} indicating
     *                        whether or not to follow HTTP redirects.
     * @see #getInstanceFollowRedirects
     * @since 1.3
     */
    @Override
    public void setInstanceFollowRedirects(boolean followRedirects) {
        if (cachedInteraction != null) {
            throw new IllegalStateException("Cannot set anything after interaction has been cached");
        }
        this.connection.setInstanceFollowRedirects(followRedirects);
    }

    /**
     * get the request method.
     *
     * @return the HTTP request method
     * @see #setRequestMethod(String)
     */
    @Override
    public String getRequestMethod() {
        if (mode == Mode.Bypass) {
            return this.connection.getRequestMethod();
        }
        try {
            buildCache();
            return getStringElementFromCache((interaction) -> interaction.getRequest().getMethod(), null);
        } catch (VCRException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Set the method for the URL request, one of:
     * <UL>
     * <LI>GET
     * <LI>POST
     * <LI>HEAD
     * <LI>OPTIONS
     * <LI>PUT
     * <LI>DELETE
     * <LI>TRACE
     * </UL> are legal, subject to protocol restrictions.  The default
     * method is GET.
     *
     * @param method the HTTP method
     * @throws ProtocolException if the method cannot be reset or if
     *                           the requested method isn't valid for HTTP.
     * @throws SecurityException if a security manager is set and the
     *                           method is "TRACE", but the "allowHttpTrace"
     *                           NetPermission is not granted.
     * @see #getRequestMethod()
     */
    @Override
    public void setRequestMethod(String method) throws ProtocolException {
        if (cachedInteraction != null) {
            throw new IllegalStateException("Cannot set anything after interaction has been cached");
        }
        this.connection.setRequestMethod(method);
    }

    /**
     * gets the status code from an HTTP response message.
     * For example, in the case of the following status lines:
     * <PRE>
     * HTTP/1.0 200 OK
     * HTTP/1.0 401 Unauthorized
     * </PRE>
     * It will return 200 and 401 respectively.
     * Returns -1 if no code can be discerned
     * from the response (i.e., the response is not valid HTTP).
     *
     * @return the HTTP Status-Code, or -1
     * @throws IOException if an error occurred connecting to the server.
     */
    @Override
    public int getResponseCode() throws IOException {
        if (mode == Mode.Bypass) {
            return this.connection.getResponseCode();
        }
        try {
            buildCache();
            return getIntegerElementFromCache((interaction) -> interaction.getResponse().getStatus().getCode(), 0);
        } catch (VCRException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * gets the HTTP response message, if any, returned along with the
     * response code from a server.  From responses like:
     * <PRE>
     * HTTP/1.0 200 OK
     * HTTP/1.0 404 Not Found
     * </PRE>
     * Extracts the Strings "OK" and "Not Found" respectively.
     * Returns null if none could be discerned from the responses
     * (the result was not valid HTTP).
     *
     * @return the HTTP response message, or {@code null}
     * @throws IOException if an error occurred connecting to the server.
     */
    @Override
    public String getResponseMessage() throws IOException {
        if (mode == Mode.Bypass) {
            return this.connection.getResponseMessage();
        }
        try {
            buildCache();
            return getStringElementFromCache((interaction) -> interaction.getResponse().getStatus().getMessage(), null);
        } catch (VCRException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns a {@link SocketPermission} object representing the
     * permission necessary to connect to the destination host and port.
     *
     * @return a {@code SocketPermission} object representing the
     * permission necessary to connect to the destination
     * host and port.
     * @throws IOException if an error occurs while computing
     *                     the permission.
     */
    @Override
    public Permission getPermission() throws IOException {
        // not in cassette, go to real connection
        return this.connection.getPermission();
    }

    /**
     * Returns the error stream if the connection failed
     * but the server sent useful data nonetheless. The
     * typical example is when an HTTP server responds
     * with a 404, which will cause a FileNotFoundException
     * to be thrown in connect, but the server sent an HTML
     * help page with suggestions as to what to do.
     *
     * <p>This method will not cause a connection to be initiated.  If
     * the connection was not connected, or if the server did not have
     * an error while connecting or if the server had an error but
     * no error data was sent, this method will return null. This is
     * the default.
     *
     * @return an error stream if any, null if there have been no
     * errors, the connection is not connected or the server sent no
     * useful data.
     */
    @Override
    public InputStream getErrorStream() {
        // not in cassette, get from real request
        return this.connection.getErrorStream();
    }

    /**
     * Returns setting for connect timeout.
     * <p>
     * 0 return implies that the option is disabled
     * (i.e., timeout of infinity).
     *
     * @return an {@code int} that indicates the connect timeout
     * value in milliseconds
     * @see #setConnectTimeout(int)
     * @see #connect()
     * @since 1.5
     */
    @Override
    public int getConnectTimeout() {
        // not in cassette, go to real connection
        return this.connection.getConnectTimeout();
    }

    /**
     * Sets a specified timeout value, in milliseconds, to be used
     * when opening a communications link to the resource referenced
     * by this URLConnection.  If the timeout expires before the
     * connection can be established, a
     * java.net.SocketTimeoutException is raised. A timeout of zero is
     * interpreted as an infinite timeout.
     *
     * <p> Some non-standard implementation of this method may ignore
     * the specified timeout. To see the connect timeout set, please
     * call getConnectTimeout().
     *
     * @param timeout an {@code int} that specifies the connect
     *                timeout value in milliseconds
     * @throws IllegalArgumentException if the timeout parameter is negative
     * @see #getConnectTimeout()
     * @see #connect()
     * @since 1.5
     */
    @Override
    public void setConnectTimeout(int timeout) {
        if (cachedInteraction != null) {
            throw new IllegalStateException("Cannot set anything after interaction has been cached");
        }
        this.connection.setConnectTimeout(timeout);
    }

    /**
     * Returns setting for read timeout. 0 return implies that the
     * option is disabled (i.e., timeout of infinity).
     *
     * @return an {@code int} that indicates the read timeout
     * value in milliseconds
     * @see #setReadTimeout(int)
     * @see InputStream#read()
     * @since 1.5
     */
    @Override
    public int getReadTimeout() {
        // not in cassette, go to real connection
        return this.connection.getReadTimeout();
    }

    /**
     * Sets the read timeout to a specified timeout, in
     * milliseconds. A non-zero value specifies the timeout when
     * reading from Input stream when a connection is established to a
     * resource. If the timeout expires before there is data available
     * for read, a java.net.SocketTimeoutException is raised. A
     * timeout of zero is interpreted as an infinite timeout.
     *
     * <p> Some non-standard implementation of this method ignores the
     * specified timeout. To see the read timeout set, please call
     * getReadTimeout().
     *
     * @param timeout an {@code int} that specifies the timeout
     *                value to be used in milliseconds
     * @throws IllegalArgumentException if the timeout parameter is negative
     * @see #getReadTimeout()
     * @see InputStream#read()
     * @since 1.5
     */
    @Override
    public void setReadTimeout(int timeout) {
        if (cachedInteraction != null) {
            throw new IllegalStateException("Cannot set anything after interaction has been cached");
        }
        this.connection.setReadTimeout(timeout);
    }

    /**
     * Returns the value of this {@code URLConnection}'s {@code URL}
     * field.
     *
     * @return the value of this {@code URLConnection}'s {@code URL}
     * field.
     */
    @Override
    public URL getURL() {
        if (mode == Mode.Bypass) {
            return this.connection.getURL();
        }
        try {
            buildCache();
            String urlString =
                    getStringElementFromCache((interaction) -> interaction.getResponse().getUriString(), null);
            if (urlString == null) {
                throw new IllegalStateException("Could not load URL from cache");
            }
            return new URL(urlString);
        } catch (VCRException | MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the value of the {@code content-type} header field.
     *
     * @return the content type of the resource that the URL references,
     * or {@code null} if not known.
     * @see java.net.URLConnection#getHeaderField(String)
     */
    @Override
    public String getContentType() {
        return getHeaderField("content-type");
    }

    /**
     * Returns the value of the {@code content-encoding} header field.
     *
     * @return the content encoding of the resource that the URL references,
     * or {@code null} if not known.
     * @see java.net.URLConnection#getHeaderField(String)
     */
    @Override
    public String getContentEncoding() {
        return getHeaderField("content-encoding");
    }

    /**
     * Returns the value of the {@code expires} header field.
     *
     * @return the expiration date of the resource that this URL references,
     * or 0 if not known. The value is the number of milliseconds since
     * January 1, 1970 GMT.
     * @see java.net.URLConnection#getHeaderField(String)
     */
    @Override
    public long getExpiration() {
        // not in cassette, go to real connection
        return this.connection.getExpiration();
    }

    /**
     * Returns the value of the named header field.
     * <p>
     * If called on a connection that sets the same header multiple times
     * with possibly different values, only the last value is returned.
     *
     * @param name the name of a header field.
     * @return the value of the named header field, or {@code null}
     * if there is no such field in the header.
     */
    @Override
    public String getHeaderField(String name) {
        if (mode == Mode.Bypass) {
            return this.connection.getHeaderField(name);
        }
        try {
            buildCache();
            return getStringElementFromCache((interaction) -> interaction.getResponse().getHeaders().get(name).get(0),
                    null);
        } catch (VCRException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns an unmodifiable Map of the header fields.
     * The Map keys are Strings that represent the
     * response-header field names. Each Map value is an
     * unmodifiable List of Strings that represents
     * the corresponding field values.
     *
     * @return a Map of header fields
     * @since 1.4
     */
    @Override
    public Map<String, List<String>> getHeaderFields() {
        if (mode == Mode.Bypass) {
            return this.connection.getHeaderFields();
        }
        try {
            buildCache();
            if (cachedInteraction == null) {
                return Collections.emptyMap();
            }
            return cachedInteraction.getResponse().getHeaders();
        } catch (VCRException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Retrieves the contents of this URL connection.
     * <p>
     * This method first determines the content type of the object by
     * calling the {@code getContentType} method. If this is
     * the first time that the application has seen that specific content
     * type, a content handler for that content type is created.
     * <p> This is done as follows:
     * <ol>
     * <li>If the application has set up a content handler factory instance
     *     using the {@code setContentHandlerFactory} method, the
     *     {@code createContentHandler} method of that instance is called
     *     with the content type as an argument; the result is a content
     *     handler for that content type.
     * <li>If no {@code ContentHandlerFactory} has yet been set up,
     *     or if the factory's {@code createContentHandler} method
     *     returns {@code null}, then the {@linkplain java.util.ServiceLoader
     *     ServiceLoader} mechanism is used to locate {@linkplain
     *     java.net.ContentHandlerFactory ContentHandlerFactory}
     *     implementations using the system class
     *     loader. The order that factories are located is implementation
     *     specific, and an implementation is free to cache the located
     *     factories. A {@linkplain java.util.ServiceConfigurationError
     *     ServiceConfigurationError}, {@code Error} or {@code RuntimeException}
     *     thrown from the {@code createContentHandler}, if encountered, will
     *     be propagated to the calling thread. The {@code
     *     createContentHandler} method of each factory, if instantiated, is
     *     invoked, with the content type, until a factory returns non-null,
     *     or all factories have been exhausted.
     * <li>Failing that, this method tries to load a content handler
     *     class as defined by {@link java.net.ContentHandler ContentHandler}.
     *     If the class does not exist, or is not a subclass of {@code
     *     ContentHandler}, then an {@code UnknownServiceException} is thrown.
     * </ol>
     *
     * @return the object fetched. The {@code instanceof} operator
     * should be used to determine the specific kind of object
     * returned.
     * @throws IOException             if an I/O error occurs while
     *                                 getting the content.
     * @throws UnknownServiceException if the protocol does not support
     *                                 the content type.
     * @see java.net.ContentHandlerFactory#createContentHandler(String)
     * @see java.net.URLConnection#getContentType()
     * @see java.net.URLConnection#setContentHandlerFactory(java.net.ContentHandlerFactory)
     */
    @Override
    public Object getContent() throws IOException {
        if (mode == Mode.Bypass) {
            return this.connection.getContent();
        }
        try {
            buildCache();
            return getObjectElementFromCache((interaction) -> interaction.getResponse().getBody(), null);
        } catch (VCRException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns a {@code String} representation of this URL connection.
     *
     * @return a string representation of this {@code URLConnection}.
     */
    @Override
    public String toString() {
        // use the built-in toString() method
        return this.connection.toString();
    }

    /**
     * Returns the value of this {@code URLConnection}'s
     * {@code doInput} flag.
     *
     * @return the value of this {@code URLConnection}'s
     * {@code doInput} flag.
     * @see #setDoInput(boolean)
     */
    @Override
    public boolean getDoInput() {
        // not in cassette, go to real connection
        return this.connection.getDoInput();
    }

    /**
     * Sets the value of the {@code doInput} field for this
     * {@code URLConnection} to the specified value.
     * <p>
     * A URL connection can be used for input and/or output.  Set the doInput
     * flag to true if you intend to use the URL connection for input,
     * false if not.  The default is true.
     *
     * @param doinput the new value.
     * @throws IllegalStateException if already connected
     * @see #getDoInput()
     */
    @Override
    public void setDoInput(boolean doinput) {
        if (cachedInteraction != null) {
            throw new IllegalStateException("Cannot set anything after interaction has been cached");
        }
        this.connection.setDoInput(doinput);
    }

    /**
     * Returns the value of this {@code URLConnection}'s
     * {@code doOutput} flag.
     *
     * @return the value of this {@code URLConnection}'s
     * {@code doOutput} flag.
     * @see #setDoOutput(boolean)
     */
    @Override
    public boolean getDoOutput() {
        // not in cassette, go to real connection
        return this.connection.getDoOutput();
    }

    /**
     * Sets the value of the {@code doOutput} field for this
     * {@code URLConnection} to the specified value.
     * <p>
     * A URL connection can be used for input and/or output.  Set the doOutput
     * flag to true if you intend to use the URL connection for output,
     * false if not.  The default is false.
     *
     * @param dooutput the new value.
     * @throws IllegalStateException if already connected
     * @see #getDoOutput()
     */
    @Override
    public void setDoOutput(boolean dooutput) {
        if (cachedInteraction != null) {
            throw new IllegalStateException("Cannot set anything after interaction has been cached");
        }
        if (this.connection.getDoOutput() != dooutput) {
            this.connection.setDoOutput(dooutput);
        }
    }

    /**
     * Returns the value of the {@code allowUserInteraction} field for
     * this object.
     *
     * @return the value of the {@code allowUserInteraction} field for
     * this object.
     * @see #setAllowUserInteraction(boolean)
     */
    @Override
    public boolean getAllowUserInteraction() {
        // not in cassette, go to real connection
        return this.connection.getAllowUserInteraction();
    }

    /**
     * Set the value of the {@code allowUserInteraction} field of
     * this {@code URLConnection}.
     *
     * @param allowuserinteraction the new value.
     * @throws IllegalStateException if already connected
     * @see #getAllowUserInteraction()
     */
    @Override
    public void setAllowUserInteraction(boolean allowuserinteraction) {
        if (cachedInteraction != null) {
            throw new IllegalStateException("Cannot set anything after interaction has been cached");
        }
        this.connection.setAllowUserInteraction(allowuserinteraction);
    }

    /**
     * Returns the value of this {@code URLConnection}'s
     * {@code useCaches} field.
     *
     * @return the value of this {@code URLConnection}'s
     * {@code useCaches} field.
     * @see #setUseCaches(boolean)
     */
    @Override
    public boolean getUseCaches() {
        // not in cassette, go to real connection
        return this.connection.getUseCaches();
    }

    /**
     * Sets the value of the {@code useCaches} field of this
     * {@code URLConnection} to the specified value.
     * <p>
     * Some protocols do caching of documents.  Occasionally, it is important
     * to be able to "tunnel through" and ignore the caches (e.g., the
     * "reload" button in a browser).  If the UseCaches flag on a connection
     * is true, the connection is allowed to use whatever caches it can.
     * If false, caches are to be ignored.
     * The default value comes from defaultUseCaches, which defaults to
     * true.
     *
     * @param usecaches a {@code boolean} indicating whether
     *                  or not to allow caching
     * @throws IllegalStateException if already connected
     * @see #getUseCaches()
     */
    @Override
    public void setUseCaches(boolean usecaches) {
        if (cachedInteraction != null) {
            throw new IllegalStateException("Cannot set anything after interaction has been cached");
        }
        this.connection.setUseCaches(usecaches);
    }

    /**
     * Returns the value of this object's {@code ifModifiedSince} field.
     *
     * @return the value of this object's {@code ifModifiedSince} field.
     * @see #setIfModifiedSince(long)
     */
    @Override
    public long getIfModifiedSince() {
        // not in cassette, go to real connection
        return this.connection.getIfModifiedSince();
    }

    /**
     * Sets the value of the {@code ifModifiedSince} field of
     * this {@code URLConnection} to the specified value.
     *
     * @param ifmodifiedsince the new value.
     * @throws IllegalStateException if already connected
     * @see #getIfModifiedSince()
     */
    @Override
    public void setIfModifiedSince(long ifmodifiedsince) {
        if (cachedInteraction != null) {
            throw new IllegalStateException("Cannot set anything after interaction has been cached");
        }
        this.connection.setIfModifiedSince(ifmodifiedsince);
    }

    /**
     * Returns the default value of a {@code URLConnection}'s
     * {@code useCaches} flag.
     * <p>
     * This default is "sticky", being a part of the static state of all
     * URLConnections.  This flag applies to the next, and all following
     * URLConnections that are created.
     *
     * @return the default value of a {@code URLConnection}'s
     * {@code useCaches} flag.
     * @see #setDefaultUseCaches(boolean)
     */
    @Override
    public boolean getDefaultUseCaches() {
        // not in cassette, go to real connection
        return this.connection.getDefaultUseCaches();
    }

    /**
     * Sets the default value of the {@code useCaches} field to the
     * specified value.
     *
     * @param defaultusecaches the new value.
     * @see #getDefaultUseCaches()
     */
    @Override
    public void setDefaultUseCaches(boolean defaultusecaches) {
        if (cachedInteraction != null) {
            throw new IllegalStateException("Cannot set anything after interaction has been cached");
        }
        this.connection.setDefaultUseCaches(defaultusecaches);
    }

    /**
     * Sets the general request property. If a property with the key already
     * exists, overwrite its value with the new value.
     *
     * <p> NOTE: HTTP requires all request properties which can
     * legally have multiple instances with the same key
     * to use a comma-separated list syntax which enables multiple
     * properties to be appended into a single property.
     *
     * @param key   the keyword by which the request is known
     *              (e.g., "{@code Accept}").
     * @param value the value associated with it.
     * @throws IllegalStateException if already connected
     * @throws NullPointerException  if key is {@code null}
     * @see #getRequestProperty(String)
     */
    @Override
    public void setRequestProperty(String key, String value) {
        if (cachedInteraction != null) {
            throw new IllegalStateException("Cannot set anything after interaction has been cached");
        }
        this.connection.setRequestProperty(key, value);
    }

    /**
     * Adds a general request property specified by a
     * key-value pair.  This method will not overwrite
     * existing values associated with the same key.
     *
     * @param key   the keyword by which the request is known
     *              (e.g., "{@code Accept}").
     * @param value the value associated with it.
     * @throws IllegalStateException if already connected
     * @throws NullPointerException  if key is null
     * @see #getRequestProperties()
     * @since 1.4
     */
    @Override
    public void addRequestProperty(String key, String value) {
        this.connection.addRequestProperty(key, value);
    }

    /**
     * Returns the value of the named general request property for this
     * connection.
     *
     * @param key the keyword by which the request is known (e.g., "Accept").
     * @return the value of the named general request property for this
     * connection. If key is null, then null is returned.
     * @throws IllegalStateException if already connected
     * @see #setRequestProperty(String, String)
     */
    @Override
    public String getRequestProperty(String key) {
        if (mode == Mode.Bypass) {
            return this.connection.getRequestProperty(key);
        }
        try {
            buildCache();
            return getStringElementFromCache((interaction) -> interaction.getRequest().getHeaders().get(key).toString(),
                    null);
        } catch (VCRException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns an unmodifiable Map of general request
     * properties for this connection. The Map keys
     * are Strings that represent the request-header
     * field names. Each Map value is a unmodifiable List
     * of Strings that represents the corresponding
     * field values.
     *
     * @return a Map of the general request properties for this connection.
     * @throws IllegalStateException if already connected
     * @since 1.4
     */
    @Override
    public Map<String, List<String>> getRequestProperties() {
        // always return the real connection's request properties
        return this.connection.getRequestProperties();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        if (mode == Mode.Bypass) {
            return this.connection.getInputStream();
        }
        try {
            buildCache();
            return createInputStream(this.cachedInteraction.getResponse().getBody());
        } catch (VCRException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    //CHECKSTYLE.OFF: ParameterName
    public long getHeaderFieldDate(String name, long Default) {
        // not in cassette, go to real connection
        return this.connection.getHeaderFieldDate(name, Default);
    }
    //CHECKSTYLE.ON: ParameterName

    /**
     * Returns the value of the {@code content-length} header field.
     * <p>
     * <B>Note</B>: {@link #getContentLengthLong() getContentLengthLong()}
     * should be preferred over this method, since it returns a {@code long}
     * instead and is therefore more portable.</P>
     *
     * @return the content length of the resource that this connection's URL
     * references, {@code -1} if the content length is not known,
     * or if the content length is greater than Integer.MAX_VALUE.
     */
    @Override
    public int getContentLength() {
        // not in cassette, go to real connection
        return this.connection.getContentLength();
    }

    /**
     * Returns the value of the {@code content-length} header field as a
     * long.
     *
     * @return the content length of the resource that this connection's URL
     * references, or {@code -1} if the content length is
     * not known.
     * @since 1.7
     */
    @Override
    public long getContentLengthLong() {
        // not in cassette, go to real connection
        return this.connection.getContentLengthLong();
    }

    /**
     * Returns the value of the {@code date} header field.
     *
     * @return the sending date of the resource that the URL references,
     * or {@code 0} if not known. The value returned is the
     * number of milliseconds since January 1, 1970 GMT.
     * @see java.net.URLConnection#getHeaderField(String)
     */
    @Override
    public long getDate() {
        // not in cassette, go to real connection
        return this.connection.getDate();
    }

    /**
     * Returns the value of the {@code last-modified} header field.
     * The result is the number of milliseconds since January 1, 1970 GMT.
     *
     * @return the date the resource referenced by this
     * {@code URLConnection} was last modified, or 0 if not known.
     * @see java.net.URLConnection#getHeaderField(String)
     */
    @Override
    public long getLastModified() {
        // not in cassette, go to real connection
        return this.connection.getLastModified();
    }

    /**
     * Returns the value of the named field parsed as a number.
     * <p>
     * This form of {@code getHeaderField} exists because some
     * connection types (e.g., {@code http-ng}) have pre-parsed
     * headers. Classes for that connection type can override this method
     * and short-circuit the parsing.
     *
     * @param name    the name of the header field.
     * @param Default the default value.
     * @return the value of the named field, parsed as an integer. The
     * {@code Default} value is returned if the field is
     * missing or malformed.
     */
    @Override
    //CHECKSTYLE.OFF: ParameterName
    public int getHeaderFieldInt(String name, int Default) {
        // not in cassette, go to real connection
        return this.connection.getHeaderFieldInt(name, Default);
    }
    //CHECKSTYLE.ON: ParameterName

    /**
     * Returns the value of the named field parsed as a number.
     * <p>
     * This form of {@code getHeaderField} exists because some
     * connection types (e.g., {@code http-ng}) have pre-parsed
     * headers. Classes for that connection type can override this method
     * and short-circuit the parsing.
     *
     * @param name    the name of the header field.
     * @param Default the default value.
     * @return the value of the named field, parsed as a long. The
     * {@code Default} value is returned if the field is
     * missing or malformed.
     * @since 1.7
     */
    @Override
    //CHECKSTYLE.OFF: ParameterName
    public long getHeaderFieldLong(String name, long Default) {
        // not in cassette, go to real connection
        return this.connection.getHeaderFieldLong(name, Default);
    }
    //CHECKSTYLE.ON: ParameterName

    /**
     * Retrieves the contents of this URL connection.
     *
     * @param classes the {@code Class} array
     *                indicating the requested types
     * @return the object fetched that is the first match of the type
     * specified in the classes array. null if none of
     * the requested types are supported.
     * The {@code instanceof} operator should be used to
     * determine the specific kind of object returned.
     * @throws IOException             if an I/O error occurs while
     *                                 getting the content.
     * @throws UnknownServiceException if the protocol does not support
     *                                 the content type.
     * @see java.net.URLConnection#getContent()
     * @see java.net.ContentHandlerFactory#createContentHandler(String)
     * @see java.net.URLConnection#getContent(Class[])
     * @see java.net.URLConnection#setContentHandlerFactory(java.net.ContentHandlerFactory)
     * @since 1.3
     */
    @Override
    @SuppressWarnings("rawtypes")
    public Object getContent(Class[] classes) throws IOException {
        // not in cassette, go to real connection
        return this.connection.getContent(classes);
    }

    /**
     * Returns an output stream that writes to this connection.
     *
     * @return an output stream that writes to this connection.
     * @throws IOException             if an I/O error occurs while
     *                                 creating the output stream.
     * @throws UnknownServiceException if the protocol does not support
     *                                 output.
     */
    @Override
    public OutputStream getOutputStream() throws IOException {
        // use proxy requestBody to store inputted data
        if (!this.getDoOutput()) {
            throw new IOException("Cannot get output stream when doOutput is false");
        }
        return this.requestBody;
    }
}
