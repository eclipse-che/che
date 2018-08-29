/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2013 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation jens.lukowski@gmx.de -
 * contributed code to convert prefix and postfix expressions into a combination of setter and
 * getter calls. Nikolay Metchev <nikolaymetchev@gmail.com> - [encapsulate field] Encapsulating
 * parenthesized field assignment yields compilation error - https://bugs.eclipse.org/177095
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.refactoring.sef;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.internal.corext.SourceRangeFactory;
import org.eclipse.jdt.internal.corext.dom.ASTNodes;
import org.eclipse.jdt.internal.corext.dom.Bindings;
import org.eclipse.jdt.internal.corext.dom.NecessaryParenthesesChecker;
import org.eclipse.jdt.internal.corext.refactoring.RefactoringCoreMessages;
import org.eclipse.jdt.internal.corext.refactoring.base.JavaStatusContext;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.text.edits.TextEditGroup;

/**
 * Analyzer to find all references to the field and to determine how to convert them into setter or
 * getter calls.
 */
class AccessAnalyzer extends ASTVisitor {

  private ICompilationUnit fCUnit;
  private IVariableBinding fFieldBinding;
  private ITypeBinding fDeclaringClassBinding;
  private String fGetter;
  private String fSetter;
  private ASTRewrite fRewriter;
  private ImportRewrite fImportRewriter;
  private List<TextEditGroup> fGroupDescriptions;
  private RefactoringStatus fStatus;
  private boolean fSetterMustReturnValue;
  private boolean fEncapsulateDeclaringClass;
  private boolean fIsFieldFinal;

  private boolean fRemoveStaticImport;
  private boolean fReferencingGetter;
  private boolean fReferencingSetter;

  private static final String READ_ACCESS =
      RefactoringCoreMessages.SelfEncapsulateField_AccessAnalyzer_encapsulate_read_access;
  private static final String WRITE_ACCESS =
      RefactoringCoreMessages.SelfEncapsulateField_AccessAnalyzer_encapsulate_write_access;
  private static final String PREFIX_ACCESS =
      RefactoringCoreMessages.SelfEncapsulateField_AccessAnalyzer_encapsulate_prefix_access;
  private static final String POSTFIX_ACCESS =
      RefactoringCoreMessages.SelfEncapsulateField_AccessAnalyzer_encapsulate_postfix_access;

  public AccessAnalyzer(
      SelfEncapsulateFieldRefactoring refactoring,
      ICompilationUnit unit,
      IVariableBinding field,
      ITypeBinding declaringClass,
      ASTRewrite rewriter,
      ImportRewrite importRewrite) {
    Assert.isNotNull(refactoring);
    Assert.isNotNull(unit);
    Assert.isNotNull(field);
    Assert.isNotNull(declaringClass);
    Assert.isNotNull(rewriter);
    Assert.isNotNull(importRewrite);
    fCUnit = unit;
    fFieldBinding = field.getVariableDeclaration();
    fDeclaringClassBinding = declaringClass;
    fRewriter = rewriter;
    fImportRewriter = importRewrite;
    fGroupDescriptions = new ArrayList<TextEditGroup>();
    fGetter = refactoring.getGetterName();
    fSetter = refactoring.getSetterName();
    fEncapsulateDeclaringClass = refactoring.getEncapsulateDeclaringClass();
    try {
      fIsFieldFinal = Flags.isFinal(refactoring.getField().getFlags());
    } catch (JavaModelException e) {
      // assume non final field
    }
    fStatus = new RefactoringStatus();
  }

  public boolean getSetterMustReturnValue() {
    return fSetterMustReturnValue;
  }

  public RefactoringStatus getStatus() {
    return fStatus;
  }

  public List<TextEditGroup> getGroupDescriptions() {
    return fGroupDescriptions;
  }

