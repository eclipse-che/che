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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.internal.corext.dom.ASTNodeFactory;
import org.eclipse.jdt.internal.corext.dom.ASTNodes;
import org.eclipse.jdt.internal.corext.dom.ModifierRewrite;
import org.eclipse.jdt.internal.corext.dom.VariableDeclarationRewrite;
import org.eclipse.jdt.internal.corext.fix.LinkedProposalPositionGroup;
import org.eclipse.jdt.internal.corext.fix.LinkedProposalPositionGroup.PositionInformation;
import org.eclipse.jdt.internal.ui.text.correction.ASTResolving;
import org.eclipse.jdt.ui.CodeGeneration;
import org.eclipse.swt.graphics.Image;

public class ModifierChangeCorrectionProposal extends LinkedCorrectionProposal {

  private IBinding fBinding;
  private ASTNode fNode;
  private int fIncludedModifiers;
  private int fExcludedModifiers;

  public ModifierChangeCorrectionProposal(
      String label,
      ICompilationUnit targetCU,
      IBinding binding,
      ASTNode node,
      int includedModifiers,
      int excludedModifiers,
      int relevance,
      Image image) {
    super(label, targetCU, null, relevance, image);
    fBinding = binding;
    fNode = node;
    fIncludedModifiers = includedModifiers;
    fExcludedModifiers = excludedModifiers;
  }

  @Override
  protected ASTRewrite getRewrite() throws CoreException {
    CompilationUnit astRoot = ASTResolving.findParentCompilationUnit(fNode);
    ASTNode boundNode = astRoot.findDeclaringNode(fBinding);
    ASTNode declNode = null;

    if (boundNode != null) {
      declNode = boundNode; // is same CU
    } else {
      // setSelectionDescription(selectionDescription);
      CompilationUnit newRoot = ASTResolving.createQuickFixAST(getCompilationUnit(), null);
      declNode = newRoot.findDeclaringNode(fBinding.getKey());
    }
    if (declNode != null) {
      AST ast = declNode.getAST();
      ASTRewrite rewrite = ASTRewrite.create(ast);

      if (declNode.getNodeType() == ASTNode.VARIABLE_DECLARATION_FRAGMENT) {
        VariableDeclarationFragment fragment = (VariableDeclarationFragment) declNode;
        ASTNode parent = declNode.getParent();
        if (parent instanceof FieldDeclaration) {
          FieldDeclaration fieldDecl = (FieldDeclaration) parent;
          if (fieldDecl.fragments().size() > 1
              && (fieldDecl.getParent() instanceof AbstractTypeDeclaration)) { // split
            VariableDeclarationRewrite.rewriteModifiers(
                fieldDecl,
                new VariableDeclarationFragment[] {fragment},
                fIncludedModifiers,
                fExcludedModifiers,
                rewrite,
                null);
            return rewrite;
          }
        } else if (parent instanceof VariableDeclarationStatement) {
          VariableDeclarationStatement varDecl = (VariableDeclarationStatement) parent;
          if (varDecl.fragments().size() > 1 && (varDecl.getParent() instanceof Block)) { // split
            VariableDeclarationRewrite.rewriteModifiers(
                varDecl,
                new VariableDeclarationFragment[] {fragment},
                fIncludedModifiers,
                fExcludedModifiers,
                rewrite,
                null);
            return rewrite;
          }
        } else if (parent instanceof VariableDeclarationExpression) {
          // can't separate
        }
        declNode = parent;
      } else if (declNode.getNodeType() == ASTNode.METHOD_DECLARATION) {
        MethodDeclaration methodDecl = (MethodDeclaration) declNode;
        if (!methodDecl.isConstructor()) {
          IMethodBinding methodBinding = methodDecl.resolveBinding();
          if (methodDecl.getBody() == null
              && methodBinding != null
              && Modifier.isAbstract(methodBinding.getModifiers())
              && Modifier.isStatic(fIncludedModifiers)) {
            // add body
            ICompilationUnit unit = getCompilationUnit();
            String delimiter = unit.findRecommendedLineSeparator();
            String bodyStatement = ""; // $NON-NLS-1$

            Block body = ast.newBlock();
            rewrite.set(methodDecl, MethodDeclaration.BODY_PROPERTY, body, null);
            Type returnType = methodDecl.getReturnType2();
            if (returnType != null) {
              Expression expression =
                  ASTNodeFactory.newDefaultExpression(
                      ast, returnType, methodDecl.getExtraDimensions());
              if (expression != null) {
                ReturnStatement returnStatement = ast.newReturnStatement();
                returnStatement.setExpression(expression);
                bodyStatement =
                    ASTNodes.asFormattedString(
                        returnStatement, 0, delimiter, unit.getJavaProject().getOptions(true));
              }
            }
            String placeHolder =
                CodeGeneration.getMethodBodyContent(
                    unit,
                    methodBinding.getDeclaringClass().getName(),
                    methodBinding.getName(),
                    false,
                    bodyStatement,
                    delimiter);
            if (placeHolder != null) {
              ReturnStatement todoNode =
                  (ReturnStatement)
                      rewrite.createStringPlaceholder(placeHolder, ASTNode.RETURN_STATEMENT);
              body.statements().add(todoNode);
            }
          }
        }
      }
      ModifierRewrite listRewrite = ModifierRewrite.create(rewrite, declNode);
      PositionInformation trackedDeclNode =
          listRewrite.setModifiers(fIncludedModifiers, fExcludedModifiers, null);

      LinkedProposalPositionGroup positionGroup =
          new LinkedProposalPositionGroup("group"); // $NON-NLS-1$
      positionGroup.addPosition(trackedDeclNode);
      getLinkedProposalModel().addPositionGroup(positionGroup);

      if (boundNode != null) {
        // only set end position if in same CU
        setEndPosition(rewrite.track(fNode));
      }
      return rewrite;
    }
    return null;
  }
}
