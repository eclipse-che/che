/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.core.refactoring;

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

import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.edits.UndoEdit;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.link.LinkedModeModel;

import org.eclipse.ltk.internal.core.refactoring.BufferValidationState;
import org.eclipse.ltk.internal.core.refactoring.Changes;
import org.eclipse.ltk.internal.core.refactoring.ContentStamps;
import org.eclipse.ltk.internal.core.refactoring.Lock;
import org.eclipse.ltk.internal.core.refactoring.RefactoringCorePlugin;

/**
 * A change to perform the reverse change of a {@link TextFileChange}.
 * <p>
 * This class is not intended to be instantiated by clients. It is
 * usually created by a <code>TextFileChange</code> object.
 * </p>
 * <p>
 * The class should be subclassed by clients also subclassing <code>
 * TextFileChange</code> to provide a proper undo change object.
 * </p>
 * @since 3.0
 */
public class UndoTextFileChange extends Change {

	private String fName;
	private UndoEdit fUndo;
	private IFile fFile;
	private ContentStamp fContentStampToRestore;
	private int fSaveMode;

	private boolean fDirty;
	private BufferValidationState fValidationState;

	/**
	 * Create a new undo text file change object.
	 *
	 * @param name the human readable name of the change
	 * @param file the file the change is working on
	 * @param stamp the content stamp to restore when the undo is executed
	 * @param undo the edit representing the undo modifications
	 * @param saveMode the save mode as specified by {@link TextFileChange}
	 *
	 * @see TextFileChange#KEEP_SAVE_STATE
	 * @see TextFileChange#FORCE_SAVE
	 * @see TextFileChange#LEAVE_DIRTY
	 */
	protected UndoTextFileChange(String name, IFile file, UndoEdit undo, ContentStamp stamp, int saveMode) {
		Assert.isNotNull(name);
		Assert.isNotNull(file);
		Assert.isNotNull(undo);
		fName= name;
		fFile= file;
		fUndo= undo;
		fContentStampToRestore= stamp;
		fSaveMode= saveMode;
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
	public int getSaveMode() {
		return fSaveMode;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getName() {
		return fName;
	}

	/**
	 * Hook to create an undo change for the given undo edit. This hook
	 * gets called while performing the change to construct the corresponding
	 * undo change object.
	 * <p>
	 * Subclasses may override it to create a different undo change.
	 * </p>
	 * @param edit the {@link UndoEdit undo edit} to create a undo change for
	 * @param stampToRestore the content stamp to restore when the undo
	 *  edit is executed.
	 *
	 * @return the undo change
	 *
	 * @throws CoreException if an undo change can't be created
	 */
	protected Change createUndoChange(UndoEdit edit, ContentStamp stampToRestore) throws CoreException {
		return new UndoTextFileChange(getName(), fFile, edit, stampToRestore, fSaveMode);
	}

	/**
	 * {@inheritDoc}
	 */
	public Object getModifiedElement() {
		return fFile;
	}

	/**
	 * {@inheritDoc}
	 */
	public Object[] getAffectedObjects() {
		Object modifiedElement= getModifiedElement();
		if (modifiedElement == null)
			return null;
		return new Object[] { modifiedElement };
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
				throw new CoreException(new Status(IStatus.ERROR, RefactoringCorePlugin.getPluginId(), "UndoTextFileChange has not been initialialized")); //$NON-NLS-1$

			ITextFileBuffer buffer= FileBuffers.getTextFileBufferManager().getTextFileBuffer(fFile.getFullPath(), LocationKind.IFILE);
			fDirty= buffer != null && buffer.isDirty();
			return fValidationState.isValid(needsSaving(), true);
		} finally {
			pm.done();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Change perform(IProgressMonitor pm) throws CoreException {
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

			boolean[] setContentStampSuccess= { false };
			UndoEdit redo= performEdits(buffer, document, setContentStampSuccess);

			if (needsSaving()) {
				buffer.commit(pm, false);
				if (!setContentStampSuccess[0]) {
					// We weren't able to restore document stamp.
					// Since we save restore the file stamp instead
					ContentStamps.set(fFile, fContentStampToRestore);
				}
			}
			return createUndoChange(redo, currentStamp);
		} catch (BadLocationException e) {
			if (fValidationState == null || !fValidationState.wasDerived())
				throw Changes.asCoreException(e);
			else
				return new NullChange();
		} catch (MalformedTreeException e) {
			if (fValidationState == null || !fValidationState.wasDerived())
				throw Changes.asCoreException(e);
			else
				return new NullChange();
		} catch (CoreException e) {
			if (fValidationState == null || !fValidationState.wasDerived())
				throw e;
			else
				return new NullChange();
		} finally {
			if (buffer != null)
				manager.disconnect(fFile.getFullPath(), LocationKind.IFILE, new SubProgressMonitor(pm, 1));
		}
	}

	private UndoEdit performEdits(ITextFileBuffer buffer, final IDocument document, final boolean[] setContentStampSuccess) throws MalformedTreeException, BadLocationException, CoreException {
		if (! buffer.isSynchronizationContextRequested()) {
			return doPerformEdits(document, setContentStampSuccess);
		}
		
		ITextFileBufferManager fileBufferManager= FileBuffers.getTextFileBufferManager();
		
		/** The lock for waiting for computation in the UI thread to complete. */
		final Lock completionLock= new Lock();
		final UndoEdit[] result= new UndoEdit[1];
		final BadLocationException[] badLocationException= new BadLocationException[1];
		final MalformedTreeException[] malformedTreeException= new MalformedTreeException[1];
		final CoreException[] coreException= new CoreException[1];
		Runnable runnable= new Runnable() {
			public void run() {
				synchronized (completionLock) {
					try {
						result[0]= doPerformEdits(document, setContentStampSuccess);
					} catch (BadLocationException e) {
						badLocationException[0]= e;
					} catch (MalformedTreeException e) {
						malformedTreeException[0]= e;
					} catch (CoreException e) {
						coreException[0]= e;
					} finally {
						completionLock.fDone= true;
						completionLock.notifyAll();
					}
				}
			}
		};
		
		synchronized (completionLock) {
			fileBufferManager.execute(runnable);
			while (! completionLock.fDone) {
				try {
					completionLock.wait(500);
				} catch (InterruptedException x) {
				}
			}
		}
		
		if (badLocationException[0] != null) {
			throw badLocationException[0];
		} else if (malformedTreeException[0] != null) {
			throw malformedTreeException[0];
		} else if (coreException[0] != null) {
			throw coreException[0];
		}
		return result[0];
	}

	private UndoEdit doPerformEdits(IDocument document, boolean[] setContentStampSuccess) throws MalformedTreeException, BadLocationException, CoreException {
		// perform the changes
		LinkedModeModel.closeAllModels(document);
		UndoEdit redo= fUndo.apply(document, TextEdit.CREATE_UNDO);

		// try to restore the document content stamp
		setContentStampSuccess[0]= ContentStamps.set(document, fContentStampToRestore);
		return redo;
	}

	/**
	 * {@inheritDoc}
	 */
	public void dispose() {
		if (fValidationState != null) {
			fValidationState.dispose();
		}
	}

	private boolean needsSaving() {
		return (fSaveMode & TextFileChange.FORCE_SAVE) != 0 || !fDirty && (fSaveMode & TextFileChange.KEEP_SAVE_STATE) != 0;
	}
}
