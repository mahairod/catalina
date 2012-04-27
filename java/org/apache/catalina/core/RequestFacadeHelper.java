/*
 * Copyright (c) 2012 Oracle and/or its affiliates. All rights reserved.
 *
 */

package org.apache.catalina.core;

import org.apache.catalina.Globals;
import org.apache.catalina.Session;
import org.apache.catalina.connector.Constants;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.connector.SessionTracker;
import org.apache.catalina.util.StringManager;

import javax.servlet.ServletRequest;

/**
 * This class exposes some of the functionality of
 * org.apache.catalina.connector.Request and
 * org.apache.catalina.connector.Response.
 *
 * It is in this package for purpose of package visibility
 * of methods.
 *
 * @author Shing Wai Chan
 */
public class RequestFacadeHelper {
    //use the same resource properties as in org.apache.catalina.connector.RequestFacade
    private static final StringManager sm =
            StringManager.getManager(Constants.Package);

    private Request request;

    private Response response;

    public RequestFacadeHelper(Request request) {
        this.request = request;
        this.response = (Response)request.getResponse();
    }

    public static RequestFacadeHelper getInstance(ServletRequest srvRequest) {
        RequestFacadeHelper reqFacHelper =
           (RequestFacadeHelper) srvRequest.getAttribute(Globals.REQUEST_FACADE_HELPER);
        return reqFacHelper;
    }

    /**
     * Increment the depth of application dispatch
     */
    int incrementDispatchDepth() {
        validateRequest();
        return request.incrementDispatchDepth();
    }

    /**
     * Decrement the depth of application dispatch
     */
    int decrementDispatchDepth() {
        validateRequest();
        return request.decrementDispatchDepth();
    }

    /**
     * Check if the application dispatching has reached the maximum
     */
    boolean isMaxDispatchDepthReached() {
        validateRequest();
        return request.isMaxDispatchDepthReached();
    }

    void track(Session localSession) {
        validateRequest();
        SessionTracker sessionTracker = (SessionTracker)
            request.getNote(Globals.SESSION_TRACKER);
        if (sessionTracker != null) {
            sessionTracker.track(localSession);
        }
    }

    String getContextPath(boolean maskDefaultContextMapping) {
        validateRequest();
        return request.getContextPath(maskDefaultContextMapping);
    }

    void disableAsyncSupport() {
        validateRequest();
        request.disableAsyncSupport();
    }

    // --- for Response ---

    // START SJSAS 6374990
    boolean isResponseError() {
        validateResponse();
        return response.isError();
    }

    String getResponseMessage() {
        validateResponse();
        return response.getMessage();
    }

    int getResponseContentCount() {
        validateResponse();
        return response.getContentCount();
    }
    // END SJSAS 6374990

    void resetResponse() {
        validateResponse();
        response.setSuspended(false);
        response.setAppCommitted(false);
    }


    public void clear() {
        request = null;
        response = null;
    }

    private void validateRequest() {
        if (request == null) {
            throw new IllegalStateException(
                    sm.getString("requestFacade.nullRequest"));
        }
    }

    private void validateResponse() {
        if (response == null) {
            throw new IllegalStateException(
                    sm.getString("responseFacade.nullResponse"));
        }
    }
}
