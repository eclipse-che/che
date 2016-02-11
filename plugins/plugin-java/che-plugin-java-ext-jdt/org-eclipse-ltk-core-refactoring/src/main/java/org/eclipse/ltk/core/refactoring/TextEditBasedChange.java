/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Ed Swartz <ed.swartz@nokia.com> -
 *         (bug 157203: [ltk] [patch] TextEditBasedChange/TextChange provides incorrect diff when one side is empty)
 *******************************************************************************/
package org.eclipse.ltk.core.refactoring;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.edits.TextEditCopier;
import org.eclipse.text.edits.TextEditGroup;
import org.eclipse.text.edits.TextEditProcessor;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

import org.eclipse.ltk.internal.core.refactoring.Changes;

/**
 * An abstract base implementation of a change which is based on text edits.
 *
 * @since 3.2
 */
public abstract class TextEditBasedChange extends Change {

	/**
	 * Text edit processor which has the ability to selectively include or exclude single text edits.
	 */
	static final class LocalTextEditProcessor extends TextEditProcessor {
		public static final int EXCLUDE= 1;
		public static final int INCLUDE= 2;

		private TextEdit[] fExcludes;
		private TextEdit[] fIncludes;

		protected LocalTextEditProcessor(IDocument document, TextEdit root, int flags) {
			super(document, root, flags);
		}
		public void setIncludes(TextEdit[] includes) {
			Assert.isNotNull(includes);
			Assert.isTrue(fExcludes == null);
			fIncludes= flatten(includes);
		}
		public void setExcludes(TextEdit[] excludes) {
			Assert.isNotNull(excludes);
			Assert.isTrue(fIncludes == null);
			fExcludes= flatten(excludes);
		}
		protected boolean considerEdit(TextEdit edit) {
			if (fExcludes != null) {
				for (int i= 0; i < fExcludes.length; i++) {
					if (edit.equals(fExcludes[i]))
						return false;
				}
				return true;
			}
			if (fIncludes != null) {
				for (int i= 0; i < fIncludes.length; i++) {
					if (edit.equals(fIncludes[i]))
						return true;
				}
				return false;
			}
			return true;
		}
		private TextEdit[] flatten(TextEdit[] edits) {
			List result= new ArrayList(5);
			for (int i= 0; i < edits.length; i++) {
				flatten(result, edits[i]);
			}
			return (TextEdit[])result.toArray(new TextEdit[result.size()]);
		}
		private void flatten(List result, TextEdit edit) {
			result.add(edit);
			TextEdit[] children= edit.getChildren();
			for (int i= 0; i < children.length; i++) {
				flatten(result, children[i]);
			}
		}
	}

	/**
	 * Value objects encapsulating a document with an associated region.
	 */
	static final class PreviewAndRegion {
		public IDocument document;
		public IRegion region;
		public PreviewAndRegion(IDocument d, IRegion r) {
			document= d;
			region= r;
		}
	}

	/**
	 * A special object denoting all edits managed by the change. This even
	 * includes those edits not managed by a {@link TextEditBasedChangeGroup}.
	 */
	static final TextEditBasedChangeGroup[] ALL_EDITS= new TextEditBasedChangeGroup[0];

	/** The list of change groups */
	private List fChangeGroups;
	private GroupCategorySet fCombiedGroupCategories;

	/** The name of the change */
	private String fName;

	/** The text type */
	private String fTextType;

	/** Should the positions of edits be tracked during change generation? */
	private boolean fTrackEdits;

	/**
	 * Creates a new abstract text edit change with the specified name.  The name is a
	 * human-readable value that is displayed to users.  The name does not
	 * need to be unique, but it must not be <code>null</code>.
	 * <p>
	 * The text type of this text edit change is set to <code>txt</code>.
	 * </p>
	 *
	 * @param name the name of the text edit change
	 *
	 * @see #setTextType(String)
	 */
	protected TextEditBasedChange(String name) {
		Assert.isNotNull(name, "Name must not be null"); //$NON-NLS-1$
		fChangeGroups= new ArrayList(5);
		fName= name;
		fTextType= "txt"; //$NON-NLS-1$
	}

