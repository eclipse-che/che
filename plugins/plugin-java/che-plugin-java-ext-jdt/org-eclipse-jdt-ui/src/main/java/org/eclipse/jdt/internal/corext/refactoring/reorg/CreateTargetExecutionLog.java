/*******************************************************************************
 * Copyright (c) 2006, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.corext.refactoring.reorg;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Objects of this class can be used as a log to trace the creation of new
 * destinations during refactorings like move.
 *
 * @since 3.3
 */
public final class CreateTargetExecutionLog {

	private Map<Object, Object> fCreations= new LinkedHashMap<Object, Object>(2);

	/**
	 * Returns the element which got created for the given selection.
	 *
	 * @param selection
	 *            the selection
	 * @return the created element, or <code>null</code>
	 */
	public Object getCreatedElement(Object selection) {
		return fCreations.get(selection);
	}

	/**
	 * Returns all created elements.
	 *
	 * @return all created elements
	 */
	public Object[] getCreatedElements() {
		return fCreations.values().toArray();
	}

	/**
	 * Returns all selected elements.
	 *
	 * @return all selected elements
	 */
	public Object[] getSelectedElements() {
		return fCreations.keySet().toArray();
	}

	/**
	 * Logs that the given element got created by the refactoring.
	 *
	 * @param selection
	 *            the selected object
	 * @param element
	 *            the element that got created for the selection
	 */
	public void markAsCreated(Object selection, Object element) {
		fCreations.put(selection, element);
	}
}
