/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2010 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.refactoring.typeconstraints;

import org.eclipse.jdt.core.dom.*;

/**
 * Empty implementation of a creator - provided to allow subclasses to override only a subset of
 * methods. Subclass to provide constraint creation functionality.
 */
public class ConstraintCreator {

  public static final ITypeConstraint[] EMPTY_ARRAY = new ITypeConstraint[0];

  /**
   * @param node the AST node
   * @return array of type constraints, may be empty
   * @see
   *     org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.AnonymousClassDeclaration)
   */
  public ITypeConstraint[] create(AnonymousClassDeclaration node) {
    return EMPTY_ARRAY;
  }

  /**
   * @param node the AST node
   * @return array of type constraints, may be empty
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.ArrayAccess)
   */
  public ITypeConstraint[] create(ArrayAccess node) {
    return EMPTY_ARRAY;
  }

  /**
   * @param node the AST node
   * @return array of type constraints, may be empty
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.ArrayCreation)
   */
  public ITypeConstraint[] create(ArrayCreation node) {
    return EMPTY_ARRAY;
  }

  /**
   * @param node the AST node
   * @return array of type constraints, may be empty
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.ArrayInitializer)
   */
  public ITypeConstraint[] create(ArrayInitializer node) {
    return EMPTY_ARRAY;
  }

  /**
   * @param node the AST node
   * @return array of type constraints, may be empty
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.ArrayType)
   */
  public ITypeConstraint[] create(ArrayType node) {
    return EMPTY_ARRAY;
  }

  /**
   * @param node the AST node
   * @return array of type constraints, may be empty
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.AssertStatement)
   */
  public ITypeConstraint[] create(AssertStatement node) {
    return EMPTY_ARRAY;
  }

  /**
   * @param node the AST node
   * @return array of type constraints, may be empty
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.Assignment)
   */
  public ITypeConstraint[] create(Assignment node) {
    return EMPTY_ARRAY;
  }

  /**
   * @param node the AST node
   * @return array of type constraints, may be empty
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.Block)
   */
  public ITypeConstraint[] create(Block node) {
    return EMPTY_ARRAY;
  }

  /**
   * @param node the AST node
   * @return array of type constraints, may be empty
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.BooleanLiteral)
   */
  public ITypeConstraint[] create(BooleanLiteral node) {
    return EMPTY_ARRAY;
  }

  /**
   * @param node the AST node
   * @return array of type constraints, may be empty
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.BreakStatement)
   */
  public ITypeConstraint[] create(BreakStatement node) {
    return EMPTY_ARRAY;
  }

  /**
   * @param node the AST node
   * @return array of type constraints, may be empty
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.CastExpression)
   */
  public ITypeConstraint[] create(CastExpression node) {
    return EMPTY_ARRAY;
  }

  /**
   * @param node the AST node
   * @return array of type constraints, may be empty
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.CatchClause)
   */
  public ITypeConstraint[] create(CatchClause node) {
    return EMPTY_ARRAY;
  }

  /**
   * @param node the AST node
   * @return array of type constraints, may be empty
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.CharacterLiteral)
   */
  public ITypeConstraint[] create(CharacterLiteral node) {
    return EMPTY_ARRAY;
  }

  /**
   * @param node the AST node
   * @return array of type constraints, may be empty
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.ClassInstanceCreation)
   */
  public ITypeConstraint[] create(ClassInstanceCreation node) {
    return EMPTY_ARRAY;
  }

  /**
   * @param node the AST node
   * @return array of type constraints, may be empty
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.CompilationUnit)
   */
  public ITypeConstraint[] create(CompilationUnit node) {
    return EMPTY_ARRAY;
  }

  /**
   * @param node the AST node
   * @return array of type constraints, may be empty
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.ConditionalExpression)
   */
  public ITypeConstraint[] create(ConditionalExpression node) {
    return EMPTY_ARRAY;
  }

  /**
   * @param node the AST node
   * @return array of type constraints, may be empty
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.ConstructorInvocation)
   */
  public ITypeConstraint[] create(ConstructorInvocation node) {
    return EMPTY_ARRAY;
  }

  /**
   * @param node the AST node
   * @return array of type constraints, may be empty
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.ContinueStatement)
   */
  public ITypeConstraint[] create(ContinueStatement node) {
    return EMPTY_ARRAY;
  }

  /**
   * @param node the AST node
   * @return array of type constraints, may be empty
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.DoStatement)
   */
  public ITypeConstraint[] create(DoStatement node) {
    return EMPTY_ARRAY;
  }

  /**
   * @param node the AST node
   * @return array of type constraints, may be empty
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.EmptyStatement)
   */
  public ITypeConstraint[] create(EmptyStatement node) {
    return EMPTY_ARRAY;
  }

