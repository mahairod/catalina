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

import org.apache.catalina.util.StringManager;

import javax.servlet.ServletOutputStream;
import java.io.IOException;

/**
 * Coyote implementation of the servlet output stream.
 * 
 * @author Costin Manolache
 * @author Remy Maucherat
 */
public class CoyoteOutputStream 
    extends ServletOutputStream {


    /**
     * The string manager for this package.
     */
    private static final StringManager sm =
        StringManager.getManager(Constants.Package);


    // ----------------------------------------------------- Instance Variables


    protected OutputBuffer ob;


    // ----------------------------------------------------------- Constructors


    // BEGIN S1AS 6175642
    /*
    protected CoyoteOutputStream(OutputBuffer ob) {
    */
    public CoyoteOutputStream(OutputBuffer ob) {
    // END S1AS 6175642
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


    // --------------------------------------------------- OutputStream Methods


    public void write(int i)
        throws IOException {
        // Disallow operation if the object has gone out of scope
        if (ob == null) {
            throw new IllegalStateException(
                sm.getString("object.invalidScope"));
        }

        ob.writeByte(i);
    }


    public void write(byte[] b)
        throws IOException {
        // Disallow operation if the object has gone out of scope
        if (ob == null) {
            throw new IllegalStateException(
                sm.getString("object.invalidScope"));
        }

        write(b, 0, b.length);
    }


    public void write(byte[] b, int off, int len)
        throws IOException {
        // Disallow operation if the object has gone out of scope
        if (ob == null) {
            throw new IllegalStateException(
                sm.getString("object.invalidScope"));
        }

        ob.write(b, off, len);
    }


    /**
     * Will send the buffer to the client.
     */
    public void flush()
        throws IOException {
        // Disallow operation if the object has gone out of scope
        if (ob == null) {
            throw new IllegalStateException(
                sm.getString("object.invalidScope"));
        }

        ob.flush();
    }


    public void close()
        throws IOException {
        // Disallow operation if the object has gone out of scope
        if (ob == null) {
            throw new IllegalStateException(
                sm.getString("object.invalidScope"));
        }

        ob.close();
    }


    // -------------------------------------------- ServletOutputStream Methods


    public void print(String s)
        throws IOException {
        // Disallow operation if the object has gone out of scope
        if (ob == null) {
            throw new IllegalStateException(
                sm.getString("object.invalidScope"));
        }

        ob.write(s);
    }


}

