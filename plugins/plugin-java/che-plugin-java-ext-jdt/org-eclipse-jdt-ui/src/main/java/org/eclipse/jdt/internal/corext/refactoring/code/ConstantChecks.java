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

import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.internal.corext.dom.fragments.ASTFragmentFactory;
import org.eclipse.jdt.internal.corext.dom.fragments.IExpressionFragment;

class ConstantChecks {
  private abstract static class ExpressionChecker extends ASTVisitor {

    private final IExpressionFragment fExpression;
    protected boolean fResult = true;

    public ExpressionChecker(IExpressionFragment ex) {
      fExpression = ex;
    }

    public boolean check() {
      fResult = true;
      fExpression.getAssociatedNode().accept(this);
      return fResult;
    }
  }

  private static class LoadTimeConstantChecker extends ExpressionChecker {
    public LoadTimeConstantChecker(IExpressionFragment ex) {
      super(ex);
    }

    @Override
    public boolean visit(SuperFieldAccess node) {
      fResult = false;
      return false;
    }

    @Override
    public boolean visit(SuperMethodInvocation node) {
      fResult = false;
      return false;
    }

    @Override
    public boolean visit(ThisExpression node) {
      fResult = false;
      return false;
    }

    @Override
    public boolean visit(FieldAccess node) {
      fResult &=
          new LoadTimeConstantChecker(
                  (IExpressionFragment)
                      ASTFragmentFactory.createFragmentForFullSubtree(node.getExpression()))
              .check();
      return false;
    }

    @Override
    public boolean visit(MethodInvocation node) {
      if (node.getExpression() == null) {
        visitName(node.getName());
      } else {
        fResult &=
            new LoadTimeConstantChecker(
                    (IExpressionFragment)
                        ASTFragmentFactory.createFragmentForFullSubtree(node.getExpression()))
                .check();
      }

      return false;
    }

    @Override
    public boolean visit(QualifiedName node) {
      return visitName(node);
    }

    @Override
    public boolean visit(SimpleName node) {
      return visitName(node);
    }

    private boolean visitName(Name name) {
      fResult &= checkName(name);
      return false; // Do not descend further
    }

    private boolean checkName(Name name) {
      IBinding binding = name.resolveBinding();
      if (binding == null) return true; /* If the binding is null because of compile errors etc.,
				                  scenarios which may have been deemed unacceptable in
				                  the presence of semantic information will be admitted. */

      // If name represents a member:
      if (binding instanceof IVariableBinding || binding instanceof IMethodBinding)
        return isMemberReferenceValidInClassInitialization(name);
      else if (binding instanceof ITypeBinding) return !((ITypeBinding) binding).isTypeVariable();
      else {
        return true; // e.g. a NameQualifiedType's qualifier, which can be a package binding
      }
    }

    private boolean isMemberReferenceValidInClassInitialization(Name name) {
      IBinding binding = name.resolveBinding();
      Assert.isTrue(binding instanceof IVariableBinding || binding instanceof IMethodBinding);

      if (name instanceof SimpleName) return Modifier.isStatic(binding.getModifiers());
      else {
        Assert.isTrue(name instanceof QualifiedName);
        return checkName(((QualifiedName) name).getQualifier());
      }
    }
  }

  private static class StaticFinalConstantChecker extends ExpressionChecker {
    public StaticFinalConstantChecker(IExpressionFragment ex) {
      super(ex);
    }

    @Override
    public boolean visit(SuperFieldAccess node) {
      fResult = false;
      return false;
    }

    @Override
    public boolean visit(SuperMethodInvocation node) {
      fResult = false;
      return false;
    }

    @Override
    public boolean visit(ThisExpression node) {
      fResult = false;
      return false;
    }

    @Override
    public boolean visit(QualifiedName node) {
      return visitName(node);
    }

    @Override
    public boolean visit(SimpleName node) {
      return visitName(node);
    }

    private boolean visitName(Name name) {
      IBinding binding = name.resolveBinding();
      if (binding == null) {
        /* If the binding is null because of compile errors etc.,
          scenarios which may have been deemed unacceptable in
          the presence of semantic information will be admitted.
          Descend deeper.
        */
        return true;
      }

      int modifiers = binding.getModifiers();
      if (binding instanceof IVariableBinding) {
        if (!(Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers))) {
          fResult = false;
          return false;
        }
      } else if (binding instanceof IMethodBinding) {
        if (!Modifier.isStatic(modifiers)) {
          fResult = false;
          return false;
        }
      } else if (binding instanceof ITypeBinding) {
        return false; // It's o.k.  Don't descend deeper.

      } else {
        return false; // e.g. a NameQualifiedType's qualifier, which can be a package binding
      }

      // Descend deeper:
      return true;
    }
  }

  public static boolean isStaticFinalConstant(IExpressionFragment ex) {
    return new StaticFinalConstantChecker(ex).check();
  }

  public static boolean isLoadTimeConstant(IExpressionFragment ex) {
    return new LoadTimeConstantChecker(ex).check();
  }
}
