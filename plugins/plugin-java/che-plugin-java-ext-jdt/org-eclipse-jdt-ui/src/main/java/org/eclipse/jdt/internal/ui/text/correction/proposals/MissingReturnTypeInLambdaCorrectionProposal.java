/**
 * ***************************************************************************** Copyright (c) 2013,
 * 2014 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.ui.text.correction.proposals;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.internal.corext.dom.ASTNodeFactory;
import org.eclipse.jdt.internal.corext.dom.Bindings;
import org.eclipse.jdt.internal.corext.dom.ScopeAnalyzer;
import org.eclipse.jdt.internal.ui.text.correction.ASTResolving;

public class MissingReturnTypeInLambdaCorrectionProposal
    extends MissingReturnTypeCorrectionProposal {

  private final LambdaExpression lambdaExpression;

  public MissingReturnTypeInLambdaCorrectionProposal(
      ICompilationUnit cu, LambdaExpression lambda, ReturnStatement existingReturn, int relevance) {
    super(cu, null, existingReturn, relevance);
    lambdaExpression = lambda;
    fExistingReturn = existingReturn;
  }

  @Override
  protected AST getAST() {
    return lambdaExpression.getAST();
  }

  @Override
  public ITypeBinding getReturnTypeBinding() {
    IMethodBinding methodBinding = lambdaExpression.resolveMethodBinding();
    if (methodBinding != null && methodBinding.getReturnType() != null) {
      return methodBinding.getReturnType();
    }
    return null;
  }

  @Override
  protected CompilationUnit getCU() {
    return (CompilationUnit) lambdaExpression.getRoot();
  }

  @Override
  protected Expression createDefaultExpression(AST ast) {
    return ASTNodeFactory.newDefaultExpression(ast, getReturnTypeBinding());
  }

  @Override
  protected ASTNode getBody() {
    return lambdaExpression.getBody();
  }

  @Override
  protected int getModifiers() {
    return 0;
  }

  @Override
  protected Expression computeProposals(
      AST ast,
      ITypeBinding returnBinding,
      int returnOffset,
      CompilationUnit root,
      Expression result) {
    ScopeAnalyzer analyzer = new ScopeAnalyzer(root);
    IBinding[] bindings =
        analyzer.getDeclarationsInScope(
            returnOffset, ScopeAnalyzer.VARIABLES | ScopeAnalyzer.CHECK_VISIBILITY);

    org.eclipse.jdt.core.dom.NodeFinder finder =
        new org.eclipse.jdt.core.dom.NodeFinder(root, returnOffset, 0);
    ASTNode varDeclFrag =
        ASTResolving.findAncestor(finder.getCoveringNode(), ASTNode.VARIABLE_DECLARATION_FRAGMENT);
    IVariableBinding varDeclFragBinding = null;
    if (varDeclFrag != null)
      varDeclFragBinding = ((VariableDeclarationFragment) varDeclFrag).resolveBinding();
    for (int i = 0; i < bindings.length; i++) {
      IVariableBinding curr = (IVariableBinding) bindings[i];
      ITypeBinding type = curr.getType();
      // Bindings are compared to make sure that a lambda does not return a variable which is yet to
      // be initialised.
      if (type != null
          && type.isAssignmentCompatible(returnBinding)
          && testModifier(curr)
          && !Bindings.equals(curr, varDeclFragBinding)) {
        if (result == null) {
          result = ast.newSimpleName(curr.getName());
        }
        addLinkedPositionProposal(RETURN_EXPRESSION_KEY, curr.getName(), null);
      }
    }
    return result;
  }
}
