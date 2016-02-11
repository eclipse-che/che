/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.corext.refactoring.base;

public class RefactoringStatusCodes {

	private RefactoringStatusCodes() {
	}

	public static final int OVERRIDES_ANOTHER_METHOD= 						1;
	public static final int METHOD_DECLARED_IN_INTERFACE= 					2;

	public static final int EXPRESSION_NOT_RVALUE= 								64;
	public static final int EXPRESSION_NOT_RVALUE_VOID= 						65;
	public static final int EXTRANEOUS_TEXT= 											66;

	public static final int NOT_STATIC_FINAL_SELECTED= 							128;
	public static final int SYNTAX_ERRORS= 												129;
	public static final int DECLARED_IN_CLASSFILE= 									130;
	public static final int CANNOT_INLINE_BLANK_FINAL= 							131;
	public static final int LOCAL_AND_ANONYMOUS_NOT_SUPPORTED= 	132;
	public static final int REFERENCE_IN_CLASSFILE= 								133;

	public static final int NATIVE_METHOD= 192;
	public static final int MAIN_METHOD= 193;

	// inline method error codes
	public static final int INLINE_METHOD_FIELD_INITIALIZER= 						256;
	public static final int INLINE_METHOD_LOCAL_INITIALIZER= 						257;
	public static final int INLINE_METHOD_NULL_BINDING= 						   		258;
	public static final int INLINE_METHOD_ONLY_SIMPLE_FUNCTIONS=				259;
	public static final int INLINE_METHOD_EXECUTION_FLOW= 							260;
	public static final int INLINE_METHOD_INITIALIZER_IN_FRAGEMENT= 			261;
}
