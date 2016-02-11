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

package org.eclipse.jdt.internal.corext.refactoring.typeconstraints2;

import org.eclipse.jdt.internal.corext.refactoring.typeconstraints.types.ArrayType;

/**
 * An ArrayTypeVariable2 is a ConstraintVariable which stands for an array type.
 */
public class ArrayTypeVariable2 extends ConstraintVariable2 {

	public ArrayTypeVariable2(ArrayType type) {
		super(type);
	}

	// hashCode() and equals(..) not necessary (unique per construction)

}
