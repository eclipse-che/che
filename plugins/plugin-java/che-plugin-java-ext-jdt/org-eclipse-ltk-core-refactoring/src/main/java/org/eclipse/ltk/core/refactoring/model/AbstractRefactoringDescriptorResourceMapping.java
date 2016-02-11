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
package org.eclipse.ltk.core.refactoring.model;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.mapping.ModelProvider;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.resources.mapping.ResourceMappingContext;
import org.eclipse.core.resources.mapping.ResourceTraversal;

import org.eclipse.ltk.core.refactoring.RefactoringDescriptorProxy;
import org.eclipse.ltk.internal.core.refactoring.RefactoringCorePlugin;
import org.eclipse.ltk.internal.core.refactoring.history.RefactoringHistoryManager;
import org.eclipse.ltk.internal.core.refactoring.history.RefactoringHistoryService;

/**
 * Partial implementation of a resource mapping for a refactoring descriptor
 * object.
 * <p>
 * Note: this class is intended to be implemented by clients which need to
 * enhance a model provider with a refactoring model.
 * </p>
 *
 * @see ResourceMapping
 * @see ModelProvider
 *
 * @since 3.2
 */
public abstract class AbstractRefactoringDescriptorResourceMapping extends ResourceMapping {

	/** The refactoring descriptor */
	private final RefactoringDescriptorProxy fDescriptor;

	/** The resource traversals */
	private ResourceTraversal[] fResourceTraversals= null;

	/**
	 * Creates a new abstract refactoring descriptor resource mapping.
	 *
	 * @param descriptor
	 *            the refactoring descriptor
	 */
	protected AbstractRefactoringDescriptorResourceMapping(final RefactoringDescriptorProxy descriptor) {
		Assert.isNotNull(descriptor);
		fDescriptor= descriptor;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean equals(final Object object) {
		if (object instanceof AbstractRefactoringDescriptorResourceMapping) {
			final AbstractRefactoringDescriptorResourceMapping mapping= (AbstractRefactoringDescriptorResourceMapping) object;
			return mapping.fDescriptor.equals(fDescriptor);
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public final Object getModelObject() {
		return fDescriptor;
	}

	/**
	 * {@inheritDoc}
	 */
	public final IProject[] getProjects() {
		final String project= fDescriptor.getProject();
		if (project != null && !"".equals(project)) //$NON-NLS-1$
			return new IProject[] { ResourcesPlugin.getWorkspace().getRoot().getProject(project)};
		return new IProject[] {};
	}

	/**
	 * Returns the associated resource.
	 *
	 * @return the associated resource, or <code>null</code> if the descriptor
	 *         contains no timestamp or project information
	 */
	public final IResource getResource() {
		try {
			final ResourceTraversal[] traversals= getTraversals(null, null);
			if (traversals.length > 0) {
				final IResource[] resources= traversals[0].getResources();
				if (resources.length > 0)
					return resources[0];
			}
		} catch (CoreException exception) {
			RefactoringCorePlugin.log(exception);
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public final ResourceTraversal[] getTraversals(final ResourceMappingContext context, final IProgressMonitor monitor) throws CoreException {
		if (fResourceTraversals == null) {
			fResourceTraversals= new ResourceTraversal[] {};
			final long stamp= fDescriptor.getTimeStamp();
			if (stamp >= 0) {
				final IPath path= RefactoringHistoryManager.stampToPath(stamp);
				if (path != null) {
					final IProject[] projects= getProjects();
					if (projects != null && projects.length == 1 && projects[0] != null) {
						final IFolder folder= projects[0].getFolder(RefactoringHistoryService.NAME_HISTORY_FOLDER).getFolder(path);
						fResourceTraversals= new ResourceTraversal[] { new ResourceTraversal(new IResource[] { folder.getFile(RefactoringHistoryService.NAME_HISTORY_FILE)}, IResource.DEPTH_ZERO, IResource.NONE), new ResourceTraversal(new IResource[] { folder.getFile(RefactoringHistoryService.NAME_INDEX_FILE)}, IResource.DEPTH_ZERO, IResource.NONE)};
					}
				}
			}
		}
		final ResourceTraversal[] traversals= new ResourceTraversal[fResourceTraversals.length];
		System.arraycopy(fResourceTraversals, 0, traversals, 0, fResourceTraversals.length);
		return traversals;
	}

	/**
	 * {@inheritDoc}
	 */
	public int hashCode() {
		return fDescriptor.hashCode();
	}
}
