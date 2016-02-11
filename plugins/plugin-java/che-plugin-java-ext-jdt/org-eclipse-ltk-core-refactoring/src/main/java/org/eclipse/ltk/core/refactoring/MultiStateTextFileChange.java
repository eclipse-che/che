/*******************************************************************************
 * Copyright (c) 2005, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.core.refactoring;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;

import org.eclipse.core.resources.IFile;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.LocationKind;

import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.edits.TextEditCopier;
import org.eclipse.text.edits.TextEditGroup;
import org.eclipse.text.edits.TextEditProcessor;
import org.eclipse.text.edits.UndoEdit;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.DocumentRewriteSession;
import org.eclipse.jface.text.DocumentRewriteSessionType;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension4;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IPositionUpdater;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;

import org.eclipse.ltk.internal.core.refactoring.BufferValidationState;
import org.eclipse.ltk.internal.core.refactoring.Changes;
import org.eclipse.ltk.internal.core.refactoring.ContentStamps;
import org.eclipse.ltk.internal.core.refactoring.Lock;
import org.eclipse.ltk.internal.core.refactoring.MultiStateUndoChange;
import org.eclipse.ltk.internal.core.refactoring.NonDeletingPositionUpdater;
import org.eclipse.ltk.internal.core.refactoring.RefactoringCorePlugin;

/**
 * A multi state text file change is a special change object that applies a sequence of {@link TextEdit
 * text edit trees} to a document. The multi state text file change manages the text edit trees.
 * <p>
 * A multi state text file change offers the ability to access the original content of
 * the document as well as creating a preview of the change. The edit
 * trees get copied when creating any kind of preview. Therefore no region
 * updating on the original edit trees takes place when requesting a preview
 * (for more information on region updating see class {@link TextEdit TextEdit}.
 * If region tracking is required for a preview it can be enabled via a call
 * to the method {@link #setKeepPreviewEdits(boolean) setKeepPreviewEdits}.
 * If enabled the multi state text file change keeps the copied edit trees executed for the
 * preview allowing clients to map an original edit to an executed edit. The
 * executed edit can then be used to determine its position in the preview.
 * </p>
 *
 * @since 3.2
 */
public class MultiStateTextFileChange extends TextEditBasedChange {

	private static final class ComposableBufferChange {

		private TextEdit fEdit;

		private List fGroups;

		private final TextEdit getEdit() {
			return fEdit;
		}

		private final List getGroups() {
			return fGroups;
		}

		private final void setEdit(final TextEdit edit) {
			Assert.isNotNull(edit);

			fEdit= edit;
		}

		private final void setGroups(final List groups) {
			Assert.isNotNull(groups);

			fGroups= groups;
		}
	}

	private static final class ComposableBufferChangeGroup extends TextEditBasedChangeGroup {

		private final Set fEdits= new HashSet();

		private ComposableBufferChangeGroup(final MultiStateTextFileChange change, final TextEditGroup group) {
			super(change, group);

			final TextEdit[] edits= group.getTextEdits();
			for (int index= 0; index < edits.length; index++)
				cacheEdit(edits[index]);
		}

		private final void cacheEdit(final TextEdit edit) {
			fEdits.add(edit);

			final TextEdit[] edits= edit.getChildren();
			for (int index= 0; index < edits.length; index++)
				cacheEdit(edits[index]);
		}

		private final boolean containsEdit(final TextEdit edit) {
			return fEdits.contains(edit);
		}

		private final Set getCachedEdits() {
			return fEdits;
		}
	}

	private static final class ComposableEditPosition extends Position {

		private String fText;

		private final String getText() {
			return fText;
		}

		private final void setText(final String text) {
			Assert.isNotNull(text);

			fText= text;
		}
	}

	private static final class ComposableUndoEdit {

		private ComposableBufferChangeGroup fGroup;

		private TextEdit fOriginal;

		private ReplaceEdit fUndo;

		private final ComposableBufferChangeGroup getGroup() {
			return fGroup;
		}

		private final TextEdit getOriginal() {
			return fOriginal;
		}

		private final String getOriginalText() {
			if (fOriginal instanceof ReplaceEdit) {
				return ((ReplaceEdit) getOriginal()).getText();
			} else if (fOriginal instanceof InsertEdit) {
				return ((InsertEdit) getOriginal()).getText();
			}
			return ""; //$NON-NLS-1$
		}

