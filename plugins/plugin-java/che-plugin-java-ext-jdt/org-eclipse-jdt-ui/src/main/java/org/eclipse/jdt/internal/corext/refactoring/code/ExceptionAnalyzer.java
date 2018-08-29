/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2014 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.refactoring.code;

import java.util.List;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.internal.corext.refactoring.util.AbstractExceptionAnalyzer;

/* package */ class ExceptionAnalyzer extends AbstractExceptionAnalyzer {

  public static ITypeBinding[] perform(ASTNode[] statements) {
    ExceptionAnalyzer analyzer = new ExceptionAnalyzer();
    for (int i = 0; i < statements.length; i++) {
      statements[i].accept(analyzer);
    }
    List<ITypeBinding> exceptions = analyzer.getCurrentExceptions();
    return exceptions.toArray(new ITypeBinding[exceptions.size()]);
  }

  @Override
  public boolean visit(ThrowStatement node) {
    ITypeBinding exception = node.getExpression().resolveTypeBinding();
    if (exception == null) // Safety net for null bindings when compiling fails.
    return true;

    addException(exception, node.getAST());
    return true;
  }

  @Override
  public boolean visit(MethodInvocation node) {
    return handleExceptions((IMethodBinding) node.getName().resolveBinding(), node);
  }

  @Override
  public boolean visit(SuperMethodInvocation node) {
    return handleExceptions((IMethodBinding) node.getName().resolveBinding(), node);
  }

  @Override
  public boolean visit(ClassInstanceCreation node) {
    return handleExceptions(node.resolveConstructorBinding(), node);
  }

  private boolean handleExceptions(IMethodBinding binding, ASTNode node) {
    if (binding == null) return true;
    addExceptions(binding.getExceptionTypes(), node.getAST());
    return true;
  }
}
