/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */


package org.apache.catalina.startup;

import org.xml.sax.Attributes;

import org.apache.tomcat.util.IntrospectionUtils;
import com.sun.org.apache.commons.digester.Rule;

/**
 * Rule that uses the introspection utils to set properties of a context 
 * (everything except "path").
 * 
 * @author Remy Maucherat
 */
public class SetContextPropertiesRule extends Rule {


    // ----------------------------------------------------------- Constructors


    // ----------------------------------------------------- Instance Variables


    // --------------------------------------------------------- Public Methods


    /**
     * Handle the beginning of an XML element.
     *
     * @param attributes The attributes of this element
     *
     * @exception Exception if a processing error occurs
     */
    public void begin(String namespace, String nameX, Attributes attributes)
        throws Exception {

        for (int i = 0; i < attributes.getLength(); i++) {
            String name = attributes.getLocalName(i);
            if ("".equals(name)) {
                name = attributes.getQName(i);
            }
            if ("path".equals(name)) { 
                continue;
            }
            String value = attributes.getValue(i);
            IntrospectionUtils.setProperty(digester.peek(), name, value);
        }

    }


}
