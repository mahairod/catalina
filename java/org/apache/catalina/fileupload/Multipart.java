/*
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 */

/**
 * This class is the base for implementing servlet 3.0 file upload
 *
 * @author Kin-man Chung
 */

package org.apache.catalina.fileupload;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class Multipart {

    private final String location;
    private final long maxFileSize;
    private final long maxRequestSize;
    private final int fileSizeThreshold;
    private File repository;
    private ProgressListener listener;

    private final HttpServletRequest request;
    private ArrayList<Part> parts;
    private List<Part> unmodifiableParts;

    public Multipart(HttpServletRequest request, String location,
                long maxFileSize, long maxRequestSize, int fileSizeThreshold) {
        this.request = request;
        this.location = location;
        this.maxFileSize = maxFileSize;
        this.maxRequestSize = maxRequestSize;
        this.fileSizeThreshold = fileSizeThreshold;
        repository = (File) request.getServletContext().getAttribute(
            ServletContext.TEMPDIR);
        if (location != null && location.length() != 0) {
            File tempFile= new File(location);
            if (tempFile.isAbsolute()) {
                repository = tempFile;
            } else {
                repository = new File(repository, location);
            }
        }
    }

    public String getLocation() {
        return location;
    }

    public int getFileSizeThreshold() {
        return fileSizeThreshold;
    }

    public long getMaxFileSize() {
        return maxFileSize;
    }

    public long getMaxRequestSize() {
        return maxRequestSize;
    }

    public File getRepository() {
        return repository;
    }

    private boolean isMultipart() {

        if (!request.getMethod().toLowerCase(Locale.ENGLISH).equals("post")) {
            return false;
        }
        String contentType = request.getContentType();
        if (contentType == null) {
            return false;
        }
        if (contentType.toLowerCase(Locale.ENGLISH).startsWith("multipart/form-data")) {
            return true;
        }
        return false;
    }

    private void initParts() throws IOException, ServletException {
        if (parts != null) {
            return;
        }
        parts = new ArrayList<Part>();
        try {
            RequestItemIterator iter = new RequestItemIterator(this, request);
            while (iter.hasNext()) {
                RequestItem requestItem = iter.next();
                PartItem partItem = new PartItem(this,
                                         requestItem.getHeaders(),
                                         requestItem.getFieldName(),
                                         requestItem.getContentType(),
                                         requestItem.isFormField(),
                                         requestItem.getName());
                Streams.copy(requestItem.openStream(),
                             partItem.getOutputStream(), true);
                parts.add((Part)partItem);
            }
        } catch (SizeException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public synchronized Collection<Part> getParts()
            throws IOException, ServletException {
        if (! isMultipart()) {
            throw new ServletException("The request content-type is not a multipart/form-data");
        }

        initParts();

        if (null == unmodifiableParts) {
            unmodifiableParts = Collections.unmodifiableList(parts);
        }

        return unmodifiableParts;
    }

    public Part getPart(String name) throws IOException, ServletException {

        if (! isMultipart()) {
            throw new ServletException("The request content-type is not a multipart/form-data");
        }

        initParts();
        for (Part part: parts) {
            String fieldName = part.getName();
            if (name.equals(fieldName)) {
                return part;
            }
        } 
        return null;
    }

    /**
     * Returns the progress listener.
     * @return The progress listener, if any, or null.
     */
    public ProgressListener getProgressListener() {
        return listener;
    }

    /**
     * Sets the progress listener.
     * @param pListener The progress listener, if any. Defaults to null.
     */
    public void setProgressListener(ProgressListener pListener) {
        listener = pListener;
    }

}

