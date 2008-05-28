/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 */

package com.sun.enterprise.web;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.ArrayList;
import org.apache.catalina.Context;
import org.apache.catalina.Container;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.session.StandardManager;
import com.sun.enterprise.deployment.runtime.web.SessionManager;
import com.sun.enterprise.util.uuid.UuidGenerator;

public class MemoryStrategyBuilder extends BasePersistenceStrategyBuilder {
    
    public void initializePersistenceStrategy(
            Context ctx,
            SessionManager smBean,
            ServerConfigLookup serverConfigLookup) {

        super.initializePersistenceStrategy(ctx, smBean, serverConfigLookup);
        String persistenceType = "memory";        
        String ctxPath = ctx.getPath();
        if(ctxPath != null && !ctxPath.equals("")) {    
            Object[] params = { ctx.getPath(), persistenceType };
            _logger.log(Level.FINE, "webcontainer.noPersistence", params); 
        }
        StandardManager mgr = new StandardManager();
        if (sessionFilename == null) {
            mgr.setPathname(sessionFilename);
        } else {
            mgr.setPathname(prependContextPathTo(sessionFilename, ctx));
        }
        StandardContext sctx = (StandardContext) ctx;
        sctx.restrictedSetPipeline(new PESessionLockingStandardPipeline(sctx));

        mgr.setMaxActiveSessions(maxSessions);

        // START OF 6364900
        mgr.setSessionLocker(new PESessionLocker(ctx));
        // END OF 6364900        

        ctx.setManager(mgr);

        // START CR 6275709
        if (sessionIdGeneratorClassname != null) {
            try {
                UuidGenerator generator = (UuidGenerator)
                    Class.forName(sessionIdGeneratorClassname).newInstance();
                mgr.setUuidGenerator(generator);
            } catch (Exception ex) {
                _logger.log(Level.SEVERE,
                            "Unable to load session uuid generator "
                            + sessionIdGeneratorClassname,
                            ex);
            }
        }
        // END CR 6275709
        
        if (!sctx.isSessionTimeoutOveridden()) {
            mgr.setMaxInactiveInterval(sessionMaxInactiveInterval); 
        }        
    }    
}
