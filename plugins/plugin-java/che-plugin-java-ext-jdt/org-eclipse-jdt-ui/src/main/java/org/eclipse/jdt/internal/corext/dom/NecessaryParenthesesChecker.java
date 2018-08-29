/**
 * ***************************************************************************** Copyright (c) 2011,
 * 2013 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.dom;

import java.util.Iterator;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jdt.internal.corext.refactoring.code.OperatorPrecedence;

/**
 * Helper class to check if an expression requires parentheses.
 *
 * @since 3.7
 */
public class NecessaryParenthesesChecker {

  /*
   * Get the expression wrapped by the parentheses
   * i.e. ((((expression)))) -> expression
   */
  private static Expression getExpression(ParenthesizedExpression node) {
    Expression expression = node.getExpression();
    while (expression instanceof ParenthesizedExpression) {
      expression = ((ParenthesizedExpression) expression).getExpression();
    }
    return expression;
  }

  private static boolean expressionTypeNeedsParentheses(Expression expression) {
    int type = expression.getNodeType();
    return type == ASTNode.INFIX_EXPRESSION
        || type == ASTNode.CONDITIONAL_EXPRESSION
        || type == ASTNode.PREFIX_EXPRESSION
        || type == ASTNode.POSTFIX_EXPRESSION
        || type == ASTNode.CAST_EXPRESSION
        || type == ASTNode.INSTANCEOF_EXPRESSION
        || type == ASTNode.ARRAY_CREATION
        || type == ASTNode.ASSIGNMENT;
  }

  private static boolean locationNeedsParentheses(StructuralPropertyDescriptor locationInParent) {
    if (locationInParent instanceof ChildListPropertyDescriptor
        && locationInParent != InfixExpression.EXTENDED_OPERANDS_PROPERTY) {
      // e.g. argument lists of MethodInvocation, ClassInstanceCreation, dimensions of ArrayCreation
      // ...
      return false;
    }
    if (locationInParent == VariableDeclarationFragment.INITIALIZER_PROPERTY
        || locationInParent == SingleVariableDeclaration.INITIALIZER_PROPERTY
        || locationInParent == ReturnStatement.EXPRESSION_PROPERTY
        || locationInParent == EnhancedForStatement.EXPRESSION_PROPERTY
        || locationInParent == ForStatement.EXPRESSION_PROPERTY
        || locationInParent == WhileStatement.EXPRESSION_PROPERTY
        || locationInParent == DoStatement.EXPRESSION_PROPERTY
        || locationInParent == AssertStatement.EXPRESSION_PROPERTY
        || locationInParent == AssertStatement.MESSAGE_PROPERTY
        || locationInParent == IfStatement.EXPRESSION_PROPERTY
        || locationInParent == SwitchStatement.EXPRESSION_PROPERTY
        || locationInParent == SwitchCase.EXPRESSION_PROPERTY
        || locationInParent == ArrayAccess.INDEX_PROPERTY
        || locationInParent == ThrowStatement.EXPRESSION_PROPERTY
        || locationInParent == SynchronizedStatement.EXPRESSION_PROPERTY
        || locationInParent == ParenthesizedExpression.EXPRESSION_PROPERTY) {
      return false;
    }
    return true;
  }

  /*
   * Do all operands in expression have same type
   */
  private static boolean isAllOperandsHaveSameType(
      InfixExpression expression, ITypeBinding leftOperandType, ITypeBinding rightOperandType) {
    ITypeBinding binding = leftOperandType;
    if (binding == null) return false;

    ITypeBinding current = rightOperandType;
    if (binding != current) return false;

    for (Iterator<Expression> iterator = expression.extendedOperands().iterator();
        iterator.hasNext(); ) {
      Expression operand = iterator.next();
      current = operand.resolveTypeBinding();
      if (binding != current) return false;
    }

    return true;
  }

