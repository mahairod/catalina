/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */

package org.apache.coyote.http11.filters;

import java.io.IOException;

import org.apache.tomcat.util.buf.ByteChunk;

import org.apache.coyote.InputBuffer;
import org.apache.coyote.Request;
import org.apache.coyote.http11.InputFilter;

/**
 * Void input filter, which returns -1 when attempting a read. Used with a GET,
 * HEAD, or a similar request.
 * 
 * @author Remy Maucherat
 */
public class VoidInputFilter implements InputFilter {


    // -------------------------------------------------------------- Constants


    protected static final String ENCODING_NAME = "void";
    protected static final ByteChunk ENCODING = new ByteChunk();


    // ----------------------------------------------------- Static Initializer


    static {
        ENCODING.setBytes(ENCODING_NAME.getBytes(), 0, ENCODING_NAME.length());
    }


    // ----------------------------------------------------- Instance Variables


    // --------------------------------------------------- OutputBuffer Methods


    /**
     * Write some bytes.
     * 
     * @return number of bytes written by the filter
     */
    public int doRead(ByteChunk chunk, Request req)
        throws IOException {

        return -1;

    }


    // --------------------------------------------------- OutputFilter Methods


    /**
     * Set the associated reauest.
     */
    public void setRequest(Request request) {
    }


    /**
     * Set the next buffer in the filter pipeline.
     */
    public void setBuffer(InputBuffer buffer) {
    }


    /**
     * Make the filter ready to process the next request.
     */
    public void recycle() {
    }


    /**
     * Return the name of the associated encoding; Here, the value is 
     * "void".
     */
    public ByteChunk getEncodingName() {
        return ENCODING;
    }


    /**
     * End the current request. It is acceptable to write extra bytes using
     * buffer.doWrite during the execution of this method.
     * 
     * @return Should return 0 unless the filter does some content length 
     * delimitation, in which case the number is the amount of extra bytes or
     * missing bytes, which would indicate an error. 
     * Note: It is recommended that extra bytes be swallowed by the filter.
     */
    public long end()
        throws IOException {
        return 0;
    }


}
