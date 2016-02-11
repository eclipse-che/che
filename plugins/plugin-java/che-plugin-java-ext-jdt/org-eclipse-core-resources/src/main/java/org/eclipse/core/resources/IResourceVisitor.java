/*******************************************************************************
 *  Copyright (c) 2000, 2009 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.resources;

import org.eclipse.core.runtime.CoreException;

/** 
 * This interface is implemented by objects that visit resource trees.
 * <p> 
 * Usage:
 * <pre>
 * class Visitor implements IResourceVisitor {
 *    public boolean visit(IResource res) {
 *       // your code here
 *       return true;
 *    }
 * }
 * IResource root = ...;
 * root.accept(new Visitor());
 * </pre>
 * </p> 
 * <p>
 * Clients may implement this interface.
 * </p>
 *
 * @see IResource#accept(IResourceVisitor)
 */
public interface IResourceVisitor {
	/** 
	 * Visits the given resource.
	 *
	 * @param resource the resource to visit
	 * @return <code>true</code> if the resource's members should
	 *		be visited; <code>false</code> if they should be skipped
	 * @exception CoreException if the visit fails for some reason.
	 */
	public boolean visit(IResource resource) throws CoreException;
}
