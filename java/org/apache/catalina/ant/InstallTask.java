/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */


package org.apache.catalina.ant;


import java.net.URLEncoder;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;


/**
 * Ant task that implements the <code>/install</code> command, supported by the
 * Tomcat manager application.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.2 $ $Date: 2005/12/08 01:27:23 $
 * @since 4.1
 * @deprecated Replaced by DeployTask
 */
public class InstallTask extends AbstractCatalinaTask {


    // ------------------------------------------------------------- Properties


    /**
     * URL of the context configuration file for this application, if any.
     */
    protected String config = null;

    public String getConfig() {
        return (this.config);
    }

    public void setConfig(String config) {
        this.config = config;
    }


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


    /**
     * URL of the web application archive (WAR) file, or the unpacked directory
     * containing this application, if any.
     */
    protected String war = null;

    public String getWar() {
        return (this.war);
    }

    public void setWar(String war) {
        this.war = war;
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
        if ((config == null) && (war == null)) {
            throw new BuildException
                ("Must specify at least one of 'config' and 'war'");
        }
        StringBuffer sb = new StringBuffer("/install?path=");
        sb.append(URLEncoder.encode(this.path));
        if (config != null) {
            sb.append("&config=");
            sb.append(URLEncoder.encode(config));
        }
        if (war != null) {
            sb.append("&war=");
            sb.append(URLEncoder.encode(war));
        }
        execute(sb.toString());

    }


}
