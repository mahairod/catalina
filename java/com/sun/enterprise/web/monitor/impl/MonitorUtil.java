/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */

/*
 * Copyright 2004-2005 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */

package com.sun.enterprise.web.monitor.impl;

import java.text.MessageFormat;
import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.enterprise.web.logging.pwc.LogDomains;

class MonitorUtil {

    private static Logger _logger = LogDomains.getLogger(LogDomains.PWC_LOGGER);

    /*
     * Convenience method for logging messages that require parametric
     * replacement.
     */
    static void log(Level level,
                    String key,
                    Object[] params,
                    Throwable t) {

        String msg = _logger.getResourceBundle().getString(key);
        msg = MessageFormat.format(msg, params);
        _logger.log(level, msg, t);
    }
}
