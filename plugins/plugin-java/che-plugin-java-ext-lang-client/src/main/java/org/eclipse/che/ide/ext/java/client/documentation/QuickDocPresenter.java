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
package org.eclipse.che.ide.ext.java.client.documentation;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.editor.orion.client.OrionEditorWidget;
import org.eclipse.che.ide.util.loging.Log;

/** @author Evgen Vidolob */
@Singleton
public class QuickDocPresenter implements QuickDocumentation {

  private final EditorAgent editorAgent;

  @Inject
  public QuickDocPresenter(EditorAgent editorAgent) {
    this.editorAgent = editorAgent;
  }

  @Override
  public void showDocumentation() {
    EditorPartPresenter activeEditor = editorAgent.getActiveEditor();
    if (activeEditor == null) {
      return;
    }

    if (!(activeEditor instanceof TextEditor)) {
      Log.error(getClass(), "Quick Document support only TextEditor as editor");
      return;
    }

    TextEditor editor = ((TextEditor) activeEditor);
    ((OrionEditorWidget) editor.getEditorWidget()).showTooltip();
  }
}
