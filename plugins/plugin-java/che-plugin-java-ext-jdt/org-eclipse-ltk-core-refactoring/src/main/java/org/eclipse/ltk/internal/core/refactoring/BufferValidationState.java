/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.core.refactoring;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.IFileBuffer;
import org.eclipse.core.filebuffers.IFileBufferListener;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.LocationKind;

import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension4;
import org.eclipse.jface.text.IDocumentListener;

import org.eclipse.ltk.core.refactoring.RefactoringStatus;

public abstract class BufferValidationState {

	protected final IFile fFile;
	protected final boolean fExisted;
	protected final boolean fDerived;
	protected final boolean fWasDirty;
	protected final String fEncoding;

	protected static class ModificationStamp {
		private int fKind;
		private long fValue;
		public static final int FILE= 1;
		public static final int DOCUMENT= 2;

		public static ModificationStamp createFile(long value) {
			return new ModificationStamp(FILE, value);
		}
		public static ModificationStamp createDocument(long value) {
			return new ModificationStamp(DOCUMENT, value);
		}
		private ModificationStamp(int kind, long value) {
			fKind= kind;
			fValue= value;
		}
		public boolean isFileStamp() {
			return fKind == FILE;
		}
		public boolean isDocumentStamp() {
			return fKind == DOCUMENT;
		}
		public int getKind() {
			return fKind;
		}
		public long getValue() {
			return fValue;
		}
	}

	public static BufferValidationState create(IFile file) {
		ITextFileBuffer buffer= getBuffer(file);
		if (buffer == null) {
			return new ModificationStampValidationState(file);
		} else {
			IDocument document= buffer.getDocument();
			if (document instanceof IDocumentExtension4) {
				return new ModificationStampValidationState(file);
			} else {
				if (buffer.isDirty()) {
					return new NoStampValidationState(file);
				} else {
					return new ModificationStampValidationState(file);
				}
			}
		}
	}

	public boolean wasDirty() {
		return fWasDirty;
	}

	public boolean wasDerived() {
		return fDerived;
	}

	public RefactoringStatus isValid(boolean needsSaving) throws CoreException {
		return isValid(needsSaving, false);
	}

	public RefactoringStatus isValid(boolean needsSaving, boolean resilientForDerived) throws CoreException {
		if (resilientForDerived && fDerived) {
			return new RefactoringStatus();
		}
		if (!fExisted) {
			if (fFile.exists())
				return RefactoringStatus.createFatalErrorStatus(Messages.format(
					RefactoringCoreMessages.TextChanges_error_existing,
					BasicElementLabels.getPathLabel(fFile.getFullPath(), false)));
		} else {
			if (!fFile.exists())
				return RefactoringStatus.createFatalErrorStatus(Messages.format(
					RefactoringCoreMessages.TextChanges_error_not_existing,
					BasicElementLabels.getPathLabel(fFile.getFullPath(), false)));
		}
		if (needsSaving) {
			if (fFile.isReadOnly()) {
				return RefactoringStatus.createFatalErrorStatus(Messages.format(
					RefactoringCoreMessages.TextChanges_error_read_only,
					BasicElementLabels.getPathLabel(fFile.getFullPath(), false)));
			} else if (!fFile.isSynchronized(IResource.DEPTH_ZERO)) {
				return RefactoringStatus.createFatalErrorStatus(Messages.format(
					RefactoringCoreMessages.TextChanges_error_outOfSync,
					BasicElementLabels.getPathLabel(fFile.getFullPath(), false)));
			}
		}
		if (fEncoding == null) {
			return RefactoringStatus.createFatalErrorStatus(Messages.format(
				RefactoringCoreMessages.BufferValidationState_no_character_encoding,
				BasicElementLabels.getPathLabel(fFile.getFullPath(), false)));
		} else if (!fEncoding.equals(fFile.getCharset(true))) {
			return RefactoringStatus.createFatalErrorStatus(Messages.format(
				RefactoringCoreMessages.BufferValidationState_character_encoding_changed,
				BasicElementLabels.getPathLabel(fFile.getFullPath(), false)));
		}
		return new RefactoringStatus();
	}

	public void dispose() {
	}


	protected BufferValidationState(IFile file) {
		fFile= file;
		fExisted= file.exists();
		fDerived= file.isDerived();
		fWasDirty= isDirty(fFile);
		String encoding;
		try {
			encoding= file.getCharset(true);
		} catch (CoreException e) {
			encoding= null;
		}
		fEncoding= encoding;
	}

