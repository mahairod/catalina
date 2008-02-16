/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */

package com.sun.appserv.web.cache.filter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.apache.catalina.util.RequestUtil;
// import org.apache.catalina.Context;

/**
 * a wrapper to HttpServletResponse to cache the outbound headers and content
 * @see javax.servlet.http.HttpServletResponseWrapper and 
 * @see javax.servlet.http.HttpServletResponse
 * @see org.apache.catalina.connector.HttpResponseBase
 */
public class CachingResponseWrapper extends HttpServletResponseWrapper {

    int statusCode = HttpCacheEntry.VALUE_NOT_SET;

    /**
     * The HTTP headers explicitly added via addHeader(), but not including
     * those to be added with setContentLength(), setContentType(), and so on.
     * This collection is keyed by the header name, and the elements are
     * ArrayLists containing the associated values that have been set.
     *
     */
    HashMap headers = new HashMap();
    /**
     * cache all the set/addDateHeader calls
     */
    HashMap dateHeaders = new HashMap();
    /**
     * The set of Cookies associated with this Response.
     */
    ArrayList cookies = new ArrayList();

    int contentLength = HttpCacheEntry.VALUE_NOT_SET;
    String contentType;
    Locale locale;

    /**
     * Error flag. True if the response runs into an error.
     * treat the response to be in the error state if the servlet
     * doesn't get the OutpuStream or the Writer.
     */
    boolean error = true;

    /**
     * OutputStream and PrintWriter objects for this response.
     */
    CachingOutputStreamWrapper cosw;
    PrintWriter writer;

    /** 
     * Constructs a response adaptor wrapping the given response.
     * @throws java.lang.IllegalArgumentException if the response is null
     */
    public CachingResponseWrapper(HttpServletResponse response) {
	    super(response);
    }
    
    /**
     * Create and return a ServletOutputStream to write the content
     * associated with this Response.
     *
     * @exception IOException if an input/output error occurs
     */
    private CachingOutputStreamWrapper createCachingOutputStreamWrapper()
                                throws IOException {
        return new CachingOutputStreamWrapper();
    }

    /**
     * Return the servlet output stream associated with this Response.
     *
     * @exception IllegalStateException if <code>getWriter</code> has
     *  already been called for this response
     * @exception IOException if an input/output error occurs
     */
    public ServletOutputStream getOutputStream() throws IOException {
        if (writer != null)
            throw new IllegalStateException ("getOutputStream<>getWriter");

        if (cosw == null) {
            cosw = createCachingOutputStreamWrapper();
        }

        error = false;
        return (ServletOutputStream)cosw;
    }  
      
    /**
     * Return the writer associated with this Response.
     *
     * @exception IllegalStateException if <code>getOutputStream</code> has
     *  already been called for this response
     * @exception IOException if an input/output error occurs
     */
    public PrintWriter getWriter() throws IOException {

        if (writer != null)
            return (writer);

        if (cosw != null)
            throw new IllegalStateException ("getWriter<>getOutputStream");

        cosw = createCachingOutputStreamWrapper();

        OutputStreamWriter osw =
            new OutputStreamWriter(cosw, getCharacterEncoding());

        writer = new PrintWriter(osw);

        error = false;
        return (writer);
	}
    
    /**
     * Set the content length (in bytes) for this Response.
     *
     * @param len The new content length
     */
    public void setContentLength(int len) {
        super.setContentLength(len);

        this.contentLength = len;
    }
    
    /**
     * Set the content type for this Response.
     *
     * @param type The new content type
     */
    public void setContentType(String type) {
        super.setContentType(type);

        this.contentType = type;
    }
    
    /**
     * Set the Locale that is appropriate for this response, including
     * setting the appropriate character encoding.
     *
     * @param locale The new locale
     */
    public void setLocale(Locale locale) {
        super.setLocale(locale);

        this.locale = locale;
    }

    /**
     * The default behavior of this method is to call addCookie(Cookie cookie)
     * on the wrapped response object.
     */
    public void addCookie(Cookie cookie) {
        super.addCookie(cookie);

        synchronized (cookies) {
            cookies.add(cookie);
        }
    }
    
