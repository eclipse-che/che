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

import org.eclipse.core.resources.IResource;

import org.eclipse.ltk.core.refactoring.participants.RefactoringProcessor;

/**
 * An <code>IResourceMapper</code> provides methods to map an original
 * resource to its refactored counterparts.
 * <p>
 * An <code>IResourceMapper</code> can be obtained via
 * {@link RefactoringProcessor#getAdapter(Class)}.
 * </p>
 * <p>
 * This interface is intended to be implemented by clients.
 * </p>
 *
 * @since 3.2
 */
public interface IResourceMapper {

	/**
	 * Returns the refactored resource for the given element.
	 * <p>
	 * Note that the returned resource might not yet exist
	 * when the method is called.
	 * </p>
	 *
	 * @param element the resource to be refactored
	 *
	 * @return the refactored element for the given element
	 */
	IResource getRefactoredResource(IResource element);
}
