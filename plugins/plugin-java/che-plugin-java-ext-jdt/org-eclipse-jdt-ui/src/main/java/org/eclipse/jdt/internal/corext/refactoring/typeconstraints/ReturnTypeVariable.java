/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2011 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.refactoring.typeconstraints;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.internal.corext.dom.ASTNodes;
import org.eclipse.jdt.internal.corext.dom.Bindings;

public class ReturnTypeVariable extends ConstraintVariable {

  private final IMethodBinding fMethodBinding;

  public ReturnTypeVariable(ReturnStatement returnStatement) {
    this(getMethod(returnStatement).resolveBinding());
    Assert.isNotNull(returnStatement);
  }

  public ReturnTypeVariable(IMethodBinding methodBinding) {
    super(methodBinding.getReturnType());
    fMethodBinding = methodBinding;
  }

  public static MethodDeclaration getMethod(ReturnStatement returnStatement) {
    return (MethodDeclaration) ASTNodes.getParent(returnStatement, MethodDeclaration.class);
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "[" + Bindings.asString(fMethodBinding) + "]_returnType"; // $NON-NLS-1$ //$NON-NLS-2$
  }

  public IMethodBinding getMethodBinding() {
    return fMethodBinding;
  }
}
