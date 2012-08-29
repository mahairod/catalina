/*
 * Copyright (c) 2012 Oracle and/or its affiliates. All rights reserved.
 *
 */

package org.apache.catalina.connector;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.WebConnection;

/**
 * Implementation of WebConnection for Servlet 3.1
 *
 * @author Amy Roh
 * @version $Revision: 1.23 $ $Date: 2007/07/09 20:46:45 $
 */
public class WebConnectionImpl implements WebConnection {

    protected static final Logger log = Logger.getLogger(Connector.class.getName());

    private ServletInputStream inputStream;

    private ServletOutputStream outputStream;

    // ----------------------------------------------------------- Constructor

    public WebConnectionImpl(ServletInputStream inputStream, ServletOutputStream outputStream) {
        this.inputStream = inputStream;
        this.outputStream = outputStream;
    }

    /**
     * Returns an input stream for this web connection.
     *
     * @return a ServletInputStream for reading binary data
     *
     * @exception java.io.IOException if an I/O error occurs
     */
    public ServletInputStream getInputStream() throws IOException {
        return inputStream;
    }

    /**
     * Returns an output stream for this web connection.
     *
     * @return a ServletOutputStream for writing binary data
     *
     * @exception IOException if an I/O error occurs
     */
    public ServletOutputStream getOutputStream() throws IOException{
        return outputStream;
    }
}