		private final ReplaceEdit getUndo() {
			return fUndo;
		}

		private final void setGroup(final ComposableBufferChangeGroup group) {
			Assert.isNotNull(group);

			fGroup= group;
		}

		private final void setOriginal(final TextEdit edit) {
			fOriginal= edit;
		}

		private final void setUndo(final ReplaceEdit undo) {
			Assert.isNotNull(undo);

			fUndo= undo;
		}
	}

	/** The position category for the resulting edit positions */
	private static final String COMPOSABLE_POSITION_CATEGORY= "ComposableEditPositionCategory_" + System.currentTimeMillis(); //$NON-NLS-1$

	/** The position category for the preview region range marker */
	private static final String MARKER_POSITION_CATEGORY= "MarkerPositionCategory_" + System.currentTimeMillis(); //$NON-NLS-1$

	/** The text file buffer */
	private ITextFileBuffer fBuffer;

	/** The last string obtained from a document */
	private String fCachedString;

	/**
	 * The internal change objects (element type:
	 * <code>ComposableBufferChange</code>)
	 */
	private final ArrayList fChanges= new ArrayList(4);

	/** The content stamp */
	private ContentStamp fContentStamp;

	/** The text edit copier */
	private TextEditCopier fCopier;

	/** The text file buffer reference count */
	private int fCount;

	/** The dirty flag */
	private boolean fDirty;

	/** The affected file */
	private IFile fFile;

	/** The save mode */
	private int fSaveMode= TextFileChange.KEEP_SAVE_STATE;

	/** The validation state */
	private BufferValidationState fValidationState;

	/**
	 * Creates a new composite text file change.
	 * <p>
	 * The default text type is <code>txt</code>.
	 * </p>
	 *
	 * @param name
	 *            the name of the composite text file change
	 * @param file
	 *            the text file to apply the change to
	 */
	public MultiStateTextFileChange(final String name, final IFile file) {
		super(name);

		Assert.isNotNull(file);
		fFile= file;

		setTextType("txt"); //$NON-NLS-1$
	}

	/**
	 * Acquires a document from the file buffer manager.
	 *
	 * @param monitor
	 *            the progress monitor to use
	 * @return the document
	 * @throws CoreException if the document could not successfully be acquired
	 */
	private IDocument acquireDocument(final IProgressMonitor monitor) throws CoreException {
		if (fCount > 0)
			return fBuffer.getDocument();

		final ITextFileBufferManager manager= FileBuffers.getTextFileBufferManager();
		final IPath path= fFile.getFullPath();

		manager.connect(path, LocationKind.IFILE, monitor);
		fCount++;

		fBuffer= manager.getTextFileBuffer(path, LocationKind.IFILE);

		final IDocument document= fBuffer.getDocument();
		fContentStamp= ContentStamps.get(fFile, document);

		return document;
	}

	/**
	 * Adds a new text change to this composite change.
	 * <p>
	 * The text change which is added is not changed in any way. Rather
	 * the contents of the text change are retrieved and stored internally
	 * in this composite text change.
	 * </p>
	 *
	 * @param change
	 *            the text change to add
	 */
	public final void addChange(final TextChange change) {
		Assert.isNotNull(change);

		final ComposableBufferChange result= new ComposableBufferChange();
		result.setEdit(change.getEdit());

		final TextEditBasedChangeGroup[] groups= change.getChangeGroups();
		final List list= new ArrayList(groups.length);

		for (int index= 0; index < groups.length; index++) {

			final TextEditBasedChangeGroup group= new ComposableBufferChangeGroup(this, groups[index].getTextEditGroup());
			list.add(group);

			addChangeGroup(group);
		}
		result.setGroups(list);
		fChanges.add(result);
	}

