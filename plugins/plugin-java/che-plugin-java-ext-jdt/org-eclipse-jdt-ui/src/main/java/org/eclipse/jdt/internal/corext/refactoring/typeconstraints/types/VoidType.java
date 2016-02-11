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

import org.eclipse.jdt.core.Signature;


public final class VoidType extends TType {

	protected VoidType(TypeEnvironment environment) {
		super(environment, Signature.createTypeSignature("void", true)); //$NON-NLS-1$
	}

	@Override
	public int getKind() {
		return VOID_TYPE;
	}

	@Override
	public TType[] getSubTypes() {
		throw new UnsupportedOperationException();
	}

	@Override
	protected boolean doEquals(TType type) {
		return true;
	}

	@Override
	public int hashCode() {
		return 12345;
	}
	
	@Override
	protected boolean doCanAssignTo(TType lhs) {
		return false;
	}

	@Override
	public String getName() {
		return "void"; //$NON-NLS-1$
	}

	@Override
	protected String getPlainPrettySignature() {
		return getName();
	}
}