  /**
   * @param node the AST node
   * @return array of type constraints, may be empty
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.ExpressionStatement)
   */
  public ITypeConstraint[] create(ExpressionStatement node) {
    return EMPTY_ARRAY;
  }

  /**
   * @param node the AST node
   * @return array of type constraints, may be empty
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.FieldAccess)
   */
  public ITypeConstraint[] create(FieldAccess node) {
    return EMPTY_ARRAY;
  }

  /**
   * @param node the AST node
   * @return array of type constraints, may be empty
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.FieldDeclaration)
   */
  public ITypeConstraint[] create(FieldDeclaration node) {
    return EMPTY_ARRAY;
  }

  /**
   * @param node the AST node
   * @return array of type constraints, may be empty
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.ForStatement)
   */
  public ITypeConstraint[] create(ForStatement node) {
    return EMPTY_ARRAY;
  }

  /**
   * @param node the AST node
   * @return array of type constraints, may be empty
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.IfStatement)
   */
  public ITypeConstraint[] create(IfStatement node) {
    return EMPTY_ARRAY;
  }

  /**
   * @param node the AST node
   * @return array of type constraints, may be empty
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.ImportDeclaration)
   */
  public ITypeConstraint[] create(ImportDeclaration node) {
    return EMPTY_ARRAY;
  }

  /**
   * @param node the AST node
   * @return array of type constraints, may be empty
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.InfixExpression)
   */
  public ITypeConstraint[] create(InfixExpression node) {
    return EMPTY_ARRAY;
  }

  /**
   * @param node the AST node
   * @return array of type constraints, may be empty
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.Initializer)
   */
  public ITypeConstraint[] create(Initializer node) {
    return EMPTY_ARRAY;
  }

  /**
   * @param node the AST node
   * @return array of type constraints, may be empty
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.InstanceofExpression)
   */
  public ITypeConstraint[] create(InstanceofExpression node) {
    return EMPTY_ARRAY;
  }

  /**
   * @param node the AST node
   * @return array of type constraints, may be empty
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.Javadoc)
   */
  public ITypeConstraint[] create(Javadoc node) {
    return EMPTY_ARRAY;
  }

  /**
   * @param node the AST node
   * @return array of type constraints, may be empty
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.LabeledStatement)
   */
  public ITypeConstraint[] create(LabeledStatement node) {
    return EMPTY_ARRAY;
  }

  /**
   * @param node the AST node
   * @return array of type constraints, may be empty
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.MethodDeclaration)
   */
  public ITypeConstraint[] create(MethodDeclaration node) {
    return EMPTY_ARRAY;
  }

  /**
   * @param node the AST node
   * @return array of type constraints, may be empty
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.MethodInvocation)
   */
  public ITypeConstraint[] create(MethodInvocation node) {
    return EMPTY_ARRAY;
  }

  /**
   * @param node the AST node
   * @return array of type constraints, may be empty
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.NullLiteral)
   */
  public ITypeConstraint[] create(NullLiteral node) {
    return EMPTY_ARRAY;
  }

  /**
   * @param node the AST node
   * @return array of type constraints, may be empty
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.NumberLiteral)
   */
  public ITypeConstraint[] create(NumberLiteral node) {
    return EMPTY_ARRAY;
  }

  /**
   * @param node the AST node
   * @return array of type constraints, may be empty
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.PackageDeclaration)
   */
  public ITypeConstraint[] create(PackageDeclaration node) {
    return EMPTY_ARRAY;
  }

  /**
   * @param node the AST node
   * @return array of type constraints, may be empty
   * @see
   *     org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.ParenthesizedExpression)
   */
  public ITypeConstraint[] create(ParenthesizedExpression node) {
    return EMPTY_ARRAY;
  }

  /**
   * @param node the AST node
   * @return array of type constraints, may be empty
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.PostfixExpression)
   */
  public ITypeConstraint[] create(PostfixExpression node) {
    return EMPTY_ARRAY;
  }

  /**
   * @param node the AST node
   * @return array of type constraints, may be empty
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.PrefixExpression)
   */
  public ITypeConstraint[] create(PrefixExpression node) {
    return EMPTY_ARRAY;
  }

  /**
   * @param node the AST node
   * @return array of type constraints, may be empty
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.PrimitiveType)
   */
  public ITypeConstraint[] create(PrimitiveType node) {
    return EMPTY_ARRAY;
  }

  /**
   * @param node the AST node
   * @return array of type constraints, may be empty
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.QualifiedName)
   */
  public ITypeConstraint[] create(QualifiedName node) {
    return EMPTY_ARRAY;
  }

  /**
   * @param node the AST node
   * @return array of type constraints, may be empty
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.ReturnStatement)
   */
  public ITypeConstraint[] create(ReturnStatement node) {
    return EMPTY_ARRAY;
  }

