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
package org.eclipse.che.plugin.languageserver.ide.quickopen;

import org.eclipse.che.ide.api.editor.text.TextRange;
import org.eclipse.che.plugin.languageserver.ide.util.OpenFileInEditorHelper;

/**
 * Quick open entry that can be opened in editor.
 *
 * @author Evgen Vidolob
 */
public class EditorQuickOpenEntry extends QuickOpenEntry {

  private final OpenFileInEditorHelper editorHelper;

  public EditorQuickOpenEntry(OpenFileInEditorHelper editorHelper) {
    this.editorHelper = editorHelper;
  }

  protected String getFilePath() {
    return null;
  }

  protected TextRange getTextRange() {
    return null;
  }

  @Override
  public boolean run(Mode mode) {
    if (mode == Mode.OPEN) {
      String filePath = getFilePath();
      editorHelper.openFile(filePath, getTextRange());
      return true;
    }
    return false;
  }
}