	// Copied from TextChange
	private TextEditProcessor createTextEditProcessor(ComposableBufferChange change, IDocument document, int flags, boolean preview) {
		List excludes= new ArrayList(0);
		for (final Iterator iterator= change.getGroups().iterator(); iterator.hasNext();) {
			TextEditBasedChangeGroup group= (TextEditBasedChangeGroup) iterator.next();
			if (!group.isEnabled())
				excludes.addAll(Arrays.asList(group.getTextEdits()));
		}

		if (preview) {
			fCopier= new TextEditCopier(change.getEdit());
			TextEdit copiedEdit= fCopier.perform();
			boolean keep= getKeepPreviewEdits();
			if (keep)
				flags= flags | TextEdit.UPDATE_REGIONS;
			LocalTextEditProcessor result= new LocalTextEditProcessor(document, copiedEdit, flags);
			result.setExcludes(mapEdits((TextEdit[]) excludes.toArray(new TextEdit[excludes.size()]), fCopier));
			if (!keep)
				fCopier= null;
			return result;
		} else {
			LocalTextEditProcessor result= new LocalTextEditProcessor(document, change.getEdit(), flags | TextEdit.UPDATE_REGIONS);
			result.setExcludes((TextEdit[]) excludes.toArray(new TextEdit[excludes.size()]));
			return result;
		}
	}

	/**
	 * Creates the corresponding text edit to the event.
	 *
	 * @param document
	 *            the document
	 * @param offset
	 *            the offset of the event
	 * @param length
	 *            the length of the event
	 * @param text
	 *            the text of the event
	 * @return the undo edit
	 */
	private ReplaceEdit createUndoEdit(final IDocument document, final int offset, final int length, final String text) {
		String currentText= null;
		try {
			currentText= document.get(offset, length);
		} catch (BadLocationException cannotHappen) {
			// Cannot happen
		}

		if (fCachedString != null && fCachedString.equals(currentText))
			currentText= fCachedString;
		else
			fCachedString= currentText;

		return new ReplaceEdit(offset, text != null ? text.length() : 0, currentText);
	}

	/*
	 * @see org.eclipse.ltk.core.refactoring.Change#dispose()
	 */
	public final void dispose() {
		if (fValidationState != null) {
			fValidationState.dispose();
		}
	}

	/*
	 * @see org.eclipse.ltk.core.refactoring.TextEditBasedChange#getCurrentContent(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public final String getCurrentContent(final IProgressMonitor monitor) throws CoreException {
		return getCurrentDocument(monitor).get();
	}

	/*
	 * @see org.eclipse.ltk.core.refactoring.TextEditBasedChange#getCurrentContent(org.eclipse.jface.text.IRegion,boolean,int,org.eclipse.core.runtime.IProgressMonitor)
	 */
	public final String getCurrentContent(final IRegion region, final boolean expand, final int surround, final IProgressMonitor monitor) throws CoreException {
		Assert.isNotNull(region);
		Assert.isTrue(surround >= 0);
		final IDocument document= getCurrentDocument(monitor);
		Assert.isTrue(document.getLength() >= region.getOffset() + region.getLength());
		return getContent(document, region, expand, surround);
	}

	/**
	 * Returns a document representing the current state of the buffer,
	 * prior to the application of the change.
	 * <p>
	 * The returned document should not be modified.
	 * </p>
	 *
	 * @param monitor
	 *            the progress monitor to use, or <code>null</code>
	 * @return the current document, or the empty document
	 * @throws CoreException
	 *             if no document could be acquired
	 */
	public final IDocument getCurrentDocument(IProgressMonitor monitor) throws CoreException {
		if (monitor == null)
			monitor= new NullProgressMonitor();
		IDocument result= null;
		monitor.beginTask("", 2); //$NON-NLS-1$
		try {
			result= acquireDocument(new SubProgressMonitor(monitor, 1));
		} finally {
			if (result != null)
				releaseDocument(result, new SubProgressMonitor(monitor, 1));
		}
		monitor.done();
		if (result == null)
			result= new Document();
		return result;
	}

	/*
	 * @see org.eclipse.ltk.core.refactoring.Change#getModifiedElement()
	 */
	public final Object getModifiedElement() {
		return fFile;
	}

