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
package org.eclipse.jdt.internal.corext.refactoring.tagging;

public interface IReferenceUpdating {

	/**
	 * Informs the refactoring object whether references should be updated.
	 * 
	 * @param update <code>true</code> to enable reference updating
	 */
	public void setUpdateReferences(boolean update);

	/**
	 * Asks the refactoring object whether references should be updated.
	 * 
	 * @return <code>true</code> iff reference updating is enabled
	 */
	public boolean getUpdateReferences();

}

