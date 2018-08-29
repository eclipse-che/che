/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2014 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.refactoring.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.UnionType;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.internal.corext.dom.Bindings;

public abstract class AbstractExceptionAnalyzer extends ASTVisitor {

  private List<ITypeBinding> fCurrentExceptions; // Elements in this list are of type TypeBinding
  private Stack<List<ITypeBinding>> fTryStack;

  protected AbstractExceptionAnalyzer() {
    fTryStack = new Stack<List<ITypeBinding>>();
    fCurrentExceptions = new ArrayList<ITypeBinding>(1);
    fTryStack.push(fCurrentExceptions);
  }

  @Override
  public abstract boolean visit(ThrowStatement node);

  @Override
  public abstract boolean visit(MethodInvocation node);

  @Override
  public abstract boolean visit(ClassInstanceCreation node);

  @Override
  public boolean visit(TypeDeclaration node) {
    // Don't dive into a local type.
    if (node.isLocalTypeDeclaration()) return false;
    return true;
  }

  @Override
  public boolean visit(EnumDeclaration node) {
    // Don't dive into a local type.
    if (node.isLocalTypeDeclaration()) return false;
    return true;
  }

  @Override
  public boolean visit(AnnotationTypeDeclaration node) {
    // Don't dive into a local type.
    if (node.isLocalTypeDeclaration()) return false;
    return true;
  }

  @Override
  public boolean visit(AnonymousClassDeclaration node) {
    // Don't dive into a local type.
    return false;
  }

  @Override
  public boolean visit(LambdaExpression node) {
    // Don't dive into a lambda type.
    return false;
  }

  @Override
  public boolean visit(TryStatement node) {
    fCurrentExceptions = new ArrayList<ITypeBinding>(1);
    fTryStack.push(fCurrentExceptions);

    // visit try block
    node.getBody().accept(this);

    List<VariableDeclarationExpression> resources = node.resources();
    for (Iterator<VariableDeclarationExpression> iterator = resources.iterator();
        iterator.hasNext(); ) {
      iterator.next().accept(this);
    }

    // Remove those exceptions that get catch by following catch blocks
    List<CatchClause> catchClauses = node.catchClauses();
    if (!catchClauses.isEmpty()) handleCatchArguments(catchClauses);
    List<ITypeBinding> current = fTryStack.pop();
    fCurrentExceptions = fTryStack.peek();
    for (Iterator<ITypeBinding> iter = current.iterator(); iter.hasNext(); ) {
      addException(iter.next(), node.getAST());
    }

    // visit catch and finally
    for (Iterator<CatchClause> iter = catchClauses.iterator(); iter.hasNext(); ) {
      iter.next().accept(this);
    }
    if (node.getFinally() != null) node.getFinally().accept(this);

    // return false. We have visited the body by ourselves.
    return false;
  }

  @Override
  public boolean visit(VariableDeclarationExpression node) {
    if (node.getLocationInParent() == TryStatement.RESOURCES_PROPERTY) {
      Type type = node.getType();
      ITypeBinding resourceTypeBinding = type.resolveBinding();
      if (resourceTypeBinding != null) {
        IMethodBinding methodBinding =
            Bindings.findMethodInHierarchy(
                resourceTypeBinding, "close", new ITypeBinding[0]); // $NON-NLS-1$
        if (methodBinding != null) {
          addExceptions(methodBinding.getExceptionTypes(), node.getAST());
        }
      }
    }
    return super.visit(node);
  }

  protected void addExceptions(ITypeBinding[] exceptions, AST ast) {
    if (exceptions == null) return;
    for (int i = 0; i < exceptions.length; i++) {
      addException(exceptions[i], ast);
    }
  }

  protected void addException(ITypeBinding exception, AST ast) {
    exception = Bindings.normalizeForDeclarationUse(exception, ast);
    if (!fCurrentExceptions.contains(exception)) fCurrentExceptions.add(exception);
  }

  protected List<ITypeBinding> getCurrentExceptions() {
    return fCurrentExceptions;
  }

  private void handleCatchArguments(List<CatchClause> catchClauses) {
    for (Iterator<CatchClause> iter = catchClauses.iterator(); iter.hasNext(); ) {
      Type type = iter.next().getException().getType();
      if (type instanceof UnionType) {
        List<Type> types = ((UnionType) type).types();
        for (Iterator<Type> iterator = types.iterator(); iterator.hasNext(); ) {
          removeCaughtExceptions(iterator.next().resolveBinding());
        }
      } else {
        removeCaughtExceptions(type.resolveBinding());
      }
    }
  }

  private void removeCaughtExceptions(ITypeBinding catchTypeBinding) {
    if (catchTypeBinding == null) return;
    for (Iterator<ITypeBinding> exceptions =
            new ArrayList<ITypeBinding>(fCurrentExceptions).iterator();
        exceptions.hasNext(); ) {
      ITypeBinding throwTypeBinding = exceptions.next();
      if (catches(catchTypeBinding, throwTypeBinding)) fCurrentExceptions.remove(throwTypeBinding);
    }
  }

  private boolean catches(ITypeBinding catchTypeBinding, ITypeBinding throwTypeBinding) {
    while (throwTypeBinding != null) {
      if (throwTypeBinding == catchTypeBinding) return true;
      throwTypeBinding = throwTypeBinding.getSuperclass();
    }
    return false;
  }
}
