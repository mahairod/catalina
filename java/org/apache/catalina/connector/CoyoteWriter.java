/*
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import org.apache.catalina.util.StringManager;


/**
 * Coyote implementation of the servlet writer.
 * 
 * @author Remy Maucherat
 * @author Kin-man Chung
 */
public class CoyoteWriter
    extends PrintWriter {


    // -------------------------------------------------------------- Constants


    // No need for a do privileged block - every web app has permission to read
    // this by default
    private static final char[] LINE_SEP =
        System.getProperty("line.separator").toCharArray();

    /**
     * The string manager for this package.
     */
    private static final StringManager sm =
        StringManager.getManager(Constants.Package);


    // ----------------------------------------------------- Instance Variables


    protected OutputBuffer ob;
    protected boolean error = false;


    // ----------------------------------------------------------- Constructors


    public CoyoteWriter(OutputBuffer ob) {
        super(ob);
        this.ob = ob;
    }


    // --------------------------------------------------------- Public Methods


    /**
    * Prevent cloning the facade.
    */
    protected Object clone()
        throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }
    
    
    // -------------------------------------------------------- Package Methods


    /**
     * Clear facade.
     */
    void clear() {
        ob = null;
    }

    /**
     * Recycle.
     */
    void recycle() {
        error = false;
    }


    // --------------------------------------------------------- Writer Methods


    public void flush() {

        // Disallow operation if the object has gone out of scope
        if (ob == null) {
            throw new IllegalStateException(
                sm.getString("object.invalidScope"));
        }

        if (error)
            return;

        try {
            ob.flush();
        } catch (IOException e) {
            error = true;
        }

    }


    public void close() {

        // Disallow operation if the object has gone out of scope
        if (ob == null) {
            throw new IllegalStateException(
                sm.getString("object.invalidScope"));
        }

        // We don't close the PrintWriter - super() is not called,
        // so the stream can be reused. We close ob.
        try {
            ob.close();
        } catch (IOException ex ) {
            ;
        }
        error = false;

    }


    public boolean checkError() {
        // Disallow operation if the object has gone out of scope
        if (ob == null) {
            throw new IllegalStateException(
                sm.getString("object.invalidScope"));
        }
        flush();
        return error;
    }


    public void write(int c) {

        // Disallow operation if the object has gone out of scope
        if (ob == null) {
            throw new IllegalStateException(
                sm.getString("object.invalidScope"));
        }

        if (error)
            return;

        try {
            ob.write(c);
        } catch (IOException e) {
            error = true;
        }

    }


    public void write(char buf[], int off, int len) {

        // Disallow operation if the object has gone out of scope
        if (ob == null) {
            throw new IllegalStateException(
                sm.getString("object.invalidScope"));
        }

        if (error)
            return;

        try {
            ob.write(buf, off, len);
        } catch (IOException e) {
            error = true;
        }
    }


    public void write(char buf[]) {
	write(buf, 0, buf.length);
    }


    public void write(String s, int off, int len) {

        // Disallow operation if the object has gone out of scope
        if (ob == null) {
            throw new IllegalStateException(
                sm.getString("object.invalidScope"));
        }

        if (error)
            return;

        try {
            ob.write(s, off, len);
        } catch (IOException e) {
            error = true;
        }

    }


    public void write(String s) {
        write(s, 0, s.length());
    }


    public void write(byte[] buff, int off, int len) {

        // Disallow operation if the object has gone out of scope
        if (ob == null) {
            throw new IllegalStateException(
                sm.getString("object.invalidScope"));
        }

        if (error)
            return;

        try {
            ob.write(buff, off, len);
        } catch (IOException e) {
            error = true;
        }
    }


    // ---------------------------------------------------- PrintWriter Methods


    public void print(boolean b) {
        if (b) {
            write("true");
        } else {
            write("false");
        }
    }


    public void print(char c) {
        write(c);
    }


    public void print(int i) {
        write(String.valueOf(i));
    }


    public void print(long l) {
        write(String.valueOf(l));
    }


    public void print(float f) {
        write(String.valueOf(f));
    }


    public void print(double d) {
        write(String.valueOf(d));
    }


    public void print(char s[]) {
        write(s);
    }


    public void print(String s) {
        if (s == null) {
            s = "null";
        }
        write(s);
    }


    public void print(Object obj) {
        write(String.valueOf(obj));
    }


    public void println() {
        write(LINE_SEP);
    }


    public void println(boolean b) {
        print(b);
        println();
    }


    public void println(char c) {
        print(c);
        println();
    }


    public void println(int i) {
        print(i);
        println();
    }


    public void println(long l) {
        print(l);
        println();
    }


    public void println(float f) {
        print(f);
        println();
    }


    public void println(double d) {
        print(d);
        println();
    }


    public void println(char c[]) {
        print(c);
        println();
    }


    public void println(String s) {
        print(s);
        println();
    }


    public void println(Object o) {
        print(o);
        println();
    }
}
