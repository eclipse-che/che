/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.core.refactoring.resource;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IPath;

import org.eclipse.ltk.core.refactoring.RefactoringContribution;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.resource.MoveResourcesDescriptor;

/**
 * Refactoring contribution for the move resources refactoring.
 *
 * @since 3.4
 */
public final class MoveResourcesRefactoringContribution extends RefactoringContribution {

	/**
	 * Key used for the number of resource to be moved
	 */
	private static final String ATTRIBUTE_NUMBER_OF_RESOURCES= "resources"; //$NON-NLS-1$

	/**
	 * Key prefix used for the path of the resources to be moved.
	 * <p>
	 * The element arguments are simply distinguished by appending a number to
	 * the argument name, e.g. element1. The indices of this argument are one-based.
	 * </p>
	 */
	private static final String ATTRIBUTE_ELEMENT= "element"; //$NON-NLS-1$

	/**
	 * Key used for the new resource name
	 */
	private static final String ATTRIBUTE_DESTINATION= "destination"; //$NON-NLS-1$

	/**
	 * Key used for the 'update references' property
	 */
	private static final String ATTRIBUTE_UPDATE_REFERENCES= "updateReferences"; //$NON-NLS-1$


	/* (non-Javadoc)
	 * @see org.eclipse.ltk.core.refactoring.RefactoringContribution#retrieveArgumentMap(org.eclipse.ltk.core.refactoring.RefactoringDescriptor)
	 */
	public Map retrieveArgumentMap(final RefactoringDescriptor descriptor) {
		HashMap map= new HashMap();

		if (descriptor instanceof MoveResourcesDescriptor) {
			MoveResourcesDescriptor moveDescriptor= (MoveResourcesDescriptor) descriptor;
			IPath[] paths= moveDescriptor.getResourcePathsToMove();
			String project= moveDescriptor.getProject();
			IPath destinationPath= moveDescriptor.getDestinationPath();

			map.put(ATTRIBUTE_NUMBER_OF_RESOURCES, String.valueOf(paths.length));
			for (int i= 0; i < paths.length; i++) {
				map.put(ATTRIBUTE_ELEMENT + (i + 1), ResourceProcessors.resourcePathToHandle(project, paths[i]));
			}
			map.put(ATTRIBUTE_DESTINATION, ResourceProcessors.resourcePathToHandle(project, destinationPath));
			map.put(ATTRIBUTE_UPDATE_REFERENCES, moveDescriptor.isUpdateReferences() ? "true" : "false"); //$NON-NLS-1$//$NON-NLS-2$
			return map;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ltk.core.refactoring.RefactoringContribution#createDescriptor()
	 */
	public RefactoringDescriptor createDescriptor() {
		return new MoveResourcesDescriptor();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ltk.core.refactoring.RefactoringContribution#createDescriptor(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.util.Map, int)
	 */
	public RefactoringDescriptor createDescriptor(String id, String project, String description, String comment, Map arguments, int flags) {
		try {
			int numResources= Integer.parseInt((String) arguments.get(ATTRIBUTE_NUMBER_OF_RESOURCES));
			if (numResources < 0 || numResources > 100000) {
				throw new IllegalArgumentException("Can not restore MoveResourceDescriptor from map, number of moved elements invalid"); //$NON-NLS-1$
			}

			IPath[] resourcePaths= new IPath[numResources];
			for (int i= 0; i < numResources; i++) {
				String resource= (String) arguments.get(ATTRIBUTE_ELEMENT + String.valueOf(i + 1));
				if (resource == null) {
					throw new IllegalArgumentException("Can not restore MoveResourceDescriptor from map, resource missing"); //$NON-NLS-1$
				}
				resourcePaths[i]= ResourceProcessors.handleToResourcePath(project, resource);
			}

			String destination= (String) arguments.get(ATTRIBUTE_DESTINATION);
			if (destination == null) {
				throw new IllegalArgumentException("Can not restore MoveResourceDescriptor from map, destination missing"); //$NON-NLS-1$
			}
			IPath destPath= ResourceProcessors.handleToResourcePath(project, destination);

			boolean updateReferences= "true".equals(arguments.get(ATTRIBUTE_UPDATE_REFERENCES)); //$NON-NLS-1$

			MoveResourcesDescriptor descriptor= new MoveResourcesDescriptor();
			descriptor.setProject(project);
			descriptor.setDescription(description);
			descriptor.setComment(comment);
			descriptor.setFlags(flags);
			descriptor.setResourcePathsToMove(resourcePaths);
			descriptor.setDestinationPath(destPath);
			descriptor.setUpdateReferences(updateReferences);
			return descriptor;

		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Can not restore MoveResourceDescriptor from map"); //$NON-NLS-1$
		}
	}
}