  private static boolean isIntegerType(ITypeBinding binding) {
    if (binding == null) return false;

    if (!binding.isPrimitive()) return false;

    String name = binding.getName();
    if (isIntegerNumber(name)) return true;

    return false;
  }

  private static boolean isStringType(ITypeBinding binding) {
    if (binding == null) return false;

    return "java.lang.String".equals(binding.getQualifiedName()); // $NON-NLS-1$
  }

  /*
   * Is the given expression associative?
   *
   * This is true if and only if:<br>
   * <code>left operator (right) == (right) operator left == right operator left</code>
   */
  private static boolean isAssociative(
      InfixExpression.Operator operator,
      ITypeBinding infixExprType,
      boolean isAllOperandsHaveSameType) {
    if (operator == InfixExpression.Operator.PLUS)
      return isStringType(infixExprType)
          || isIntegerType(infixExprType) && isAllOperandsHaveSameType;

    if (operator == InfixExpression.Operator.TIMES)
      return isIntegerType(infixExprType) && isAllOperandsHaveSameType;

    if (operator == InfixExpression.Operator.CONDITIONAL_AND
        || operator == InfixExpression.Operator.CONDITIONAL_OR
        || operator == InfixExpression.Operator.AND
        || operator == InfixExpression.Operator.OR
        || operator == InfixExpression.Operator.XOR) return true;

    return false;
  }

  private static boolean isIntegerNumber(String name) {
    return "int".equals(name)
        || "long".equals(name)
        || "byte".equals(name)
        || "char".equals(name)
        || "short"
            .equals(name); // $NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
  }

  private static boolean needsParenthesesInInfixExpression(
      Expression expression,
      InfixExpression parentInfix,
      StructuralPropertyDescriptor locationInParent,
      ITypeBinding leftOperandType) {
    InfixExpression.Operator parentInfixOperator = parentInfix.getOperator();
    ITypeBinding rightOperandType;
    ITypeBinding parentInfixExprType;
    if (leftOperandType == null) { // parentInfix has bindings
      leftOperandType = parentInfix.getLeftOperand().resolveTypeBinding();
      rightOperandType = parentInfix.getRightOperand().resolveTypeBinding();
      parentInfixExprType = parentInfix.resolveTypeBinding();
    } else {
      rightOperandType = expression.resolveTypeBinding();
      parentInfixExprType =
          getInfixExpressionType(parentInfixOperator, leftOperandType, rightOperandType);
    }
    boolean isAllOperandsHaveSameType =
        isAllOperandsHaveSameType(parentInfix, leftOperandType, rightOperandType);

    if (locationInParent == InfixExpression.LEFT_OPERAND_PROPERTY) {
      // we have (expr op expr) op expr
      // infix expressions are evaluated from left to right -> parentheses not needed
      return false;
    } else if (isAssociative(parentInfixOperator, parentInfixExprType, isAllOperandsHaveSameType)) {
      // we have parent op (expr op expr) and op is associative
      // left op (right) == (right) op left == right op left
      if (expression instanceof InfixExpression) {
        InfixExpression infixExpression = (InfixExpression) expression;
        Operator operator = infixExpression.getOperator();

        if (isStringType(parentInfixExprType)) {
          if (parentInfixOperator == InfixExpression.Operator.PLUS
              && operator == InfixExpression.Operator.PLUS
              && isStringType(infixExpression.resolveTypeBinding())) {
            // 1 + ("" + 2) == 1 + "" + 2
            // 1 + (2 + "") != 1 + 2 + ""
            // "" + (2 + "") == "" + 2 + ""
            return !isStringType(infixExpression.getLeftOperand().resolveTypeBinding())
                && !isStringType(leftOperandType);
          }
          // "" + (1 + 2), "" + (1 - 2) etc
          return true;
        }

        if (parentInfixOperator != InfixExpression.Operator.TIMES) return false;

        if (operator == InfixExpression.Operator.TIMES)
          // x * (y * z) == x * y * z
          return false;

        if (operator == InfixExpression.Operator.REMAINDER
            || operator == InfixExpression.Operator.DIVIDE)
          // x * (y % z) != x * y % z , x * (y / z) == x * y / z rounding involved
          return true;

        return false;
      }
      return false;
    } else {
      return true;
    }
  }

