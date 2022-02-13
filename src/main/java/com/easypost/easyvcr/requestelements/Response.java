package com.easypost.easyvcr.requestelements;

import com.easypost.easyvcr.Statics;
import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpEntity;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.HttpParams;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.easypost.easyvcr.internalutilities.Tools.createInputStream;

/**
 * Represents an HTTP response tracked by EasyVCR.
 */
public final class Response extends HttpElement {
    /**
     * The body of the response.
     */
    private String body;

    /**
     * The HTTP version of the response.
     */
    private HttpVersion httpVersion;

    /**
     * The headers of the response.
     */
    private Map<String, List<String>> headers;

    /**
     * The status of the response.
     */
    private Status status;

    /**
     * The errors of the response.
     */
    private String errors;

    /**
     * The URI of the response.
     */
    private URI uri;

    /**
     * Build a CloseableHttpResponse out of this Response.
     *
     * @return a CloseableHttpResponse representation of this Response.
     */
    public CloseableHttpResponse toCloseableHttpResponse() {

        return new CloseableHttpResponse() {
            @Override
            public void close() throws IOException {
                // not implemented
            }

            @Override
            public StatusLine getStatusLine() {
                // not implemented
                return new StatusLine() {
                    @Override
                    public ProtocolVersion getProtocolVersion() {
                        return Response.this.httpVersion.asProtocolVersion();
                    }

                    @Override
                    public int getStatusCode() {
                        return Response.this.status.getCode();
                    }

                    @Override
                    public String getReasonPhrase() {
                        return Response.this.status.getMessage();
                    }
                };
            }

            @Override
            public void setStatusLine(StatusLine statusLine) {
                // not implemented
            }

            @Override
            public void setStatusLine(ProtocolVersion protocolVersion, int i) {
                // not implemented
            }

            @Override
            public void setStatusLine(ProtocolVersion protocolVersion, int i, String s) {
                // not implemented
            }

            @Override
            public HttpEntity getEntity() {
                return new HttpEntity() {
                    @Override
                    public boolean isRepeatable() {
                        return false;
                    }

                    @Override
                    public boolean isChunked() {
                        return false;
                    }

                    @Override
                    public long getContentLength() {
                        return Response.this.body.length();
                    }

                    @Override
                    public Header getContentType() {
                        // TODO: May be accidentally recursive
                        return Response.this.toCloseableHttpResponse().getFirstHeader("Content-Type");
                    }

                    @Override
                    public Header getContentEncoding() {
                        // TODO: May be accidentally recursive
                        return Response.this.toCloseableHttpResponse().getFirstHeader("Content-Encoding");
                    }

                    @Override
                    public InputStream getContent() throws IOException, UnsupportedOperationException {
                        return createInputStream(Response.this.body);
                    }

                    @Override
                    public void writeTo(OutputStream outputStream) throws IOException {
                        if (Response.this.body != null) {
                            outputStream.write(Response.this.body.getBytes());
                        }
                    }

                    @Override
                    public boolean isStreaming() {
                        return false;
                    }

                    @Override
                    public void consumeContent() throws IOException {
                        // not implemented
                    }
                };
            }

            @Override
            public void setEntity(HttpEntity httpEntity) {
                // not implemented
            }

            @Override
            public Locale getLocale() {
                // not implemented
                return null;
            }

            @Override
            public void setLocale(Locale locale) {
                // not implemented
            }

            @Override
            public void setStatusCode(int i) throws IllegalStateException {
                if (Response.this.status != null) {
                    Response.this.status.setCode(i);
                }
            }

            @Override
            public ProtocolVersion getProtocolVersion() {
                return Response.this.getHttpVersion().asProtocolVersion();
            }

            @Override
            public boolean containsHeader(String s) {
                return Response.this.getHeaders().containsKey(s);
            }

            @Override
            public Header[] getHeaders(String s) {
                List<String> matchingHeaderValues = Response.this.getHeaders().get(s);
                if (matchingHeaderValues == null) {
                    return null;
                }
                Header[] headers = new Header[matchingHeaderValues.size()];
                for (int i = 0; i < matchingHeaderValues.size(); i++) {
                    headers[i] = new BasicHeader(s, matchingHeaderValues.get(i));
                }
                return headers;
            }

            @Override
            public Header getFirstHeader(String s) {
                Header[] headers = getHeaders(s);
                if (headers == null || headers.length == 0) {
                    return null;
                }
                return headers[0];
            }

            @Override
            public void setReasonPhrase(String s) throws IllegalStateException {
                if (Response.this.status != null) {
                    Response.this.status.setMessage(s);
                }
            }

            @Override
            public Header getLastHeader(String s) {
                Header[] headers = getHeaders(s);
                if (headers == null || headers.length == 0) {
                    return null;
                }
                return headers[headers.length - 1];
            }

            @Override
            public Header[] getAllHeaders() {
                Map<String, List<String>> headerMap = Response.this.getHeaders();
                if (headerMap == null) {
                    return null;
                }
                List<Header> headers = new ArrayList<>();
                for (Map.Entry<String, List<String>> entry : headerMap.entrySet()) {
                    for (String value : entry.getValue()) {
                        headers.add(new BasicHeader(entry.getKey(), value));
                    }
                }

                return headers.toArray(new Header[0]);
            }

            @Override
            public void addHeader(Header header) {
                // not implemented
            }

            @Override
            public void addHeader(String s, String s1) {
                // not implemented
            }

            @Override
            public void setHeader(Header header) {
                // not implemented
            }

            @Override
            public void setHeader(String s, String s1) {
                // not implemented
            }

            @Override
            public void setHeaders(Header[] headers) {
                // not implemented
            }

            @Override
            public void removeHeader(Header header) {
                // not implemented
            }

            @Override
            public void removeHeaders(String s) {
                // not implemented
            }

            @Override
            public HeaderIterator headerIterator() {
                // not implemented
                return null;
            }

            @Override
            public HeaderIterator headerIterator(String s) {
                // not implemented
                return null;
            }

            @Override
            public HttpParams getParams() {
                // not implemented
                return null;
            }

            @Override
            public void setParams(HttpParams httpParams) {
                // not implemented
            }
        };
    }

