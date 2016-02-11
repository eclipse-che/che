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
package org.eclipse.ltk.core.refactoring;

import org.eclipse.core.runtime.Assert;

import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.edits.TextEditGroup;

/**
 * A special text edit group that manages an additional set of
 * group categories.
 * <p>
 * Note: this class is not intended to be subclassed
 * </p>
 *
 * @since 3.2
 *
 * @noextend This class is not intended to be subclassed by clients.
 */
public class CategorizedTextEditGroup extends TextEditGroup {

	private GroupCategorySet fGroupCategories;

	/**
	 * Creates a new text edit group with the given name and group
	 * categories.
	 *
	 * @param name the name of the text edit group. Must be
	 *  a human readable string
	 * @param groupCategories a set of group categories
	 */
	public CategorizedTextEditGroup(String name, GroupCategorySet groupCategories) {
		super(name);
		Assert.isNotNull(groupCategories);
		fGroupCategories= groupCategories;
	}

	/**
	 * Creates a new text edit group with a name, a single {@link TextEdit}
	 * and a set of group categories.
	 *
	 * @param name the name of the text edit group. Must be
	 *  a human readable string
	 * @param edit the edit to manage
	 * @param groupCategories a set of group categories
	 */
	public CategorizedTextEditGroup(String name, TextEdit edit, GroupCategorySet groupCategories) {
		super(name, edit);
		Assert.isNotNull(groupCategories);
		fGroupCategories= groupCategories;
	}

	/**
	 * Creates a new text edit group with the given name, array of edits
	 * and a set of group categories.
	 *
	 * @param name the name of the text edit group. Must be
	 *  a human readable string
	 * @param edits the array of edits
	 * @param groupCategories a set of group categories
	 */
	public CategorizedTextEditGroup(String name, TextEdit[] edits, GroupCategorySet groupCategories) {
		super(name, edits);
		Assert.isNotNull(groupCategories);
		fGroupCategories= groupCategories;
	}

	/**
	 * Returns the set of group categories.
	 *
	 * @return the group categories of this text edit group
	 */
	public GroupCategorySet getGroupCategorySet() {
		return fGroupCategories;
	}
}
