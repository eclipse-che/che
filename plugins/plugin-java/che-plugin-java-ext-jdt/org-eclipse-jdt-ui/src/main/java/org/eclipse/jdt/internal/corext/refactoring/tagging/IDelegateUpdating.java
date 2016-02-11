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

/**
 * Interface for refactorings which are able to create
 * appropriate delegates for the refactored elements.
 *
 * @since 3.2
 *
 */
public interface IDelegateUpdating {

	/**
	 * Performs a dynamic check whether this refactoring object is capable of
	 * creating appropriate delegates for the refactored elements. The
	 * return value of this method may change according to the state of the
	 * refactoring.
	 */
	public boolean canEnableDelegateUpdating();

	/**
	 * If <code>canEnableDelegateUpdating</code> returns
	 * <code>true</code>, then this method is used to ask the refactoring
	 * object whether delegates will be created.
	 * This call can be ignored if <code>canEnableDelegateUpdating</code>
	 * returns <code>false</code>.
	 */
	public boolean getDelegateUpdating();

	/**
	 * Returns the title of the preference to update delegates in the user
	 * interface.
	 *
	 * @param plural
	 *            <code>true</code> if several delegates are created,
	 *            <code>false</code> otherwise
	 * @return the non-null string denoting the title
	 */
	public String getDelegateUpdatingTitle(boolean plural);

	/**
	 * If <code>canEnableDelegateUpdating</code> returns
	 * <code>true</code>, then this method is used to ask the refactoring
	 * object whether delegates will be deprecated.
	 * This call can be ignored if <code>canEnableDelegateUpdating</code>
	 * returns <code>false</code>.
	 */
	public boolean getDeprecateDelegates();

	/**
	 * If <code>canEnableDelegateUpdating</code> returns
	 * <code>true</code>, then this method may be called to set whether
	 * to create delegates.
	 * This call can be ignored if <code>canEnableDelegateUpdating</code>
	 * returns <code>false</code>.
	 */
	public void setDelegateUpdating(boolean updating);

	/**
	 * If <code>canEnableDelegateUpdating</code> returns
	 * <code>true</code>, then this method may be called to set whether
	 * to deprecate delegates.
	 * This call can be ignored if <code>canEnableDelegateUpdating</code>
	 * returns <code>false</code>.
	 */
	public void setDeprecateDelegates(boolean deprecate);
}
