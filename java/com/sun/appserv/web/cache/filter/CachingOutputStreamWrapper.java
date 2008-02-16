/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */

package com.sun.appserv.web.cache.filter;

import java.io.IOException;
import java.io.ByteArrayOutputStream;

import javax.servlet.ServletOutputStream;

/**
 * an output stream wrapper to cache response bytes
 */
public class CachingOutputStreamWrapper extends ServletOutputStream {

    ByteArrayOutputStream baos;

    public CachingOutputStreamWrapper() {
        this.baos = new ByteArrayOutputStream(4096);
    }

    /**
     * Write the specified byte to our output stream.
     *
     * @param b The byte to be written
     *
     * @exception IOException if an input/output error occurs
     */
    public void write(int b) throws IOException {
        baos.write(b);
    }

    /**
     * Write <code>b.length</code> bytes from the specified byte array
     * to our output stream.
     *
     * @param b The byte array to be written
     *
     * @exception IOException if an input/output error occurs
     */
    public void write(byte b[]) throws IOException {
        baos.write(b, 0, b.length);
    }

    /**
     * Write <code>len</code> bytes from the specified byte array, starting
     * at the specified offset, to our output stream.
     *
     * @param b The byte array containing the bytes to be written
     * @param off Zero-relative starting offset of the bytes to be written
     * @param len The number of bytes to be written
     *
     * @exception IOException if an input/output error occurs
     */
    public void write(byte b[], int off, int len) throws IOException {
        baos.write(b, off, len);
    }

    /**
     * Flush any buffered data for this output stream, which also causes the
     * response to be committed.
     */
    public void flush() throws IOException {
        // nothing to do with cached bytes
    }

    /**
     * Close this output stream, causing any buffered data to be flushed and
     * any further output data to throw an IOException.
     */
    public void close() throws IOException {
        // nothing to do with cached bytes
    }

    /**
     * return the cached bytes
     */
    public byte[] getBytes() {
        return baos.toByteArray();
    }
}
