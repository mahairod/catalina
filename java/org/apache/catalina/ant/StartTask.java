/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */


package org.apache.catalina.ant;


import java.net.URLEncoder;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;


/**
 * Ant task that implements the <code>/start</code> command, supported by the
 * Tomcat manager application.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.2 $ $Date: 2005/12/08 01:27:25 $
 * @since 4.1
 */
public class StartTask extends AbstractCatalinaTask {


    // ------------------------------------------------------------- Properties


    /**
     * The context path of the web application we are managing.
     */
    protected String path = null;

    public String getPath() {
        return (this.path);
    }

    public void setPath(String path) {
        this.path = path;
    }


    // --------------------------------------------------------- Public Methods


    /**
     * Execute the requested operation.
     *
     * @exception BuildException if an error occurs
     */
    public void execute() throws BuildException {

        super.execute();
        if (path == null) {
            throw new BuildException
                ("Must specify 'path' attribute");
        }
        execute("/start?path=" + URLEncoder.encode(this.path));

    }


}