	/*
	 * @see org.eclipse.ltk.core.refactoring.TextEditBasedChange#getPreviewContent(org.eclipse.ltk.core.refactoring.TextEditBasedChangeGroup[],org.eclipse.jface.text.IRegion,boolean,int,org.eclipse.core.runtime.IProgressMonitor)
	 */
	public final String getPreviewContent(final TextEditBasedChangeGroup[] groups, final IRegion region, final boolean expand, final int surround, final IProgressMonitor monitor) throws CoreException {

		final Set cachedGroups= new HashSet(Arrays.asList(groups));
		final IDocument document= new Document(getCurrentDocument(monitor).get());

		// Marks the region in the document to be previewed
		final Position range= new Position(region.getOffset(), region.getLength());
		try {

			ComposableBufferChange change= null;

			final TextEditBasedChangeGroup[] changedGroups= getChangeGroups();

			LinkedList compositeUndo= new LinkedList();
			for (int index= 0; index < fChanges.size(); index++) {
				change= (ComposableBufferChange) fChanges.get(index);

				TextEdit copy= null;
				try {
					// Have to use a copy
					fCopier= new TextEditCopier(change.getEdit());
					copy= fCopier.perform();

					// Create a mapping from the copied edits to its originals
					final Map originalMap= new HashMap();
					for (final Iterator outer= change.getGroups().iterator(); outer.hasNext();) {

						final ComposableBufferChangeGroup group= (ComposableBufferChangeGroup) outer.next();
						for (final Iterator inner= group.getCachedEdits().iterator(); inner.hasNext();) {

							final TextEdit originalEdit= (TextEdit) inner.next();
							final TextEdit copiedEdit= fCopier.getCopy(originalEdit);

							if (copiedEdit != null)
								originalMap.put(copiedEdit, originalEdit);
							else
								RefactoringCorePlugin.logErrorMessage("Could not find a copy for the indexed text edit " + originalEdit.toString()); //$NON-NLS-1$
						}
					}

					final ComposableBufferChangeGroup[] currentGroup= { null};
					final TextEdit[] currentEdit= { null};

					// Text edit processor which sets the change group and
					// current edit when processing
					final TextEditProcessor processor= new TextEditProcessor(document, copy, TextEdit.NONE) {

						protected final boolean considerEdit(final TextEdit edit) {

							final TextEdit originalEdit= (TextEdit) originalMap.get(edit);
							if (originalEdit != null) {

								currentEdit[0]= originalEdit;

								boolean found= false;
								for (int offset= 0; offset < changedGroups.length && !found; offset++) {

									final ComposableBufferChangeGroup group= (ComposableBufferChangeGroup) changedGroups[offset];
									if (group.containsEdit(originalEdit)) {

										currentGroup[0]= group;
										found= true;
									}
								}
								if (!found)
									currentGroup[0]= null;

							} else if (!(edit instanceof MultiTextEdit)) {
								RefactoringCorePlugin.logErrorMessage("Could not find the original of the copied text edit " + edit.toString()); //$NON-NLS-1$
							}
							return true;
						}
					};

					final LinkedList eventUndos= new LinkedList();

					// Listener to record the undos on the document (offsets
					// relative to the document event)
					final IDocumentListener listener= new IDocumentListener() {

						public final void documentAboutToBeChanged(final DocumentEvent event) {
							final ComposableUndoEdit edit= new ComposableUndoEdit();

							edit.setGroup(currentGroup[0]);
							edit.setOriginal(currentEdit[0]);
							edit.setUndo(createUndoEdit(document, event.getOffset(), event.getLength(), event.getText()));

							eventUndos.addFirst(edit);
						}

						public final void documentChanged(final DocumentEvent event) {
							// Do nothing
						}
					};

					try {
						// Record undos in LIFO order
						document.addDocumentListener(listener);
						processor.performEdits();
					} finally {
						document.removeDocumentListener(listener);
					}

					compositeUndo.addFirst(eventUndos);

				} finally {
					fCopier= null;
				}
			}

			final IPositionUpdater positionUpdater= new IPositionUpdater() {

				public final void update(final DocumentEvent event) {

					final int eventOffset= event.getOffset();
					final int eventLength= event.getLength();
					final int eventOldEndOffset= eventOffset + eventLength;
					final String eventText= event.getText();
					final int eventNewLength= eventText == null ? 0 : eventText.length();
					final int eventNewEndOffset= eventOffset + eventNewLength;
					final int deltaLength= eventNewLength - eventLength;

					try {

						final Position[] positions= event.getDocument().getPositions(COMPOSABLE_POSITION_CATEGORY);
						for (int index= 0; index < positions.length; index++) {

							final Position position= positions[index];
							if (position.isDeleted())
								continue;

							final int offset= position.getOffset();
							final int length= position.getLength();
							final int end= offset + length;

							if (offset > eventOldEndOffset) {
								// position comes way after change - shift
								position.setOffset(offset + deltaLength);
							} else if (end < eventOffset) {
								// position comes way before change - leave
								// alone
							} else if (offset == eventOffset) {
								// leave alone, since the edits are overlapping
							} else if (offset <= eventOffset && end >= eventOldEndOffset) {
								// event completely internal to the position
								// -
								// adjust length
								position.setLength(length + deltaLength);
							} else if (offset < eventOffset) {
								// event extends over end of position - include
								// the
								// replacement text into the position
								position.setLength(eventNewEndOffset - offset);
							} else if (end > eventOldEndOffset) {
								// event extends from before position into it -
								// adjust
								// offset and length, including the replacement
								// text into
								// the position
								position.setOffset(eventOffset);
								int deleted= eventOldEndOffset - offset;
								position.setLength(length - deleted + eventNewLength);
							} else {
								// event comprises the position - keep it at the
								// same
								// position, but always inside the replacement
								// text
								int newOffset= Math.min(offset, eventNewEndOffset);
								int newEndOffset= Math.min(end, eventNewEndOffset);
								position.setOffset(newOffset);
								position.setLength(newEndOffset - newOffset);
							}
						}
					} catch (BadPositionCategoryException exception) {
						// ignore and return
					}
				}
			};

			try {

				document.addPositionCategory(COMPOSABLE_POSITION_CATEGORY);
				document.addPositionUpdater(positionUpdater);

				// Apply undos in LIFO order to get to the original document
				// Track the undos of edits which are in change groups to be
				// previewed and insert
				// Undo edits for them (corresponding to the linearized net
				// effect on the original document)
				final LinkedList undoQueue= new LinkedList();
				for (final Iterator outer= compositeUndo.iterator(); outer.hasNext();) {
					for (final Iterator inner= ((List) outer.next()).iterator(); inner.hasNext();) {

						final ComposableUndoEdit edit= (ComposableUndoEdit) inner.next();
						final ReplaceEdit undo= edit.getUndo();

						final int offset= undo.getOffset();
						final int length= undo.getLength();
						final String text= undo.getText();

						ComposableEditPosition position= new ComposableEditPosition();
						if (cachedGroups.contains(edit.getGroup())) {

							if (text == null || text.equals("")) { //$NON-NLS-1$
								position.offset= offset;
								if (length != 0) {
									// Undo is a delete, create final insert
									// edit
									position.length= 0;
									position.setText(edit.getOriginalText());
								} else
									RefactoringCorePlugin.logErrorMessage("Dubious undo edit found: " + undo.toString()); //$NON-NLS-1$

							} else if (length == 0) {
								position.offset= offset;
								// Undo is an insert, create final delete
								// edit
								position.setText(""); //$NON-NLS-1$
								position.length= text.length();
							} else {
								// Undo is a replace, create final replace edit
								position.offset= offset;
								position.length= length;
								position.setText(edit.getOriginalText());
							}

							document.addPosition(COMPOSABLE_POSITION_CATEGORY, position);
						}

						position= new ComposableEditPosition();
						position.offset= undo.getOffset();
						position.length= undo.getLength();
						position.setText(undo.getText());

						undoQueue.add(position);
					}

					for (final Iterator iterator= undoQueue.iterator(); iterator.hasNext();) {
						final ComposableEditPosition position= (ComposableEditPosition) iterator.next();

						document.replace(position.offset, position.length, position.getText());
						iterator.remove();
					}
				}

				// Use a simple non deleting position updater for the range
				final IPositionUpdater markerUpdater= new NonDeletingPositionUpdater(MARKER_POSITION_CATEGORY);

				try {

					final Position[] positions= document.getPositions(COMPOSABLE_POSITION_CATEGORY);
					document.addPositionCategory(MARKER_POSITION_CATEGORY);
					document.addPositionUpdater(markerUpdater);
					document.addPosition(MARKER_POSITION_CATEGORY, range);

					for (int index= 0; index < positions.length; index++) {
						final ComposableEditPosition position= (ComposableEditPosition) positions[index];

						document.replace(position.offset, position.length, position.getText() != null ? position.getText() : ""); //$NON-NLS-1$
					}

				} catch (BadPositionCategoryException exception) {
					RefactoringCorePlugin.log(exception);
				} finally {
					document.removePositionUpdater(markerUpdater);
					try {
						document.removePosition(MARKER_POSITION_CATEGORY, range);
						document.removePositionCategory(MARKER_POSITION_CATEGORY);
					} catch (BadPositionCategoryException exception) {
						// Cannot happen
					}
				}
			} catch (BadPositionCategoryException exception) {
				RefactoringCorePlugin.log(exception);
			} finally {
				document.removePositionUpdater(positionUpdater);
				try {
					document.removePositionCategory(COMPOSABLE_POSITION_CATEGORY);
				} catch (BadPositionCategoryException exception) {
					RefactoringCorePlugin.log(exception);
				}
			}

			return getContent(document, new Region(range.offset, range.length), expand, surround);

		} catch (MalformedTreeException exception) {
			RefactoringCorePlugin.log(exception);
		} catch (BadLocationException exception) {
			RefactoringCorePlugin.log(exception);
		}
		return getPreviewDocument(monitor).get();
	}

