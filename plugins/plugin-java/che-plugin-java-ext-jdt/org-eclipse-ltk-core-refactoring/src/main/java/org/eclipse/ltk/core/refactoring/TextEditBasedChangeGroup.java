/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
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

import org.eclipse.jface.text.IRegion;

/**
 * This class is a wrapper around a {@link TextEditGroup TextEditGroup}
 * adding support for marking a group as active and inactive.
 * <p>
 * Note: this class is not intended to be extended by clients.
 * </p>
 *
 * @see TextEditGroup
 *
 * @since 3.2
 *
 * @noextend This class is not intended to be subclassed by clients.
 */
public class TextEditBasedChangeGroup {

	/** The associated change */
	private TextEditBasedChange fChange;
	private boolean fIsEnabled;
	private TextEditGroup fTextEditGroup;

	/**
	 * Creates new <code>TextEditBasedChangeGroup</code> for the given <code>
	 * TextEditBasedChange</code> and <code>TextEditGroup</code>.
	 *
	 * @param change the change owning this text edit change group
	 * @param group the underlying text edit group
	 */
	public TextEditBasedChangeGroup(TextEditBasedChange change, TextEditGroup group) {
		Assert.isNotNull(change);
		Assert.isNotNull(group);
		fChange= change;
		fIsEnabled= true;
		fTextEditGroup= group;
	}

	/**
	 * Returns the text edit change this group belongs to.
	 *
	 * @return the text edit change this group belongs to
	 */
	public TextEditBasedChange getTextEditChange() {
		return fChange;
	}

	/**
	 * Returns the groups's name by forwarding the method
	 * to the underlying text edit group.
	 *
	 * @return the group's name
	 */
	public String getName() {
		return fTextEditGroup.getName();
	}

	/**
	 * Returns the region covered by the underlying
	 * text edit group.
	 *
	 * @return the region covered by the underlying
	 *  text edit group
	 */
	public IRegion getRegion() {
		return fTextEditGroup.getRegion();
	}

	/**
	 * Returns the underlying text edit group.
	 *
	 * @return the underlying text edit group
	 */
	public TextEditGroup getTextEditGroup() {
		return fTextEditGroup;
	}

	/**
	 * Returns the text edits managed by the underlying
	 * text edit group.
	 *
	 * @return the text edits managed by the underlying
	 *  text edit group
	 */
	public TextEdit[] getTextEdits() {
		return fTextEditGroup.getTextEdits();
	}

	/**
	 * Returns whether the group is enabled or not.
	 *
	 * @return <code>true</code> if the group is marked as
	 *  enabled; <code>false</code> otherwise
	 */
	public boolean isEnabled() {
		return fIsEnabled;
	}

	/**
	 * Marks the group as enabled or disabled. If a group
	 * is marked as disabled the text edits managed by the
	 * underlying text edit group aren't executed when
	 * performing the text change that owns this group.
	 *
	 * @param enabled <code>true</code> to mark this group
	 *  as enabled, <code>false</code> to mark it as disabled
	 */
	public void setEnabled(boolean enabled) {
		fIsEnabled= enabled;
	}

	/**
	 * Returns the set of group categories.
	 *
	 * @return the group categories of this change group
	 */
	public GroupCategorySet getGroupCategorySet() {
		if (fTextEditGroup instanceof CategorizedTextEditGroup) {
			return ((CategorizedTextEditGroup)fTextEditGroup).getGroupCategorySet();
		}
		return GroupCategorySet.NONE;
	}
}