/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */

package com.sun.enterprise.web.io;

/**
 * This interface defines additional functionalities a web container can
 * provide for the response writer.  If implementated, perfermance will
 * likely to be improved.
 *
 * @author Kin-man Chung
 */
 
public interface ByteWriter {

    /**
     * Write a portion of a byte array to the output.
     *
     * @param  buff  A byte array
     * @param  off   Offset from which to start reading byte
     * @param  len   Number of bytes to write
     * @param  strlen If non-zero, the length of the string from which
     *               the bytes was converted.  If zero, then the string
     *               length is unknown.
     */
    void write(byte buff[], int off, int len, int strlen)
        throws java.io.IOException;
}
