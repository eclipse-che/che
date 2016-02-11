/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.core.refactoring;

import java.util.LinkedList;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;

import org.eclipse.core.resources.IFile;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.LocationKind;

import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.edits.UndoEdit;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.ContentStamp;
import org.eclipse.ltk.core.refactoring.NullChange;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextFileChange;

/**
 * A change to perform the reverse change of a {@link org.eclipse.ltk.core.refactoring.MultiStateTextFileChange}.
 * <p>
 * This class is not intended to be instantiated by clients. It is usually
 * created by a <code>MultiStateTextFileChange</code> object.
 * </p>
 * <p>
 * The class should be subclassed by clients also subclassing <code>
 * MultiStateTextFileChange</code>
 * to provide a proper undo change object.
 * </p>
 *
 * @since 3.2
 */
public class MultiStateUndoChange extends Change {

	private ContentStamp fContentStampToRestore;

	private boolean fDirty;

	private IFile fFile;

	private String fName;

	private int fSaveMode;

	private UndoEdit[] fUndos;

	private BufferValidationState fValidationState;

	/**
	 * Create a new multi state undo change object.
	 *
	 * @param name
	 *            the human readable name of the change
	 * @param file
	 *            the file the change is working on
	 * @param stamp
	 *            the content stamp to restore when the undo is executed
	 * @param undos
	 *            the edit representing the undo modifications
	 * @param saveMode
	 *            the save mode as specified by {@link TextFileChange}
	 *
	 * @see TextFileChange#KEEP_SAVE_STATE
	 * @see TextFileChange#FORCE_SAVE
	 * @see TextFileChange#LEAVE_DIRTY
	 */
	public MultiStateUndoChange(String name, IFile file, UndoEdit[] undos, ContentStamp stamp, int saveMode) {
		Assert.isNotNull(name);
		Assert.isNotNull(file);
		Assert.isNotNull(undos);
		fName= name;
		fFile= file;
		fUndos= undos;
		fContentStampToRestore= stamp;
		fSaveMode= saveMode;
	}

	/**
	 * Hook to create an undo change for the given undo edit. This hook gets
	 * called while performing the change to construct the corresponding undo
	 * change object.
	 * <p>
	 * Subclasses may override it to create a different undo change.
	 * </p>
	 *
	 * @param edits
	 *            the {@link UndoEdit undo edit} to create a undo change for
	 * @param stampToRestore
	 *            the content stamp to restore when the undo edit is executed.
	 *
	 * @return the undo change
	 *
	 * @throws CoreException
	 *             if an undo change can't be created
	 */
	protected Change createUndoChange(UndoEdit[] edits, ContentStamp stampToRestore) throws CoreException {
		return new MultiStateUndoChange(getName(), fFile, edits, stampToRestore, fSaveMode);
	}

	/**
	 * {@inheritDoc}
	 */
	public void dispose() {
		if (fValidationState != null) {
			fValidationState.dispose();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public final Object[] getAffectedObjects() {
		Object modifiedElement= getModifiedElement();
		if (modifiedElement == null)
			return null;
		return new Object[] { modifiedElement};
	}

	/**
	 * {@inheritDoc}
	 */
	public final Object getModifiedElement() {
		return fFile;
	}

	/**
	 * {@inheritDoc}
	 */
	public final String getName() {
		return fName;
	}

	/**
	 * Returns the change's save mode.
	 *
	 * @return the change's save mode
	 *
	 * @see TextFileChange#KEEP_SAVE_STATE
	 * @see TextFileChange#FORCE_SAVE
	 * @see TextFileChange#LEAVE_DIRTY
	 */
	public final int getSaveMode() {
		return fSaveMode;
	}

	/**
	 * {@inheritDoc}
	 */
	public void initializeValidationData(IProgressMonitor pm) {
		if (pm == null)
			pm= new NullProgressMonitor();
		pm.beginTask("", 1); //$NON-NLS-1$
		try {
			fValidationState= BufferValidationState.create(fFile);
		} finally {
			pm.done();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public RefactoringStatus isValid(IProgressMonitor pm) throws CoreException {
		if (pm == null)
			pm= new NullProgressMonitor();
		pm.beginTask("", 1); //$NON-NLS-1$
		try {
			if (fValidationState == null)
				throw new CoreException(new Status(IStatus.ERROR, RefactoringCorePlugin.getPluginId(), "MultiStateUndoChange has not been initialialized")); //$NON-NLS-1$

			ITextFileBuffer buffer= FileBuffers.getTextFileBufferManager().getTextFileBuffer(fFile.getFullPath(), LocationKind.IFILE);
			fDirty= buffer != null && buffer.isDirty();
			return fValidationState.isValid(needsSaving(), true);
		} finally {
			pm.done();
		}
	}

	public final boolean needsSaving() {
		return (fSaveMode & TextFileChange.FORCE_SAVE) != 0 || !fDirty && (fSaveMode & TextFileChange.KEEP_SAVE_STATE) != 0;
	}

	/**
	 * {@inheritDoc}
	 */
	public Change perform(IProgressMonitor pm) throws CoreException {
		if (fValidationState == null || fValidationState.isValid(needsSaving(), false).hasFatalError())
			return new NullChange();
		if (pm == null)
			pm= new NullProgressMonitor();
		ITextFileBufferManager manager= FileBuffers.getTextFileBufferManager();
		pm.beginTask("", 2); //$NON-NLS-1$
		ITextFileBuffer buffer= null;
		try {
			manager.connect(fFile.getFullPath(), LocationKind.IFILE, new SubProgressMonitor(pm, 1));
			buffer= manager.getTextFileBuffer(fFile.getFullPath(), LocationKind.IFILE);
			IDocument document= buffer.getDocument();
			ContentStamp currentStamp= ContentStamps.get(fFile, document);
			// perform the changes
			LinkedList list= new LinkedList();
			for (int index= 0; index < fUndos.length; index++) {
				UndoEdit edit= fUndos[index];
				UndoEdit redo= edit.apply(document, TextEdit.CREATE_UNDO);
				list.addFirst(redo);

			}

			// try to restore the document content stamp
			boolean success= ContentStamps.set(document, fContentStampToRestore);
			if (needsSaving()) {
				buffer.commit(pm, false);
				if (!success) {
					// We weren't able to restore document stamp.
					// Since we save restore the file stamp instead
					ContentStamps.set(fFile, fContentStampToRestore);
				}
			}
			return createUndoChange((UndoEdit[]) list.toArray(new UndoEdit[list.size()]), currentStamp);
		} catch (BadLocationException e) {
			throw Changes.asCoreException(e);
		} finally {
			if (buffer != null)
				manager.disconnect(fFile.getFullPath(), LocationKind.IFILE, new SubProgressMonitor(pm, 1));
		}
	}
}
