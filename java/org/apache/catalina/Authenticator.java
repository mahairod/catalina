/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */


package org.apache.catalina;


/**
 * An <b>Authenticator</b> is a component (usually a Valve or Container) that
 * provides some sort of authentication service.  The interface itself has no
 * functional significance,  but is used as a tagging mechanism so that other
 * components can detect the presence (via an "instanceof Authenticator" test)
 * of an already configured authentication service.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.2 $ $Date: 2005/12/08 01:27:13 $
 */

public interface Authenticator {


}
