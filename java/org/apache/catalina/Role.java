/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */


package org.apache.catalina;


import java.security.Principal;


/**
 * <p>Abstract representation of a security role, suitable for use in
 * environments like JAAS that want to deal with <code>Principals</code>.</p>
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.2 $ $Date: 2005/12/08 01:27:19 $
 * @since 4.1
 */

public interface Role extends Principal {


    // ------------------------------------------------------------- Properties


    /**
     * Return the description of this role.
     */
    public String getDescription();


    /**
     * Set the description of this role.
     *
     * @param description The new description
     */
    public void setDescription(String description);


    /**
     * Return the role name of this role, which must be unique
     * within the scope of a {@link UserDatabase}.
     */
    public String getRolename();


    /**
     * Set the role name of this role, which must be unique
     * within the scope of a {@link UserDatabase}.
     *
     * @param rolename The new role name
     */
    public void setRolename(String rolename);


    /**
     * Return the {@link UserDatabase} within which this Role is defined.
     */
    public UserDatabase getUserDatabase();


}
