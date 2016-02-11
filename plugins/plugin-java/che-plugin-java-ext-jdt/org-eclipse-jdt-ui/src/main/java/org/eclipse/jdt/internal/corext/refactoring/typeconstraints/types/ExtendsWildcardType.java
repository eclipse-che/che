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

public final class ExtendsWildcardType extends WildcardType {

	protected ExtendsWildcardType(TypeEnvironment environment) {
		super(environment);
	}

	@Override
	public int getKind() {
		return EXTENDS_WILDCARD_TYPE;
	}

	@Override
	public TType getErasure() {
		return fBound.getErasure();
	}

	@Override
	public TType[] getSubTypes() {
		return new TType[] { fBound };
	}

	@Override
	protected boolean doCanAssignTo(TType lhs) {
		switch (lhs.getKind()) {
			case ARRAY_TYPE:
			case STANDARD_TYPE:
			case PARAMETERIZED_TYPE:
			case RAW_TYPE:
				return getBound().canAssignTo(lhs);

			case UNBOUND_WILDCARD_TYPE:
				return true;
			case SUPER_WILDCARD_TYPE:
			case EXTENDS_WILDCARD_TYPE:
				return ((WildcardType)lhs).checkAssignmentBound(getBound());

			case TYPE_VARIABLE:
				return ((TypeVariable)lhs).checkAssignmentBound(getBound());
			case CAPTURE_TYPE:
				return ((CaptureType)lhs).checkLowerBound(this);

			default:
				return false;
		}
	}

	@Override
	protected boolean checkTypeArgument(TType rhs) {
		switch(rhs.getKind()) {
			case ARRAY_TYPE:
			case STANDARD_TYPE:
			case PARAMETERIZED_TYPE:
				return rhs.canAssignTo(getBound());
			case RAW_TYPE:
				// unchecked conversion not allowed here; fix for https://bugs.eclipse.org/bugs/show_bug.cgi?id=127583
				return ! getBound().isParameterizedType() && rhs.canAssignTo(getBound());

			case UNBOUND_WILDCARD_TYPE:
				return getBound().isJavaLangObject();
			case EXTENDS_WILDCARD_TYPE:
				return ((ExtendsWildcardType)rhs).getBound().canAssignTo(getBound());
			case SUPER_WILDCARD_TYPE:
				return getBound().isJavaLangObject();

			case TYPE_VARIABLE:
				return rhs.canAssignTo(getBound());

			case CAPTURE_TYPE:
				return checkTypeArgument(((CaptureType)rhs).getWildcard());

			default:
				return false;
		}
	}

	@Override
	protected boolean checkAssignmentBound(TType rhs) {
		// ? extends Number is a set of all subtyes of number and number.
		// so the only thing that can be assigned is null since null is
		// a sub type of everything
		return rhs.isNullType();
	}

	@Override
	public String getName() {
		return internalGetName("extends"); //$NON-NLS-1$
	}

	@Override
	protected String getPlainPrettySignature() {
		return internalGetPrettySignature("extends"); //$NON-NLS-1$
	}
}
