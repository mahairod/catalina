/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */

package com.sun.enterprise.web;

import java.util.logging.Logger;
import java.util.logging.Level;
import org.apache.catalina.Context;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.session.FileStore;
import org.apache.catalina.session.PersistentManager;
import com.sun.enterprise.deployment.runtime.web.SessionManager;

public class FileStrategyBuilder extends BasePersistenceStrategyBuilder {
    
    public void initializePersistenceStrategy(
            Context ctx,
            SessionManager smBean,
            ServerConfigLookup serverConfigLookup) {

        super.initializePersistenceStrategy(ctx, smBean, serverConfigLookup);
        
        Object[] params = { ctx.getPath() };
        _logger.log(Level.INFO, "webcontainer.filePersistence", params);
        PersistentManager mgr = new PersistentManager();
        mgr.setMaxActiveSessions(maxSessions);

        mgr.setMaxIdleBackup(0);     // FIXME: Make configurable

        FileStore store = new FileStore();
        store.setDirectory(directory);
        mgr.setStore(store);
        
        // For intra-vm session locking
        StandardContext sctx = (StandardContext) ctx;
        sctx.restrictedSetPipeline(new PESessionLockingStandardPipeline(sctx));

        // Special code for Java Server Faces
        if (ctx.findParameter(JSF_HA_ENABLED) == null) {
            ctx.addParameter(JSF_HA_ENABLED, "true");
        }   
     
        //START OF 6364900
        mgr.setSessionLocker(new PESessionLocker(ctx));
        //END OF 6364900        

        ctx.setManager(mgr); 
        
        if(!sctx.isSessionTimeoutOveridden()) {
            mgr.setMaxInactiveInterval(sessionMaxInactiveInterval); 
        }        
    }
}
