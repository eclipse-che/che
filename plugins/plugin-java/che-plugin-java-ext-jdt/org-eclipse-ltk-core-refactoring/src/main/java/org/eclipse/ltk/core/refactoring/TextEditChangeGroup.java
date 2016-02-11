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

import org.eclipse.text.edits.TextEditGroup;

/**
 * This class is a wrapper around a {@link TextEditGroup TextEditGroup}
 * adding support for marking a group as active and inactive.
 * <p>
 * Note: this class is not intended to be extended by clients.
 * </p>
 *
 * @see TextEditGroup
 *
 * @since 3.0
 *
 * @noextend This class is not intended to be subclassed by clients.
 */
public class TextEditChangeGroup extends TextEditBasedChangeGroup {

	/**
	 * Creates new <code>TextEditChangeGroup</code> for the given <code>
	 * TextChange</code> and <code>TextEditGroup</code>.
	 *
	 * @param change the change owning this text edit change group
	 * @param group the underlying text edit group
	 */
	public TextEditChangeGroup(TextChange change, TextEditGroup group) {
		super(change, group);
	}

	/**
	 * Returns the text change this group belongs to.
	 *
	 * @return the text change this group belongs to
	 */
	public TextChange getTextChange() {
		return (TextChange) getTextEditChange();
	}
}