  @Override
  public boolean visit(Assignment node) {
    Expression leftHandSide = node.getLeftHandSide();
    if (!considerBinding(resolveBinding(leftHandSide), leftHandSide)) return true;

    checkParent(node);
    Expression rightHandSide = node.getRightHandSide();
    if (!fIsFieldFinal) {
      // Write access.
      AST ast = node.getAST();
      MethodInvocation invocation = ast.newMethodInvocation();
      invocation.setName(ast.newSimpleName(fSetter));
      fReferencingSetter = true;
      Expression receiver = getReceiver(leftHandSide);
      if (receiver != null)
        invocation.setExpression((Expression) fRewriter.createCopyTarget(receiver));
      List<Expression> arguments = invocation.arguments();
      if (node.getOperator() == Assignment.Operator.ASSIGN) {
        arguments.add((Expression) fRewriter.createCopyTarget(rightHandSide));
      } else {
        // This is the compound assignment case: field+= 10;
        InfixExpression exp = ast.newInfixExpression();
        exp.setOperator(ASTNodes.convertToInfixOperator(node.getOperator()));
        MethodInvocation getter = ast.newMethodInvocation();
        getter.setName(ast.newSimpleName(fGetter));
        fReferencingGetter = true;
        if (receiver != null)
          getter.setExpression((Expression) fRewriter.createCopyTarget(receiver));
        exp.setLeftOperand(getter);
        Expression rhs = (Expression) fRewriter.createCopyTarget(rightHandSide);
        if (NecessaryParenthesesChecker.needsParenthesesForRightOperand(
            rightHandSide, exp, leftHandSide.resolveTypeBinding())) {
          ParenthesizedExpression p = ast.newParenthesizedExpression();
          p.setExpression(rhs);
          rhs = p;
        }
        exp.setRightOperand(rhs);
        arguments.add(exp);
      }
      fRewriter.replace(node, invocation, createGroupDescription(WRITE_ACCESS));
    }
    rightHandSide.accept(this);
    return false;
  }

  @Override
  public boolean visit(SimpleName node) {
    if (!node.isDeclaration() && considerBinding(node.resolveBinding(), node)) {
      fReferencingGetter = true;
      fRewriter.replace(
          node,
          fRewriter.createStringPlaceholder(
              fGetter + "()", ASTNode.METHOD_INVOCATION), // $NON-NLS-1$
          createGroupDescription(READ_ACCESS));
    }
    return true;
  }

  @Override
  public boolean visit(ImportDeclaration node) {
    if (considerBinding(node.resolveBinding(), node)) {
      fRemoveStaticImport = true;
    }
    return false;
  }

  @Override
  public boolean visit(PrefixExpression node) {
    Expression operand = node.getOperand();
    if (!considerBinding(resolveBinding(operand), operand)) return true;

    PrefixExpression.Operator operator = node.getOperator();
    if (operator != PrefixExpression.Operator.INCREMENT
        && operator != PrefixExpression.Operator.DECREMENT) return true;

    checkParent(node);

    fRewriter.replace(
        node,
        createInvocation(node.getAST(), node.getOperand(), node.getOperator().toString()),
        createGroupDescription(PREFIX_ACCESS));
    return false;
  }

  @Override
  public boolean visit(PostfixExpression node) {
    Expression operand = node.getOperand();
    if (!considerBinding(resolveBinding(operand), operand)) return true;

    ASTNode parent = node.getParent();
    if (!(parent instanceof ExpressionStatement)) {
      fStatus.addError(
          RefactoringCoreMessages
              .SelfEncapsulateField_AccessAnalyzer_cannot_convert_postfix_expression,
          JavaStatusContext.create(fCUnit, SourceRangeFactory.create(node)));
      return false;
    }
    fRewriter.replace(
        node,
        createInvocation(node.getAST(), node.getOperand(), node.getOperator().toString()),
        createGroupDescription(POSTFIX_ACCESS));
    return false;
  }

  @Override
  public boolean visit(MethodDeclaration node) {
    String name = node.getName().getIdentifier();
    if (name.equals(fGetter) || name.equals(fSetter)) return false;
    return true;
  }