	/**
	 * Adds a {@link TextEditBasedChangeGroup text edit change group}.
	 * The edits managed by the given text edit change group must be part of
	 * the change's root edit.
	 *
	 * @param group the text edit change group to add
	 */
	public void addChangeGroup(TextEditBasedChangeGroup group) {
		Assert.isTrue(group != null);
		fChangeGroups.add(group);
		if (fCombiedGroupCategories != null) {
			fCombiedGroupCategories= GroupCategorySet.union(fCombiedGroupCategories, group.getGroupCategorySet());
		}
	}

	/**
	 * Adds a {@link TextEditGroup text edit group}. This method is a convenience
	 * method for calling <code>change.addChangeGroup(new
	 * TextEditBasedChangeGroup(change, group));</code>.
	 *
	 * @param group the text edit group to add
	 */
	public void addTextEditGroup(TextEditGroup group) {
		addChangeGroup(new TextEditBasedChangeGroup(this, group));
	}

	/**
	 * Returns <code>true</code> if the change has one of the given group
	 * categories. Otherwise <code>false</code> is returned.
	 *
	 * @param groupCategories the group categories to check
	 *
	 * @return whether the change has one of the given group
	 *  categories
	 *
	 * @since 3.2
	 */
	public boolean hasOneGroupCategory(List groupCategories) {
		if (fCombiedGroupCategories == null) {
			fCombiedGroupCategories= GroupCategorySet.NONE;
			for (Iterator iter= fChangeGroups.iterator(); iter.hasNext();) {
				TextEditBasedChangeGroup group= (TextEditBasedChangeGroup)iter.next();
				fCombiedGroupCategories= GroupCategorySet.union(fCombiedGroupCategories, group.getGroupCategorySet());
			}
		}
		return fCombiedGroupCategories.containsOneCategory(groupCategories);
	}

	/**
	 * Returns the {@link TextEditBasedChangeGroup text edit change groups} managed by this
	 * buffer change.
	 *
	 * @return the text edit change groups
	 */
	public final TextEditBasedChangeGroup[] getChangeGroups() {
		return (TextEditBasedChangeGroup[])fChangeGroups.toArray(new TextEditBasedChangeGroup[fChangeGroups.size()]);
	}

	String getContent(IDocument document, IRegion region, boolean expandRegionToFullLine, int surroundingLines) throws CoreException {
		try {
			if (expandRegionToFullLine) {
				int startLine= Math.max(document.getLineOfOffset(region.getOffset()) - surroundingLines, 0);
				int endLine;
				if (region.getLength() == 0) {
					// no lines are in the region, so remove one from the context,
					// or else spurious changes show up that look like deletes from the source
					if (surroundingLines == 0) {
						// empty: show nothing
						return ""; //$NON-NLS-1$
					}

					endLine= Math.min(
						document.getLineOfOffset(region.getOffset()) + surroundingLines - 1,
						document.getNumberOfLines() - 1);
				} else {
					endLine= Math.min(
						document.getLineOfOffset(region.getOffset() + region.getLength() - 1) + surroundingLines,
						document.getNumberOfLines() - 1);
				}

				int offset= document.getLineInformation(startLine).getOffset();
				IRegion endLineRegion= document.getLineInformation(endLine);
				int length = endLineRegion.getOffset() + endLineRegion.getLength() - offset;
				return document.get(offset, length);

			} else {
				return document.get(region.getOffset(), region.getLength());
			}
		} catch (BadLocationException e) {
			throw Changes.asCoreException(e);
		}
	}

