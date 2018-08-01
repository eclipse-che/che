/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.api.editor.texteditor;

import javax.validation.constraints.NotNull;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.document.Document;
import org.eclipse.che.ide.api.editor.editorconfig.EditorUpdateAction;
import org.eclipse.che.ide.api.editor.editorconfig.TextEditorConfiguration;
import org.eclipse.che.ide.api.editor.keymap.KeyBinding;
import org.eclipse.che.ide.api.editor.position.PositionConverter;
import org.eclipse.che.ide.api.editor.text.LinearRange;
import org.eclipse.che.ide.api.editor.text.Position;
import org.eclipse.che.ide.api.editor.text.TextPosition;
import org.eclipse.che.ide.api.editor.text.TextRange;

/** Public view on the editor component. */
public interface TextEditor extends EditorPartPresenter {

  /**
   * Initializes this editor with the configuration and document provider.
   *
   * @param configuration the configuration of this editor.
   */
  void initialize(@NotNull TextEditorConfiguration configuration);

  /**
   * Returns the text editor configuration that was used for initialization.
   *
   * @return the text editor configuration
   */
  TextEditorConfiguration getConfiguration();

  /** @return the text editor view implementation */
  @Override
  TextEditorPartView getView();

  /** @return the text editor widget */
  EditorWidget getEditorWidget();

  /**
   * Add an editor-specific key binding.
   *
   * @param keyBinding the key binding
   */
  void addKeybinding(KeyBinding keyBinding);

  /**
   * Closes this text editor after optionally saving changes.
   *
   * @param save <code>true</code> if unsaved changed should be saved, and <code>false</code> if
   *     unsaved changed should be discarded
   */
  @Override
  void close(boolean save);

  /**
   * Returns whether the text in this text editor can be changed by the user.
   *
   * @return <code>true</code> if it can be edited, and <code>false</code> if it is read-only
   */
  boolean isEditable();

  /**
   * Abandons all modifications applied to this text editor's input element's textual presentation
   * since the last save operation.
   */
  void doRevertToSaved();

  /**
   * Returns the document backing the text content.
   *
   * @return the document
   */
  Document getDocument();

  /**
   * Return the content type of the editor content.<br>
   * Returns null if the type is not known yet.
   *
   * @return the content type
   */
  String getContentType();

  /**
   * Returns the selection range as a {@link TextRange} (i.e. two line, char objects: start en end).
   *
   * @return the selection range
   */
  TextRange getSelectedTextRange();

  /**
   * Returns the selection range as a {@link LinearRange} (ie.e a start offset and a length).
   *
   * @return the selection range
   */
  LinearRange getSelectedLinearRange();

  /**
   * Returns the cursor position as a {@link TextPosition} object (a line char position).
   *
   * @return the cursor position
   */
  TextPosition getCursorPosition();

  /** Sets the new cursor position. */
  void setCursorPosition(TextPosition textPosition);

  /**
   * Returns the cursor model for the editor.
   *
   * @return the cursor model
   */
  CursorModelWithHandler getCursorModel();

  /**
   * Returns a position converter relative to this editor (pixel coordinates <-> line char
   * positions).
   *
   * @return a position converter
   */
  PositionConverter getPositionConverter();

  /**
   * Returns the cursor position as an offset from the start.
   *
   * @return the cursor position
   */
  int getCursorOffset();

  /**
   * Returns the top visible line. Used to determine editor vertical scroll position
   *
   * @return the top visible line
   */
  int getTopVisibleLine();

  /**
   * Set (scroll) top visible line
   *
   * @param line the top line
   */
  void setTopLine(int line);

  /**
   * Displays a message to the user.
   *
   * @param message message
   */
  void showMessage(String message);

  /**
   * Returns focus state of the text editor
   *
   * @return <code>true</code> if the text editor is focused or <code>false</code> otherwise
   */
  boolean isFocused();

  /** Give the focus to the editor. */
  void setFocus();

  /** Calls all editor update actions for this editor. */
  void refreshEditor();

  /**
   * Adds an editor update action for this editor.
   *
   * @param action the action to add
   */
  void addEditorUpdateAction(EditorUpdateAction action);

  /**
   * Get word position under offset.
   *
   * @param offset the offset for look for a word
   * @return the word position
   */
  @Nullable
  Position getWordAtOffset(int offset);

  /**
   * Update 'dirty' state of editor when state of editor content is changed
   *
   * @param dirty {@code true} when editor content is modified and {@code false} when editor content
   *     is saved
   */
  void updateDirtyState(boolean dirty);
}
