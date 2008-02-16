/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */

package org.apache.catalina.ssi;

import java.io.ByteArrayOutputStream;
import javax.servlet.ServletOutputStream;


/**
 * Class that extends ServletOuputStream, used as a wrapper from within
 * <code>SsiInclude</code>
 *
 * @author Bip Thelin
 * @version $Revision: 1.3 $, $Date: 2007/02/13 19:16:20 $
 * @see ServletOutputStream and ByteArrayOutputStream
 */
public class ByteArrayServletOutputStream extends ServletOutputStream {
    /**
     * Our buffer to hold the stream.
     */
    protected ByteArrayOutputStream buf = null;


    /**
     * Construct a new ServletOutputStream.
     */
    public ByteArrayServletOutputStream() {
        buf = new ByteArrayOutputStream();
    }


    /**
     * @return the byte array.
     */
    public byte[] toByteArray() {
        return buf.toByteArray();
    }


    /**
     * Write to our buffer.
     *
     * @param b The parameter to write
     */
    public void write(int b) {
        buf.write(b);
    }
}
