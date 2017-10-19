/**
 * ***************************************************************************** Copyright (c)
 * 2012-2015 Red Hat, Inc. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: Red Hat, Inc. - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.che.ide.ext.java.jdt.text.edits;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.eclipse.che.ide.api.editor.text.BadLocationException;
import org.eclipse.che.ide.ext.java.jdt.text.Document;

/**
 * A <code>TextEditProcessor</code> manages a set of edits and applies them as a whole to an <code>
 * IDocument</code>.
 *
 * <p>This class isn't intended to be subclassed.
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @see TextEdit#apply(Document)
 */
public class TextEditProcessor {

  private Document fDocument;

  private TextEdit fRoot;

  private int fStyle;

  private boolean fChecked;

  private MalformedTreeException fException;

  private List fSourceEdits;

  /**
   * Constructs a new edit processor for the given document.
   *
   * @param document the document to manipulate
   * @param root the root of the text edit tree describing the modifications. By passing a text edit
   *     a a text edit processor the ownership of the edit is transfered to the text edit
   *     processors. Clients must not modify the edit (e.g adding new children) any longer.
   * @param style {@link TextEdit#NONE}, {@link TextEdit#CREATE_UNDO} or {@link
   *     TextEdit#UPDATE_REGIONS})
   */
  public TextEditProcessor(Document document, TextEdit root, int style) {
    this(document, root, style, false);
  }

  private TextEditProcessor(Document document, TextEdit root, int style, boolean secondary) {
    // Assert.isNotNull(document);
    // Assert.isNotNull(root);
    fDocument = document;
    fRoot = root;
    if (fRoot instanceof MultiTextEdit) ((MultiTextEdit) fRoot).defineRegion(0);
    fStyle = style;
    if (secondary) {
      fChecked = true;
      fSourceEdits = new ArrayList();
    }
  }

  /**
   * Creates a special internal processor used to during source computation inside move source and
   * copy source edits
   *
   * @param document the document to be manipulated
   * @param root the edit tree
   * @param style {@link TextEdit#NONE}, {@link TextEdit#CREATE_UNDO} or {@link
   *     TextEdit#UPDATE_REGIONS})
   * @return a secondary text edit processor
   * @since 3.1
   */
  static TextEditProcessor createSourceComputationProcessor(
      Document document, TextEdit root, int style) {
    return new TextEditProcessor(document, root, style, true);
  }

  /**
   * Returns the document to be manipulated.
   *
   * @return the document
   */
  public Document getDocument() {
    return fDocument;
  }

  /**
   * Returns the edit processor's root edit.
   *
   * @return the processor's root edit
   */
  public TextEdit getRoot() {
    return fRoot;
  }

  /**
   * Returns the style bits of the text edit processor
   *
   * @return the style bits
   * @see TextEdit#CREATE_UNDO
   * @see TextEdit#UPDATE_REGIONS
   */
  public int getStyle() {
    return fStyle;
  }

  /**
   * Checks if the processor can execute all its edits.
   *
   * @return <code>true</code> if the edits can be executed. Return <code>false
   * </code>otherwise. One major reason why edits cannot be executed are wrong offset or length
   *     values of edits. Calling perform in this case will very likely end in a <code>
   *     BadLocationException</code>.
   */
  public boolean canPerformEdits() {
    try {
      fRoot.dispatchCheckIntegrity(this);
      fChecked = true;
    } catch (MalformedTreeException e) {
      fException = e;
      return false;
    }
    return true;
  }

  /**
   * Executes the text edits.
   *
   * @return an object representing the undo of the executed edits
   * @throws MalformedTreeException is thrown if the edit tree isn't in a valid state. This
   *     exception is thrown before any edit is executed. So the document is still in its original
   *     state.
   * @throws BadLocationException is thrown if one of the edits in the tree can't be executed. The
   *     state of the document is undefined if this exception is thrown.
   */
  public UndoEdit performEdits() throws MalformedTreeException, BadLocationException {
    if (!fChecked) {
      fRoot.dispatchCheckIntegrity(this);
    } else {
      if (fException != null) throw fException;
    }
    return fRoot.dispatchPerformEdits(this);
  }

  /**
   * Tells whether this processor considers the given edit.
   *
   * <p>Note that this class isn't intended to be subclassed.
   *
   * @param edit the text edit
   * @return <code>true</code> if this processor considers the given edit
   */
  protected boolean considerEdit(TextEdit edit) {
    return true;
  }

  // ---- checking --------------------------------------------------------------------

  void checkIntegrityDo() throws MalformedTreeException {
    fSourceEdits = new ArrayList();
    fRoot.traverseConsistencyCheck(this, fDocument, fSourceEdits);
    if (fRoot.getExclusiveEnd() > fDocument.getLength())
      throw new MalformedTreeException(
          null, fRoot, "End position lies outside document range"); // $NON-NLS-1$
  }

  void checkIntegrityUndo() {
    if (fRoot.getExclusiveEnd() > fDocument.getLength())
      throw new MalformedTreeException(
          null, fRoot, "End position lies outside document range"); // $NON-NLS-1$
  }

  // ---- execution --------------------------------------------------------------------

  UndoEdit executeDo() throws BadLocationException {
    UndoCollector collector = new UndoCollector(fRoot);
    try {
      if (createUndo()) collector.connect(fDocument);
      computeSources();
      fRoot.traverseDocumentUpdating(this, fDocument);
      if (updateRegions()) {
        fRoot.traverseRegionUpdating(this, fDocument, 0, false);
      }
    } finally {
      collector.disconnect(fDocument);
    }
    return collector.undo;
  }

  private void computeSources() {
    for (Iterator iter = fSourceEdits.iterator(); iter.hasNext(); ) {
      List list = (List) iter.next();
      if (list != null) {
        for (Iterator edits = list.iterator(); edits.hasNext(); ) {
          TextEdit edit = (TextEdit) edits.next();
          edit.traverseSourceComputation(this, fDocument);
        }
      }
    }
  }

  UndoEdit executeUndo() throws BadLocationException {
    UndoCollector collector = new UndoCollector(fRoot);
    try {
      if (createUndo()) collector.connect(fDocument);
      TextEdit[] edits = fRoot.getChildren();
      for (int i = edits.length - 1; i >= 0; i--) {
        edits[i].performDocumentUpdating(fDocument);
      }
    } finally {
      collector.disconnect(fDocument);
    }
    return collector.undo;
  }

  private boolean createUndo() {
    return (fStyle & TextEdit.CREATE_UNDO) != 0;
  }

  private boolean updateRegions() {
    return (fStyle & TextEdit.UPDATE_REGIONS) != 0;
  }
}
