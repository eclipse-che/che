/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2011 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.refactoring.typeconstraints;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.core.dom.*;

public final class ConstraintCollector extends ASTVisitor {

  private final ConstraintCreator fCreator;
  private final Set<ITypeConstraint> fConstraints;

  public ConstraintCollector() {
    this(new FullConstraintCreator());
  }

  public ConstraintCollector(ConstraintCreator creator) {
    Assert.isNotNull(creator);
    fCreator = creator;
    fConstraints = new LinkedHashSet<ITypeConstraint>();
  }

  private void add(ITypeConstraint[] constraints) {
    fConstraints.addAll(Arrays.asList(constraints));
  }

  public void clear() {
    fConstraints.clear();
  }

  public ITypeConstraint[] getConstraints() {
    return fConstraints.toArray(new ITypeConstraint[fConstraints.size()]);
  }

  // ------------------------- visit methods -------------------------//

  /* (non-Javadoc)
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.AnonymousClassDeclaration)
   */
  @Override
  public boolean visit(AnonymousClassDeclaration node) {
    add(fCreator.create(node));
    return true;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.ArrayAccess)
   */
  @Override
  public boolean visit(ArrayAccess node) {
    add(fCreator.create(node));
    return true;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.ArrayCreation)
   */
  @Override
  public boolean visit(ArrayCreation node) {
    add(fCreator.create(node));
    return true;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.ArrayInitializer)
   */
  @Override
  public boolean visit(ArrayInitializer node) {
    add(fCreator.create(node));
    return true;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.ArrayType)
   */
  @Override
  public boolean visit(ArrayType node) {
    add(fCreator.create(node));
    return true;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.AssertStatement)
   */
  @Override
  public boolean visit(AssertStatement node) {
    add(fCreator.create(node));
    return true;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.Assignment)
   */
  @Override
  public boolean visit(Assignment node) {
    add(fCreator.create(node));
    return true;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.Block)
   */
  @Override
  public boolean visit(Block node) {
    add(fCreator.create(node));
    return true;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.BooleanLiteral)
   */
  @Override
  public boolean visit(BooleanLiteral node) {
    add(fCreator.create(node));
    return true;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.BreakStatement)
   */
  @Override
  public boolean visit(BreakStatement node) {
    add(fCreator.create(node));
    return true;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.CastExpression)
   */
  @Override
  public boolean visit(CastExpression node) {
    add(fCreator.create(node));
    return true;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.CatchClause)
   */
  @Override
  public boolean visit(CatchClause node) {
    add(fCreator.create(node));
    return true;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.CharacterLiteral)
   */
  @Override
  public boolean visit(CharacterLiteral node) {
    add(fCreator.create(node));
    return true;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.ClassInstanceCreation)
   */
  @Override
  public boolean visit(ClassInstanceCreation node) {
    add(fCreator.create(node));
    return true;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.CompilationUnit)
   */
  @Override
  public boolean visit(CompilationUnit node) {
    add(fCreator.create(node));
    return true;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.ConditionalExpression)
   */
  @Override
  public boolean visit(ConditionalExpression node) {
    add(fCreator.create(node));
    return true;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.ConstructorInvocation)
   */
  @Override
  public boolean visit(ConstructorInvocation node) {
    add(fCreator.create(node));
    return true;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.ContinueStatement)
   */
  @Override
  public boolean visit(ContinueStatement node) {
    add(fCreator.create(node));
    return true;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.DoStatement)
   */
  @Override
  public boolean visit(DoStatement node) {
    add(fCreator.create(node));
    return true;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.EmptyStatement)
   */
  @Override
  public boolean visit(EmptyStatement node) {
    add(fCreator.create(node));
    return true;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.ExpressionStatement)
   */
  @Override
  public boolean visit(ExpressionStatement node) {
    add(fCreator.create(node));
    return true;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.FieldAccess)
   */
  @Override
  public boolean visit(FieldAccess node) {
    add(fCreator.create(node));
    return true;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.FieldDeclaration)
   */
  @Override
  public boolean visit(FieldDeclaration node) {
    add(fCreator.create(node));
    return true;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.ForStatement)
   */
  @Override
  public boolean visit(ForStatement node) {
    add(fCreator.create(node));
    return true;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.IfStatement)
   */
  @Override
  public boolean visit(IfStatement node) {
    add(fCreator.create(node));
    return true;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.ImportDeclaration)
   */
  @Override
  public boolean visit(ImportDeclaration node) {
    add(fCreator.create(node));
    return true;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.InfixExpression)
   */
  @Override
  public boolean visit(InfixExpression node) {
    add(fCreator.create(node));
    return true;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.Initializer)
   */
  @Override
  public boolean visit(Initializer node) {
    add(fCreator.create(node));
    return true;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.InstanceofExpression)
   */
  @Override
  public boolean visit(InstanceofExpression node) {
    add(fCreator.create(node));
    return true;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.Javadoc)
   */
  @Override
  public boolean visit(Javadoc node) {
    add(fCreator.create(node));
    return true;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.LabeledStatement)
   */
  @Override
  public boolean visit(LabeledStatement node) {
    add(fCreator.create(node));
    return true;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.MarkerAnnotation)
   */
  @Override
  public boolean visit(MarkerAnnotation node) {
    return false;
  }
  /* (non-Javadoc)
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.MethodDeclaration)
   */
  @Override
  public boolean visit(MethodDeclaration node) {
    add(fCreator.create(node));
    return true;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.MethodInvocation)
   */
  @Override
  public boolean visit(MethodInvocation node) {
    add(fCreator.create(node));
    return true;
  }
  /* (non-Javadoc)
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.NormalAnnotation)
   */
  @Override
  public boolean visit(NormalAnnotation node) {
    return false;
  }
  /* (non-Javadoc)
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.NullLiteral)
   */
  @Override
  public boolean visit(NullLiteral node) {
    add(fCreator.create(node));
    return true;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.NumberLiteral)
   */
  @Override
  public boolean visit(NumberLiteral node) {
    add(fCreator.create(node));
    return true;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.PackageDeclaration)
   */
  @Override
  public boolean visit(PackageDeclaration node) {
    add(fCreator.create(node));
    return true;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.ParenthesizedExpression)
   */
  @Override
  public boolean visit(ParenthesizedExpression node) {
    add(fCreator.create(node));
    return true;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.PostfixExpression)
   */
  @Override
  public boolean visit(PostfixExpression node) {
    add(fCreator.create(node));
    return true;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.PrefixExpression)
   */
  @Override
  public boolean visit(PrefixExpression node) {
    add(fCreator.create(node));
    return true;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.PrimitiveType)
   */
  @Override
  public boolean visit(PrimitiveType node) {
    add(fCreator.create(node));
    return true;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.QualifiedName)
   */
  @Override
  public boolean visit(QualifiedName node) {
    add(fCreator.create(node));
    return true;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.ReturnStatement)
   */
  @Override
  public boolean visit(ReturnStatement node) {
    add(fCreator.create(node));
    return true;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.SimpleName)
   */
  @Override
  public boolean visit(SimpleName node) {
    add(fCreator.create(node));
    return true;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.SimpleType)
   */
  @Override
  public boolean visit(SimpleType node) {
    add(fCreator.create(node));
    return true;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.SingleMemberAnnotation)
   */
  @Override
  public boolean visit(SingleMemberAnnotation node) {
    return false;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.SingleVariableDeclaration)
   */
  @Override
  public boolean visit(SingleVariableDeclaration node) {
    add(fCreator.create(node));
    return true;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.StringLiteral)
   */
  @Override
  public boolean visit(StringLiteral node) {
    add(fCreator.create(node));
    return true;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.SuperConstructorInvocation)
   */
  @Override
  public boolean visit(SuperConstructorInvocation node) {
    add(fCreator.create(node));
    return true;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.SuperFieldAccess)
   */
  @Override
  public boolean visit(SuperFieldAccess node) {
    add(fCreator.create(node));
    return true;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.SuperMethodInvocation)
   */
  @Override
  public boolean visit(SuperMethodInvocation node) {
    add(fCreator.create(node));
    return true;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.SwitchCase)
   */
  @Override
  public boolean visit(SwitchCase node) {
    add(fCreator.create(node));
    return true;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.SwitchStatement)
   */
  @Override
  public boolean visit(SwitchStatement node) {
    add(fCreator.create(node));
    return true;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.SynchronizedStatement)
   */
  @Override
  public boolean visit(SynchronizedStatement node) {
    add(fCreator.create(node));
    return true;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.ThisExpression)
   */
  @Override
  public boolean visit(ThisExpression node) {
    add(fCreator.create(node));
    return true;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.ThrowStatement)
   */
  @Override
  public boolean visit(ThrowStatement node) {
    add(fCreator.create(node));
    return true;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.TryStatement)
   */
  @Override
  public boolean visit(TryStatement node) {
    add(fCreator.create(node));
    return true;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.TypeDeclaration)
   */
  @Override
  public boolean visit(TypeDeclaration node) {
    add(fCreator.create(node));
    return true;

    // TODO account for enums and annotations
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.TypeDeclarationStatement)
   */
  @Override
  public boolean visit(TypeDeclarationStatement node) {
    add(fCreator.create(node));
    return true;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.TypeLiteral)
   */
  @Override
  public boolean visit(TypeLiteral node) {
    add(fCreator.create(node));
    return true;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.VariableDeclarationExpression)
   */
  @Override
  public boolean visit(VariableDeclarationExpression node) {
    add(fCreator.create(node));
    return true;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.VariableDeclarationFragment)
   */
  @Override
  public boolean visit(VariableDeclarationFragment node) {
    add(fCreator.create(node));
    return true;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.VariableDeclarationStatement)
   */
  @Override
  public boolean visit(VariableDeclarationStatement node) {
    add(fCreator.create(node));
    return true;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.WhileStatement)
   */
  @Override
  public boolean visit(WhileStatement node) {
    add(fCreator.create(node));
    return true;
  }
}
