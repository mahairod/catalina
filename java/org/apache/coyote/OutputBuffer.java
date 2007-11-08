/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */

package org.apache.coyote;

import java.io.IOException;

import org.apache.tomcat.util.buf.ByteChunk;


/**
 * Output buffer.
 *
 * This class is used internally by the protocol implementation. All writes
 * from higher level code should happen via Resonse.doWrite().
 * 
 * @author Remy Maucherat
 */
public interface OutputBuffer {

    /**
     * Writes the response. The caller ( tomcat ) owns the chunks.
     *
     * @param chunk Data to write
     * @param response Used to allow buffers that can be shared by multiple
     *        responses.
     *
     * @throws IOException
     */
    public int doWrite(ByteChunk chunk, Response response) 
        throws IOException;


}
