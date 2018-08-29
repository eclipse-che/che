/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2011 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation Benjamin Muskalla
 * <bmuskalla@eclipsesource.com> - [quick fix] proposes wrong cast from Object to primitive int -
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=100593
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.ui.text.correction.proposals;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite.ImportRewriteContext;
import org.eclipse.jdt.internal.corext.codemanipulation.ContextSensitiveImportRewriteContext;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.text.correction.ASTResolving;

public class CastCorrectionProposal extends LinkedCorrectionProposal {

  public static final String ADD_CAST_ID = "org.eclipse.jdt.ui.correction.addCast"; // $NON-NLS-1$

  private final Expression fNodeToCast;
  private final ITypeBinding fCastType;

  /**
   * Creates a cast correction proposal.
   *
   * @param label the display name of the proposal
   * @param targetCU the compilation unit that is modified
   * @param nodeToCast the node to cast
   * @param castType the type to cast to, may be <code>null</code>
   * @param relevance the relevance of this proposal
   */
  public CastCorrectionProposal(
      String label,
      ICompilationUnit targetCU,
      Expression nodeToCast,
      ITypeBinding castType,
      int relevance) {
    super(
        label,
        targetCU,
        null,
        relevance,
        JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_CAST));
    fNodeToCast = nodeToCast;
    fCastType = castType;
    setCommandId(ADD_CAST_ID);
  }

  private Type getNewCastTypeNode(ASTRewrite rewrite, ImportRewrite importRewrite) {
    AST ast = rewrite.getAST();

    ImportRewriteContext context =
        new ContextSensitiveImportRewriteContext(
            (CompilationUnit) fNodeToCast.getRoot(), fNodeToCast.getStartPosition(), importRewrite);

    if (fCastType != null) {
      return importRewrite.addImport(fCastType, ast, context);
    }

    ASTNode node = fNodeToCast;
    ASTNode parent = node.getParent();
    if (parent instanceof CastExpression) {
      node = parent;
      parent = parent.getParent();
    }
    while (parent instanceof ParenthesizedExpression) {
      node = parent;
      parent = parent.getParent();
    }
    if (parent instanceof MethodInvocation) {
      MethodInvocation invocation = (MethodInvocation) node.getParent();
      if (invocation.getExpression() == node) {
        IBinding targetContext = ASTResolving.getParentMethodOrTypeBinding(node);
        ITypeBinding[] bindings =
            ASTResolving.getQualifierGuess(
                node.getRoot(),
                invocation.getName().getIdentifier(),
                invocation.arguments(),
                targetContext);
        if (bindings.length > 0) {
          ITypeBinding first = getCastFavorite(bindings, fNodeToCast.resolveTypeBinding());

          Type newTypeNode = importRewrite.addImport(first, ast, context);
          addLinkedPosition(rewrite.track(newTypeNode), true, "casttype"); // $NON-NLS-1$
          for (int i = 0; i < bindings.length; i++) {
            addLinkedPositionProposal("casttype", bindings[i]); // $NON-NLS-1$
          }
          return newTypeNode;
        }
      }
    }
    Type newCastType = ast.newSimpleType(ast.newSimpleName("Object")); // $NON-NLS-1$
    addLinkedPosition(rewrite.track(newCastType), true, "casttype"); // $NON-NLS-1$
    return newCastType;
  }

  private ITypeBinding getCastFavorite(
      ITypeBinding[] suggestedCasts, ITypeBinding nodeToCastBinding) {
    if (nodeToCastBinding == null) {
      return suggestedCasts[0];
    }
    ITypeBinding favourite = suggestedCasts[0];
    for (int i = 0; i < suggestedCasts.length; i++) {
      ITypeBinding curr = suggestedCasts[i];
      if (nodeToCastBinding.isCastCompatible(curr)) {
        return curr;
      }
      if (curr.isInterface()) {
        favourite = curr;
      }
    }
    return favourite;
  }

  @Override
  protected ASTRewrite getRewrite() throws CoreException {
    AST ast = fNodeToCast.getAST();
    ASTRewrite rewrite = ASTRewrite.create(ast);
    ImportRewrite importRewrite = createImportRewrite((CompilationUnit) fNodeToCast.getRoot());

    Type newTypeNode = getNewCastTypeNode(rewrite, importRewrite);

    if (fNodeToCast.getNodeType() == ASTNode.CAST_EXPRESSION) {
      CastExpression expression = (CastExpression) fNodeToCast;
      rewrite.replace(expression.getType(), newTypeNode, null);
    } else {
      Expression expressionCopy = (Expression) rewrite.createCopyTarget(fNodeToCast);
      if (needsInnerParantheses(fNodeToCast)) {
        ParenthesizedExpression parenthesizedExpression = ast.newParenthesizedExpression();
        parenthesizedExpression.setExpression(expressionCopy);
        expressionCopy = parenthesizedExpression;
      }

      CastExpression castExpression = ast.newCastExpression();
      castExpression.setExpression(expressionCopy);
      castExpression.setType(newTypeNode);

      ASTNode replacingNode = castExpression;
      if (needsOuterParantheses(fNodeToCast)) {
        ParenthesizedExpression parenthesizedExpression = ast.newParenthesizedExpression();
        parenthesizedExpression.setExpression(castExpression);
        replacingNode = parenthesizedExpression;
      }

      rewrite.replace(fNodeToCast, replacingNode, null);
    }
    return rewrite;
  }

  private static boolean needsInnerParantheses(ASTNode nodeToCast) {
    int nodeType = nodeToCast.getNodeType();

    // nodes have weaker precedence than cast
    return nodeType == ASTNode.INFIX_EXPRESSION
        || nodeType == ASTNode.CONDITIONAL_EXPRESSION
        || nodeType == ASTNode.ASSIGNMENT
        || nodeType == ASTNode.INSTANCEOF_EXPRESSION;
  }

  private static boolean needsOuterParantheses(ASTNode nodeToCast) {
    ASTNode parent = nodeToCast.getParent();
    if (parent instanceof MethodInvocation) {
      if (((MethodInvocation) parent).getExpression() == nodeToCast) {
        return true;
      }
    } else if (parent instanceof QualifiedName) {
      if (((QualifiedName) parent).getQualifier() == nodeToCast) {
        return true;
      }
    } else if (parent instanceof FieldAccess) {
      if (((FieldAccess) parent).getExpression() == nodeToCast) {
        return true;
      }
    }
    return false;
  }
}
