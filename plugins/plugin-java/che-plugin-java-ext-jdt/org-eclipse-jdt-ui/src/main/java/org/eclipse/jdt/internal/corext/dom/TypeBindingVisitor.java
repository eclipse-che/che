/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2014 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: Dmitry Stalnov (dstalnov@fusionone.com) - contributed fix for bug "inline method
 * - doesn't handle implicit cast" (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=24941).
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.dom;

import org.eclipse.jdt.core.dom.ITypeBinding;

/**
 * Interface used in type binding visiting algorithms.
 *
 * @see Bindings#visitHierarchy(ITypeBinding, TypeBindingVisitor)
 */
public interface TypeBindingVisitor {

  /**
   * @param type a type binding
   * @return <code>true</code> to continue visiting types, or <code>false</code> to abort and return
   *     <code>false</code>
   */
  public boolean visit(ITypeBinding type);
}
