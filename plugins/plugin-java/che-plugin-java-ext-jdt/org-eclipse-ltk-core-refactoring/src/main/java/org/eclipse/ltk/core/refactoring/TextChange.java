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
package org.eclipse.ltk.core.refactoring;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.edits.TextEditCopier;
import org.eclipse.text.edits.TextEditGroup;
import org.eclipse.text.edits.TextEditProcessor;
import org.eclipse.text.edits.UndoEdit;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentRewriteSession;
import org.eclipse.jface.text.DocumentRewriteSessionType;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension4;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.link.LinkedModeModel;

import org.eclipse.ltk.internal.core.refactoring.Changes;

/**
 * A text change is a special change object that applies a {@link TextEdit
 * text edit tree} to a document. The text change manages the text edit tree.
 * Access to the document must be provided by concrete subclasses via the method
 * {@link #acquireDocument(IProgressMonitor) aquireDocument},
 * {@link #commit(IDocument document, IProgressMonitor pm) commitDocument}, and
 * {@link #releaseDocument(IDocument, IProgressMonitor) releaseDocument}.
 * <p>
 * A text change offers the ability to access the original content of
 * the document as well as creating a preview of the change. The edit
 * tree gets copied when creating any kind of preview. Therefore no region
 * updating on the original edit tree takes place when requesting a preview
 * (for more information on region updating see class {@link TextEdit TextEdit}.
 * If region tracking is required for a preview, it can be enabled via a call
 * to the method {@link #setKeepPreviewEdits(boolean) setKeepPreviewEdits}.
 * If enabled, the text change keeps the copied edit tree executed for the
 * preview, allowing clients to map an original edit to an executed edit. The
 * executed edit can then be used to determine its position in the preview.
 * </p>
 * <p>
 * Note: this class is not intended to be subclassed outside the refactoring
 * framework.
 * </p>
 *
 * @since 3.0
 *
 * @noextend This class is not intended to be subclassed by clients.
 */
public abstract class TextChange extends TextEditBasedChange {

	private TextEdit fEdit;
	private TextEditCopier fCopier;

	/**
	 * Creates a new text change with the specified name.  The name is a
	 * human-readable value that is displayed to users.  The name does not
	 * need to be unique, but it must not be <code>null</code>.
	 * <p>
	 * The text type of this text change is set to <code>txt</code>.
	 * </p>
	 *
	 * @param name the name of the text change
	 *
	 * @see #setTextType(String)
	 */
	protected TextChange(String name) {
		super(name);
	}

	//---- Edit management -----------------------------------------------

	/**
	 * Sets the root text edit that should be applied to the
	 * document represented by this text change.
	 *
	 * @param edit the root text edit. The root text edit
	 *  can only be set once.
	 */
	public void setEdit(TextEdit edit) {
		Assert.isTrue(fEdit == null, "Root edit can only be set once"); //$NON-NLS-1$
		Assert.isTrue(edit != null);
		fEdit= edit;
	}

	/**
	 * Returns the root text edit.
	 *
	 * @return the root text edit or <code>null</code> if no root edit has been
	 *  set
	 */
	public TextEdit getEdit() {
		return fEdit;
	}

	/**
	 * Adds a {@link TextEditGroup text edit group}. This method is a convenience
	 * method for calling <code>change.addTextEditChangeGroup(new
	 * TextEditChangeGroup(change, group));</code>.
	 *
	 * @param group the text edit group to add
	 */
	public void addTextEditGroup(TextEditGroup group) {
		addTextEditChangeGroup(new TextEditChangeGroup(this, group));
	}

	/**
	 * Adds a {@link TextEditChangeGroup text edit change group}. Calling this method
	 * requires that a root edit has been set via the method {@link #setEdit(TextEdit)
	 * setEdit}. The edits managed by the given text edit change group must be part of
	 * the change's root edit.
	 *
	 * @param group the text edit change group to add
	 */
	public void addTextEditChangeGroup(TextEditChangeGroup group) {
		Assert.isTrue(fEdit != null, "Can only add a description if a root edit exists"); //$NON-NLS-1$
		addChangeGroup(group);
	}

