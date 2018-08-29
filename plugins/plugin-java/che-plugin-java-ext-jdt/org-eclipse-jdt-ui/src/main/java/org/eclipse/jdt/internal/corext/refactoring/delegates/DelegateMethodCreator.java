/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2011 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.refactoring.delegates;

import java.util.List;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.ChildPropertyDescriptor;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.MethodRef;
import org.eclipse.jdt.core.dom.MethodRefParameter;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.internal.corext.dom.ASTNodeFactory;
import org.eclipse.jdt.internal.corext.refactoring.RefactoringCoreMessages;

/**
 * Delegate creator for static and non-static methods.
 *
 * @since 3.2
 */
public class DelegateMethodCreator extends DelegateCreator {

  private ASTNode fDelegateInvocation;
  private MethodRef fDocMethodReference;

  @Override
  protected void initialize() {

    Assert.isTrue(getDeclaration() instanceof MethodDeclaration);

    if (getNewElementName() == null)
      setNewElementName(((MethodDeclaration) getDeclaration()).getName().getIdentifier());

    setInsertBefore(true);
  }

  @Override
  protected ASTNode createBody(BodyDeclaration bd) throws JavaModelException {

    MethodDeclaration methodDeclaration = (MethodDeclaration) bd;

    // interface or abstract method ? => don't create a method body.
    if (methodDeclaration.getBody() == null) return null;

    return createDelegateMethodBody(methodDeclaration);
  }

  @Override
  protected ASTNode createDocReference(final BodyDeclaration declaration)
      throws JavaModelException {
    fDocMethodReference = getAst().newMethodRef();
    fDocMethodReference.setName(getAst().newSimpleName(getNewElementName()));
    if (isMoveToAnotherFile()) fDocMethodReference.setQualifier(createDestinationTypeName());
    createArguments((MethodDeclaration) declaration, fDocMethodReference.parameters(), false);
    return fDocMethodReference;
  }

  @Override
  protected ASTNode getBodyHead(BodyDeclaration result) {
    return result;
  }

  @Override
  protected ChildPropertyDescriptor getJavaDocProperty() {
    return MethodDeclaration.JAVADOC_PROPERTY;
  }

  @Override
  protected ChildPropertyDescriptor getBodyProperty() {
    return MethodDeclaration.BODY_PROPERTY;
  }

  /**
   * @return the delegate incovation, either a {@link ConstructorInvocation} or a {@link
   *     MethodInvocation}. May be null if the delegate method is abstract (and therefore has no
   *     body at all)
   */
  public ASTNode getDelegateInvocation() {
    return fDelegateInvocation;
  }

  /**
   * @return the javadoc reference to the old method in the javadoc comment. May be null if no
   *     comment was created.
   */
  public MethodRef getJavadocReference() {
    return fDocMethodReference;
  }

  /**
   * Creates the corresponding statement for the method invocation, based on the return type.
   *
   * @param declaration the method declaration where the invocation statement is inserted
   * @param invocation the method invocation being encapsulated by the resulting statement
   * @return the corresponding statement
   */
  protected Statement createMethodInvocation(
      final MethodDeclaration declaration, final MethodInvocation invocation) {
    Assert.isNotNull(declaration);
    Assert.isNotNull(invocation);
    Statement statement = null;
    final Type type = declaration.getReturnType2();
    if (type == null) statement = createExpressionStatement(invocation);
    else {
      if (type instanceof PrimitiveType) {
        final PrimitiveType primitive = (PrimitiveType) type;
        if (primitive.getPrimitiveTypeCode().equals(PrimitiveType.VOID))
          statement = createExpressionStatement(invocation);
        else statement = createReturnStatement(invocation);
      } else statement = createReturnStatement(invocation);
    }
    return statement;
  }

  /** {@inheritDoc} */
  @Override
  protected IBinding getDeclarationBinding() {
    final MethodDeclaration declaration = (MethodDeclaration) getDeclaration();
    return declaration.resolveBinding();
  }

  @SuppressWarnings("unchecked")
  private void createArguments(
      final MethodDeclaration declaration,
      final List<? extends ASTNode> arguments,
      boolean methodInvocation) {
    Assert.isNotNull(declaration);
    Assert.isNotNull(arguments);
    SingleVariableDeclaration variable = null;
    final int size = declaration.parameters().size();
    for (int index = 0; index < size; index++) {
      variable = (SingleVariableDeclaration) declaration.parameters().get(index);

      if (methodInvocation) {
        // we are creating method invocation parameters
        final SimpleName expression = getAst().newSimpleName(variable.getName().getIdentifier());
        ((List<Expression>) arguments).add(expression);
      } else {
        // we are creating type info for the javadoc
        final MethodRefParameter parameter = getAst().newMethodRefParameter();
        parameter.setType(ASTNodeFactory.newType(getAst(), variable));
        if ((index == size - 1) && declaration.isVarargs()) parameter.setVarargs(true);
        ((List<MethodRefParameter>) arguments).add(parameter);
      }
    }
  }

  private Block createDelegateMethodBody(final MethodDeclaration declaration) {
    Assert.isNotNull(declaration);

    MethodDeclaration old = (MethodDeclaration) getDeclaration();
    List<Expression> arguments;
    Statement call;
    if (old.isConstructor()) {
      ConstructorInvocation invocation = getAst().newConstructorInvocation();
      arguments = invocation.arguments();
      call = invocation;
      fDelegateInvocation = invocation;
    } else {
      MethodInvocation invocation = getAst().newMethodInvocation();
      invocation.setName(getAst().newSimpleName(getNewElementName()));
      invocation.setExpression(getAccess());
      arguments = invocation.arguments();
      call = createMethodInvocation(declaration, invocation);
      fDelegateInvocation = invocation;
    }
    createArguments(declaration, arguments, true);

    final Block body = getAst().newBlock();
    body.statements().add(call);

    return body;
  }

  /**
   * Creates a new expression statement for the method invocation.
   *
   * @param invocation the method invocation
   * @return the corresponding statement
   */
  private ExpressionStatement createExpressionStatement(final MethodInvocation invocation) {
    Assert.isNotNull(invocation);
    return invocation.getAST().newExpressionStatement(invocation);
  }

  /**
   * Creates a new return statement for the method invocation.
   *
   * @param invocation the method invocation to create a return statement for
   * @return the corresponding statement
   */
  private ReturnStatement createReturnStatement(final MethodInvocation invocation) {
    Assert.isNotNull(invocation);
    final ReturnStatement statement = invocation.getAST().newReturnStatement();
    statement.setExpression(invocation);
    return statement;
  }

  @Override
  protected String getTextEditGroupLabel() {
    return RefactoringCoreMessages.DelegateMethodCreator_text_edit_group_field;
  }
}
