/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ltk.internal.core.refactoring.resource.undostates;

import java.util.Map;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;

/**
 * {@link MarkerUndoState} is a lightweight description of a marker that can be used
 * to describe a marker to be created or updated.
 *
 * This class is not intended to be instantiated or used by clients.
 *
 * @since 3.4
 *
 */
public class MarkerUndoState {

	protected IResource resource;

	private String type;
	private Map attributes;

	/**
	 *
	 * Create a {@link MarkerUndoState} from the specified marker.
	 *
	 * @param marker
	 *            the marker to be described
	 * @throws CoreException if the marker is invalid
	 */
	public MarkerUndoState(IMarker marker) throws CoreException {
		this.type = marker.getType();
		this.attributes = marker.getAttributes();
		this.resource = marker.getResource();

	}

	/**
	 * Create a {@link MarkerUndoState} from the specified marker type, attributes,
	 * and resource.
	 *
	 * @param type
	 *            the type of marker to be created.
	 * @param attributes
	 *            the attributes to be assigned to the marker
	 * @param resource
	 *            the resource on which the marker should be created
	 */
	public MarkerUndoState(String type, Map attributes, IResource resource) {
		this.type = type;
		this.attributes = attributes;
		this.resource = resource;
	}

	/**
	 * Create a marker from the marker description.
	 *
	 * @return the created marker
	 * @throws CoreException if the marker could not be created
	 */
	public IMarker createMarker() throws CoreException {
		IMarker marker = resource.createMarker(type);
		marker.setAttributes(attributes);
		return marker;
	}

	/**
	 * Update an existing marker using the attributes in the marker description.
	 *
	 * @param marker
	 *            the marker to be updated
	 * @throws CoreException if the marker could not be updated
	 */
	public void updateMarker(IMarker marker) throws CoreException {
		marker.setAttributes(attributes);
	}

	/**
	 * Return the resource associated with this marker.
	 *
	 * @return the resource associated with this marker
	 */
	public IResource getResource() {
		return resource;
	}

	/**
	 * Return the marker type associated with this marker.
	 *
	 * @return the string marker type of this marker
	 */
	public String getType() {
		return type;
	}
}