	/**
	 * Returns the {@link TextEditChangeGroup text edit change groups} managed by this
	 * text change.
	 *
	 * @return the text edit change groups
	 */
	public TextEditChangeGroup[] getTextEditChangeGroups() {
		final TextEditBasedChangeGroup[] groups= getChangeGroups();
		final TextEditChangeGroup[] result= new TextEditChangeGroup[groups.length];
		System.arraycopy(groups, 0, result, 0, groups.length);
		return result;
	}

	/**
	 * Adds the given edit to the edit tree. The edit is added as a top
	 * level edit.
	 *
	 * @param edit the text edit to add
	 *
	 * @throws MalformedTreeException if the edit can't be added. Reason
	 *  is that is overlaps with an already existing edit
	 *
	 * @since 3.1
	 */
	public void addEdit(TextEdit edit) throws MalformedTreeException {
		Assert.isTrue(fEdit != null, "root must exist to add an edit"); //$NON-NLS-1$
		fEdit.addChild(edit);
	}

	//---- Document management -----------------------------------------------

	/**
	 * Acquires a reference to the document to be changed by this text
	 * change. A document acquired by this call <em>MUST</em> be released
	 * via a call to {@link #releaseDocument(IDocument, IProgressMonitor)}.
	 * <p>
	 * The method <code>releaseDocument</code> must be called as many times as
	 * <code>aquireDocument</code> has been called.
	 * </p>
	 *
	 * @param pm a progress monitor
	 *
	 * @return a reference to the document to be changed
	 *
	 * @throws CoreException if the document can't be acquired
	 */
	protected abstract IDocument acquireDocument(IProgressMonitor pm) throws CoreException;

	/**
	 * Commits the document acquired via a call to {@link #acquireDocument(IProgressMonitor)
	 * aquireDocument}. It is up to the implementors of this method to decide what committing
	 * a document means. Typically, the content of the document is written back to the file
	 * system.
	 *
	 * @param document the document to commit
	 * @param pm a progress monitor
	 *
	 * @throws CoreException if the document can't be committed
	 */
	protected abstract void commit(IDocument document, IProgressMonitor pm) throws CoreException;

	/**
	 * Releases the document acquired via a call to {@link #acquireDocument(IProgressMonitor)
	 * aquireDocument}.
	 *
	 * @param document the document to release
	 * @param pm a progress monitor
	 *
	 * @throws CoreException if the document can't be released
	 */
	protected abstract void releaseDocument(IDocument document, IProgressMonitor pm) throws CoreException;

	/**
	 * Hook to create an undo change for the given undo edit. This hook
	 * gets called while performing the change to construct the corresponding
	 * undo change object.
	 *
	 * @param edit the {@link UndoEdit} to create an undo change for
	 *
	 * @return the undo change or <code>null</code> if no undo change can
	 *  be created. Returning <code>null</code> results in the fact that
	 *  the whole change tree can't be undone. So returning <code>null</code>
	 *  is only recommended if an exception occurred during the creation of the
	 *  undo change.
	 */
	protected abstract Change createUndoChange(UndoEdit edit);

	/**
	 * {@inheritDoc}
	 */
	public Change perform(IProgressMonitor pm) throws CoreException {
		pm.beginTask("", 3); //$NON-NLS-1$
		IDocument document= null;

		try {
			document= acquireDocument(new SubProgressMonitor(pm, 1));

			UndoEdit undo= performEdits(document);
			
			commit(document, new SubProgressMonitor(pm, 1));
			return createUndoChange(undo);
			
		} catch (BadLocationException e) {
			throw Changes.asCoreException(e);
		} catch (MalformedTreeException e) {
			throw Changes.asCoreException(e);
		} finally {
			releaseDocument(document, new SubProgressMonitor(pm, 1));
			pm.done();
		}
	}

