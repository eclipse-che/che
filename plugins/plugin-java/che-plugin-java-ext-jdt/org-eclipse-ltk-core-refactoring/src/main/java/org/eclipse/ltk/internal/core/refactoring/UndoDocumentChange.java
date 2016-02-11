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
package org.eclipse.ltk.internal.core.refactoring;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;

import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.edits.UndoEdit;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

public class UndoDocumentChange extends Change {

	private String fName;
	private UndoEdit fUndo;
	private IDocument fDocument;
	private int fLength;

	public UndoDocumentChange(String name, IDocument document, UndoEdit undo) {
		fName= name;
		fUndo= undo;
		fDocument= document;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getName() {
		return fName;
	}

	/**
	 * {@inheritDoc}
	 */
	public Object getModifiedElement() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public void initializeValidationData(IProgressMonitor pm) {
		fLength= fDocument.getLength();
	}

	/**
	 * {@inheritDoc}
	 */
	public RefactoringStatus isValid(IProgressMonitor pm) throws CoreException {
		if (pm == null)
			pm= new NullProgressMonitor();
		pm.beginTask("", 1); //$NON-NLS-1$
		RefactoringStatus result= TextChanges.isValid(fDocument, fLength);
		pm.worked(1);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public Change perform(IProgressMonitor pm) throws CoreException {
		try {
			UndoEdit redo= performEdits();
			Change result= new UndoDocumentChange(getName(), fDocument, redo);
			return result;
		} catch (MalformedTreeException e) {
			throw Changes.asCoreException(e);
		} catch (BadLocationException e) {
			throw Changes.asCoreException(e);
		}
	}

	private UndoEdit performEdits() throws BadLocationException, MalformedTreeException {
		ITextFileBufferManager fileBufferManager= FileBuffers.getTextFileBufferManager();
		
		ITextFileBuffer fileBuffer= fileBufferManager.getTextFileBuffer(fDocument);
		if (fileBuffer == null || ! fileBuffer.isSynchronizationContextRequested()) {
			return fUndo.apply(fDocument, TextEdit.CREATE_UNDO);
		}
		
		/** The lock for waiting for computation in the UI thread to complete. */
		final Lock completionLock= new Lock();
		final UndoEdit[] result= new UndoEdit[1];
		final BadLocationException[] exception= new BadLocationException[1];
		Runnable runnable= new Runnable() {
			public void run() {
				synchronized (completionLock) {
					try {
						result[0]= fUndo.apply(fDocument, TextEdit.CREATE_UNDO);
					} catch (BadLocationException e) {
						exception[0]= e;
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
		
		if (exception[0] != null) {
			throw exception[0];
		}
		
		return result[0];
	}

}
