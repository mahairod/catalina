/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */

package com.sun.el.parser;

import javax.el.ELException;
import javax.el.MethodInfo;

import com.sun.el.lang.EvaluationContext;

/* All AST nodes must implement this interface.  It provides basic
   machinery for constructing the parent and child relationships
   between nodes. */

/**
 * @author Jacob Hookom [jacob@hookom.net]
 * @version $Change: 181177 $$DateTime: 2001/06/26 08:45:09 $$Author: kchung $
 */
public interface Node {

  /** This method is called after the node has been made the current
    node.  It indicates that child nodes can now be added to it. */
  public void jjtOpen();

  /** This method is called after all the child nodes have been
    added. */
  public void jjtClose();

  /** This pair of methods are used to inform the node of its
    parent. */
  public void jjtSetParent(Node n);
  public Node jjtGetParent();

  /** This method tells the node to add its argument to the node's
    list of children.  */
  public void jjtAddChild(Node n, int i);

  /** This method returns a child node.  The children are numbered
     from zero, left to right. */
  public Node jjtGetChild(int i);

  /** Return the number of children the node has. */
  public int jjtGetNumChildren();
  
  public String getImage();
  
  public Object getValue(EvaluationContext ctx) throws ELException;
  public void setValue(EvaluationContext ctx, Object value) throws ELException;
  public Class getType(EvaluationContext ctx) throws ELException;
  public boolean isReadOnly(EvaluationContext ctx) throws ELException;
  public void accept(NodeVisitor visitor) throws ELException;
  public MethodInfo getMethodInfo(EvaluationContext ctx, Class[] paramTypes) throws ELException;
  public Object invoke(EvaluationContext ctx, Class[] paramTypes, Object[] paramValues) throws ELException;

  public boolean equals(Object n);
  public int hashCode();
}
