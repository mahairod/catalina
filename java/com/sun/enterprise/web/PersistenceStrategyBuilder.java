/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 */

package com.sun.enterprise.web;

import java.util.logging.Logger;
import org.apache.catalina.Context;
import com.sun.enterprise.deployment.runtime.web.SessionManager;

public interface PersistenceStrategyBuilder {
    
    public void initializePersistenceStrategy(Context ctx,
                                              SessionManager smBean);
    public void setPersistenceFrequency(String persistenceFrequency);
    public void setPersistenceScope(String persistenceScope);
    public void setPassedInPersistenceType(String persistenceType);
}