  /**
   * Returns the type of infix expression based on its operands and operator.
   *
   * @param operator the operator of infix expression
   * @param leftOperandType the type of left operand of infix expression
   * @param rightOperandType the type of right operand of infix expression
   * @return the type of infix expression if the type of both the operands is same or if the type of
   *     either operand of a + operator is String, <code>null</code> otherwise.
   * @since 3.9
   */
  private static ITypeBinding getInfixExpressionType(
      InfixExpression.Operator operator,
      ITypeBinding leftOperandType,
      ITypeBinding rightOperandType) {
    if (leftOperandType == rightOperandType) {
      return leftOperandType;
    }
    if (operator == InfixExpression.Operator.PLUS) {
      if (isStringType(leftOperandType)) {
        return leftOperandType;
      } else if (isStringType(rightOperandType)) {
        return rightOperandType;
      }
    }
    // If the left and right operand types are different, we assume that parentheses are needed.
    // This is to avoid complications of numeric promotions and for readability of complicated code.
    return null;
  }

  /**
   * Can the parentheses be removed from the parenthesized expression ?
   *
   * <p><b>Note:</b> The parenthesized expression must not be an unparented node.
   *
   * @param expression the parenthesized expression
   * @return <code>true</code> if parentheses can be removed, <code>false</code> otherwise.
   */
  public static boolean canRemoveParentheses(Expression expression) {
    return canRemoveParentheses(
        expression, expression.getParent(), expression.getLocationInParent());
  }

  /**
   * Can the parentheses be removed from the parenthesized expression when inserted into <code>
   * parent</code> at <code>locationInParent</code> ?
   *
   * <p><b>Note:</b> The parenthesized expression can be an unparented node.
   *
   * @param expression the parenthesized expression
   * @param parent the parent node
   * @param locationInParent location of expression in the parent
   * @return <code>true</code> if parentheses can be removed, <code>false</code> otherwise.
   */
  public static boolean canRemoveParentheses(
      Expression expression, ASTNode parent, StructuralPropertyDescriptor locationInParent) {
    if (!(expression instanceof ParenthesizedExpression)) {
      return false;
    }
    return !needsParentheses(
        getExpression((ParenthesizedExpression) expression), parent, locationInParent);
  }

  /**
   * Does the <code>rightOperand</code> need parentheses when inserted into <code>infixExpression
   * </code> ?
   *
   * <p><b>Note:</b>
   *
   * <ul>
   *   <li>The <code>infixExpression</code> can be a new node (not from a resolved AST) with no
   *       bindings.
   *   <li>The <code>infixExpression</code> must not have additional operands.
   *   <li>The <code>rightOperand</code> node must have bindings.
   * </ul>
   *
   * @param rightOperand the right operand in <code>infixExpression</code>
   * @param infixExpression the parent infix expression
   * @param leftOperandType the type of the left operand in <code>infixExpression</code>
   * @return <code>true</code> if <code>rightOperand</code> needs parentheses, <code>false</code>
   *     otherwise.
   * @since 3.9
   */
  public static boolean needsParenthesesForRightOperand(
      Expression rightOperand, InfixExpression infixExpression, ITypeBinding leftOperandType) {
    return needsParentheses(
        rightOperand, infixExpression, InfixExpression.RIGHT_OPERAND_PROPERTY, leftOperandType);
  }

