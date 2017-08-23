/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2010 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.refactoring.code;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.internal.corext.dom.CodeScopeBuilder;

public class CallContext {

  public ASTNode invocation;
  public Expression[] arguments;
  public String receiver;
  public boolean receiverIsStatic;
  public CodeScopeBuilder.Scope scope;
  public int callMode;
  public ImportRewrite importer;
  public ICompilationUnit compilationUnit;

  public CallContext(ASTNode inv, CodeScopeBuilder.Scope s, int cm, ImportRewrite i) {
    super();
    invocation = inv;
    scope = s;
    callMode = cm;
    importer = i;
  }

  public ITypeBinding getReceiverType() {
    Expression expression = Invocations.getExpression(invocation);
    if (expression != null) {
      return expression.resolveTypeBinding();
    }
    IMethodBinding method = Invocations.resolveBinding(invocation);
    if (method != null) {
      return method.getDeclaringClass();
    }
    return null;
  }
}