	/*
	 * @see org.eclipse.ltk.core.refactoring.TextEditBasedChange#getPreviewContent(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public final String getPreviewContent(final IProgressMonitor monitor) throws CoreException {
		return getPreviewDocument(monitor).get();
	}

	/**
	 * Returns a document representing the preview of the refactored buffer,
	 * after the application of the change object.
	 *
	 * @param monitor
	 *            the progress monitor to use, or <code>null</code>
	 * @return the preview document, or an empty document
	 * @throws CoreException
	 *             if no document could be acquired
	 */
	public final IDocument getPreviewDocument(IProgressMonitor monitor) throws CoreException {
		if (monitor == null)
			monitor= new NullProgressMonitor();

		IDocument result= null;
		IDocument document= null;

		try {

			document= acquireDocument(new SubProgressMonitor(monitor, 1));
			if (document != null) {
				result= new Document(document.get());

				performChanges(result, null, true);
			}

		} catch (BadLocationException exception) {
			throw Changes.asCoreException(exception);
		} finally {
			if (document != null) {
				releaseDocument(document, new SubProgressMonitor(monitor, 1));
			}
			monitor.done();
		}
		if (result == null)
			result= new Document();
		return result;
	}

	/**
	 * Returns the save mode of this change.
	 *
	 * @return the save mode
	 */
	public final int getSaveMode() {
		return fSaveMode;
	}

