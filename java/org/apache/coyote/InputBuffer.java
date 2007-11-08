/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */

package org.apache.coyote;

import java.io.IOException;

import org.apache.tomcat.util.buf.ByteChunk;


/**
 * Input buffer.
 *
 * This class is used only in the protocol implementation. All reading from
 * tomcat ( or adapter ) should be done using Request.doRead().
 * 
 * @author Remy Maucherat
 */
public interface InputBuffer {


    /** Return from the input stream.
        IMPORTANT: the current model assumes that the protocol will 'own' the
        buffer and return a pointer to it in ByteChunk ( i.e. the param will
        have chunk.getBytes()==null before call, and the result after the call ).
    */
    public int doRead(ByteChunk chunk, Request request) 
        throws IOException;


}
