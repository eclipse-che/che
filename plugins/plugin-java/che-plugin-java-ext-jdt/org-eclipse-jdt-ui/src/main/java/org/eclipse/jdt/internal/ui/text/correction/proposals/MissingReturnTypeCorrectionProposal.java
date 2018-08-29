/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2014 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.ui.text.correction.proposals;

import java.util.List;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.internal.corext.dom.ASTNodeFactory;
import org.eclipse.jdt.internal.corext.dom.ASTNodes;
import org.eclipse.jdt.internal.corext.dom.ScopeAnalyzer;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.text.correction.CorrectionMessages;

public class MissingReturnTypeCorrectionProposal extends LinkedCorrectionProposal {

  protected static final String RETURN_EXPRESSION_KEY = "value"; // $NON-NLS-1$

  private MethodDeclaration fMethodDecl;
  protected ReturnStatement fExistingReturn;

  public MissingReturnTypeCorrectionProposal(
      ICompilationUnit cu, MethodDeclaration decl, ReturnStatement existingReturn, int relevance) {
    super(
        "",
        cu,
        null,
        relevance,
        JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_CHANGE)); // $NON-NLS-1$
    fMethodDecl = decl;
    fExistingReturn = existingReturn;
  }

  @Override
  public String getName() {
    if (fExistingReturn != null) {
      return CorrectionMessages
          .MissingReturnTypeCorrectionProposal_changereturnstatement_description;
    } else {
      return CorrectionMessages.MissingReturnTypeCorrectionProposal_addreturnstatement_description;
    }
  }

  /*(non-Javadoc)
   * @see org.eclipse.jdt.internal.ui.text.correction.ASTRewriteCorrectionProposal#getRewrite()
   */
  @Override
  protected ASTRewrite getRewrite() {
    AST ast = getAST();

    ITypeBinding returnBinding = getReturnTypeBinding();

    if (fExistingReturn != null) {
      ASTRewrite rewrite = ASTRewrite.create(ast);

      Expression expression =
          evaluateReturnExpressions(ast, returnBinding, fExistingReturn.getStartPosition());
      if (expression != null) {
        rewrite.set(fExistingReturn, ReturnStatement.EXPRESSION_PROPERTY, expression, null);

        addLinkedPosition(rewrite.track(expression), true, RETURN_EXPRESSION_KEY);
      }
      return rewrite;
    } else {
      ASTRewrite rewrite = ASTRewrite.create(ast);

      ASTNode body = getBody();
      // For lambda the body can be a block or an expression.
      if (body instanceof Block) {
        Block block = (Block) body;
        List<Statement> statements = block.statements();
        int nStatements = statements.size();
        ASTNode lastStatement = null;
        if (nStatements > 0) {
          lastStatement = statements.get(nStatements - 1);
        }

        if (returnBinding != null
            && lastStatement instanceof ExpressionStatement
            && lastStatement.getNodeType() != ASTNode.ASSIGNMENT) {
          Expression expression = ((ExpressionStatement) lastStatement).getExpression();
          ITypeBinding binding = expression.resolveTypeBinding();
          if (binding != null && binding.isAssignmentCompatible(returnBinding)) {
            Expression placeHolder = (Expression) rewrite.createMoveTarget(expression);

            ReturnStatement returnStatement = ast.newReturnStatement();
            returnStatement.setExpression(placeHolder);

            rewrite.replace(lastStatement, returnStatement, null);
            return rewrite;
          }
        }

        int offset;
        if (lastStatement == null) {
          offset = block.getStartPosition() + 1;
        } else {
          offset = lastStatement.getStartPosition() + lastStatement.getLength();
        }

        ReturnStatement returnStatement = ast.newReturnStatement();
        Expression expression = evaluateReturnExpressions(ast, returnBinding, offset);

        returnStatement.setExpression(expression);

        rewrite.getListRewrite(block, Block.STATEMENTS_PROPERTY).insertLast(returnStatement, null);

        addLinkedPosition(
            rewrite.track(returnStatement.getExpression()), true, RETURN_EXPRESSION_KEY);
      }
      return rewrite;
    }
  }

  /*
   * Evaluates possible return expressions. The favourite expression is returned.
   */
  private Expression evaluateReturnExpressions(
      AST ast, ITypeBinding returnBinding, int returnOffset) {
    CompilationUnit root = getCU();

    Expression result = null;
    if (returnBinding != null) {
      result = computeProposals(ast, returnBinding, returnOffset, root, result);
    }
    Expression defaultExpression = createDefaultExpression(ast);
    addLinkedPositionProposal(RETURN_EXPRESSION_KEY, ASTNodes.asString(defaultExpression), null);
    if (result == null) {
      return defaultExpression;
    }
    return result;
  }

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
    for (int i = 0; i < bindings.length; i++) {
      IVariableBinding curr = (IVariableBinding) bindings[i];
      ITypeBinding type = curr.getType();
      if (type != null && type.isAssignmentCompatible(returnBinding) && testModifier(curr)) {
        if (result == null) {
          result = ast.newSimpleName(curr.getName());
        }
        addLinkedPositionProposal(RETURN_EXPRESSION_KEY, curr.getName(), null);
      }
    }
    return result;
  }

  protected boolean testModifier(IVariableBinding curr) {
    int modifiers = curr.getModifiers();
    int staticFinal = Modifier.STATIC | Modifier.FINAL;
    if ((modifiers & staticFinal) == staticFinal) {
      return false;
    }
    if (Modifier.isStatic(modifiers) && !Modifier.isStatic(getModifiers())) {
      return false;
    }
    return true;
  }

  protected Expression createDefaultExpression(AST ast) {
    return ASTNodeFactory.newDefaultExpression(
        ast, fMethodDecl.getReturnType2(), fMethodDecl.getExtraDimensions());
  }

  protected CompilationUnit getCU() {
    return (CompilationUnit) fMethodDecl.getRoot();
  }

  protected ASTNode getBody() {
    return fMethodDecl.getBody();
  }

  protected AST getAST() {
    return fMethodDecl.getAST();
  }

  protected ITypeBinding getReturnTypeBinding() {
    IMethodBinding methodBinding = fMethodDecl.resolveBinding();
    if (methodBinding != null && methodBinding.getReturnType() != null) {
      return methodBinding.getReturnType();
    }
    return null;
  }

  protected int getModifiers() {
    return fMethodDecl.getModifiers();
  }
}
