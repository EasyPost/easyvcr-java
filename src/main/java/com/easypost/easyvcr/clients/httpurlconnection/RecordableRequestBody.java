package com.easypost.easyvcr.clients.httpurlconnection;

import java.io.IOException;
import java.io.OutputStream;

public final class RecordableRequestBody extends OutputStream {

    private String data;

    /**
     * Constructor.
     */
    public RecordableRequestBody() {
        this.data = "";
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
        data += new String(b, off, len);
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
    public String getData() {
        return data;
    }

    /**
     * Check if this object is storing any data.
     *
     * @return true if this object is storing any data, false otherwise
     */
    public boolean hasData() {
        return !data.isEmpty();
    }

    /**
     * Write the String data stored in this object to the given OutputStream.
     *
     * @param outputStream the OutputStream to write to
     * @throws IOException if an I/O error occurs
     */
    public void writeToOutputStream(OutputStream outputStream) throws IOException {
        outputStream.write(data.getBytes());
    }
}
