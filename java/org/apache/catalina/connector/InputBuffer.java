/*
 * Copyright (c) 1997-2011 Oracle and/or its affiliates. All rights reserved.
 *
 *
 *
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.catalina.connector;

import java.io.IOException;
import java.io.Reader;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.catalina.util.StringManager;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.util.ByteChunk.ByteInputChannel;
import org.glassfish.grizzly.http.util.CharChunk;

/**
 * The buffer used by Tomcat request. This is a derivative of the Tomcat 3.3
 * OutputBuffer, adapted to handle input instead of output. This allows 
 * complete recycling of the facade objects (the ServletInputStream and the
 * BufferedReader).
 *
 * @author Remy Maucherat
 */
public class InputBuffer extends Reader
    implements ByteInputChannel, CharChunk.CharInputChannel,
               CharChunk.CharOutputChannel {

    private static final Logger log = Logger.getLogger(InputBuffer.class.getName());

    /**
     * The string manager for this package.
     */
    private static final StringManager sm =
        StringManager.getManager(Constants.Package);


    // -------------------------------------------------------------- Constants


    public static final int DEFAULT_BUFFER_SIZE = 8*1024;
    static final int debug = 0;


    // ----------------------------------------------------- Instance Variables


    /**
     * Associated Grizzly request.
     */
    private Request grizzlyRequest;

    private org.glassfish.grizzly.http.server.io.InputBuffer grizzlyInputBuffer;
    

    // ----------------------------------------------------------- Constructors


    /**
     * Default constructor. Allocate the buffer with the default buffer size.
     */
    public InputBuffer() {

        this(DEFAULT_BUFFER_SIZE);

    }


    /**
     * Alternate constructor which allows specifying the initial buffer size.
     *
     * @param size Buffer size to use
     */
    public InputBuffer(int size) {

//        this.size = size;
//        bb = new ByteChunk(size);
//        bb.setLimit(size);
//        bb.setByteInputChannel(this);
    }

    // ------------------------------------------------------------- Properties


    /**
     * Associated Grizzly request.
     * 
     * @param grizzlyRequest Associated Grizzly request
     */
    public void setRequest(Request grizzlyRequest) {
	this.grizzlyRequest = grizzlyRequest;
        this.grizzlyInputBuffer = grizzlyRequest.getInputBuffer();
    }


    /**
     * Get associated Grizzly request.
     * 
     * @return the associated Grizzly request
     */
    public Request getRequest() {
        return this.grizzlyRequest;
    }


    // --------------------------------------------------------- Public Methods


    /**
     * Recycle the output buffer.
     */
    public void recycle() {

        if (log.isLoggable(Level.FINEST))
            log.finest("recycle()");

        grizzlyInputBuffer = null;
        grizzlyRequest = null;

    }


    /**
     * Close the input buffer.
     * 
     * @throws IOException An underlying IOException occurred
     */
    public void close()
        throws IOException {
        grizzlyInputBuffer.close();
    }


    public int available()
        throws IOException {
        return grizzlyInputBuffer.readyData();
    }


    // ------------------------------------------------- Bytes Handling Methods


    /** 
     * Reads new bytes in the byte chunk.
     * 
     * @param cbuf Byte buffer to be written to the response
     * @param off Offset
     * @param len Length
     * 
     * @throws IOException An underlying IOException occurred
     */
    public int realReadBytes(byte cbuf[], int off, int len)
	throws IOException {
        return grizzlyInputBuffer.read(cbuf, off, len);
    }


    public int readByte()
        throws IOException {
        if (grizzlyInputBuffer.isClosed())
            throw new IOException(sm.getString("inputBuffer.streamClosed"));

        return grizzlyInputBuffer.readByte();
    }


    public int read(final byte[] b, final int off, final int len)
        throws IOException {
        if (grizzlyInputBuffer.isClosed())
            throw new IOException(sm.getString("inputBuffer.streamClosed"));

        return grizzlyInputBuffer.read(b, off, len);
    }


    // ------------------------------------------------- Chars Handling Methods


    /**
     * Since the converter will use append, it is possible to get chars to
     * be removed from the buffer for "writing". Since the chars have already
     * been read before, they are ignored. If a mark was set, then the
     * mark is lost.
     */
    public void realWriteChars(char c[], int off, int len) 
        throws IOException {
        // START OF SJSAS 6231069
//        initChar();
        // END OF SJSAS 6231069
//        markPos = -1;
    }


    public void setEncoding(final String encoding) {
        grizzlyInputBuffer.setDefaultEncoding(encoding);
    }


    public int realReadChars(final char cbuf[], final int off, final int len)
        throws IOException {

        return grizzlyInputBuffer.read(cbuf, off, len);

    }


    public int read()
        throws IOException {

        if (grizzlyInputBuffer.isClosed())
            throw new IOException(sm.getString("inputBuffer.streamClosed"));

        return grizzlyInputBuffer.readChar();
    }


    public int read(char[] cbuf)
        throws IOException {

        return read(cbuf, 0, cbuf.length);
    }


    public int read(char[] cbuf, int off, int len)
        throws IOException {

        if (grizzlyInputBuffer.isClosed())
            throw new IOException(sm.getString("inputBuffer.streamClosed"));

        return grizzlyInputBuffer.read(cbuf, off, len);
    }


    public long skip(long n)
        throws IOException {

        if (grizzlyInputBuffer.isClosed())
            throw new IOException(sm.getString("inputBuffer.streamClosed"));

        if (n < 0) {
            throw new IllegalArgumentException();
        }
        return grizzlyInputBuffer.skip(n, true);

    }


    public boolean ready()
        throws IOException {

        if (grizzlyInputBuffer.isClosed())
            throw new IOException(sm.getString("inputBuffer.streamClosed"));

        return grizzlyInputBuffer.ready();
    }


    public boolean markSupported() {
        return true;
    }


    public void mark(int readAheadLimit)
        throws IOException {
        grizzlyInputBuffer.mark(readAheadLimit);
    }


    public void reset()
        throws IOException {

        if (grizzlyInputBuffer.isClosed())
            throw new IOException(sm.getString("inputBuffer.streamClosed"));
        grizzlyInputBuffer.reset();
    }


    public void checkConverter() 
        throws IOException {

        grizzlyInputBuffer.processingChars();

    }
}
