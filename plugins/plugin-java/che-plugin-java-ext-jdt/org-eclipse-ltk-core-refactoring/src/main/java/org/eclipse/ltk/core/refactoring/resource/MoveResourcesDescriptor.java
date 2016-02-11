/*******************************************************************************
 * Copyright (c) 2007, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.core.refactoring.resource;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringContribution;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.MoveRefactoring;
import org.eclipse.ltk.internal.core.refactoring.BasicElementLabels;
import org.eclipse.ltk.internal.core.refactoring.Messages;
import org.eclipse.ltk.internal.core.refactoring.RefactoringCoreMessages;
import org.eclipse.ltk.internal.core.refactoring.resource.MoveResourcesProcessor;

/**
 * Refactoring descriptor for the move resource refactoring.
 * <p>
 * An instance of this refactoring descriptor may be obtained by calling
 * {@link RefactoringContribution#createDescriptor()} on a refactoring
 * contribution requested by invoking
 * {@link RefactoringCore#getRefactoringContribution(String)} with the
 * refactoring id ({@link #ID}).
 * </p>
 * <p>
 * Note: this class is not intended to be subclassed or instantiated by clients.
 * </p>
 *
 * @since 3.4
 *
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public final class MoveResourcesDescriptor extends RefactoringDescriptor {

	/**
	 * Refactoring id of the 'Move Resources Resource' refactoring (value:
	 * <code>org.eclipse.ltk.core.refactoring.move.resources</code>).
	 * <p>
	 * Clients may safely cast the obtained refactoring descriptor to
	 * {@link MoveResourcesDescriptor}.
	 * </p>
	 */
	public static final String ID= "org.eclipse.ltk.core.refactoring.move.resources"; //$NON-NLS-1$


	/** The destination */
	private IPath fDestinationPath;

	/** The resources to move */
	private IPath[] fResourcePathsToMove;

	/** Configures if references will be updated */
	private boolean fUpdateReferences;

	/**
	 * Creates a new refactoring descriptor.
	 * <p>
	 * Clients should not instantiated this class but use {@link RefactoringCore#getRefactoringContribution(String)}
	 * with {@link #ID} to get the contribution that can create the descriptor.
	 * </p>
	 */
	public MoveResourcesDescriptor() {
		super(ID, null, RefactoringCoreMessages.MoveResourcesDescriptor_unnamed_descriptor, null, RefactoringDescriptor.STRUCTURAL_CHANGE | RefactoringDescriptor.MULTI_CHANGE);
		fResourcePathsToMove= null;
		fDestinationPath= null;
		fUpdateReferences= true;
	}

	/**
	 * Sets the destination container to move the resources in.
	 *
	 * @param container
	 *            the destination
	 */
	public void setDestination(IContainer container) {
		Assert.isNotNull(container);
		fDestinationPath= container.getFullPath();
	}

	/**
	 * Sets the path of the destination container to move the resources in.
	 *
	 * @param path
	 *            the destination path
	 */
	public void setDestinationPath(IPath path) {
		Assert.isNotNull(path);
		fDestinationPath= path;
	}

	/**
	 * Returns the destination container to move the resources in.
	 *
	 * @return
	 *            the destination container to move the resource in
	 */
	public IPath getDestinationPath() {
		return fDestinationPath;
	}

	/**
	 * Sets the paths of the resources to move. The resources must be of type {@link IFile} or {@link IFolder}.
	 *
	 * @param resourcePaths
	 *            the paths of the resource to move
	 */
	public void setResourcePathsToMove(IPath[] resourcePaths) {
		if (resourcePaths == null) {
			throw new IllegalArgumentException(RefactoringCoreMessages.MoveResourcesDescriptor_error_empty_array);
		}
		fResourcePathsToMove= resourcePaths;
	}

	/**
	 * Sets the resources to move. The resources must be of type {@link IFile} or {@link IFolder}.
	 *
	 * @param resources
	 *            the resource to move
	 */
	public void setResourcesToMove(IResource[] resources) {
		if (resources == null) {
			throw new IllegalArgumentException(RefactoringCoreMessages.MoveResourcesDescriptor_error_empty_array);
		}
		IPath[] paths= new IPath[resources.length];
		for (int i= 0; i < paths.length; i++) {
			paths[i]= resources[i].getFullPath();
		}
		setResourcePathsToMove(paths);
	}

	/**
	 * Returns the resource to move.
	 *
	 * @return
	 *          the resource to move
	 */
	public IPath[] getResourcePathsToMove() {
		return fResourcePathsToMove;
	}

	/**
	 * 	If set to <code>true</code>, move will also update references. The default is to update references.
	 *
	 * @param updateReferences  <code>true</code> if this move will update references
	 */
	public void setUpdateReferences(boolean updateReferences) {
		fUpdateReferences= updateReferences;
	}

	/**
	 * Returns if move will also update references
	 *
	 * @return returns <code>true</code> if this move will update references
	 */
	public boolean isUpdateReferences() {
		return fUpdateReferences;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ltk.core.refactoring.RefactoringDescriptor#createRefactoring(org.eclipse.ltk.core.refactoring.RefactoringStatus)
	 */
	public Refactoring createRefactoring(RefactoringStatus status) throws CoreException {
		IWorkspaceRoot root= ResourcesPlugin.getWorkspace().getRoot();

		IPath destinationPath= getDestinationPath();
		if (destinationPath == null) {
			status.addFatalError(RefactoringCoreMessages.MoveResourcesDescriptor_error_destination_not_set);
			return null;
		}

		IResource destination= root.findMember(destinationPath);
		if (!(destination instanceof IFolder || destination instanceof IProject) || !destination.exists()) {
			status.addFatalError(Messages.format(RefactoringCoreMessages.MoveResourcesDescriptor_error_destination_not_exists, BasicElementLabels.getPathLabel(destinationPath, false)));
			return null;
		}


		IPath[] paths= getResourcePathsToMove();
		if (paths == null) {
			status.addFatalError(RefactoringCoreMessages.MoveResourcesDescriptor_error_moved_not_set);
			return null;
		}

		IResource[] resources= new IResource[paths.length];
		for (int i= 0; i < paths.length; i++) {
			IPath path= paths[i];
			if (path == null) {
				status.addFatalError(RefactoringCoreMessages.MoveResourcesDescriptor_error_moved_contains_null);
				return null;
			}
			IResource resource= root.findMember(path);
			if (resource == null || !resource.exists()) {
				status.addFatalError(Messages.format(RefactoringCoreMessages.MoveResourcesDescriptor_error_moved_not_exists, BasicElementLabels.getPathLabel(path, false)));
				return null;
			}
			if (!(resource instanceof IFile || resource instanceof IFolder)) {
				status.addFatalError(Messages.format(RefactoringCoreMessages.MoveResourcesDescriptor_error_moved_not_file_or_folder, BasicElementLabels.getPathLabel(path, false)));
				return null;
			}
			resources[i]= resource;
		}

		MoveResourcesProcessor processor= new MoveResourcesProcessor(resources);
		processor.setDestination((IContainer) destination);
		processor.setUpdateReferences(isUpdateReferences());

		return new MoveRefactoring(processor);
	}
}