/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */

package org.apache.catalina.util;

import java.io.*;
import javax.servlet.*;

public final class ResponseUtil {

    /**
     * Copies the contents of the specified input stream to the specified
     * output stream.
     *
     * @param istream The input stream to read from
     * @param ostream The output stream to write to
     *
     * @return Exception that occurred during processing, or null
     */
    public static IOException copy(InputStream istream,
                                   ServletOutputStream ostream) {

        IOException exception = null;
        byte buffer[] = new byte[2048];
        int len = buffer.length;
        while (true) {
            try {
                len = istream.read(buffer);
                if (len == -1)
                    break;
                ostream.write(buffer, 0, len);
            } catch (IOException e) {
                exception = e;
                len = -1;
                break;
            }
        }
        return exception;

    }


    /**
     * Copies the contents of the specified input stream to the specified
     * output stream.
     *
     * @param reader The reader to read from
     * @param writer The writer to write to
     *
     * @return Exception that occurred during processing, or null
     */
    public static IOException copy(Reader reader, PrintWriter writer) {

        IOException exception = null;
        char buffer[] = new char[2048];
        int len = buffer.length;
        while (true) {
            try {
                len = reader.read(buffer);
                if (len == -1)
                    break;
                writer.write(buffer, 0, len);
            } catch (IOException e) {
                exception = e;
                len = -1;
                break;
            }
        }
        return exception;

    }

}
