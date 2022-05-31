package com.easypost.easyvcr.clients.httpurlconnection;

import java.io.IOException;
import java.io.OutputStream;

public final class RecordableRequestBody extends OutputStream {

    private byte[] data;

    /**
     * Constructor.
     */
    public RecordableRequestBody() {
        this.data = new byte[0];
    }

    @Override
    public void write(int b) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        byte[] newData = new byte[data.length + len];
        System.arraycopy(data, 0, newData, 0, data.length);
        System.arraycopy(b, off, newData, data.length, len);
        data = newData;
    }

    @Override
    public void close() throws IOException {
        // do nothing
    }

    /**
     * Returns the String data stored in this object.
     *
     * @return the String data stored in this object
     */
    public byte[] getData() {
        return data;
    }

    /**
     * Check if this object is storing any data.
     *
     * @return true if this object is storing any data, false otherwise
     */
    public boolean hasData() {
        return data.length > 0;
    }
}