	/*
	 * @see org.eclipse.ltk.core.refactoring.Change#initializeValidationData(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public final void initializeValidationData(IProgressMonitor monitor) {
		if (monitor == null)
			monitor= new NullProgressMonitor();
		monitor.beginTask("", 1); //$NON-NLS-1$
		try {
			fValidationState= BufferValidationState.create(fFile);
		} finally {
			monitor.worked(1);
		}
	}

	/*
	 * @see org.eclipse.ltk.core.refactoring.Change#isValid(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public final RefactoringStatus isValid(IProgressMonitor monitor) throws CoreException, OperationCanceledException {
		if (monitor == null)
			monitor= new NullProgressMonitor();
		monitor.beginTask("", 1); //$NON-NLS-1$
		try {
			if (fValidationState == null)
				throw new CoreException(new Status(IStatus.ERROR, RefactoringCorePlugin.getPluginId(), "MultiStateTextFileChange has not been initialialized")); //$NON-NLS-1$


			final ITextFileBuffer buffer= FileBuffers.getTextFileBufferManager().getTextFileBuffer(fFile.getFullPath(), LocationKind.IFILE);
			fDirty= buffer != null && buffer.isDirty();

			final RefactoringStatus status= fValidationState.isValid(needsSaving());
			if (needsSaving()) {
				status.merge(Changes.validateModifiesFiles(new IFile[] { fFile }));
			} else {
				// we are reading the file. So it should be at least in sync
				status.merge(Changes.checkInSync(new IFile[] { fFile }));
			}

			return status;
		} finally {
			monitor.done();
		}
	}

	/**
	 * Does the change need saving?
	 *
	 * @return <code>true</code> if it needs saving, <code>false</code>
	 *         otherwise
	 */
	public final boolean needsSaving() {
		return (fSaveMode & TextFileChange.FORCE_SAVE) != 0 || !fDirty && (fSaveMode & TextFileChange.KEEP_SAVE_STATE) != 0;
	}