	protected IDocument getDocument() {
		ITextFileBuffer buffer= getBuffer(fFile);
		if (buffer == null)
			return null;
		return buffer.getDocument();

	}

	protected static boolean isDirty(IFile file) {
		ITextFileBuffer buffer= getBuffer(file);
		if (buffer == null)
			return false;
		return buffer.isDirty();
	}

	protected static ITextFileBuffer getBuffer(IFile file) {
		ITextFileBufferManager manager= FileBuffers.getTextFileBufferManager();
		IPath path= file.getFullPath();
		ITextFileBuffer buffer= manager.getTextFileBuffer(path, LocationKind.IFILE);
		return buffer;
	}

	protected ModificationStamp getModificationStamp() {
		ITextFileBuffer buffer= getBuffer(fFile);
		if (buffer == null) {
			return ModificationStamp.createFile(fFile.getModificationStamp());
		} else {
			IDocument document= buffer.getDocument();
			if (document instanceof IDocumentExtension4) {
				return ModificationStamp.createDocument(((IDocumentExtension4)document).getModificationStamp());
			} else {
				return ModificationStamp.createFile(fFile.getModificationStamp());
			}
		}
	}
}

/**
 * Buffer validation state for dirty files whose document does not support
 * modification stamps.
 */
class NoStampValidationState extends BufferValidationState {

	private IDocumentListener fDocumentListener;
	private FileBufferListener fFileBufferListener;
	private boolean fChanged;
	private long fContentStamp= IResource.NULL_STAMP;

	class DocumentChangedListener implements IDocumentListener {
		public void documentAboutToBeChanged(DocumentEvent event) {
		}
		public void documentChanged(DocumentEvent event) {
			NoStampValidationState.this.documentChanged();
		}
	}

	class FileBufferListener implements IFileBufferListener {
		public void bufferCreated(IFileBuffer buffer) {
			// begin https://bugs.eclipse.org/bugs/show_bug.cgi?id=67821
			if (buffer.getLocation().equals(fFile.getFullPath()) && buffer instanceof ITextFileBuffer) {
				ITextFileBuffer textBuffer= (ITextFileBuffer)buffer;
				if (fDocumentListener == null)
					fDocumentListener= new DocumentChangedListener();
				textBuffer.getDocument().addDocumentListener(fDocumentListener);
			}
			// end fix https://bugs.eclipse.org/bugs/show_bug.cgi?id=67821
		}
		public void bufferDisposed(IFileBuffer buffer) {
			// begin fix https://bugs.eclipse.org/bugs/show_bug.cgi?id=67821
			if (fDocumentListener != null && buffer.getLocation().equals(fFile.getFullPath())) {
				if (buffer instanceof ITextFileBuffer) {
					ITextFileBuffer textBuffer= (ITextFileBuffer)buffer;
					textBuffer.getDocument().removeDocumentListener(fDocumentListener);
					fDocumentListener= null;
				}
				fContentStamp= fFile.getModificationStamp();
			}
			// end fix https://bugs.eclipse.org/bugs/show_bug.cgi?id=67821
		}
		public void bufferContentAboutToBeReplaced(IFileBuffer buffer) {
		}
		public void bufferContentReplaced(IFileBuffer buffer) {
		}
		public void stateChanging(IFileBuffer buffer) {
		}
		public void dirtyStateChanged(IFileBuffer buffer, boolean isDirty) {
		}
		public void stateValidationChanged(IFileBuffer buffer, boolean isStateValidated) {
		}
		public void underlyingFileMoved(IFileBuffer buffer, IPath path) {
		}
		public void underlyingFileDeleted(IFileBuffer buffer) {
		}
		public void stateChangeFailed(IFileBuffer buffer) {
		}
	}

	public NoStampValidationState(IFile file) {
		super(file);
		fContentStamp= file.getModificationStamp();
		fFileBufferListener= new FileBufferListener();
		FileBuffers.getTextFileBufferManager().addFileBufferListener(fFileBufferListener);
		fDocumentListener= new DocumentChangedListener();
		getDocument().addDocumentListener(fDocumentListener);
	}

