/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */


package org.apache.catalina.users;


import java.util.Iterator;
import org.apache.catalina.Group;
import org.apache.catalina.Role;
import org.apache.catalina.User;
import org.apache.catalina.UserDatabase;


/**
 * <p>Convenience base class for {@link Group} implementations.</p>
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.2 $ $Date: 2005/12/08 01:28:11 $
 * @since 4.1
 */

public abstract class AbstractGroup implements Group {


    // ----------------------------------------------------- Instance Variables


    /**
     * The description of this group.
     */
    protected String description = null;


    /**
     * The group name of this group.
     */
    protected String groupname = null;


    // ------------------------------------------------------------- Properties


    /**
     * Return the description of this group.
     */
    public String getDescription() {

        return (this.description);

    }


    /**
     * Set the description of this group.
     *
     * @param description The new description
     */
    public void setDescription(String description) {

        this.description = description;

    }


    /**
     * Return the group name of this group, which must be unique
     * within the scope of a {@link UserDatabase}.
     */
    public String getGroupname() {

        return (this.groupname);

    }


    /**
     * Set the group name of this group, which must be unique
     * within the scope of a {@link UserDatabase}.
     *
     * @param groupname The new group name
     */
    public void setGroupname(String groupname) {

        this.groupname = groupname;

    }


    /**
     * Return the set of {@link Role}s assigned specifically to this group.
     */
    public abstract Iterator getRoles();


    /**
     * Return the {@link UserDatabase} within which this Group is defined.
     */
    public abstract UserDatabase getUserDatabase();


    /**
     * Return the set of {@link User}s that are members of this group.
     */
    public abstract Iterator getUsers();


    // --------------------------------------------------------- Public Methods


    /**
     * Add a new {@link Role} to those assigned specifically to this group.
     *
     * @param role The new role
     */
    public abstract void addRole(Role role);


    /**
     * Is this group specifically assigned the specified {@link Role}?
     *
     * @param role The role to check
     */
    public abstract boolean isInRole(Role role);


    /**
     * Remove a {@link Role} from those assigned to this group.
     *
     * @param role The old role
     */
    public abstract void removeRole(Role role);


    /**
     * Remove all {@link Role}s from those assigned to this group.
     */
    public abstract void removeRoles();


    // ------------------------------------------------------ Principal Methods


    /**
     * Make the principal name the same as the group name.
     */
    public String getName() {

        return (getGroupname());

    }


}
