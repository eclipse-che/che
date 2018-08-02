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
package org.eclipse.che.ide.api.editor.autosave;

import org.eclipse.che.ide.api.editor.document.UseDocumentHandle;
import org.eclipse.che.ide.api.editor.events.DocumentChangedHandler;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;

/**
 * Editor content auto save functionality.
 *
 * @author Roman Nikitenko
 */
public interface AutoSaveMode extends DocumentChangedHandler, UseDocumentHandle {

  /** Installs auto save mode on the given editor. */
  void install(TextEditor editor);

  /** Removes auto save mode from editor. */
  void uninstall();

  /** Suspends auto save mode for editor content. */
  void suspend();

  /**
   * Resumes auto save mode for editor content and sets mode corresponding to 'Enable Autosave'
   * option in editor preferences.
   */
  void resume();

  /** Return true if auto save mode is activated, false otherwise. */
  boolean isActivated();

  enum Mode {
    /**
     * The state when auto save mode of editor content is turned on. Corresponds to the case when
     * the 'Enable Autosave' option in editor preferences is enabled.
     */
    ACTIVATED,

    /**
     * Corresponds to the state when auto save mode is suspended for processing some operations
     * (java refactoring, for example)
     */
    SUSPENDED,

    /**
     * The state when auto save mode of editor content is turned off. Corresponds to the case when
     * the 'Enable Autosave' option in editor preferences is disabled.
     */
    DEACTIVATED
  }
}
