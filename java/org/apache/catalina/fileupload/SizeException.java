/*
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 */

/**
 * This interface is used to throw an IOException when the request or parts
 * of the multipart exceeds the allowable limited.
 *
 * @author: Kin-man Chung
 */
package org.apache.catalina.fileupload;

import java.io.IOException;

public class SizeException extends IOException {

    public SizeException(String message) {
        super(message);
    }
}
