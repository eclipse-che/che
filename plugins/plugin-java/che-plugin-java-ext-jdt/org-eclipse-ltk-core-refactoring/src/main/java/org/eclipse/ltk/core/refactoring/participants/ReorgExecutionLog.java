/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.core.refactoring.participants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Objects of this class can be used as a log to trace the
 * execution of refactorings like copy and paste
 * <p>
 * This class is not intended to be subclassed by clients.
 * </p>
 *
 * @since 3.1
 *
 * @noextend This class is not intended to be subclassed by clients.
 */
public class ReorgExecutionLog {

	private Map fNewNames;
	private List fProcessedElements;
	private boolean fCanceled;

	/**
	 * Creates new reorg execution log
	 */
	public ReorgExecutionLog() {
		fNewNames= new HashMap();
		fProcessedElements= new ArrayList();
	}

	/**
	 * Logs that the reorg refactoring got canceled by the
	 * user.
	 */
	public void markAsCanceled() {
		fCanceled= true;
	}

	/**
	 * Returns <code>true</code> if the reorg refactoring got
	 * canceled; otherwise <code>false</code>
	 *
	 * @return whether the refactoring got canceled or not
	 */
	public boolean isCanceled() {
		return fCanceled;
	}

	/**
	 * Returns <code>true</code> if the specified element has been processed;
	 * otherwise <code>false</code>
	 *
	 * @param element
	 *            the element to test
	 * @return whether the specified element has been processed
	 * @since 3.3
	 */
	public boolean isProcessed(Object element) {
		return fProcessedElements.contains(element);
	}

	/**
	 * Returns <code>true</code> if the specified element has been renamed;
	 * otherwise <code>false</code>
	 *
	 * @param element
	 *            the element to test
	 * @return whether the specified element has been renamed
	 * @since 3.3
	 */
	public boolean isRenamed(Object element) {
		return fNewNames.keySet().contains(element);
	}

	/**
	 * Logs that the given element got processed by the
	 * refactoring
	 *
	 * @param element the element that got processed
	 */
	public void markAsProcessed(Object element) {
		fProcessedElements.add(element);
	}

	/**
	 * Returns all processed elements
	 *
	 * @return all processed elements
	 */
	public Object[] getProcessedElements() {
		return fProcessedElements.toArray();
	}

	/**
	 * Logs that the element got renamed to <code>newName
	 * </code> by the reorg refactoring.
	 *
	 * @param element the element which got renamed
	 * @param newName the new name of the element
	 */
	public void setNewName(Object element, String newName) {
		fNewNames.put(element, newName);
	}

	/**
	 * Returns all elements which got renamed during the
	 * reorg refactoring
	 *
	 * @return the renamed elements
	 */
	public Object[] getRenamedElements() {
		return fNewNames.keySet().toArray();
	}

	/**
	 * Returns the new name of the element. Returns <code>
	 * null</code> if the element didn't get renamed.
	 *
	 * @param element the element for which the new name is
	 *  requested
	 * @return the new name of <code>null</code>
	 */
	public String getNewName(Object element) {
		return (String)fNewNames.get(element);
	}
}
