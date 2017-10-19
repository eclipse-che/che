/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2011 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.fix;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.internal.corext.codemanipulation.StubUtility;
import org.eclipse.jdt.internal.corext.dom.ASTNodeFactory;
import org.eclipse.jdt.internal.corext.fix.CompilationUnitRewriteOperationsFix.CompilationUnitRewriteOperation;
import org.eclipse.jdt.internal.corext.refactoring.structure.CompilationUnitRewrite;
import org.eclipse.jdt.ui.CodeGeneration;
import org.eclipse.text.edits.TextEditGroup;

/**
 * Partial implementation of a serial version correction proposal.
 *
 * @since 3.1
 */
public abstract class AbstractSerialVersionOperation extends CompilationUnitRewriteOperation {

  /** The long literal suffix */
  protected static final String LONG_SUFFIX = "L"; // $NON-NLS-1$

  /** The default serial value */
  public static final long SERIAL_VALUE = 1;

  /** The default serial id expression */
  protected static final String DEFAULT_EXPRESSION = SERIAL_VALUE + LONG_SUFFIX;

  /** The name of the serial version field */
  protected static final String NAME_FIELD = "serialVersionUID"; // $NON-NLS-1$

  /** The originally selected node */
  private final ASTNode[] fNodes;

  private final ICompilationUnit fUnit;

  protected AbstractSerialVersionOperation(final ICompilationUnit unit, final ASTNode[] node) {
    fUnit = unit;
    fNodes = node;
  }

  /**
   * Adds an initializer to the specified variable declaration fragment.
   *
   * @param fragment the variable declaration fragment to add an initializer
   * @param declarationNode the declartion node
   * @return false if no id could be calculated
   */
  protected abstract boolean addInitializer(
      final VariableDeclarationFragment fragment, final ASTNode declarationNode);

  /**
   * Adds the necessary linked positions for the specified fragment.
   *
   * @param rewrite the ast rewrite to operate on
   * @param fragment the fragment to add linked positions to
   * @param positionGroups the list of {@link LinkedProposalPositionGroup}s
   */
  protected abstract void addLinkedPositions(
      final ASTRewrite rewrite,
      final VariableDeclarationFragment fragment,
      final LinkedProposalModel positionGroups);

  /** {@inheritDoc} */
  @Override
  public void rewriteAST(CompilationUnitRewrite cuRewrite, LinkedProposalModel positionGroups)
      throws CoreException {
    final ASTRewrite rewrite = cuRewrite.getASTRewrite();
    VariableDeclarationFragment fragment = null;
    for (int i = 0; i < fNodes.length; i++) {
      final ASTNode node = fNodes[i];

      final AST ast = node.getAST();

      fragment = ast.newVariableDeclarationFragment();
      fragment.setName(ast.newSimpleName(NAME_FIELD));

      final FieldDeclaration declaration = ast.newFieldDeclaration(fragment);
      declaration.setType(ast.newPrimitiveType(PrimitiveType.LONG));
      declaration
          .modifiers()
          .addAll(
              ASTNodeFactory.newModifiers(
                  ast, Modifier.PRIVATE | Modifier.STATIC | Modifier.FINAL));

      if (!addInitializer(fragment, node)) continue;

      if (fragment.getInitializer() != null) {

        final TextEditGroup editGroup =
            createTextEditGroup(FixMessages.SerialVersion_group_description, cuRewrite);
        if (node instanceof AbstractTypeDeclaration)
          rewrite
              .getListRewrite(node, ((AbstractTypeDeclaration) node).getBodyDeclarationsProperty())
              .insertAt(declaration, 0, editGroup);
        else if (node instanceof AnonymousClassDeclaration)
          rewrite
              .getListRewrite(node, AnonymousClassDeclaration.BODY_DECLARATIONS_PROPERTY)
              .insertAt(declaration, 0, editGroup);
        else if (node instanceof ParameterizedType) {
          final ParameterizedType type = (ParameterizedType) node;
          final ASTNode parent = type.getParent();
          if (parent instanceof ClassInstanceCreation) {
            final ClassInstanceCreation creation = (ClassInstanceCreation) parent;
            final AnonymousClassDeclaration anonymous = creation.getAnonymousClassDeclaration();
            if (anonymous != null)
              rewrite
                  .getListRewrite(anonymous, AnonymousClassDeclaration.BODY_DECLARATIONS_PROPERTY)
                  .insertAt(declaration, 0, editGroup);
          }
        } else Assert.isTrue(false);

        addLinkedPositions(rewrite, fragment, positionGroups);
      }

      final String comment =
          CodeGeneration.getFieldComment(
              fUnit,
              declaration.getType().toString(),
              NAME_FIELD,
              StubUtility.getLineDelimiterUsed(fUnit));
      if (comment != null && comment.length() > 0) {
        final Javadoc doc = (Javadoc) rewrite.createStringPlaceholder(comment, ASTNode.JAVADOC);
        declaration.setJavadoc(doc);
      }
    }
    if (fragment == null) return;

    positionGroups.setEndPosition(rewrite.track(fragment));
  }
}
