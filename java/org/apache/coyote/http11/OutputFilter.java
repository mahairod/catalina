/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */

package org.apache.coyote.http11;

import java.io.IOException;

import org.apache.tomcat.util.buf.ByteChunk;

import org.apache.coyote.OutputBuffer;
import org.apache.coyote.Response;

/**
 * Output filter.
 * 
 * @author Remy Maucherat
 */
public interface OutputFilter extends OutputBuffer {


    /**
     * Write some bytes.
     * 
     * @return number of bytes written by the filter
     */
    public int doWrite(ByteChunk chunk, Response unused)
        throws IOException;


    /**
     * Some filters need additional parameters from the response. All the 
     * necessary reading can occur in that method, as this method is called
     * after the response header processing is complete.
     */
    public void setResponse(Response response);


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
    public void setBuffer(OutputBuffer buffer);


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
        throws IOException;


}
