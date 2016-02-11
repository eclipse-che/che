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

import java.util.EventListener;

/**
 * A resource change listener is notified of changes to resources
 * in the workspace. 
 * These changes arise from direct manipulation of resources, or 
 * indirectly through re-synchronization with the local file system.
 * <p>
 * Clients may implement this interface.
 * </p>
 * @see IResourceDelta
 * @see IWorkspace#addResourceChangeListener(IResourceChangeListener, int)
 */
public interface IResourceChangeListener extends EventListener {
	/**
	 * Notifies this listener that some resource changes 
	 * are happening, or have already happened.
	 * <p>
	 * The supplied event gives details. This event object (and the
	 * resource delta within it) is valid only for the duration of
	 * the invocation of this method.
	 * </p>
	 * <p>
	 * Note: This method is called by the platform; it is not intended
	 * to be called directly by clients.
	 * <p>
	 * Note that during resource change event notification, further changes
	 * to resources may be disallowed.
	 * </p>
	 *
	 * @param event the resource change event
	 * @see IResourceDelta
	 */
	public void resourceChanged(IResourceChangeEvent event);
}
