/*
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 *
 *
 */

/**
 * This interface is used to throw an IOException when the request or parts
 * of the multipart exceeds the alloable limited.
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