  @Override
  public void endVisit(CompilationUnit node) {
    // If we don't had a static import to the field we don't
    // have to add any, even if we generated a setter or
    // getter access.
    if (!fRemoveStaticImport) return;

    ITypeBinding type = fFieldBinding.getDeclaringClass();
    String fieldName = fFieldBinding.getName();
    String typeName = type.getQualifiedName();
    if (fRemoveStaticImport) {
      fImportRewriter.removeStaticImport(typeName + "." + fieldName); // $NON-NLS-1$
    }
    if (fReferencingGetter) {
      fImportRewriter.addStaticImport(typeName, fGetter, false);
    }
    if (fReferencingSetter) {
      fImportRewriter.addStaticImport(typeName, fSetter, false);
    }
  }

  private boolean considerBinding(IBinding binding, ASTNode node) {
    if (!(binding instanceof IVariableBinding)) return false;
    boolean result =
        Bindings.equals(fFieldBinding, ((IVariableBinding) binding).getVariableDeclaration());
    if (!result || fEncapsulateDeclaringClass) return result;

    if (binding instanceof IVariableBinding) {
      AbstractTypeDeclaration type =
          (AbstractTypeDeclaration) ASTNodes.getParent(node, AbstractTypeDeclaration.class);
      if (type != null) {
        ITypeBinding declaringType = type.resolveBinding();
        return !Bindings.equals(fDeclaringClassBinding, declaringType);
      }
    }
    return true;
  }

  private void checkParent(ASTNode node) {
    ASTNode parent = node.getParent();
    if (!(parent instanceof ExpressionStatement)) fSetterMustReturnValue = true;
  }

  private IBinding resolveBinding(Expression expression) {
    if (expression instanceof SimpleName) return ((SimpleName) expression).resolveBinding();
    else if (expression instanceof QualifiedName)
      return ((QualifiedName) expression).resolveBinding();
    else if (expression instanceof FieldAccess)
      return ((FieldAccess) expression).getName().resolveBinding();
    else if (expression instanceof ParenthesizedExpression)
      return resolveBinding(((ParenthesizedExpression) expression).getExpression());
    return null;
  }

  private Expression getReceiver(Expression expression) {
    int type = expression.getNodeType();
    switch (type) {
      case ASTNode.SIMPLE_NAME:
        return null;
      case ASTNode.QUALIFIED_NAME:
        return ((QualifiedName) expression).getQualifier();
      case ASTNode.FIELD_ACCESS:
        return ((FieldAccess) expression).getExpression();
      case ASTNode.PARENTHESIZED_EXPRESSION:
        return getReceiver(((ParenthesizedExpression) expression).getExpression());
    }
    return null;
  }

  private MethodInvocation createInvocation(AST ast, Expression operand, String operator) {
    Expression receiver = getReceiver(operand);
    MethodInvocation invocation = ast.newMethodInvocation();
    invocation.setName(ast.newSimpleName(fSetter));
    if (receiver != null)
      invocation.setExpression((Expression) fRewriter.createCopyTarget(receiver));
    InfixExpression argument = ast.newInfixExpression();
    invocation.arguments().add(argument);
    if ("++".equals(operator)) { // $NON-NLS-1$
      argument.setOperator(InfixExpression.Operator.PLUS);
    } else if ("--".equals(operator)) { // $NON-NLS-1$
      argument.setOperator(InfixExpression.Operator.MINUS);
    } else {
      Assert.isTrue(false, "Should not happen"); // $NON-NLS-1$
    }
    MethodInvocation getter = ast.newMethodInvocation();
    getter.setName(ast.newSimpleName(fGetter));
    if (receiver != null) getter.setExpression((Expression) fRewriter.createCopyTarget(receiver));
    argument.setLeftOperand(getter);
    argument.setRightOperand(ast.newNumberLiteral("1")); // $NON-NLS-1$

    fReferencingGetter = true;
    fReferencingSetter = true;

    return invocation;
  }

  private TextEditGroup createGroupDescription(String name) {
    TextEditGroup result = new TextEditGroup(name);
    fGroupDescriptions.add(result);
    return result;
  }
}
