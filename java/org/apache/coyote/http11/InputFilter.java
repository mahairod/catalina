/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */

package org.apache.coyote.http11;

import java.io.IOException;

import org.apache.tomcat.util.buf.ByteChunk;

import org.apache.coyote.InputBuffer;
import org.apache.coyote.Request;

/**
 * Input filter interface.
 * 
 * @author Remy Maucherat
 */
public interface InputFilter extends InputBuffer {


    /**
     * Read bytes.
     * 
     * @return Number of bytes read.
     */
    public int doRead(ByteChunk chunk, Request unused)
        throws IOException;


    /**
     * Some filters need additional parameters from the request. All the 
     * necessary reading can occur in that method, as this method is called
     * after the request header processing is complete.
     */
    public void setRequest(Request request);


    /**
     * Make the filter ready to process the next request.
     */
    public void recycle();


    /**
     * Get the name of the encoding handled by this filter.
     */
    public ByteChunk getEncodingName();


    /**
     * Set the next buffer in the filter pipeline.
     */
    public void setBuffer(InputBuffer buffer);


    /**
     * End the current request.
     * 
     * @return 0 is the expected return value. A positive value indicates that
     * too many bytes were read. This method is allowed to use buffer.doRead
     * to consume extra bytes. The result of this method can't be negative (if
     * an error happens, an IOException should be thrown instead).
     */
    public long end()
        throws IOException;


}