  /**
   * @param node the AST node
   * @return array of type constraints, may be empty
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.SimpleName)
   */
  public ITypeConstraint[] create(SimpleName node) {
    return EMPTY_ARRAY;
  }

  /**
   * @param node the AST node
   * @return array of type constraints, may be empty
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.SimpleType)
   */
  public ITypeConstraint[] create(SimpleType node) {
    return EMPTY_ARRAY;
  }

  /**
   * @param node the AST node
   * @return array of type constraints, may be empty
   * @see
   *     org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.SingleVariableDeclaration)
   */
  public ITypeConstraint[] create(SingleVariableDeclaration node) {
    return EMPTY_ARRAY;
  }

  /**
   * @param node the AST node
   * @return array of type constraints, may be empty
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.StringLiteral)
   */
  public ITypeConstraint[] create(StringLiteral node) {
    return EMPTY_ARRAY;
  }

  /**
   * @param node the AST node
   * @return array of type constraints, may be empty
   * @see
   *     org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.SuperConstructorInvocation)
   */
  public ITypeConstraint[] create(SuperConstructorInvocation node) {
    return EMPTY_ARRAY;
  }

  /**
   * @param node the AST node
   * @return array of type constraints, may be empty
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.SuperFieldAccess)
   */
  public ITypeConstraint[] create(SuperFieldAccess node) {
    return EMPTY_ARRAY;
  }

  /**
   * @param node the AST node
   * @return array of type constraints, may be empty
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.SuperMethodInvocation)
   */
  public ITypeConstraint[] create(SuperMethodInvocation node) {
    return EMPTY_ARRAY;
  }

  /**
   * @param node the AST node
   * @return array of type constraints, may be empty
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.SwitchCase)
   */
  public ITypeConstraint[] create(SwitchCase node) {
    return EMPTY_ARRAY;
  }

  /**
   * @param node the AST node
   * @return array of type constraints, may be empty
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.SwitchStatement)
   */
  public ITypeConstraint[] create(SwitchStatement node) {
    return EMPTY_ARRAY;
  }

  /**
   * @param node the AST node
   * @return array of type constraints, may be empty
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.SynchronizedStatement)
   */
  public ITypeConstraint[] create(SynchronizedStatement node) {
    return EMPTY_ARRAY;
  }

  /**
   * @param node the AST node
   * @return array of type constraints, may be empty
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.ThisExpression)
   */
  public ITypeConstraint[] create(ThisExpression node) {
    return EMPTY_ARRAY;
  }

  /**
   * @param node the AST node
   * @return array of type constraints, may be empty
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.ThrowStatement)
   */
  public ITypeConstraint[] create(ThrowStatement node) {
    return EMPTY_ARRAY;
  }

  /**
   * @param node the AST node
   * @return array of type constraints, may be empty
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.TryStatement)
   */
  public ITypeConstraint[] create(TryStatement node) {
    return EMPTY_ARRAY;
  }

  /**
   * @param node the AST node
   * @return array of type constraints, may be empty
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.TypeDeclaration)
   */
  public ITypeConstraint[] create(TypeDeclaration node) {
    return EMPTY_ARRAY;

    // TODO account for enums and annotations
  }

  /**
   * @param node the AST node
   * @return array of type constraints, may be empty
   * @see
   *     org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.TypeDeclarationStatement)
   */
  public ITypeConstraint[] create(TypeDeclarationStatement node) {
    return EMPTY_ARRAY;
  }

  /**
   * @param node the AST node
   * @return array of type constraints, may be empty
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.TypeLiteral)
   */
  public ITypeConstraint[] create(TypeLiteral node) {
    return EMPTY_ARRAY;
  }

  /**
   * @param node the AST node
   * @return array of type constraints, may be empty
   * @see
   *     org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.VariableDeclarationExpression)
   */
  public ITypeConstraint[] create(VariableDeclarationExpression node) {
    return EMPTY_ARRAY;
  }

  /**
   * @param node the AST node
   * @return array of type constraints, may be empty
   * @see
   *     org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.VariableDeclarationFragment)
   */
  public ITypeConstraint[] create(VariableDeclarationFragment node) {
    return EMPTY_ARRAY;
  }

  /**
   * @param node the AST node
   * @return array of type constraints, may be empty
   * @see
   *     org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.VariableDeclarationStatement)
   */
  public ITypeConstraint[] create(VariableDeclarationStatement node) {
    return EMPTY_ARRAY;
  }

  /**
   * @param node the AST node
   * @return array of type constraints, may be empty
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.WhileStatement)
   */
  public ITypeConstraint[] create(WhileStatement node) {
    return EMPTY_ARRAY;
  }
}