	/**
	 * Returns the current content of the document this text
	 * change is associated with.
	 *
	 * @param pm a progress monitor to report progress or <code>null</code>
	 *  if no progress reporting is desired
	 * @return the current content of the text edit change
	 *
	 * @exception CoreException if the content can't be accessed
	 */
	public abstract String getCurrentContent(IProgressMonitor pm) throws CoreException;

	/**
	 * Returns the current content of the text edit change clipped to a specific
	 * region. The region is determined as follows:
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
	 * @param region the starting region for the text to be returned
	 * @param expandRegionToFullLine if <code>true</code> is passed the region
	 *  is extended to cover full lines
	 * @param surroundingLines the number of surrounding lines to be added to
	 *  the clipping region. Is only considered if <code>expandRegionToFullLine
	 *  </code> is <code>true</code>
	 * @param pm a progress monitor to report progress or <code>null</code>
	 *  if no progress reporting is desired
	 *
	 * @return the current content of the text edit change clipped to a region
	 *  determined by the given parameters.
	 *
	 * @throws CoreException if an exception occurs while accessing the current content
	 */
	public abstract String getCurrentContent(IRegion region, boolean expandRegionToFullLine, int surroundingLines, IProgressMonitor pm) throws CoreException;

	/**
	 * Returns whether preview edits are remembered for further region
	 * tracking or not.
	 *
	 * @return <code>true</code> if executed text edits are remembered
	 * during preview generation; otherwise <code>false</code>
	 */
	public boolean getKeepPreviewEdits() {
		return fTrackEdits;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getName() {
		return fName;
	}

	/**
	 * Returns a preview of the text edit change clipped to a specific region.
	 * The preview is created by applying the text edits managed by the
	 * given array of {@link TextEditBasedChangeGroup text edit change groups}.
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
	public abstract String getPreviewContent(TextEditBasedChangeGroup[] changeGroups, IRegion region, boolean expandRegionToFullLine, int surroundingLines, IProgressMonitor pm) throws CoreException;

	/**
	 * Returns the preview content as a string.
	 *
	 * @param pm a progress monitor to report progress or <code>null</code>
	 *  if no progress reporting is desired
	 * @return the preview
	 *
	 * @throws CoreException if the preview can't be created
	 */
	public abstract String getPreviewContent(IProgressMonitor pm) throws CoreException;

	/**
	 * Returns the text edit change's text type.
	 *
	 * @return the text edit change's text type
	 */
	public String getTextType() {
		return fTextType;
	}

	TextEdit[] mapEdits(TextEdit[] edits, TextEditCopier copier) {
		if (edits == null)
			return null;
		final List result= new ArrayList(edits.length);
		for (int i= 0; i < edits.length; i++) {
			TextEdit edit= copier.getCopy(edits[i]);
			if (edit != null)
				result.add(edit);
		}
		return (TextEdit[]) result.toArray(new TextEdit[result.size()]);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		for (Iterator iter= fChangeGroups.iterator(); iter.hasNext();) {
			TextEditBasedChangeGroup element= (TextEditBasedChangeGroup) iter.next();
			element.setEnabled(enabled);
		}
	}

	/**
	 * Controls whether the text edit change should keep executed edits during
	 * preview generation.
	 *
	 * @param keep if <code>true</code> executed preview edits are kept
	 */
	public void setKeepPreviewEdits(boolean keep) {
		fTrackEdits= keep;
	}

	/**
	 * Sets the text type. The text type is used to determine the content
	 * merge viewer used to present the difference between the original
	 * and the preview content in the user interface. Content merge viewers
	 * are defined via the extension point <code>org.eclipse.compare.contentMergeViewers</code>.
	 * <p>
	 * The default text type is <code>txt</code>.
	 * </p>
	 *
	 * @param type the text type. If <code>null</code> is passed the text type is
	 *  reseted to the default text type <code>txt</code>.
	 */
	public void setTextType(String type) {
		if (type == null)
			type= "txt"; //$NON-NLS-1$
		fTextType= type;
	}
}
