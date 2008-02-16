/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */
package org.apache.jasper.compiler;

import javax.servlet.jsp.tagext.*;

/**
 * TagInfo extension used by tag handlers that are implemented via tag files.
 * This class provides access to the name of the Map used to store the
 * dynamic attribute names and values passed to the custom action invocation.
 * This information is used by the code generator.
 */
class JasperTagInfo extends TagInfo {

    private String dynamicAttrsMapName;

    public JasperTagInfo(String tagName,
			 String tagClassName,
			 String bodyContent,
			 String infoString,
			 TagLibraryInfo taglib,
			 TagExtraInfo tagExtraInfo,
			 TagAttributeInfo[] attributeInfo,
			 String displayName,
			 String smallIcon,
			 String largeIcon,
			 TagVariableInfo[] tvi,
			 String mapName) {

	super(tagName, tagClassName, bodyContent, infoString, taglib,
	      tagExtraInfo, attributeInfo, displayName, smallIcon, largeIcon,
	      tvi);
	this.dynamicAttrsMapName = mapName;
    }

    public String getDynamicAttributesMapName() {
	return dynamicAttrsMapName;
    }

    public boolean hasDynamicAttributes() {
        return dynamicAttrsMapName != null;
    }
}
