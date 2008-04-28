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
 * Access log formatter using the <i>combined</i> access log format from
 * Apache.
 */
public class CombinedAccessLogFormatterImpl extends CommonAccessLogFormatterImpl {

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

        super.appendLogEntry(request, response, charBuffer);

        ServletRequest req = request.getRequest();
        HttpServletRequest hreq = (HttpServletRequest) req;

        appendReferer(charBuffer, hreq);
        charBuffer.put(SPACE);

        appendUserAgent(charBuffer, hreq);
    }


    /*
     * Appends the value of the 'referer' header of the given request to
     * the given char buffer.
     */
    private void appendReferer(CharBuffer cb, HttpServletRequest hreq) {
        cb.put("\"");
        String referer = hreq.getHeader("referer");
        if (referer == null) {
            referer = NULL_VALUE;
        }
        cb.put(referer);
        cb.put("\"");
    }


    /*
     * Appends the value of the 'user-agent' header of the given request to
     * the given char buffer.
     */
    private void appendUserAgent(CharBuffer cb, HttpServletRequest hreq) {
        cb.put("\"");
        String ua = hreq.getHeader("user-agent");
        if (ua == null) {
            ua = NULL_VALUE;
        }
        cb.put(ua);
        cb.put("\"");
    }

}
