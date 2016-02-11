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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.Assert;

/**
 * A special set to manage group categories. Group category sets are value
 * objects and are therefore best used as static final fields to share a
 * group category set between n {@link TextEditBasedChangeGroup}s.
 * <p>
 * Note: this class is not intended to be subclassed
 * </p>
 *
 * @since 3.2
 *
 * @noextend This class is not intended to be subclassed by clients.
 */
public class GroupCategorySet {

	/**
	 * Constant representing a group category set containing no
	 * group categories.
	 */
	public static final GroupCategorySet NONE= new GroupCategorySet() {
		public boolean contains(GroupCategory category) {
			return false;
		}
		public List asList() {
			return Collections.EMPTY_LIST;
		}
	};

	/**
	 * Creates a new group category set containing the union of the given two
	 * group category sets
	 *
	 * @param one the first set of group categories
	 * @param two the second set of group categories
	 *
	 * @return the union
	 */
	public static GroupCategorySet union(GroupCategorySet one, GroupCategorySet two) {
		Assert.isNotNull(one);
		Assert.isNotNull(two);
		// for performance we are using identity here. This is
		// valid since group categories are value objects and
		// therefore best used as static final fields.
		if (one == two)
			return one;
		if (one == NONE)
			return two;
		if (two == NONE)
			return one;

		Set combined= new HashSet();
		combined.addAll(one.asList());
		combined.addAll(two.asList());
		return new GroupCategorySet(combined);
	}

	private List/*<GroupCategory>*/ fContent;

	private GroupCategorySet() {
		fContent= Collections.EMPTY_LIST;
	}

	private GroupCategorySet(Set/*<GroupCategory>*/ categories) {
		fContent= new ArrayList(categories);
	}

	/**
	 * Creates a new list of group categories initialized
	 * with the given group category.
	 *
	 * @param category the first category
	 */
	public GroupCategorySet(GroupCategory category) {
		Assert.isNotNull(category);
		fContent= new ArrayList(1);
		fContent.add(category);
	}

	/**
	 * Creates a new set of group categories initialized
	 * from the given array of group categories
	 *
	 * @param categories the initial group categories
	 */
	public GroupCategorySet(GroupCategory[] categories) {
		Assert.isNotNull(categories);
		fContent= new ArrayList(categories.length);
		for (int i= 0; i < categories.length; i++) {
			if (!fContent.contains(categories[i]))
				fContent.add(categories[i]);
		}
	}

	/**
	 * Returns whether the given category is contained
	 * in this set of group categories
	 *
	 * @param category the category to test containment for
	 *
	 * @return <code>true</code> if the category is contained
	 *  in this set; otherwise <code>false</code>
	 */
	public boolean contains(GroupCategory category) {
		return fContent.contains(category);
	}

	/**
	 * Returns whether one of the given categories is contained
	 * in this set of group categories
	 *
	 * @param categories the categories to test containment for
	 *
	 * @return <code>true</code> if one of the given categories is
	 *  contained in this set; otherwise <code>false</code>
	 */
	public boolean containsOneCategory(List/*<GroupCategory>*/ categories) {
		for (Iterator iter= categories.iterator(); iter.hasNext();) {
			GroupCategory category= (GroupCategory)iter.next();
			if(contains(category))
				return true;
		}
		return false;
	}

	/**
	 * Converts the group categories into a a unmodifiable
	 * list.
	 *
	 * @return an unmodifiable list containing all group
	 *  categories
	 */
	public List/*<GroupCategory>*/ asList() {
		return Collections.unmodifiableList(fContent);
	}
}
