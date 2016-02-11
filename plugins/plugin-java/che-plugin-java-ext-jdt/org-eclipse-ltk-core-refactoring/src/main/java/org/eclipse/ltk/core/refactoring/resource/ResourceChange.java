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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.LocationKind;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension4;

import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.internal.core.refactoring.BasicElementLabels;
import org.eclipse.ltk.internal.core.refactoring.Messages;
import org.eclipse.ltk.internal.core.refactoring.RefactoringCoreMessages;
import org.eclipse.ltk.internal.core.refactoring.Resources;

/**
 * Abstract change for resource based changes. The change controls the resource time stamp
 * and read only state of the resource and makes sure it is not changed before executing the change.
 *
 * @since 3.4
 */
public abstract class ResourceChange extends Change {

	/**
	 * The default validation method. It tests the modified element for existence and makes sure it has not been modified
	 * since the change has been created.
	 */
	public static final int VALIDATE_DEFAULT= 0;

	/**
	 * The 'not read only' validation method performs the default validations (see {@link #VALIDATE_DEFAULT}) and additionally ensures that the element
	 * is not read only.
	 */
	public static final int VALIDATE_NOT_READ_ONLY= 1 << 0;

	/**
	 * The 'not dirty' validation method performs the default validations (see {@link #VALIDATE_DEFAULT}) and additionally ensures that the element
	 * does not contain unsaved modifications.
	 */
	public static final int VALIDATE_NOT_DIRTY= 1 << 1;

	/**
	 * The 'save if dirty' validation method performs the default validations (see {@link #VALIDATE_DEFAULT}) and will
	 * save all unsaved modifications to the resource.
	 */
	public static final int SAVE_IF_DIRTY= 1 << 2;

	private long fModificationStamp;
	private boolean fReadOnly;
	private int fValidationMethod;

	/**
	 * Creates the resource change. The modification state will be
	 */
	public ResourceChange() {
		fModificationStamp= IResource.NULL_STAMP;
		fReadOnly= false;
		fValidationMethod= VALIDATE_DEFAULT;
	}

	/**
	 * Returns the resource of this change.
	 *
	 * @return the resource of this change
	 */
	protected abstract IResource getModifiedResource();

	/* (non-Javadoc)
	 * @see org.eclipse.ltk.core.refactoring.Change#initializeValidationData(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void initializeValidationData(IProgressMonitor pm) {
		IResource resource= getModifiedResource();
		if (resource != null) {
			fModificationStamp= getModificationStamp(resource);
			fReadOnly= Resources.isReadOnly(resource);
		}
	}

	/**
	 * Sets the validation methods used when the current resource is validated in {@link #isValid(IProgressMonitor)}.
	 * <p>
	 * By default the validation method is {@link #VALIDATE_DEFAULT}. Change implementors can add {@link #VALIDATE_NOT_DIRTY},
	 *  {@link #VALIDATE_NOT_READ_ONLY} or {@link #SAVE_IF_DIRTY}.
	 * </p>
	 *
	 * @param validationMethod the validation method used in {@link #isValid(IProgressMonitor)}.
	 * Supported validation methods currently are:
	 * <ul><li>{@link #VALIDATE_DEFAULT}</li>
	 * <li>{@link #VALIDATE_NOT_DIRTY}</li>
	 * <li>{@link #VALIDATE_NOT_READ_ONLY}</li>
	 * <li>{@link #SAVE_IF_DIRTY}</li>
	 * </ul>
	 * or combinations of these variables.
	 */
	public void setValidationMethod(int validationMethod) {
		fValidationMethod= validationMethod;
	}

	/**
	 * This implementation of {@link Change#isValid(IProgressMonitor)} tests the modified resource using the validation method
	 * specified by {@link #setValidationMethod(int)}.
	 */
	public RefactoringStatus isValid(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		pm.beginTask("", 2); //$NON-NLS-1$
		try {
			RefactoringStatus result= new RefactoringStatus();
			IResource resource= getModifiedResource();
			checkExistence(result, resource);
			if (result.hasFatalError())
				return result;
			if (fValidationMethod == VALIDATE_DEFAULT)
				return result;

			ValidationState state= new ValidationState(resource);
			state.checkModificationStamp(result, fModificationStamp);
			if (result.hasFatalError())
				return result;
			state.checkSameReadOnly(result, fReadOnly);
			if (result.hasFatalError())
				return result;
			if ((fValidationMethod & VALIDATE_NOT_READ_ONLY) != 0) {
				state.checkReadOnly(result);
				if (result.hasFatalError())
					return result;
			}
			if ((fValidationMethod & SAVE_IF_DIRTY) != 0) {
				state.saveIfDirty(result, fModificationStamp, new SubProgressMonitor(pm, 1));
			}
			if ((fValidationMethod & VALIDATE_NOT_DIRTY) != 0) {
				state.checkDirty(result);
			}
			return result;
		} finally {
			pm.done();
		}
	}

