/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.resources;

import org.eclipse.core.resources.team.FileModificationValidator;
import org.eclipse.core.runtime.IStatus;

/**
 * The file modification validator is a Team-related hook for pre-checking operations 
 * that modify the contents of files.
 * <p>
 * This interface is used only in conjunction with the
 * "org.eclipse.core.resources.fileModificationValidator"
 * extension point. It is intended to be implemented only 
 * by the Eclipse Platform Team plug-in.
 * </p>
 * 
 * @since 2.0
 * @deprecated clients should subclass {@link FileModificationValidator} instead
 * of implementing this interface
 */
@Deprecated
public interface IFileModificationValidator {
	/**
	 * Validates that the given files can be modified.  The files must all exist
	 * in the workspace.  The optional context object may be supplied if
	 * UI-based validation is required.  If the context is <code>null</code>, the 
	 * validator must attempt to perform the validation in a headless manner.
	 * The returned status is <code>IStatus.OK</code> if this validator 
	 * believes the given file can be modified.  Other return statuses indicate
	 * the reason why the individual files cannot be modified.
	 * 
	 * @param files the files that are to be modified; these files must all exist in the workspace
	 * @param context the <code>org.eclipse.swt.widgets.Shell</code> that is to be used to
	 *    parent any dialogs with the user, or <code>null</code> if there is no UI context (declared
	 *   as an <code>Object</code> to avoid any direct references on the SWT component)
	 * @return a status object that is OK if things are fine, otherwise a status describing
	 *    reasons why modifying the given files is not reasonable
	 * @see IWorkspace#validateEdit(IFile[], Object)
	 */
	public IStatus validateEdit(IFile[] files, Object context);

	/**
	 * Validates that the given file can be saved.  This method is called from 
	 * <code>IFile#setContents</code> and <code>IFile#appendContents</code> 
	 * before any attempt to write data to disk.  The returned status is 
	 * <code>IStatus.OK</code> if this validator believes the given file can be 
	 * successfully saved.  In all other cases the return value is a non-OK status.  
	 * Note that a return value of <code>IStatus.OK</code> does not guarantee 
	 * that the save will succeed.
	 * 
	 * @param file the file that is to be modified; this file must exist in the workspace
	 * @return a status indicating whether or not it is reasonable to try writing to the given file; 
	 * <code>IStatus.OK</code> indicates a save should be attempted.
	 * 
	 * @see IFile#setContents(java.io.InputStream, int, org.eclipse.core.runtime.IProgressMonitor)
	 * @see IFile#appendContents(java.io.InputStream, int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IStatus validateSave(IFile file);
}
