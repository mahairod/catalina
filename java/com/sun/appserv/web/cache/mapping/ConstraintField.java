/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */

package com.sun.appserv.web.cache.mapping;

import java.util.logging.Logger;
import java.util.logging.Level;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import com.sun.enterprise.web.logging.pwc.LogDomains;

/** ConstraintField class represents a single Field and constraints on its 
 *  values; Field name and its scope are inherited from the Field class. 
 */
public class ConstraintField extends Field {

    private static final String[] SCOPE_NAMES = {
        "", "context.attribute", "request.header", "request.parameter",
        "request.cookie", "request.attribute", "session.attribute",
        "session.id"
    };

    private static Logger _logger = null;
    private static boolean _isTraceEnabled = false;

    // whether to cache if there was a match
    boolean cacheOnMatch = true;
    // whether to cache if there was a failure to match
    boolean cacheOnMatchFailure = false;

    // field value constraints 
    ValueConstraint constraints[] = new ValueConstraint[0];

    /**
     * create a new cache field, given a string representation of the scope
     * @param name name of this field
     * @param scope scope of this field
     */
    public ConstraintField (String name, String scope) 
                                throws IllegalArgumentException {
        super(name, scope);
        if (_logger == null) {
            _logger = LogDomains.getLogger(LogDomains.PWC_LOGGER);
            _isTraceEnabled = _logger.isLoggable(Level.FINE);
        }
    }

    /** set whether to cache should the constraints check pass
     * @param cacheOnMatch should the constraint check pass, should we cache?
     */
    public void setCacheOnMatch(boolean cacheOnMatch) {
        this.cacheOnMatch = cacheOnMatch;
    }

    /**
     * @return cache-on-match setting
     */
    public boolean getCacheOnMatch() {
        return cacheOnMatch;
    }

    /** set whether to cache should there be a failure forcing the constraint
     * @param cacheOnMatchFailure should there be a constraint check failure,
     *  enable cache?
     */
    public void setCacheOnMatchFailure(boolean cacheOnMatchFailure) {
        this.cacheOnMatchFailure = cacheOnMatchFailure;
    }

    /**
     * @return cache-on-match-failure setting
     */
    public boolean getCacheOnMatchFailure() {
        return cacheOnMatchFailure;
    }

    /**
     * add a constraint for this field
     * @param constraint one constraint associated with this field
     */
    public void addConstraint(ValueConstraint constraint) {
        if (constraint == null)
            return;

        ValueConstraint results[] = 
            new ValueConstraint[constraints.length + 1];
        for (int i = 0; i < constraints.length; i++)
            results[i] = constraints[i];

        results[constraints.length] = constraint;
        constraints = results;
    }

    /**
     * add an array of constraints for this field
     * @param vcs constraints associated with this field
     */
    public void setValueConstraints(ValueConstraint[] vcs) {
        if (vcs == null)
            return;

        constraints = vcs;
    }

    /** apply the constraints on the value of the field in the given request.
     *  return a true if all the constraints pass; false when the 
     *  field is not found or the field value doesn't pass the caching 
     *  constraints. 
     */ 
    public boolean applyConstraints(ServletContext context,
                                    HttpServletRequest request) {

        Object value = getValue(context, request);
        if (value == null) {
            // the field is not present in the request
            if (_isTraceEnabled) {
                _logger.fine(
                    "The constraint field " + name
                    + " is not found in the scope " + SCOPE_NAMES[scope]
                    + "; returning cache-on-match-failure: "
                    + cacheOnMatchFailure);
            }
            return cacheOnMatchFailure;
        } else if (constraints.length == 0) {
            // the field is present but has no value constraints
            if (_isTraceEnabled) {
                _logger.fine(
                    "The constraint field " + name + " value = "
                    + value.toString() + " is found in scope "
                    + SCOPE_NAMES[scope] + "; returning cache-on-match: "
                    + cacheOnMatch);
            }
            return cacheOnMatch;
        }

        // apply all the value constraints
        for (int i = 0; i < constraints.length; i++) {
            ValueConstraint c = constraints[i];

            // one of the values matched
            if (c.matches(value)) {
                if (_isTraceEnabled) {
                    _logger.fine(
                        "The constraint field " + name + " value = "
                        + value.toString() + " is found in scope "
                        + SCOPE_NAMES[scope] + "; and matches with a value "
                        + c.toString() + "; returning cache-on-match: "
                        + cacheOnMatch);
            }
                return cacheOnMatch;
            }
        }

        // none of the values matched; should we cache?
        if (_isTraceEnabled) {
            _logger.fine(
                "The constraint field " + name + " value = "
                + value.toString() + " is found in scope " + SCOPE_NAMES[scope]
                + "; but didn't match any of the value constraints; "
                + "returning cache-on-match-failure = "
                + cacheOnMatchFailure);
        }
        return cacheOnMatchFailure;
    }
}
