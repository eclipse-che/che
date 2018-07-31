/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.api.editor.texteditor;

import com.google.gwt.event.dom.client.HasBlurHandlers;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.event.dom.client.HasFocusHandlers;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.RequiresResize;
import java.util.List;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.api.editor.codeassist.AdditionalInfoCallback;
import org.eclipse.che.ide.api.editor.codeassist.CompletionProposal;
import org.eclipse.che.ide.api.editor.codeassist.CompletionsSource;
import org.eclipse.che.ide.api.editor.document.Document;
import org.eclipse.che.ide.api.editor.events.DocumentChangedEvent;
import org.eclipse.che.ide.api.editor.events.HasCursorActivityHandlers;
import org.eclipse.che.ide.api.editor.events.HasGutterClickHandlers;
import org.eclipse.che.ide.api.editor.hotkeys.HotKeyItem;
import org.eclipse.che.ide.api.editor.keymap.Keymap;
import org.eclipse.che.ide.api.editor.position.PositionConverter;
import org.eclipse.che.ide.api.editor.text.Region;

/** An interface for editor widget implementations. */
public interface EditorWidget
    extends IsWidget,
        RequiresResize,
        /* handler interfaces */
        HasBlurHandlers,
        HasChangeHandlers,
        HasCursorActivityHandlers,
        HasFocusHandlers,
        HasGutterClickHandlers,
        /* extended capabilities */
        HasKeyBindings,
        HasTextMarkers,
        LineStyler.HasLineStyler,
        UndoableEditor {

  /**
   * Returns the contents of the editor.
   *
   * @return
   */
  String getValue();

  /**
   * Sets the content of the editor.<br>
   * The operation <em>must</em> send a {@link DocumentChangedEvent} on the document private event
   * bus.
   *
   * @param newValue the new contents
   * @param initializationHandler must be called when content injected in the Editor Widget
   */
  void setValue(String newValue, ContentInitializedHandler initializationHandler);

  /**
   * Returns the current language mode for highlighting.
   *
   * @return the mode
   */
  String getMode();

  /**
   * Change readonly state of the editor.
   *
   * @param isReadOnly true to set the editor in readonly mode, false to allow edit
   */
  void setReadOnly(boolean isReadOnly);

  /** Sets whether the annotation ruler is visible. */
  void setAnnotationRulerVisible(boolean show);

  /** Sets whether the folding ruler is visible. */
  void setFoldingRulerVisible(boolean show);

  /** Sets whether the zoom ruler is visible. */
  void setZoomRulerVisible(boolean show);

  /** Sets whether the overview ruler is visible. */
  void setOverviewRulerVisible(boolean show);

  /**
   * Returns the readonly state of the editor.
   *
   * @return the readonly state, true iff the editor is readonly
   */
  boolean isReadOnly();

  /**
   * Returns the dirty state of the editor.
   *
   * @return true iff the editor is dirty (i.e. unsaved change were made)
   */
  boolean isDirty();

  /** Marks the editor as clean i.e change the dirty state to false. */
  void markClean();

  /** Marks the editor as dirty i.e change the dirty state to true. */
  void markDirty();

  /**
   * Returns the tab size (equivalent number of spaces).
   *
   * @return the tab size
   */
  int getTabSize();

  /**
   * Sets the tab size.
   *
   * @param tabSize the new value
   */
  void setTabSize(int tabSize);

  /**
   * The instance of {@link org.eclipse.che.ide.api.editor.document.Document}.
   *
   * @return the embedded document
   */
  Document getDocument();

  /**
   * Returns the selected range in the editor. In case of multiple selection support, returns the
   * primary selection. When no actual selection is done, a selection with a zero length is given
   *
   * @return the selected range
   */
  Region getSelectedRange();

  /**
   * Returns the current keymap in the editor.
   *
   * @return the current keymap
   */
  @NotNull
  Keymap getKeymap();

  /** Give the focus to the editor. */
  void setFocus();

  /**
   * Show a message to the user.
   *
   * @param message the message
   */
  void showMessage(String message);

  /**
   * Selects the given range in the editor.
   *
   * @param selection the new selection
   * @param show whether the editor should be scrolled to show the range
   */
  void setSelectedRange(Region selection, boolean show);

  /**
   * Scroll the editor to show the given range.
   *
   * @param range the range to show
   */
  void setDisplayRange(Region range);

  /**
   * Returns a position converter relative to this editor (pixel coordinates <-> line char
   * positions).
   *
   * @return a position converter
   */
  PositionConverter getPositionConverter();

  /**
   * Display the completion proposals.
   *
   * @param proposals the proposals
   */
  void showCompletionsProposals(List<CompletionProposal> proposals);

  /**
   * Display the completion proposals.
   *
   * @param completionsSource the completion source
   */
  void showCompletionProposals(CompletionsSource completionsSource);

  /** Display the default completion proposals. */
  void showCompletionProposals();

  void showCompletionProposals(
      CompletionsSource completionsSource, AdditionalInfoCallback additionalInfoCallback);

  /** Refresh the editor widget. */
  void refresh();

  boolean isCompletionProposalsShowing();

  /**
   * Return list hotKeys for editor
   *
   * @return list hotKeys
   */
  List<HotKeyItem> getHotKeys();

  /** Callback that should be called when editor widget implementation is fully initialized. */
  interface WidgetInitializedCallback {
    void initialized(EditorWidget editorWidget);
  }
}
