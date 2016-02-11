/*******************************************************************************
 * Copyright (c) 2007, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.corext.refactoring.structure;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.internal.corext.refactoring.ParameterInfo;

import java.util.List;

public interface IDefaultValueAdvisor {

	/**
	 * Creates a default expression for an added parameter for a given method invocation.
	 *
	 * @param invocationArguments arguments of the method invocation
	 * @param addedInfo the added ParamterInfo object
	 * @param parameterInfos all ParameterInfo objects, including the added ParameterInfo
	 * @param enclosingMethod the Method that encloses the invocation. Can be null if there is no enclosing method
	 * @param isRecursive true if called from a recursive invocation
	 * @param cuRewrite the CompilationUnitRewrite to use for rewrite, imports etc..
	 * @return a new Expression to be used as argument for the new parameter
	 */
	Expression createDefaultExpression(List<Expression> invocationArguments, ParameterInfo addedInfo, List<ParameterInfo> parameterInfos,
									   MethodDeclaration enclosingMethod, boolean isRecursive, CompilationUnitRewrite cuRewrite);

	/**
	 * Create a type for the added parameter.
	 *
	 * @param newTypeName the fully qualified name of the type
	 * @param startPosition the position where the type is defined in a compilation unit
	 * @param cuRewrite the CompilationUnitRewrite to use for rewrite, imports etc..
	 * @return the new type to be used in default expressions
	 */
	Type createType(String newTypeName, int startPosition, CompilationUnitRewrite cuRewrite);

}
