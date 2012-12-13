/*
 * Copyright (c) 2012 Oracle and/or its affiliates. All rights reserved.
 *
 */

package org.apache.catalina.connector;

import java.io.IOException;

import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpUpgradeHandler;
import javax.servlet.http.WebConnection;

/**
 * Implementation of WebConnection for Servlet 3.1
 *
 * @author Amy Roh
 * @author Shing Wai Chan
 * @version $Revision: 1.23 $ $Date: 2007/07/09 20:46:45 $
 */
public class WebConnectionImpl implements WebConnection {

    private ServletInputStream inputStream;

    private ServletOutputStream outputStream;

    private Request request;

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
    @Override
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
    @Override
    public ServletOutputStream getOutputStream() throws IOException{
        return outputStream;
    }

    @Override
    public void close() throws Exception {
        if ((request != null) && (request.isUpgrade())) {
            request.getHttpUpgradeHandler().destroy();
            request.getCoyoteRequest().getResponse().resume();
        }
        Exception exception = null;
        try {
            inputStream.close();
        } catch(Exception ex) {
            exception = ex;
        }
        try {
            outputStream.close();
        } catch(Exception ex) {
            exception = ex;
        }

        if (exception != null) {
            throw exception;
        }
    }

    public void setRequest(Request req) {
        request = req;
    }

}
