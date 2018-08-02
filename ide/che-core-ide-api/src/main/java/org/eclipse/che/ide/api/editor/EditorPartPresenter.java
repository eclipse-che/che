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
package org.eclipse.che.ide.api.editor;

import com.google.gwt.user.client.rpc.AsyncCallback;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.api.editor.EditorAgent.OpenEditorCallback;
import org.eclipse.che.ide.api.parts.PartPresenter;

/**
 * An editor is a visual component. It is typically used to edit or browse a document or input
 * object. The input is identified using an <code>EditorInput</code>. Modifications made in an
 * editor part follow an open-save-close lifecycle model
 *
 * <p>An editor is document or input-centric. Each editor has an input, and only one editor can
 * exist for each editor input within a page.
 *
 * @author Evgen Vidolob
 */
public interface EditorPartPresenter extends PartPresenter {

  interface EditorPartCloseHandler {
    void onClose(EditorPartPresenter editor);
  }

  /** The property id for <code>isDirty</code>. */
  int PROP_DIRTY = 0x101;

  /** The property id for editor input changed. */
  int PROP_INPUT = 0x102;

  /**
   * Initializes this editor with the given input.
   *
   * <p>This method is automatically called shortly after the part is instantiated. It marks the
   * start of the part's lifecycle.
   *
   * <p>Implementors of this method must examine the editor input object type to determine if it is
   * understood. If not, the implementor must throw a <code>PartInitException</code>
   *
   * @param input the editor input
   * @param callback callback with actions which should be performed when editor was initialized
   */
  void init(@NotNull EditorInput input, OpenEditorCallback callback);

  /**
   * Returns the input for this editor. If this value changes the part must fire a property listener
   * event with <code>PROP_INPUT</code>.
   *
   * @return the editor input
   */
  @NotNull
  EditorInput getEditorInput();

  /** Saves the contents of this editor. */
  void doSave();

  /**
   * Saves the contents of this editor.
   *
   * @param callback the callback for save operation
   */
  void doSave(@NotNull AsyncCallback<EditorInput> callback);

  /** Saves the contents of this part to another object. */
  void doSaveAs();

  /** Perform action on file changed (e.g. renamed). */
  void onFileChanged();

  /**
   * Returns whether the contents of this part have changed since the last save operation.
   *
   * @return <code>true</code> if the contents have been modified and need saving, and <code>false
   *     </code> if they have not changed since the last save
   */
  boolean isDirty();

  /**
   * Add EditorPart close handler.
   *
   * @param closeHandler the instance of CloseHandler
   */
  void addCloseHandler(@NotNull EditorPartCloseHandler closeHandler);

  /** Call this method then editor became visible */
  void activate();

  /**
   * Closes this text editor after optionally saving changes.
   *
   * @param save <code>true</code> if unsaved changed should be saved, and <code>false</code> if
   *     unsaved changed should be discarded
   */
  void close(boolean save);

  /**
   * Called when part is going to closing. Part can deny closing, by calling {@code
   * callback#onFailure}.
   *
   * @param callback callback to allow or deny closing the part
   */
  void onClosing(AsyncCallback<Void> callback);
}