    /**
     * Returns the body of the response.
     *
     * @return the body of the response
     */
    public String getBody() {
        return this.body;
    }

    /**
     * Sets the body of the response.
     *
     * @param body the body of the response
     */
    public void setBody(String body) {
        this.body = body;
    }

    /**
     * Returns the HttpVersion of the response.
     *
     * @return the HttpVersion of the response
     */
    public HttpVersion getHttpVersion() {
        return this.httpVersion;
    }

    /**
     * Sets the HTTP version of the response from a ProtocolVersion.
     *
     * @param version the HTTP version of the response as a ProtocolVersion
     */
    public void setHttpVersion(ProtocolVersion version) {
        this.httpVersion = new HttpVersion(version);
    }

    /**
     * Sets the HTTP version of the response from a String.
     *
     * @param version the HTTP version of the response as a String
     */
    public void setHttpVersion(String version) {
        this.httpVersion = new HttpVersion(version);
    }

    /**
     * Returns the headers of the response.
     *
     * @return the headers of the response
     */
    public Map<String, List<String>> getHeaders() {
        return this.headers;
    }

    /**
     * Sets the headers of the response.
     *
     * @param headers the headers of the response
     */
    public void setHeaders(Map<String, List<String>> headers) {
        this.headers = headers;
    }

    public void addReplayHeaders() {
        // add default replay headers
        Map<String, String> replayHeaders = Statics.getReplayHeaders();
        for (Map.Entry<String, String> entry : replayHeaders.entrySet()) {
            this.headers.put(entry.getKey(), Collections.singletonList(entry.getValue()));
        }
    }

    /**
     * Returns the status of the response.
     *
     * @return the status of the response
     */
    public Status getStatus() {
        return this.status;
    }

    /**
     * Sets the status of the response.
     *
     * @param status the status of the response
     */
    public void setStatus(Status status) {
        this.status = status;
    }

    /**
     * Returns the errors of the response.
     *
     * @return the errors of the response
     */
    public String getErrors() {
        return this.errors;
    }

    /**
     * Sets the errors of the response.
     *
     * @param errors the errors of the response
     */
    public void setErrors(String errors) {
        this.errors = errors;
    }

    /**
     * Returns the URI of the response.
     *
     * @return the URI of the response
     */
    public URI getUri() {
        return this.uri;
    }

    /**
     * Sets the URI of the response.
     *
     * @param uri the URI of the response
     */
    public void setUri(URI uri) {
        this.uri = uri;
    }

    /**
     * Returns the URI of the response as a string.
     *
     * @return the URI of the response as a string
     */
    public String getUriString() {
        return this.uri.toString();
    }

    /**
     * Sets the URI of the response from a string.
     *
     * @param uriString the URI of the response as a string
     */
    public void setUriString(String uriString) {
        this.uri = URI.create(uriString);
    }
}