	/**
	 * Utility method to validate a resource to be modified.
	 *
	 * @param result the status where the result will be added to
	 * @param resource the resource to validate
	 * @param validationMethod the validation method used in {@link #isValid(IProgressMonitor)}.
	 * Supported validation methods currently are:
	 * <ul><li>{@link #VALIDATE_DEFAULT}</li>
	 * <li>{@link #VALIDATE_NOT_DIRTY}</li>
	 * <li>{@link #VALIDATE_NOT_READ_ONLY}</li>
	 * <li>{@link #SAVE_IF_DIRTY}</li>
	 * </ul>
	 * or combinations of these methods.
	 */
	protected static void checkIfModifiable(RefactoringStatus result, IResource resource, int validationMethod) {
		checkExistence(result, resource);
		if (result.hasFatalError())
			return;
		if (validationMethod == VALIDATE_DEFAULT)
			return;
		ValidationState state= new ValidationState(resource);
		if ((validationMethod & VALIDATE_NOT_READ_ONLY) != 0) {
			state.checkReadOnly(result);
			if (result.hasFatalError())
				return;
		}
		if ((validationMethod & VALIDATE_NOT_DIRTY) != 0) {
			state.checkDirty(result);
		}
	}

	private static void checkExistence(RefactoringStatus status, IResource element) {
		if (element == null) {
			status.addFatalError(RefactoringCoreMessages.ResourceChange_error_no_input);
		} else if (!element.exists()) {
			status.addFatalError(Messages.format(RefactoringCoreMessages.ResourceChange_error_does_not_exist, BasicElementLabels.getPathLabel(element.getFullPath(), false)));
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ltk.core.refactoring.Change#getModifiedElement()
	 */
	public Object getModifiedElement() {
		return getModifiedResource();
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return getName();
	}

	private long getModificationStamp(IResource resource) {
		if (!(resource instanceof IFile))
			return resource.getModificationStamp();
		IFile file= (IFile)resource;
		ITextFileBuffer buffer= getBuffer(file);
		if (buffer == null) {
			return file.getModificationStamp();
		} else {
			IDocument document= buffer.getDocument();
			if (document instanceof IDocumentExtension4) {
				return ((IDocumentExtension4)document).getModificationStamp();
			} else {
				return file.getModificationStamp();
			}
		}
	}

	private static ITextFileBuffer getBuffer(IFile file) {
		ITextFileBufferManager manager= FileBuffers.getTextFileBufferManager();
		return manager.getTextFileBuffer(file.getFullPath(), LocationKind.IFILE);
	}

	private static class ValidationState {
		private IResource fResource;
		private int fKind;
		private boolean fDirty;
		private boolean fReadOnly;
		private long fModificationStamp;
		private ITextFileBuffer fTextFileBuffer;
		public static final int RESOURCE= 1;
		public static final int DOCUMENT= 2;

		public ValidationState(IResource resource) {
			fResource= resource;
			if (resource instanceof IFile) {
				initializeFile((IFile) resource);
			} else {
				initializeResource(resource);
			}
		}

		public void saveIfDirty(RefactoringStatus status, long stampToMatch, IProgressMonitor pm) throws CoreException {
			if (fDirty) {
				if (fKind == DOCUMENT && fTextFileBuffer != null && stampToMatch == fModificationStamp) {
					fTextFileBuffer.commit(pm, false);
				} else {
					status.addFatalError(Messages.format(RefactoringCoreMessages.ResourceChange_error_unsaved, BasicElementLabels.getPathLabel(fResource.getFullPath(), false)));
				}
			}
		}

		public void checkDirty(RefactoringStatus status) {
			if (fDirty) {
				status.addFatalError(Messages.format(RefactoringCoreMessages.ResourceChange_error_unsaved, BasicElementLabels.getPathLabel(fResource.getFullPath(), false)));
			}
		}

		public void checkReadOnly(RefactoringStatus status) {
			if (fReadOnly) {
				status.addFatalError(Messages.format(RefactoringCoreMessages.ResourceChange_error_read_only, BasicElementLabels.getPathLabel(fResource.getFullPath(), false)));
			}
		}

		public void checkSameReadOnly(RefactoringStatus status, boolean valueToMatch) {
			if (fReadOnly != valueToMatch) {
				status.addFatalError(Messages.format(RefactoringCoreMessages.ResourceChange_error_read_only_state_changed, BasicElementLabels.getPathLabel(fResource.getFullPath(), false)));
			}
		}

		public void checkModificationStamp(RefactoringStatus status, long stampToMatch) {
			if (fKind == DOCUMENT) {
				if (stampToMatch != IDocumentExtension4.UNKNOWN_MODIFICATION_STAMP && fModificationStamp != stampToMatch) {
					status.addFatalError(Messages.format(RefactoringCoreMessages.ResourceChange_error_has_been_modified, BasicElementLabels.getPathLabel(fResource.getFullPath(), false)));
				}
			} else {
				if (stampToMatch != IResource.NULL_STAMP && fModificationStamp != stampToMatch) {
					status.addFatalError(Messages.format(RefactoringCoreMessages.ResourceChange_error_has_been_modified, BasicElementLabels.getPathLabel(fResource.getFullPath(), false)));
				}
			}
		}

		private void initializeFile(IFile file) {
			fTextFileBuffer= getBuffer(file);
			if (fTextFileBuffer == null) {
				initializeResource(file);
			} else {
				IDocument document= fTextFileBuffer.getDocument();
				fDirty= fTextFileBuffer.isDirty();
				fReadOnly= Resources.isReadOnly(file);
				if (document instanceof IDocumentExtension4) {
					fKind= DOCUMENT;
					fModificationStamp= ((IDocumentExtension4) document).getModificationStamp();
				} else {
					fKind= RESOURCE;
					fModificationStamp= file.getModificationStamp();
				}
			}

		}

		private void initializeResource(IResource resource) {
			fKind= RESOURCE;
			fDirty= false;
			fReadOnly= Resources.isReadOnly(resource);
			fModificationStamp= resource.getModificationStamp();
		}
	}
}
