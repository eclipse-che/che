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
package org.eclipse.ltk.core.refactoring.resource;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

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
import org.eclipse.ltk.core.refactoring.participants.DeleteRefactoring;
import org.eclipse.ltk.internal.core.refactoring.BasicElementLabels;
import org.eclipse.ltk.internal.core.refactoring.Messages;
import org.eclipse.ltk.internal.core.refactoring.RefactoringCoreMessages;
import org.eclipse.ltk.internal.core.refactoring.resource.DeleteResourcesProcessor;

/**
 * Refactoring descriptor for the delete resource refactoring.
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
 * @noextend This class is not intended to be subclassed by clients.
 */
public class DeleteResourcesDescriptor extends RefactoringDescriptor {
	/**
	 * Refactoring id of the 'Delete Resources' refactoring (value:
	 * <code>org.eclipse.ltk.core.refactoring.delete.resources</code>).
	 * <p>
	 * Clients may safely cast the obtained refactoring descriptor to
	 * {@link DeleteResourcesDescriptor}.
	 * </p>
	 */
	public static final String ID= "org.eclipse.ltk.core.refactoring.delete.resources"; //$NON-NLS-1$

	private IPath[] fResourcePaths;
	private boolean fDeleteContents;

	/**
	 * Creates a new refactoring descriptor.
	 * <p>
	 * Clients should not instantiated this class but use {@link RefactoringCore#getRefactoringContribution(String)}
	 * with {@link #ID} to get the contribution that can create the descriptor.
	 * </p>
	 */
	public DeleteResourcesDescriptor() {
		super(ID, null, RefactoringCoreMessages.RenameResourceDescriptor_unnamed_descriptor, null, RefactoringDescriptor.STRUCTURAL_CHANGE | RefactoringDescriptor.MULTI_CHANGE);
		fDeleteContents= false;
	}

	/**
	 * The resource paths to delete.
	 * @return an array of IPaths.
	 */
	public IPath[] getResourcePaths() {
		return fResourcePaths;
	}

	/**
	 * The paths to the resources to be deleted.  The resources can be {@link IProject} or
	 * a mixture of {@link IFile} and {@link IFolder}.
	 *
	 * @param resourcePath paths of the resources to be deleted
	 */
	public void setResourcePaths(IPath[] resourcePath) {
		if (resourcePath == null)
			throw new IllegalArgumentException();
		fResourcePaths= resourcePath;
	}

	/**
	 * The resources to be deleted.  They can be {@link IProject} or a mixture of {@link IFile}
	 * and {@link IFolder}.
	 *
	 * @param resources IResources to be deleted
	 */
	public void setResources(IResource[] resources) {
		if (resources == null)
			throw new IllegalArgumentException();
		IPath[] paths= new IPath[resources.length];
		for (int i= 0; i < paths.length; i++) {
			paths[i]= resources[i].getFullPath();
		}
		setResourcePaths(paths);
	}

	/**
	 * <code>true</code> is returned if projects contents are also deleted.
	 *
	 * @return <code>true</code> if this will delete the project contents.  The content delete is not undoable.
	 */
	public boolean isDeleteContents() {
		return fDeleteContents;
	}

	/**
	 * If set to <code>true</code>, delete will also delete project contents.
	 *
	 * @param deleteContents <code>true</code> if this will delete the project contents.  The content delete is not undoable.
	 */
	public void setDeleteContents(boolean deleteContents) {
		fDeleteContents= deleteContents;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ltk.core.refactoring.RefactoringDescriptor#createRefactoring(org.eclipse.ltk.core.refactoring.RefactoringStatus)
	 */
	public Refactoring createRefactoring(RefactoringStatus status) throws CoreException {
		IWorkspaceRoot wsRoot= ResourcesPlugin.getWorkspace().getRoot();
		IResource[] resources= new IResource[fResourcePaths.length];
		for (int i= 0; i < fResourcePaths.length; i++) {
			IResource resource= wsRoot.findMember(fResourcePaths[i]);
			if (resource == null || !resource.exists()) {
				status.addFatalError(Messages.format(RefactoringCoreMessages.DeleteResourcesDescriptor_error_delete_not_exists, BasicElementLabels.getPathLabel(fResourcePaths[i], false)));
				return null;
			}
			resources[i]= resource;
		}
		DeleteResourcesProcessor processor= new DeleteResourcesProcessor(resources, fDeleteContents);
		return new DeleteRefactoring(processor);
	}

}
