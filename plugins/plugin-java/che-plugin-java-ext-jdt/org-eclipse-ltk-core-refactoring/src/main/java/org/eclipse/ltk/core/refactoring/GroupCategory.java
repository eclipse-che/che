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

/**
 * A group category is used to annotate change groups so that
 * they can be identified and filtered.
 * <p>
 * Note: this class is not intended to be subclassed
 * </p>
 *
 * @since 3.2
 *
 * @noextend This class is not intended to be subclassed by clients.
 */
public class GroupCategory {

	private String fId;
	private String fName;
	private String fDescription;

	/**
	 * Creates a new group category with the given name and
	 * description.
	 *
	 * @param id a unique identifier
	 * @param name the name
	 * @param description the description
	 */
	public GroupCategory(String id, String name, String description) {
		Assert.isNotNull(id);
		Assert.isNotNull(name);
		Assert.isNotNull(description);
		fId= id;
		fName= name;
		fDescription= description;
	}

	/**
	 * Returns the name of the group category.
	 *
	 * @return the name
	 */
	public String getName() {
		return fName;
	}

	/**
	 * Returns the description of the group category.
	 *
	 * @return the description
	 */
	public String getDescription() {
		return fDescription;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || !getClass().equals(obj.getClass()))
			return false;
		return fId.equals(((GroupCategory)obj).fId);
	}

	public int hashCode() {
		return fId.hashCode();
	}
}
