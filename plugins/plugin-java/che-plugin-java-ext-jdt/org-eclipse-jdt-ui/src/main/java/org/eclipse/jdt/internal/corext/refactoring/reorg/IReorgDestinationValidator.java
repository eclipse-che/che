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
package org.eclipse.jdt.internal.corext.refactoring.reorg;

public interface IReorgDestinationValidator {

	/**
	 * Is it possible, that destination contains valid destinations
	 * as children?
	 *
	 * @param destination the destination to verify
	 * @return true if destination can have valid destinations
	 */
	public boolean canChildrenBeDestinations(IReorgDestination destination);

	/**
	 * Is it possible, that the given kind of destination is a target for
	 * the reorg?
	 *
	 * @param destination the destination to verify
	 * @return true if possible
	 */
	public boolean canElementBeDestination(IReorgDestination destination);
}
