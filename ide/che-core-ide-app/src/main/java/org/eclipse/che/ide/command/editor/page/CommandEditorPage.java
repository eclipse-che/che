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
package org.eclipse.che.ide.command.editor.page;

import com.google.gwt.user.client.ui.IsWidget;
import org.eclipse.che.ide.api.command.CommandImpl;
import org.eclipse.che.ide.command.editor.CommandEditor;

/**
 * Defines the requirements for the page for {@link CommandEditor}.
 *
 * @author Artem Zatsarynnyi
 * @see CommandEditor
 */
public interface CommandEditorPage {

  /** Returns page's title. */
  String getTitle();

  /** Returns page's view. */
  IsWidget getView();

  /**
   * This method is called every time when command is opening in the editor. Typically, implementor
   * should hold the given {@code command} instance for subsequent modifying it directly and do
   * pages's initial setup.
   */
  void edit(CommandImpl command);

  /**
   * Whether the page has been modified or not?
   *
   * @return {@code true} if page is modified, and {@code false} - otherwise
   */
  boolean isDirty();

  /** Sets {@link DirtyStateListener}. */
  void setDirtyStateListener(DirtyStateListener listener);

  /** This method is called when focus need to be set up in the given page. */
  default void focus() {}

  /**
   * Listener that should be called by page every time when any command modifications on the page
   * have been performed.
   */
  interface DirtyStateListener {
    void onDirtyStateChanged();
  }
}
