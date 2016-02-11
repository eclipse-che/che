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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFileState;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.resources.IWorkspaceRoot;

import org.eclipse.ltk.internal.core.refactoring.RefactoringCoreMessages;

/**
 * {@link FileUndoState} is a lightweight description that describes a file to be
 * created.
 *
 * This class is not intended to be instantiated or used by clients.
 *
 * @since 3.4
 *
 */
public class FileUndoState extends AbstractResourceUndoState {

	protected String name;

	private URI location;
	private String charset;
	private IFileContentDescription fileContentDescription;

	/**
	 * Create a {@link FileUndoState} that can be used to later restore the given
	 * file. The file typically already exists, but this constructor will not
	 * fail if the file does not exist.
	 *
	 * @param file
	 *            the file to be restored.
	 */
	public FileUndoState(IFile file) {
		super(file);
		this.name= file.getName();
		try {
			this.charset= file.getCharset(false);
		} catch (CoreException e) {
			// we don't care, a null charset is fine.
		}
		if (file.isLinked()) {
			location= file.getLocationURI();
		}

	}

	/**
	 * Create a {@link FileUndoState} from the specified file handle. The handle does
	 * not exist, so no information should be derived from it. If a location
	 * path is specified, this file should represent a link to another location.
	 * The content description describes any state that should be used when the
	 * file resource is created.
	 *
	 * @param file
	 *            the file to be described
	 * @param linkLocation
	 *            the location of the file's link, or <code>null</code> if the
	 *            file is not linked
	 * @param fileContentDescription
	 *            the file content description that can be used to get
	 *            information about the file, such as its initial content
	 */
	public FileUndoState(IFile file, URI linkLocation, IFileContentDescription fileContentDescription) {
		super(file);
		this.name= file.getName();
		this.location= linkLocation;
		this.charset= null;
		this.fileContentDescription= fileContentDescription;
	}

	public void recordStateFromHistory(IResource resource, IProgressMonitor monitor) throws CoreException {
		Assert.isLegal(resource.getType() == IResource.FILE);

		if (location != null) {
			// file is linked, no need to record any history
			return;
		}
		IFileState[] states= ((IFile) resource).getHistory(monitor);
		if (states.length > 0) {
			final IFileState state= getMatchingFileState(states);
			this.fileContentDescription= new IFileContentDescription() {
				/*
				 * (non-Javadoc)
				 *
				 * @see org.eclipse.ui.internal.ide.undo.IFileContentDescription#exists()
				 */
				public boolean exists() {
					return state.exists();
				}

				/*
				 * (non-Javadoc)
				 *
				 * @see org.eclipse.ui.internal.ide.undo.IFileContentDescription#getContents()
				 */
				public InputStream getContents() throws CoreException {
					return state.getContents();
				}

				/*
				 * (non-Javadoc)
				 *
				 * @see org.eclipse.ui.internal.ide.undo.IFileContentDescription#getCharset()
				 */
				public String getCharset() throws CoreException {
					return state.getCharset();
				}
			};
		}
	}

	public IResource createResourceHandle() {
		IWorkspaceRoot workspaceRoot= parent.getWorkspace().getRoot();
		IPath fullPath= parent.getFullPath().append(name);
		return workspaceRoot.getFile(fullPath);
	}

	public void createExistentResourceFromHandle(IResource resource, IProgressMonitor monitor) throws CoreException {

		Assert.isLegal(resource instanceof IFile);
		if (resource.exists()) {
			return;
		}
		IFile fileHandle= (IFile) resource;
		monitor.beginTask("", 200); //$NON-NLS-1$
		monitor.setTaskName(RefactoringCoreMessages.FileDescription_NewFileProgress);
		try {
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			if (location != null) {
				fileHandle.createLink(location, IResource.ALLOW_MISSING_LOCAL, new SubProgressMonitor(monitor, 200));
			} else {
				InputStream contents= new ByteArrayInputStream(RefactoringCoreMessages.FileDescription_ContentsCouldNotBeRestored.getBytes());
				// Retrieve the contents from the file content
				// description. Other file state attributes, such as timestamps,
				// have already been retrieved from the original IResource
				// object and are restored in #restoreResourceAttributes
				if (fileContentDescription != null && fileContentDescription.exists()) {
					contents= fileContentDescription.getContents();
				}
				fileHandle.create(contents, false, new SubProgressMonitor(monitor, 100));
				fileHandle.setCharset(charset, new SubProgressMonitor(monitor, 100));
			}
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
		} catch (CoreException e) {
			if (e.getStatus().getCode() == IResourceStatus.PATH_OCCUPIED) {
				fileHandle.refreshLocal(IResource.DEPTH_ZERO, null);
			} else {
				throw e;
			}
		} finally {
			monitor.done();
		}
	}

	public boolean isValid() {
		if (location != null) {
			return super.isValid();
		}
		return super.isValid() && fileContentDescription != null && fileContentDescription.exists();
	}

	public String getName() {
		return name;
	}

	/**
	 * Get the file state that matches this file description. The local time
	 * stamp is used to try to find a matching file state. If none can be found,
	 * the most recent copy of the file state is used.
	 * @param states file states
	 * @return  best guess state
	 */
	private IFileState getMatchingFileState(IFileState[] states) {
		for (int i= 0; i < states.length; i++) {
			if (localTimeStamp == states[i].getModificationTime()) {
				return states[i];
			}
		}
		return states[0];

	}

	protected void restoreResourceAttributes(IResource resource) throws CoreException {
		super.restoreResourceAttributes(resource);
		Assert.isLegal(resource instanceof IFile);
		IFile file= (IFile) resource;
		if (charset != null) {
			file.setCharset(charset, null);
		}
	}
}