  /**
   * Does the <code>expression</code> need parentheses when inserted into <code>parent</code> at
   * <code>locationInParent</code> ?
   *
   * <p><b>Note:</b> The expression can be an unparented node.
   *
   * @param expression the expression
   * @param parent the parent node
   * @param locationInParent location of expression in the parent
   * @return <code>true</code> if <code>expression</code> needs parentheses, <code>false</code>
   *     otherwise.
   */
  public static boolean needsParentheses(
      Expression expression, ASTNode parent, StructuralPropertyDescriptor locationInParent) {
    return needsParentheses(expression, parent, locationInParent, null);
  }

  /**
   * Does the <code>expression</code> need parentheses when inserted into <code>parent</code> at
   * <code>locationInParent</code> ?
   *
   * @param expression the expression
   * @param parent the parent node
   * @param locationInParent location of expression in the parent
   * @param leftOperandType the type of the left operand in <code>parent</code> if <code>parent
   *     </code> is an infix expression with no bindings and <code>expression</code> is the right
   *     operand in it, <code>null</code> otherwise
   * @return <code>true</code> if <code>expression</code> needs parentheses, <code>false</code>
   *     otherwise.
   * @since 3.9
   */
  private static boolean needsParentheses(
      Expression expression,
      ASTNode parent,
      StructuralPropertyDescriptor locationInParent,
      ITypeBinding leftOperandType) {
    if (!expressionTypeNeedsParentheses(expression)) return false;

    if (!locationNeedsParentheses(locationInParent)) {
      return false;
    }

    if (parent instanceof Expression) {
      Expression parentExpression = (Expression) parent;

      if (expression instanceof PrefixExpression) { // see bug 405096
        return needsParenthesesForPrefixExpression(
            parentExpression, ((PrefixExpression) expression).getOperator());
      }

      int expressionPrecedence = OperatorPrecedence.getExpressionPrecedence(expression);
      int parentPrecedence = OperatorPrecedence.getExpressionPrecedence(parentExpression);

      if (expressionPrecedence > parentPrecedence)
        // (opEx) opParent and opEx binds more -> parentheses not needed
        return false;

      if (expressionPrecedence < parentPrecedence)
        // (opEx) opParent and opEx binds less -> parentheses needed
        return true;

      // (opEx) opParent binds equal

      if (parentExpression instanceof InfixExpression) {
        return needsParenthesesInInfixExpression(
            expression, (InfixExpression) parentExpression, locationInParent, leftOperandType);
      }

      if (parentExpression instanceof ConditionalExpression
          && locationInParent == ConditionalExpression.EXPRESSION_PROPERTY) {
        return true;
      }

      return false;
    }

    return true;
  }

  private static boolean needsParenthesesForPrefixExpression(
      Expression parentExpression, PrefixExpression.Operator expressionOperator) {
    if (parentExpression instanceof PrefixExpression) {
      PrefixExpression.Operator parentOperator =
          ((PrefixExpression) parentExpression).getOperator();
      if (parentOperator == PrefixExpression.Operator.PLUS
          && (expressionOperator == PrefixExpression.Operator.PLUS
              || expressionOperator == PrefixExpression.Operator.INCREMENT)) {
        return true;
      }
      if (parentOperator == PrefixExpression.Operator.MINUS
          && (expressionOperator == PrefixExpression.Operator.MINUS
              || expressionOperator == PrefixExpression.Operator.DECREMENT)) {
        return true;
      }
    } else if (parentExpression instanceof InfixExpression) {
      InfixExpression.Operator parentOperator = ((InfixExpression) parentExpression).getOperator();
      if (parentOperator == InfixExpression.Operator.PLUS
          && (expressionOperator == PrefixExpression.Operator.PLUS
              || expressionOperator == PrefixExpression.Operator.INCREMENT)) {
        return true;
      }
      if (parentOperator == InfixExpression.Operator.MINUS
          && (expressionOperator == PrefixExpression.Operator.MINUS
              || expressionOperator == PrefixExpression.Operator.DECREMENT)) {
        return true;
      }
    }
    return false;
  }
}
