/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.corext.refactoring.code;

import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;

public class OperatorPrecedence {

	private static final int ASSIGNMENT= 			0;
	private static final int CONDITIONAL=			1;
	private static final int CONDITIONAL_OR= 		2;
	private static final int CONDITIONAL_AND= 		3;
	private static final int BITWISE_INCLUSIVE_OR=	4;
	private static final int BITWISE_EXCLUSIVE_OR=	5;
	private static final int BITWISE_AND=			6;
	private static final int EQUALITY=				7;
	private static final int RELATIONAL= 			8;
	private static final int SHIFT=					9;
	private static final int ADDITIVE=				10;
	private static final int MULTIPLICATIVE=		11;
	private static final int TYPEGENERATION= 		12;
	private static final int PREFIX=				13;
	private static final int POSTFIX=				14;

	/**
	 * Returns the precedence of the expression. Expression
	 * with higher precedence are executed before expressions
	 * with lower precedence.
	 * i.e. in:
	 * <br><code> int a= ++3--;</code></br>
	 *
	 * the  precedence order is
	 * <ul>
	 * <li>3</li>
	 * <li>++</li>
	 * <li>--</li>
	 * <li>=</li>
	 * </ul>
	 * 1. 3 -(++)-> 4<br>
	 * 2. 4 -(--)-> 3<br>
	 * 3. 3 -(=)-> a<br>
	 *
	 * @param expression the expression to determine the precedence for
	 * @return the precedence the higher to stronger the binding to its operand(s)
	 */
	public static int getExpressionPrecedence(Expression expression) {
		if (expression instanceof InfixExpression) {
			return getOperatorPrecedence(((InfixExpression)expression).getOperator());
		} else if (expression instanceof Assignment) {
			return ASSIGNMENT;
		} else if (expression instanceof ConditionalExpression) {
			return CONDITIONAL;
		} else if (expression instanceof InstanceofExpression) {
			return RELATIONAL;
		} else if (expression instanceof CastExpression) {
			return TYPEGENERATION;
		} else if (expression instanceof ClassInstanceCreation) {
			return POSTFIX;
		} else if (expression instanceof PrefixExpression) {
			return PREFIX;
		} else if (expression instanceof FieldAccess) {
			return POSTFIX;
		} else if (expression instanceof MethodInvocation) {
			return POSTFIX;
		} else if (expression instanceof ArrayAccess) {
			return POSTFIX;
		} else if (expression instanceof PostfixExpression) {
			return POSTFIX;
		}
		return Integer.MAX_VALUE;
	}

	/**
	 * Returns the precedence of an infix operator. Operators
	 * with higher precedence are executed before expressions
	 * with lower precedence.
	 * <br>
	 * i.e. in: <br>
	 * <code>3 + 4 - 5 * 6;</code><br>
	 * the  precedence order is
	 * <ul>
	 * <li>*</li>
	 * <li>+</li>
	 * <li>-</li>
	 * </ul>
	 * 1. 5,6 -(*)-> 30<br>
	 * 2. 3,4 -(+)-> 7<br>
	 * 3. 7,30 -(-)-> -23<br>
	 *
	 * @param operator the expression to determine the precedence for
	 * @return the precedence the higher to stronger the binding to its operands
	 */
	public static int getOperatorPrecedence(Operator operator) {
		if (operator == Operator.CONDITIONAL_OR) {
			return CONDITIONAL_OR;
		} else if (operator == Operator.CONDITIONAL_AND) {
			return CONDITIONAL_AND;
		} else if (operator == Operator.OR) {
			return BITWISE_INCLUSIVE_OR;
		} else if (operator == Operator.XOR) {
			return BITWISE_EXCLUSIVE_OR;
		} else if (operator == Operator.AND) {
			return BITWISE_AND;
		} else if (operator == Operator.EQUALS || operator == Operator.NOT_EQUALS) {
			return EQUALITY;
		} else if (operator == Operator.LESS || operator == Operator.LESS_EQUALS || operator == Operator.GREATER || operator == Operator.GREATER_EQUALS) {
			return RELATIONAL;
		} else if (operator == Operator.LEFT_SHIFT || operator == Operator.RIGHT_SHIFT_SIGNED || operator == Operator.RIGHT_SHIFT_UNSIGNED) {
			return SHIFT;
		} else if (operator == Operator.PLUS || operator == Operator.MINUS) {
			return ADDITIVE;
		} else if (operator == Operator.REMAINDER || operator == Operator.DIVIDE || operator == Operator.TIMES) {
			return MULTIPLICATIVE;
		}
		return Integer.MAX_VALUE;
	}
}
