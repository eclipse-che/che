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

package org.eclipse.jdt.internal.corext.refactoring.typeconstraints2;

import org.eclipse.jdt.internal.corext.refactoring.typeconstraints.types.TType;

/**
 * A ImmutableTypeVariable is a ConstraintVariable which stands for an
 * immutable type (without an updatable Source location)
 */

public final class ImmutableTypeVariable2 extends ConstraintVariable2 {

	public ImmutableTypeVariable2(TType type) {
		super(type);
	}

	/*
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return getType().hashCode();
	}

	/*
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object other) {
		if (this == other)
			return true;
		if (other.getClass() != ImmutableTypeVariable2.class)
			return false;

		return getType() == ((ImmutableTypeVariable2) other).getType();
	}

	@Override
	public String toString() {
		return getType().getPrettySignature();
	}
}
