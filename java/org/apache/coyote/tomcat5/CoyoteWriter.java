/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */


package org.apache.coyote.tomcat5;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import org.apache.catalina.util.StringManager;
import com.sun.enterprise.web.io.ByteWriter;


/**
 * Coyote implementation of the servlet writer.
 * 
 * @author Remy Maucherat
 * @author Kin-man Chung
 */
public class CoyoteWriter
    extends PrintWriter implements ByteWriter {


    // -------------------------------------------------------------- Constants


    private static final char[] LINE_SEP = { '\r', '\n' };

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


    // --------------------------------------------------- ByteWriter Methods


    public void write(byte[] buff, int off, int len, int strlen) {

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
