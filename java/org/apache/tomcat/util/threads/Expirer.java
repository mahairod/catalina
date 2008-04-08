/*
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
 *
 *
 *
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.apache.tomcat.util.threads;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import org.apache.tomcat.util.buf.*;

/**
 * Expire unused objects. 
 * 
 */
public final class Expirer  implements ThreadPoolRunnable
{

    private static com.sun.org.apache.commons.logging.Log log=
        com.sun.org.apache.commons.logging.LogFactory.getLog(Expirer.class );

    // We can use Event/Listener, but this is probably simpler
    // and more efficient
    public static interface ExpireCallback {
	public void expired( TimeStamp o );
    }
    
    private int checkInterval = 60;
    private Reaper reaper;
    ExpireCallback expireCallback;

    public Expirer() {
    }

    // ------------------------------------------------------------- Properties
    public int getCheckInterval() {
	return checkInterval;
    }

    public void setCheckInterval(int checkInterval) {
	this.checkInterval = checkInterval;
    }

    public void setExpireCallback( ExpireCallback cb ) {
	expireCallback=cb;
    }
    
    // -------------------- Managed objects --------------------
    static final int INITIAL_SIZE=8;
    TimeStamp managedObjs[]=new TimeStamp[INITIAL_SIZE];
    TimeStamp checkedObjs[]=new TimeStamp[INITIAL_SIZE];
    int managedLen=managedObjs.length;
    int managedCount=0;
    
    public void addManagedObject( TimeStamp ts ) {
	synchronized( managedObjs ) {
	    if( managedCount >= managedLen ) {
		// What happens if expire is on the way ? Nothing,
		// expire will do it's job on the old array ( GC magic )
		// and the expired object will be marked as such
		// Same thing would happen ( and did ) with Hashtable
		TimeStamp newA[]=new TimeStamp[ 2 * managedLen ];
		System.arraycopy( managedObjs, 0, newA, 0, managedLen);
		managedObjs = newA;
		managedLen = 2 * managedLen;
	    }
	    managedObjs[managedCount]=ts;
	    managedCount++;
	}
    }

    public void removeManagedObject( TimeStamp ts ) {
	for( int i=0; i< managedCount; i++ ) {
	    if( ts == managedObjs[i] ) {
		synchronized( managedObjs ) {
		    managedObjs[ i ] = managedObjs[managedCount-1];
		    managedCount--;
		}
		return;
	    }
	}
    }
    
    // --------------------------------------------------------- Public Methods

    public void start() {
	// Start the background reaper thread
	if( reaper==null) {
	    reaper=new Reaper("Expirer");
	    reaper.addCallback( this, checkInterval * 1000 );
	}
	
	reaper.startReaper();
    }

    public void stop() {
	reaper.stopReaper();
    }


    // -------------------------------------------------------- Private Methods

    // ThreadPoolRunnable impl

    public Object[] getInitData() {
	return null;
    }

    public void runIt( Object td[] ) {
	long timeNow = System.currentTimeMillis();
	if( dL > 2 ) debug( "Checking " + timeNow );
	int checkedCount;
	synchronized( managedObjs ) {
	    checkedCount=managedCount;
	    if(checkedObjs.length < checkedCount)
		checkedObjs = new TimeStamp[managedLen];
	    System.arraycopy( managedObjs, 0, checkedObjs, 0, checkedCount);
	}
	for( int i=0; i< checkedCount; i++ ) {
	    TimeStamp ts=checkedObjs[i];
	    checkedObjs[i] = null;
	    
	    if (ts==null || !ts.isValid())
		continue;
	    
	    long maxInactiveInterval = ts.getMaxInactiveInterval();
	    if( dL > 3 ) debug( "TS: " + maxInactiveInterval + " " +
				ts.getLastAccessedTime());
	    if (maxInactiveInterval < 0)
		continue;
	    
	    long timeIdle = timeNow - ts.getLastAccessedTime();
	    
	    if (timeIdle >= maxInactiveInterval) {
		if( expireCallback != null ) {
		    if( dL > 0 )
			debug( ts + " " + timeIdle + " " +
			       maxInactiveInterval );
		    expireCallback.expired( ts );
		}
	    }
	}
    }

    private static final int dL=0;
    private void debug( String s ) {
        if (log.isDebugEnabled())
	    log.debug("Expirer: " + s );
    }
}
