/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */
package org.apache.tomcat.util.log;

import java.io.ByteArrayOutputStream;
import java.io.Writer;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.File;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.io.StringWriter;

import java.util.Hashtable;
import java.util.Stack;

/**
 * Per Thread System.err and System.out log capture data.
 *
 * @author Glenn L. Nielsen
 */

class CaptureLog {

    protected CaptureLog() {
        baos = new ByteArrayOutputStream();
        ps = new PrintStream(baos);
    }

    private ByteArrayOutputStream baos;
    private PrintStream ps;

    protected PrintStream getStream() {
        return ps;
    }

    protected void reset() {
        baos.reset();
    }

    protected String getCapture() {
        return baos.toString();
    }
}
