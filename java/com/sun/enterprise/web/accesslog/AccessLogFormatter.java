/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */

package com.sun.enterprise.web.accesslog;

import java.nio.CharBuffer;
import java.util.Date;
import java.util.TimeZone;
import java.text.SimpleDateFormat;
import org.apache.catalina.Request;
import org.apache.catalina.Response;

/**
 * Abstract class defining an interface for appending access log entries to the
 * access log in a customized access log format.
 */
public abstract class AccessLogFormatter {

    /**
     * The set of month abbreviations for log messages.
     */
    protected static final String months[] =
    { "Jan", "Feb", "Mar", "Apr", "May", "Jun",
      "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };

    /**
     * When formatting log lines, we often use strings like this one (" ").
     */
    protected static final String SPACE = " ";

    /**
     * A date formatter to format Dates into a day string in the format
     * "dd".
     */
    protected SimpleDateFormat dayFormatter = null;

    /**
     * A date formatter to format a Date into a month string in the format
     * "MM".
     */
    protected SimpleDateFormat monthFormatter = null;

    /**
     * A date formatter to format a Date into a year string in the format
     * "yyyy".
     */
    protected SimpleDateFormat yearFormatter = null;

    /**
     * A date formatter to format a Date into a time in the format
     * "kk:mm:ss" (kk is a 24-hour representation of the hour).
     */
    protected SimpleDateFormat timeFormatter = null;

    /**
     * The time zone relative to GMT.
     */
    protected String timeZone = null;

    protected TimeZone tz = null;

    /*
     * Flag indicating whether we need to measure the time (in milliseconds)
     * that was spent on each request
     */
    protected boolean needTimeTaken;

    /**
     * The system time when we last updated the Date that this valve
     * uses for log lines.
     */
    private Date currentDate = null;


    /**
     * Constructor.
     *
     * Initialize the timeZone and currentDate.
     */
    public AccessLogFormatter() {
        tz = TimeZone.getDefault();
        timeZone = calculateTimeZoneOffset(tz.getRawOffset());
        currentDate = new Date(System.currentTimeMillis());
    }


    /**
     * Appends an access log entry line, with info obtained from the given
     * request and response objects, to the given CharBuffer.
     *
     * @param request The request object from which to obtain access log info
     * @param response The response object from which to obtain access log info
     * @param charBuffer The CharBuffer to which to append access log info
     */
    public abstract void appendLogEntry(Request request,
                                        Response response,
                                        CharBuffer charBuffer);


    /**
     * This method returns a Date object that is accurate to within one
     * second.  If a writerThread calls this method to get a Date and it's been
     * less than 5 second since a new Date was created, this method
     * simply gives out the same Date again so that the system doesn't
     * spend time creating Date objects unnecessarily.
     */
    protected synchronized Date getDate() {

        // Only create a new Date once per second, max.
        long systime = System.currentTimeMillis();
        if ((systime - currentDate.getTime()) > 5000) {
            currentDate = new Date(systime);
        }

        return currentDate;

    }

    protected String calculateTimeZoneOffset(long offset) {
        StringBuffer sb = new StringBuffer();
        if ((offset<0))  {
            sb.append("-");
            offset = -offset;
        } else {
            sb.append("+");
        }

        long hourOffset = offset/(1000*60*60);
        long minuteOffset = (offset/(1000*60)) % 60;

        if (hourOffset<10)
            sb.append("0");
        sb.append(hourOffset);

        if (minuteOffset<10)
            sb.append("0");
        sb.append(minuteOffset);

        return sb.toString();
    }


    /**
     * Return the month abbreviation for the specified month, which must
     * be a two-digit String.
     *
     * @param month Month number ("01" .. "12").
     */
    protected String lookup(String month) {

        int index;
        try {
            index = Integer.parseInt(month) - 1;
        } catch (Throwable t) {
            index = 0;  // Can not happen, in theory
        }
        return (months[index]);

    }


    /**
     * Has the time-taken token been specified in the access log pattern?
     *
     * @return true if the time-taken token has been specified in the access
     * log pattern, false otherwise.
     */
    public boolean needTimeTaken() {
        return needTimeTaken;
    }    
}
