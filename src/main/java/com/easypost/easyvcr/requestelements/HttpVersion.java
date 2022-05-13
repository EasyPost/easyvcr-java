package com.easypost.easyvcr.requestelements;

import org.apache.http.ProtocolVersion;

/**
 * Represents an HTTP version.
 */
public final class HttpVersion {
    /**
     * HTTP protocol string.
     */
    private final String protocol;

    /**
     * HTTP minor version number.
     */
    private int minor = 0;

    /**
     * HTTP major version number.
     */
    private int major = 0;

    /**
     * Constructs a new HTTP version.
     *
     * @param version the HTTP version string
     */
    public HttpVersion(String version) {
        this.protocol = version;
    }

    /**
     * Constructs a new HTTP version.
     *
     * @param version the HTTP ProtocolVersion
     */
    public HttpVersion(ProtocolVersion version) {
        this.protocol = version.getProtocol();
        this.major = version.getMajor();
        this.minor = version.getMinor();
    }

    /**
     * Returns the HTTP version as a ProtocolVersion.
     *
     * @return the HTTP version as a ProtocolVersion
     */
    public ProtocolVersion asProtocolVersion() {
        return new ProtocolVersion(this.protocol, this.major, this.minor);
    }

    /**
     * Returns the HTTP version as a string.
     *
     * @return the HTTP version as a string
     */
    public String toString() {
        String string = this.protocol;
        if (this.major > 0) {
            string += "/" + this.major + "." + this.minor;
        }
        return string;
    }
}
