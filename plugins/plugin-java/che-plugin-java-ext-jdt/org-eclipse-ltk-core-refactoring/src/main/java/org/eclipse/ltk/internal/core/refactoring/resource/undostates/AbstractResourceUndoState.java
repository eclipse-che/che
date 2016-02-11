/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ltk.internal.core.refactoring.resource.undostates;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.resources.ResourcesPlugin;


/**
 * Base implementation of {@link ResourceUndoState} that describes the common
 * attributes of a resource to be created.
 *
 * This class is not intended to be instantiated or used by clients.
 *
 * @since 3.4
 *
 */
abstract class AbstractResourceUndoState extends ResourceUndoState {

	protected IContainer parent;
	protected long localTimeStamp= IResource.NULL_STAMP;

	private long modificationStamp= IResource.NULL_STAMP;

	private ResourceAttributes resourceAttributes;
	private MarkerUndoState[] markerDescriptions;

	/**
	 * Create a resource description with no initial attributes
	 */
	protected AbstractResourceUndoState() {
		super();
	}

	/**
	 * Create a resource state from the specified resource.
	 *
	 * @param resource
	 *            the resource to be described
	 */
	protected AbstractResourceUndoState(IResource resource) {
		parent= resource.getParent();
		if (resource.isAccessible()) {
			modificationStamp= resource.getModificationStamp();
			localTimeStamp= resource.getLocalTimeStamp();
			resourceAttributes= resource.getResourceAttributes();
			try {
				IMarker[] markers= resource.findMarkers(null, true, IResource.DEPTH_INFINITE);
				markerDescriptions= new MarkerUndoState[markers.length];
				for (int i= 0; i < markers.length; i++) {
					markerDescriptions[i]= new MarkerUndoState(markers[i]);
				}
			} catch (CoreException e) {
				// Eat this exception because it only occurs when the resource
				// does not exist and we have already checked this.
				// We do not want to throw exceptions on the simple constructor,
				// as no one has actually tried to do anything yet.
			}
		}
	}


	public IResource createResource(IProgressMonitor monitor) throws CoreException {
		IResource resource= createResourceHandle();
		createExistentResourceFromHandle(resource, monitor);
		restoreResourceAttributes(resource);
		return resource;
	}

	public boolean isValid() {
		return parent == null || parent.exists();
	}

	/**
	 * Restore any saved attributed of the specified resource. This method is called after the
	 * existent resource represented by the receiver has been created.
	 * 
	 * @param resource the newly created resource
	 * @throws CoreException if accessing the resource fails
	 */
	protected void restoreResourceAttributes(IResource resource) throws CoreException {
		if (modificationStamp != IResource.NULL_STAMP) {
			resource.revertModificationStamp(modificationStamp);
		}
		if (localTimeStamp != IResource.NULL_STAMP) {
			resource.setLocalTimeStamp(localTimeStamp);
		}
		if (resourceAttributes != null) {
			resource.setResourceAttributes(resourceAttributes);
		}
		if (markerDescriptions != null) {
			for (int i = 0; i < markerDescriptions.length; i++) {
				if (markerDescriptions[i].resource.exists())
					markerDescriptions[i].createMarker();
			}
		}
	}

	/*
	 * Return the workspace.
	 */
	IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}

	public boolean verifyExistence(boolean checkMembers) {
		IContainer p= parent;
		if (p == null) {
			p= getWorkspace().getRoot();
		}
		IResource handle= p.findMember(getName());
		return handle != null;
	}
}