	public RefactoringStatus isValid(boolean needsSaving, boolean resilientForDerived) throws CoreException {
		RefactoringStatus result= super.isValid(needsSaving, resilientForDerived);
		if (result.hasFatalError())
			return result;
		// If we have initialized the content stamp with the 'null' stamp then we can't compare it with
		// the current stamp since a change executed later could have set a concrete stamp for the
		// current content
		// if (fChanged || (fContentStamp != IResource.NULL_STAMP && fContentStamp != fFile.getModificationStamp())
		if (fChanged || fContentStamp != fFile.getModificationStamp()) {
			result.addFatalError(Messages.format(
				RefactoringCoreMessages.TextChanges_error_content_changed,
				BasicElementLabels.getPathLabel(fFile.getFullPath(), false)
				));
		}
		return result;
	}

	public void dispose() {
		if (fFileBufferListener != null) {
			FileBuffers.getTextFileBufferManager().removeFileBufferListener(fFileBufferListener);
			// fix https://bugs.eclipse.org/bugs/show_bug.cgi?id=67821
			fFileBufferListener= null;
		}
		if (fDocumentListener != null) {
			getDocument().removeDocumentListener(fDocumentListener);
			// fix https://bugs.eclipse.org/bugs/show_bug.cgi?id=67821
			fDocumentListener= null;
		}
	}

	private void documentChanged() {
		fChanged= true;
		getDocument().removeDocumentListener(fDocumentListener);
		FileBuffers.getTextFileBufferManager().removeFileBufferListener(fFileBufferListener);
		fFileBufferListener= null;
		fDocumentListener= null;
	}
}

/**
 * Buffer validation state based on modification stamp.
 */
class ModificationStampValidationState extends BufferValidationState {

	private ModificationStamp fModificationStamp;

	public ModificationStampValidationState(IFile file) {
		super(file);
		fModificationStamp= getModificationStamp();
	}

	public RefactoringStatus isValid(boolean needsSaving, boolean resilientForDerived) throws CoreException {
		RefactoringStatus result= super.isValid(needsSaving, resilientForDerived);
		if (result.hasFatalError())
			return result;
		ModificationStamp currentStamp= getModificationStamp();
		// we don't need to check the kind here since the document stamp
		// and file stamp are in sync for documents implementing
		// IDocumentExtension4. If both are file stamps the file must
		// not be dirty
		if (fModificationStamp.getValue() != currentStamp.getValue()
			// we know here that the stamp value are equal. However, if
			// the stamp is a null stamp then the king must be equal as well.
			|| (fModificationStamp.isFileStamp()
				&& fModificationStamp.getValue() == IResource.NULL_STAMP
				&& !currentStamp.isFileStamp())
			|| (fModificationStamp.isDocumentStamp()
				&& fModificationStamp.getValue() == IDocumentExtension4.UNKNOWN_MODIFICATION_STAMP
				&& !currentStamp.isDocumentStamp())
			|| (fModificationStamp.isFileStamp()
				&& currentStamp.isFileStamp() && isDirty(fFile))) {
			result.addFatalError(Messages.format(
				RefactoringCoreMessages.TextChanges_error_content_changed,
				BasicElementLabels.getPathLabel(fFile.getFullPath(), false)
				));

		}
		return result;
	}
}

/*
class SavedBufferValidationState extends BufferValidationState {
	private long fModificationStamp;

	public SavedBufferValidationState(IFile file) {
		super(file);
		fModificationStamp= file.getModificationStamp();
	}

	public RefactoringStatus isValid(boolean needsSaving) {
		RefactoringStatus result= super.isValid(needsSaving);
		if (result.hasFatalError())
			return result;
		ModificationStamp currentStamp= getModificationStamp();
		if (fModificationStamp != currentStamp.value) {
			result.addFatalError(Messages.format(
				RefactoringCoreMessages.TextChanges_error_content_changed, //$NON-NLS-1$
				fFile.getFullPath().toString()
				));
		} else if (fFile.isReadOnly()) {
			result.addFatalError(Messages.format(
				RefactoringCoreMessages.TextChanges_error_read_only, //$NON-NLS-1$
				fFile.getFullPath().toString()
				));
		} else if (!fFile.isSynchronized(IResource.DEPTH_ZERO)) {
			result.addFatalError(Messages.format(
				RefactoringCoreMessages.TextChanges_error_outOfSync, //$NON-NLS-1$
				fFile.getFullPath().toString()
				));
		} else if (isDirty(fFile) && currentStamp.isFileStamp()){
			result.addFatalError(Messages.format(
				RefactoringCoreMessages.TextChanges_error_unsaved_changes, //$NON-NLS-1$
				fFile.getFullPath().toString()
				));
		}
		return result;
	}

}
*/
