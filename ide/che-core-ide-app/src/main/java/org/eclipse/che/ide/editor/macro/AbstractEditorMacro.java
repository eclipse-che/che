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
package org.eclipse.che.ide.editor.macro;

import com.google.common.annotations.Beta;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.macro.Macro;

/**
 * Base macro provider which belongs to the current opened editor. Provides easy access to the
 * opened virtual file to allow fetch necessary information to use in custom commands, preview urls,
 * etc.
 *
 * @author Vlad Zhukovskyi
 * @see EditorAgent
 * @see Macro
 * @see EditorCurrentFileNameMacro
 * @see EditorCurrentFilePathMacro
 * @see EditorCurrentFileRelativePathMacro
 * @see EditorCurrentProjectNameMacro
 * @see EditorCurrentProjectTypeMacro
 * @since 4.7.0
 */
@Beta
public abstract class AbstractEditorMacro implements Macro {

  private EditorAgent editorAgent;

  public AbstractEditorMacro(EditorAgent editorAgent) {
    this.editorAgent = editorAgent;
  }

  /**
   * Returns the active editor or null if no active editor was found.
   *
   * @return active editor or {@code null}
   */
  public EditorPartPresenter getActiveEditor() {
    return editorAgent.getActiveEditor();
  }
}
