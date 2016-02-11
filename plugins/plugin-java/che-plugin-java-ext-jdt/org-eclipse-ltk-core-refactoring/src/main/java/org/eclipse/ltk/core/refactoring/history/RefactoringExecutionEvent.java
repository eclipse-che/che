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
package org.eclipse.ltk.core.refactoring.history;

import org.eclipse.core.runtime.Assert;

import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptorProxy;

/**
 * Event object to communicate refactoring execution notifications. These
 * include before-the-fact notification of perform, undo and redo refactoring
 * operations as well as after-the-fact notification of the above refactoring
 * operations.
 * <p>
 * Refactoring execution listeners must be prepared to receive notifications
 * from a background thread. Any UI access occurring inside the implementation
 * must be properly synchronized using the techniques specified by the client's
 * widget library.
 * </p>
 * <p>
 * Note: this class is not intended to be instantiated by clients.
 * </p>
 *
 * @see IRefactoringExecutionListener
 * @see IRefactoringHistoryService
 *
 * @since 3.2
 *
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public final class RefactoringExecutionEvent {

	/** Event type indicating that a refactoring is about to be performed (value 4) */
	public static final int ABOUT_TO_PERFORM= 4;

	/** Event type indicating that a refactoring is about to be redone (value 6) */
	public static final int ABOUT_TO_REDO= 6;

	/** Event type indicating that a refactoring is about to be undone (value 5) */
	public static final int ABOUT_TO_UNDO= 5;

	/** Event type indicating that a refactoring has been performed (value 1) */
	public static final int PERFORMED= 1;

	/** Event type indicating that a refactoring has been performed (value 3) */
	public static final int REDONE= 3;

	/** Event type indicating that a refactoring has been undone (value 2) */
	public static final int UNDONE= 2;

	/** The refactoring descriptor proxy */
	private final RefactoringDescriptorProxy fProxy;

	/** The refactoring history service */
	private final IRefactoringHistoryService fService;

	/** The event type */
	private final int fType;

	/**
	 * Creates a new refactoring execution event.
	 *
	 * @param service
	 *            the refactoring history service
	 * @param type
	 *            the event type
	 * @param proxy
	 *            the refactoring descriptor proxy
	 */
	public RefactoringExecutionEvent(final IRefactoringHistoryService service, final int type, final RefactoringDescriptorProxy proxy) {
		Assert.isNotNull(service);
		Assert.isNotNull(proxy);
		fService= service;
		fType= type;
		fProxy= proxy;
	}

	/**
	 * Returns the refactoring descriptor proxy.
	 * <p>
	 * Depending on the event, this proxy may refer to an inexisting refactoring
	 * and cannot be resolved to a refactoring descriptor. Clients should also
	 * be prepared to receive notifications for unknown refactorings, which are
	 * discriminated by their special id
	 * {@link RefactoringDescriptor#ID_UNKNOWN};
	 * </p>
	 *
	 * @return the refactoring descriptor proxy
	 */
	public RefactoringDescriptorProxy getDescriptor() {
		return fProxy;
	}

	/**
	 * Returns the event type.
	 *
	 * @return the event type
	 */
	public int getEventType() {
		return fType;
	}

	/**
	 * Returns the refactoring history service
	 *
	 * @return the refactoring history service
	 */
	public IRefactoringHistoryService getHistoryService() {
		return fService;
	}
}