	/*
	 * @see org.eclipse.ltk.core.refactoring.Change#perform(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public final Change perform(final IProgressMonitor monitor) throws CoreException {
		monitor.beginTask("", 3); //$NON-NLS-1$

		IDocument document= null;

		try {
			document= acquireDocument(new SubProgressMonitor(monitor, 1));

			final LinkedList undoList= new LinkedList();
			performChanges(document, undoList, false);

			if (needsSaving())
				fBuffer.commit(new SubProgressMonitor(monitor, 1), false);

			return new MultiStateUndoChange(getName(), fFile, (UndoEdit[]) undoList.toArray(new UndoEdit[undoList.size()]), fContentStamp, fSaveMode);

		} catch (BadLocationException exception) {
			throw Changes.asCoreException(exception);
		} finally {
			if (document != null) {
				releaseDocument(document, new SubProgressMonitor(monitor, 1));
			}
			monitor.done();
		}
	}

	/**
	 * Performs the changes on the specified document.
	 *
	 * @param document
	 *            the document to perform the changes on
	 * @param undoList
	 *            the undo list, or <code>null</code> to discard the undos
	 * @param preview
	 *            <code>true</code> if the changes are performed for preview,
	 *            <code>false</code> otherwise
	 * @throws BadLocationException
	 *             if the edit tree could not be applied
	 */
	private void performChanges(final IDocument document, final LinkedList undoList, final boolean preview) throws BadLocationException {
		if (! fBuffer.isSynchronizationContextRequested()) {
			performChangesInSynchronizationContext(document, undoList, preview);
			return;
		}
		
		ITextFileBufferManager fileBufferManager= FileBuffers.getTextFileBufferManager();
		
		/** The lock for waiting for computation in the UI thread to complete. */
		final Lock completionLock= new Lock();
		final BadLocationException[] exception= new BadLocationException[1];
		Runnable runnable= new Runnable() {
			public void run() {
				synchronized (completionLock) {
					try {
						performChangesInSynchronizationContext(document, undoList, preview);
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
	}

	private void performChangesInSynchronizationContext(final IDocument document, final LinkedList undoList, final boolean preview) throws BadLocationException {
		DocumentRewriteSession session= null;
		try {
			if (document instanceof IDocumentExtension4)
				session= ((IDocumentExtension4) document).startRewriteSession(DocumentRewriteSessionType.UNRESTRICTED);
	
			for (final Iterator iterator= fChanges.iterator(); iterator.hasNext();) {
				final ComposableBufferChange change= (ComposableBufferChange) iterator.next();
	
				final UndoEdit edit= createTextEditProcessor(change, document, undoList != null ? TextEdit.CREATE_UNDO : TextEdit.NONE, preview).performEdits();
				if (undoList != null)
					undoList.addFirst(edit);
			}
			
		} finally {
			if (session != null)
				((IDocumentExtension4) document).stopRewriteSession(session);
		}
	}

	/**
	 * Releases the document.
	 *
	 * @param document
	 *            the document to release
	 * @param monitor
	 *            the progress monitor
	 * @throws CoreException if the document could not successfully be released
	 */
	private void releaseDocument(final IDocument document, final IProgressMonitor monitor) throws CoreException {
		Assert.isTrue(fCount > 0);

		if (fCount == 1)
			FileBuffers.getTextFileBufferManager().disconnect(fFile.getFullPath(), LocationKind.IFILE, monitor);

		fCount--;
	}

	/*
	 * @see org.eclipse.ltk.core.refactoring.TextEditBasedChange#setKeepPreviewEdits(boolean)
	 */
	public final void setKeepPreviewEdits(final boolean keep) {
		super.setKeepPreviewEdits(keep);

		if (!keep)
			fCopier= null;
	}

	/**
	 * Sets the save mode.
	 *
	 * @param mode
	 *            The mode to set
	 */
	public final void setSaveMode(final int mode) {
		fSaveMode= mode;
	}
}
