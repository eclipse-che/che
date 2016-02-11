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
 * Event object to communicate refactoring history notifications. These include
 * the addition and removal of refactoring descriptors to the global refactoring
 * history index.
 * <p>
 * Refactoring history listeners must be prepared to receive notifications from
 * a background thread. Any UI access occurring inside the implementation must
 * be properly synchronized using the techniques specified by the client's
 * widget library.
 * </p>
 * <p>
 * Note: this class is not intended to be instantiated by clients.
 * </p>
 *
 * @see IRefactoringHistoryListener
 * @see IRefactoringHistoryService
 *
 * @since 3.2
 *
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public final class RefactoringHistoryEvent {

	/**
	 * Event type indicating that a refactoring descriptor has been added to its
	 * associated history (value 4)
	 */
	public static final int ADDED= 4;

	/**
	 * Event type indicating that a refactoring descriptor has been deleted from
	 * its associated history (value 3)
	 */
	public static final int DELETED= 3;

	/**
	 * Event type indicating that a refactoring descriptor has been popped from
	 * the history stack (value 2)
	 */
	public static final int POPPED= 2;

	/**
	 * Event type indicating that a refactoring descriptor has been pushed to
	 * the history stack (value 1)
	 */
	public static final int PUSHED= 1;

	/** The refactoring descriptor proxy */
	private final RefactoringDescriptorProxy fProxy;

	/** The refactoring history service */
	private final IRefactoringHistoryService fService;

	/** The event type */
	private final int fType;

	/**
	 * Creates a new refactoring history event.
	 *
	 * @param service
	 *            the refactoring history service
	 * @param type
	 *            the event type
	 * @param proxy
	 *            the refactoring descriptor proxy
	 */
	public RefactoringHistoryEvent(final IRefactoringHistoryService service, final int type, final RefactoringDescriptorProxy proxy) {
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
	 * Returns the refactoring history service.
	 *
	 * @return the refactoring history service
	 */
	public IRefactoringHistoryService getHistoryService() {
		return fService;
	}
}
