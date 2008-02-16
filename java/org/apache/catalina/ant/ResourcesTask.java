/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */


package org.apache.catalina.ant;


import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;


/**
 * Ant task that implements the <code>/resources</code> command, supported by
 * the Tomcat manager application.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.2 $ $Date: 2005/12/08 01:27:24 $
 * @since 4.1
 */
public class ResourcesTask extends AbstractCatalinaTask {


    // ------------------------------------------------------------- Properties


    /**
     * The fully qualified class name of the resource type being requested
     * (if any).
     */
    protected String type = null;

    public String getType() {
        return (this.type);
    }

    public void setType(String type) {
        this.type = type;
    }


    // --------------------------------------------------------- Public Methods


    /**
     * Execute the requested operation.
     *
     * @exception BuildException if an error occurs
     */
    public void execute() throws BuildException {

        super.execute();
        if (type != null) {
            execute("/resources?type=" + type);
        } else {
            execute("/resources");
        }

    }


}
