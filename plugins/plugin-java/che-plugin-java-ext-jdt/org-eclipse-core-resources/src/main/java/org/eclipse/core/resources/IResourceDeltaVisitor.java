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
 * An objects that visits resource deltas.
 * <p> 
 * Usage:
 * <pre>
 * class Visitor implements IResourceDeltaVisitor {
 *     public boolean visit(IResourceDelta delta) {
 *         switch (delta.getKind()) {
 *         case IResourceDelta.ADDED :
 *             // handle added resource
 *             break;
 *         case IResourceDelta.REMOVED :
 *             // handle removed resource
 *             break;
 *         case IResourceDelta.CHANGED :
 *             // handle changed resource
 *             break;
 *         }
 *     return true;
 *     }
 * }
 * IResourceDelta rootDelta = ...;
 * rootDelta.accept(new Visitor());
 * </pre>
 * </p>
 * <p>
 * Clients may implement this interface.
 * </p>
 *
 * @see IResource#accept(IResourceVisitor)
 */
public interface IResourceDeltaVisitor {
	/** 
	 * Visits the given resource delta.
	 * 
	 * @return <code>true</code> if the resource delta's children should
	 *		be visited; <code>false</code> if they should be skipped.
	 * @exception CoreException if the visit fails for some reason.
	 */
	public boolean visit(IResourceDelta delta) throws CoreException;
}
