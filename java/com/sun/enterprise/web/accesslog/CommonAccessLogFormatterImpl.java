/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */

package com.sun.enterprise.web.accesslog;

import java.nio.CharBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.catalina.HttpResponse;
import org.apache.catalina.Request;
import org.apache.catalina.Response;

/**
 * Access log formatter using the <i>common</i> access log format from
 * Apache.
 */
public class CommonAccessLogFormatterImpl extends AccessLogFormatter {

    protected static final String NULL_VALUE = "-";


    /**
     * Constructor.
     */
    public CommonAccessLogFormatterImpl() {

        super();

        dayFormatter = new SimpleDateFormat("dd");
        dayFormatter.setTimeZone(tz);
        monthFormatter = new SimpleDateFormat("MM");
        monthFormatter.setTimeZone(tz);
        yearFormatter = new SimpleDateFormat("yyyy");
        yearFormatter.setTimeZone(tz);
        timeFormatter = new SimpleDateFormat("HH:mm:ss");
        timeFormatter.setTimeZone(tz);
    }


    /**
     * Appends an access log entry line, with info obtained from the given
     * request and response objects, to the given CharBuffer.
     *
     * @param request The request object from which to obtain access log info
     * @param response The response object from which to obtain access log info
     * @param charBuffer The CharBuffer to which to append access log info
     */
    public void appendLogEntry(Request request,
                               Response response,
                               CharBuffer charBuffer) {

        ServletRequest req = request.getRequest();
        HttpServletRequest hreq = (HttpServletRequest) req;

        appendClientName(charBuffer, req);
        charBuffer.put(SPACE);

        appendClientId(charBuffer, req);
        charBuffer.put(SPACE);

        appendAuthUserName(charBuffer, hreq);
        charBuffer.put(SPACE);

        appendCurrentDate(charBuffer);
        charBuffer.put(SPACE);

        appendRequestInfo(charBuffer, hreq);
        charBuffer.put(SPACE);

        appendResponseStatus(charBuffer, response);
        charBuffer.put(SPACE);

        appendResponseLength(charBuffer, response);
        charBuffer.put(SPACE);
    }


    /*
     * Appends the client host name of the given request to the given char
     * buffer.
     */
    private void appendClientName(CharBuffer cb, ServletRequest req) {
        String value = req.getRemoteHost();
        if (value == null) {
            value = NULL_VALUE;
        }
        cb.put(value);
    }


    /*
     * Appends the client's RFC 1413 identity to the given char buffer..
     */
    private void appendClientId(CharBuffer cb, ServletRequest req) {
        cb.put(NULL_VALUE); // unsupported
    }


    /*
     * Appends the authenticated user (if any) to the given char buffer.
     */
    private void appendAuthUserName(CharBuffer cb, HttpServletRequest hreq) {
        String user = hreq.getRemoteUser();
        if (user == null) {
            user = NULL_VALUE;
        }
        cb.put(user);
    }


    /*
     * Appends the current date to the given char buffer.
     */
    private void appendCurrentDate(CharBuffer cb) {
        Date date = getDate();
        cb.put("[");
        cb.put(dayFormatter.format(date));           // Day
        cb.put('/');
        cb.put(lookup(monthFormatter.format(date))); // Month
        cb.put('/');
        cb.put(yearFormatter.format(date));          // Year
        cb.put(':');
        cb.put(timeFormatter.format(date));          // Time
        cb.put(SPACE);
        cb.put(timeZone);                            // Time Zone
        cb.put("]");
    }


    /*
     * Appends info about the given request to the given char buffer.
     */
    private void appendRequestInfo(CharBuffer cb, HttpServletRequest hreq) {
        cb.put("\"");
        cb.put(hreq.getMethod());
        cb.put(SPACE);
        cb.put(hreq.getRequestURI());
        if (hreq.getQueryString() != null) {
            cb.put('?');
            cb.put(hreq.getQueryString());
        }
        cb.put(SPACE);
        cb.put(hreq.getProtocol());
        cb.put("\"");
    }


    /*
     * Appends the response status to the given char buffer.
     */
    private void appendResponseStatus(CharBuffer cb, Response response) {
        cb.put(String.valueOf(((HttpResponse) response).getStatus()));
    }


    /*
     * Appends the content length of the given response to the given char
     * buffer.
     */
    private void appendResponseLength(CharBuffer cb, Response response) {
        cb.put("" + response.getContentCount());
    }
}
