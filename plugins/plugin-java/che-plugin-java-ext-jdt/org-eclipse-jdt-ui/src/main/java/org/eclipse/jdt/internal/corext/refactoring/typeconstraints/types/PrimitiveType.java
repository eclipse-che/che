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
package org.eclipse.jdt.internal.corext.refactoring.typeconstraints.types;

import org.eclipse.jdt.core.IJavaProject;


public final class PrimitiveType extends TType {

	/** Type code for the primitive type "int". */
	public static final int INT = 0;
	/** Type code for the primitive type "char". */
	public static final int CHAR = 1;
	/** Type code for the primitive type "boolean". */
	public static final int BOOLEAN = 2;
	/** Type code for the primitive type "short". */
	public static final int SHORT = 3;
	/** Type code for the primitive type "long". */
	public static final int LONG = 4;
	/** Type code for the primitive type "float". */
	public static final int FLOAT = 5;
	/** Type code for the primitive type "double". */
	public static final int DOUBLE = 6;
	/** Type code for the primitive type "byte". */
	public static final int BYTE = 7;

	static final String[] NAMES= {
		"int",  //$NON-NLS-1$
		"char",  //$NON-NLS-1$
		"boolean",  //$NON-NLS-1$
		"short",  //$NON-NLS-1$
		"long",  //$NON-NLS-1$
		"float",  //$NON-NLS-1$
		"double",  //$NON-NLS-1$
		"byte"};  //$NON-NLS-1$

	private int fId;

	protected PrimitiveType(TypeEnvironment environment, int id, String signature) {
		super(environment, signature);
		fId= id;
	}

	public int getId() {
		return fId;
	}

	@Override
	public int getKind() {
		return PRIMITIVE_TYPE;
	}

	@Override
	protected boolean doEquals(TType type) {
		return fId == ((PrimitiveType)type).fId;
	}

	@Override
	public int hashCode() {
		return fId;
	}
	
	@Override
	protected boolean doCanAssignTo(TType lhs) {
		if (lhs.getKind() != PRIMITIVE_TYPE) {
			if (lhs.getKind() == STANDARD_TYPE) {
				IJavaProject javaProject= ((StandardType)lhs).getJavaElementType().getJavaProject();
				return getEnvironment().createBoxed(this, javaProject).canAssignTo(lhs);
			}
			return false;
		}

		switch (((PrimitiveType)lhs).fId) {
			case BOOLEAN :
			case BYTE :
			case CHAR :
				return false;
			case DOUBLE :
				switch (fId) {
					case BYTE :
					case CHAR :
					case SHORT :
					case INT :
					case LONG :
					case FLOAT :
						return true;
					default :
						return false;
				}
			case FLOAT :
				switch (fId) {
					case BYTE :
					case CHAR :
					case SHORT :
					case INT :
					case LONG :
						return true;
					default :
						return false;
				}
			case LONG :
				switch (fId) {
					case BYTE :
					case CHAR :
					case SHORT :
					case INT :
						return true;
					default :
						return false;
				}
			case INT :
				switch (fId) {
					case BYTE :
					case CHAR :
					case SHORT :
						return true;
					default :
						return false;
				}
			case SHORT :
				return (fId == BYTE);
		}
		return false;
	}

	@Override
	public String getName() {
		return NAMES[fId];
	}

	@Override
	protected String getPlainPrettySignature() {
		return NAMES[fId];
	}
}