	/**
	 * Executes the text edits on the given document.
	 * Subclasses that override this method should call <code>super.performEdits(document)</code>.
	 * 
	 * @param document the document
	 * @return an object representing the undo of the executed edits
	 * @exception MalformedTreeException is thrown if the edit tree isn't
	 *  in a valid state. This exception is thrown before any edit is executed.
	 *  So the document is still in its original state.
	 * @exception BadLocationException is thrown if one of the edits in the
	 *  tree can't be executed. The state of the document is undefined if this
	 *  exception is thrown.
	 * @since 3.5
	 */
	protected UndoEdit performEdits(IDocument document) throws BadLocationException, MalformedTreeException {
		DocumentRewriteSession session= null;
		try {
			if (document instanceof IDocumentExtension4) {
				session= ((IDocumentExtension4)document).startRewriteSession(
					DocumentRewriteSessionType.UNRESTRICTED);
			}
	
			LinkedModeModel.closeAllModels(document);
			TextEditProcessor processor= createTextEditProcessor(document, TextEdit.CREATE_UNDO, false);
			return processor.performEdits();
			
		} finally {
			if (session != null) {
				((IDocumentExtension4)document).stopRewriteSession(session);
			}
		}
	}
	
	//---- Method to access the current content of the text change ---------

	/**
	 * Returns the document this text change is associated to. The
	 * document returned is computed at the point in time when this
	 * method is called. So calling this method multiple times may
	 * return different document instances.
	 * <p>
	 * The returned document must not be modified by the client. Doing
	 * so will result in an unexpected behavior when the change is
	 * performed.
	 * </p>
	 *
	 * @param pm a progress monitor to report progress or <code>null</code>
	 *  if no progress reporting is desired
	 * @return the document this change is working on
	 *
	 * @throws CoreException if the document can't be acquired
	 */
	public IDocument getCurrentDocument(IProgressMonitor pm) throws CoreException {
		if (pm == null)
			pm= new NullProgressMonitor();
		IDocument result= null;
		pm.beginTask("", 2); //$NON-NLS-1$
		try{
			result= acquireDocument(new SubProgressMonitor(pm, 1));
		} finally {
			if (result != null)
				releaseDocument(result, new SubProgressMonitor(pm, 1));
		}
		pm.done();
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getCurrentContent(IProgressMonitor pm) throws CoreException {
		return getCurrentDocument(pm).get();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getCurrentContent(IRegion region, boolean expandRegionToFullLine, int surroundingLines, IProgressMonitor pm) throws CoreException {
		Assert.isNotNull(region);
		Assert.isTrue(surroundingLines >= 0);
		IDocument document= getCurrentDocument(pm);
		Assert.isTrue(document.getLength() >= region.getOffset() + region.getLength());
		return getContent(document, region, expandRegionToFullLine, surroundingLines);
	}

	//---- Method to access the preview content of the text change ---------

	/**
	 * Returns the edit that got executed during preview generation
	 * instead of the given original. The method requires that <code>
	 * setKeepPreviewEdits</code> is set to <code>true</code> and that
	 * a preview has been requested via one of the <code>getPreview*
	 * </code> methods.
	 * <p>
	 * The method returns <code>null</code> if the original isn't managed
	 * by this text change.
	 * </p>
	 *
	 * @param original the original edit managed by this text change
	 *
	 * @return the edit executed during preview generation
	 */
	public TextEdit getPreviewEdit(TextEdit original) {
		Assert.isTrue(getKeepPreviewEdits() && fCopier != null && original != null);
		return fCopier.getCopy(original);
	}

	/**
	 * Returns the edits that were executed during preview generation
	 * instead of the given array of original edits. The method requires
	 * that <code>setKeepPreviewEdits</code> is set to <code>true</code>
	 * and that a preview has been requested via one of the <code>
	 * getPreview*</code> methods.
	 * <p>
	 * The method returns an empty array if none of the original edits
	 * is managed by this text change.
	 * </p>
	 *
	 * @param originals an array of original edits managed by this text
	 *  change
	 *
	 * @return an array of edits containing the corresponding edits
	 *  executed during preview generation
	 */
	public TextEdit[] getPreviewEdits(TextEdit[] originals) {
		Assert.isTrue(getKeepPreviewEdits() && fCopier != null && originals != null);
		if (originals.length == 0)
			return new TextEdit[0];
		List result= new ArrayList(originals.length);
		for (int i= 0; i < originals.length; i++) {
			TextEdit copy= fCopier.getCopy(originals[i]);
			if (copy != null)
				result.add(copy);
		}
		return (TextEdit[]) result.toArray(new TextEdit[result.size()]);
	}

	/**
	 * Returns a document containing a preview of the text change. The
	 * preview is computed by executing the all managed text edits. The
	 * method considers the active state of the added {@link TextEditChangeGroup
	 * text edit change groups}.
	 *
	 * @param pm a progress monitor to report progress or <code>null</code>
	 *  if no progress reporting is desired
	 * @return a document containing the preview of the text change
	 *
	 * @throws CoreException if the preview can't be created
	 */
	public IDocument getPreviewDocument(IProgressMonitor pm) throws CoreException {
		PreviewAndRegion result= getPreviewDocument(ALL_EDITS, pm);
		return result.document;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getPreviewContent(IProgressMonitor pm) throws CoreException {
		return getPreviewDocument(pm).get();
	}

	/**
	 * Returns a preview of the text change clipped to a specific region.
	 * The preview is created by applying the text edits managed by the
	 * given array of {@link TextEditChangeGroup text edit change groups}.
	 * The region is determined as follows:
	 * <ul>
	 *   <li>if <code>expandRegionToFullLine</code> is <code>false</code>
	 *       then the parameter <code>region</code> determines the clipping.
	 *   </li>
	 *   <li>if <code>expandRegionToFullLine</code> is <code>true</code>
	 *       then the region determined by the parameter <code>region</code>
	 *       is extended to cover full lines.
	 *   </li>
	 *   <li>if <code>surroundingLines</code> &gt; 0 then the given number
	 *       of surrounding lines is added. The value of <code>surroundingLines
	 *       </code> is only considered if <code>expandRegionToFullLine</code>
	 *       is <code>true</code>
	 *   </li>
	 * </ul>
	 *
	 * @param changeGroups a set of change groups for which a preview is to be
	 *  generated
	 * @param region the starting region for the clipping
	 * @param expandRegionToFullLine if <code>true</code> is passed the region
	 *  is extended to cover full lines
	 * @param surroundingLines the number of surrounding lines to be added to
	 *  the clipping region. Is only considered if <code>expandRegionToFullLine
	 *  </code> is <code>true</code>
	 * @param pm a progress monitor to report progress or <code>null</code>
	 *  if no progress reporting is desired
	 *
	 * @return the current content of the text change clipped to a region
	 *  determined by the given parameters.
	 *
	 * @throws CoreException if an exception occurs while generating the preview
	 *
	 * @see #getCurrentContent(IRegion, boolean, int, IProgressMonitor)
	 */
	public String getPreviewContent(TextEditChangeGroup[] changeGroups, IRegion region, boolean expandRegionToFullLine, int surroundingLines, IProgressMonitor pm) throws CoreException {
		return getPreviewContent((TextEditBasedChangeGroup[])changeGroups, region, expandRegionToFullLine, surroundingLines, pm);
	}

	/**
	 * Returns a preview of the text change clipped to a specific region.
	 * The preview is created by applying the text edits managed by the
	 * given array of {@link TextEditChangeGroup text edit change groups}.
	 * The region is determined as follows:
	 * <ul>
	 *   <li>if <code>expandRegionToFullLine</code> is <code>false</code>
	 *       then the parameter <code>region</code> determines the clipping.
	 *   </li>
	 *   <li>if <code>expandRegionToFullLine</code> is <code>true</code>
	 *       then the region determined by the parameter <code>region</code>
	 *       is extended to cover full lines.
	 *   </li>
	 *   <li>if <code>surroundingLines</code> &gt; 0 then the given number
	 *       of surrounding lines is added. The value of <code>surroundingLines
	 *       </code> is only considered if <code>expandRegionToFullLine</code>
	 *       is <code>true</code>
	 *   </li>
	 * </ul>
	 *
	 * @param changeGroups a set of change groups for which a preview is to be
	 *  generated
	 * @param region the starting region for the clipping
	 * @param expandRegionToFullLine if <code>true</code> is passed the region
	 *  is extended to cover full lines
	 * @param surroundingLines the number of surrounding lines to be added to
	 *  the clipping region. Is only considered if <code>expandRegionToFullLine
	 *  </code> is <code>true</code>
	 * @param pm a progress monitor to report progress or <code>null</code>
	 *  if no progress reporting is desired
	 *
	 * @return the current content of the text change clipped to a region
	 *  determined by the given parameters.
	 *
	 * @throws CoreException if an exception occurs while generating the preview
	 *
	 * @see #getCurrentContent(IRegion, boolean, int, IProgressMonitor)
	 *
	 * @since 3.2
	 */
	public String getPreviewContent(TextEditBasedChangeGroup[] changeGroups, IRegion region, boolean expandRegionToFullLine, int surroundingLines, IProgressMonitor pm) throws CoreException {
		IRegion currentRegion= getRegion(changeGroups);
		Assert.isTrue(region.getOffset() <= currentRegion.getOffset() &&
			currentRegion.getOffset() + currentRegion.getLength() <= region.getOffset() + region.getLength());
		// Make sure that all edits in the change groups are rooted under the edit the text change stand for.
		TextEdit root= getEdit();
		Assert.isNotNull(root, "No root edit"); //$NON-NLS-1$
		for (int c= 0; c < changeGroups.length; c++) {
			TextEditBasedChangeGroup group= changeGroups[c];
			TextEdit[] edits= group.getTextEdits();
			for (int e= 0; e < edits.length; e++) {

				// TODO: enable once following bug is fixed
				// https://bugs.eclipse.org/bugs/show_bug.cgi?id=130909
				// Assert.isTrue(root == edits[e].getRoot(), "Wrong root edit"); //$NON-NLS-1$
			}
		}
		PreviewAndRegion result= getPreviewDocument(changeGroups, pm);
		int delta;
		if (result.region == null) {	// all edits were delete edits so no new region
			delta= -currentRegion.getLength();
		} else {
			delta= result.region.getLength() - currentRegion.getLength();
		}
		return getContent(result.document, new Region(region.getOffset(), region.getLength() + delta), expandRegionToFullLine, surroundingLines);

	}

	//---- private helper methods --------------------------------------------------

	private PreviewAndRegion getPreviewDocument(TextEditBasedChangeGroup[] changes, IProgressMonitor pm) throws CoreException {
		IDocument document= new Document(getCurrentDocument(pm).get());
		boolean trackChanges= getKeepPreviewEdits();
		setKeepPreviewEdits(true);
		TextEditProcessor processor= changes == ALL_EDITS
			? createTextEditProcessor(document, TextEdit.NONE, true)
			: createTextEditProcessor(document, TextEdit.NONE, changes);
		try {
			processor.performEdits();
			return new PreviewAndRegion(document, getNewRegion(changes));
		} catch (BadLocationException e) {
			throw Changes.asCoreException(e);
		} finally {
			setKeepPreviewEdits(trackChanges);
		}
	}

	private TextEditProcessor createTextEditProcessor(IDocument document, int flags, boolean preview) {
		if (fEdit == null)
			return new TextEditProcessor(document, new MultiTextEdit(0,0), flags);
		List excludes= new ArrayList(0);
		TextEditBasedChangeGroup[] groups= getChangeGroups();
		for (int index= 0; index < groups.length; index++) {
			TextEditBasedChangeGroup edit= groups[index];
			if (!edit.isEnabled()) {
				excludes.addAll(Arrays.asList(edit.getTextEditGroup().getTextEdits()));
			}
		}
		if (preview) {
			fCopier= new TextEditCopier(fEdit);
			TextEdit copiedEdit= fCopier.perform();
			boolean keep= getKeepPreviewEdits();
			if (keep)
				flags= flags | TextEdit.UPDATE_REGIONS;
			LocalTextEditProcessor result= new LocalTextEditProcessor(document, copiedEdit, flags);
			result.setExcludes(mapEdits(
				(TextEdit[])excludes.toArray(new TextEdit[excludes.size()]),
				fCopier));
			if (!keep)
				fCopier= null;
			return result;
		} else {
			LocalTextEditProcessor result= new LocalTextEditProcessor(document, fEdit, flags | TextEdit.UPDATE_REGIONS);
			result.setExcludes((TextEdit[])excludes.toArray(new TextEdit[excludes.size()]));
			return result;
		}
	}

	private TextEditProcessor createTextEditProcessor(IDocument document, int flags, TextEditBasedChangeGroup[] changes) {
		if (fEdit == null)
			return new TextEditProcessor(document, new MultiTextEdit(0,0), flags);
		List includes= new ArrayList(0);
		for (int c= 0; c < changes.length; c++) {
			TextEditBasedChangeGroup change= changes[c];
			Assert.isTrue(change.getTextEditChange() == this);
			if (change.isEnabled()) {
				includes.addAll(Arrays.asList(change.getTextEditGroup().getTextEdits()));
			}
		}
		fCopier= new TextEditCopier(fEdit);
		TextEdit copiedEdit= fCopier.perform();
		boolean keep= getKeepPreviewEdits();
		if (keep)
			flags= flags | TextEdit.UPDATE_REGIONS;
		LocalTextEditProcessor result= new LocalTextEditProcessor(document, copiedEdit, flags);
		result.setIncludes(mapEdits(
			(TextEdit[])includes.toArray(new TextEdit[includes.size()]),
			fCopier));
		if (!keep)
			fCopier= null;
		return result;
	}

	private IRegion getRegion(TextEditBasedChangeGroup[] changes) {
		if (changes == ALL_EDITS) {
			if (fEdit == null)
				return null;
			return fEdit.getRegion();
		} else {
			List edits= new ArrayList();
			for (int i= 0; i < changes.length; i++) {
				edits.addAll(Arrays.asList(changes[i].getTextEditGroup().getTextEdits()));
			}
			if (edits.size() == 0)
				return null;
			return TextEdit.getCoverage((TextEdit[]) edits.toArray(new TextEdit[edits.size()]));
		}
	}

	private IRegion getNewRegion(TextEditBasedChangeGroup[] changes) {
		if (changes == ALL_EDITS) {
			if (fEdit == null)
				return null;
			return fCopier.getCopy(fEdit).getRegion();
		} else {
			List result= new ArrayList();
			for (int c= 0; c < changes.length; c++) {
				TextEdit[] edits= changes[c].getTextEditGroup().getTextEdits();
				for (int e= 0; e < edits.length; e++) {
					TextEdit copy= fCopier.getCopy(edits[e]);
					if (copy != null)
						result.add(copy);
				}
			}
			if (result.size() == 0)
				return null;
			return TextEdit.getCoverage((TextEdit[]) result.toArray(new TextEdit[result.size()]));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void setKeepPreviewEdits(boolean keep) {
		super.setKeepPreviewEdits(keep);

		if (!keep)
			fCopier= null;
	}
}