    /**
     * Set the specified header to the specified value.
     *
     * @param name Name of the header to set
     * @param value Value to be set
     */
    public void setHeader(String name, String value) {
        super.setHeader(name, value);

        ArrayList values = new ArrayList();
        values.add(value);

        synchronized (headers) {
            headers.put(name, values);
        }
    }

    /**
     * Set the specified integer header to the specified value.
     *
     * @param name Name of the header to set
     * @param value Integer value to be set
     */
    public void setIntHeader(String name, int value) {
        setHeader(name, "" + value);
    }

    /**
     * Add the specified header to the specified value.
     *
     * @param name Name of the header to set
     * @param value Value to be set
     */
    public void addHeader(String name, String value) {
        super.addHeader(name, value);

        ArrayList values = (ArrayList) headers.get(name);
        if (values == null) {
            values = new ArrayList();

            synchronized (headers) {
                headers.put(name, values);
            }
        }

        values.add(value);
    }

    /**
     * Add the specified integer header to the specified value.
     *
     * @param name Name of the header to set
     * @param value Integer value to be set
     */
    public void addIntHeader(String name, int value) {
        addHeader(name, "" + value);
    }

    /**
     * Set the specified date header to the specified value.
     *
     * @param name Name of the header to set
     * @param value Date value to be set
     */
    public void setDateHeader(String name, long value) {
        super.setDateHeader(name, value);

        ArrayList values = new ArrayList();
        values.add(Long.valueOf(value));

        synchronized (dateHeaders) {
            dateHeaders.put(name, values);
        }
    }

    /**
     * Add the specified date header to the specified value.
     *
     * @param name Name of the header to set
     * @param value Date value to be set
     */
    public void addDateHeader(String name, long value) {
        super.addDateHeader(name, value);

        ArrayList values = (ArrayList) dateHeaders.get(name);
        if (values == null) {
            values = new ArrayList();

            synchronized (dateHeaders) {
                dateHeaders.put(name, values);
            }
        }

        values.add(Long.valueOf(value));
    }

    /**
     * Set the HTTP status to be returned with this response.
     *
     * @param sc The new HTTP status
     */
    public void setStatus(int sc) {
        super.setStatus(sc);

        this.statusCode = sc;
    }
 
    /**
     * Send an error response with the specified status and a
     * default message.
     *
     * @param status HTTP status code to send
     *
     * @exception IllegalStateException if this response has
     *  already been committed
     * @exception IOException if an input/output error occurs
     */
    public void sendError(int status) throws IOException {
        super.sendError(status);

        error = true;
    }

    /**
     * Send an error response with the specified status and message.
     *
     * @param status HTTP status code to send
     * @param message Corresponding message to send
     *
     * @exception IllegalStateException if this response has
     *  already been committed
     * @exception IOException if an input/output error occurs
     */
    public void sendError(int status, String message) throws IOException {
        super.sendError(status, message);

        error = true;
    }

    /**
     * has the response been set to error
     */
    public boolean isError() {
        return error;
    }

    /**
     * return the Expires: date header value
     */
    public Long getExpiresDateHeader() {
        return (Long)dateHeaders.get("Expires");
    }

    /**
     * called by doFilter to cache the response that was just sent out
     * @return the entry with cached response headers and body.
     */
    public HttpCacheEntry cacheResponse() throws IOException {
        // create a new entry
        HttpCacheEntry entry = new HttpCacheEntry();
        entry.responseHeaders = headers;
        entry.dateHeaders = dateHeaders;
        entry.cookies = cookies;

        entry.contentLength = contentLength;
        entry.contentType = contentType;
        entry.locale = locale;

        entry.statusCode = statusCode;

        // flush the writer??
        if (writer != null) {
            writer.flush();
        }

        entry.bytes = cosw.getBytes();
        return entry;
    }
    
    /**
     * clear the contents of this wrapper
     */
    public void clear() {
        cosw = null;
        writer = null;
    }
}
