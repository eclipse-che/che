/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
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

public final class IndependentTypeVariable2 extends ConstraintVariable2 {

	public IndependentTypeVariable2(TType type) {
		super(type);
	}

	// hashCode() and equals(..) not necessary (unique per construction)
}
