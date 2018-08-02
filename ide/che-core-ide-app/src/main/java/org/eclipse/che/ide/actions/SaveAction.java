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
package org.eclipse.che.ide.actions;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.ProjectAction;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.EditorWithAutoSave;

/**
 * Save editor content Action
 *
 * @author Evgen Vidolob
 */
@Singleton
public class SaveAction extends ProjectAction {

  private final EditorAgent editorAgent;

  @Inject
  public SaveAction(Resources resources, EditorAgent editorAgent) {
    super("Save", "Save changes for current file", resources.save());
    this.editorAgent = editorAgent;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    editorAgent.getActiveEditor().doSave();
  }

  @Override
  public void updateProjectAction(ActionEvent e) {
    EditorPartPresenter editor = editorAgent.getActiveEditor();
    if (editor != null) {
      if (editor instanceof EditorWithAutoSave) {
        if (((EditorWithAutoSave) editor).isAutoSaveEnabled()) {
          e.getPresentation().setEnabledAndVisible(false);
          return;
        }
      }
      e.getPresentation().setVisible(true);
      e.getPresentation().setEnabled(editor.isDirty());

    } else {
      e.getPresentation().setEnabledAndVisible(false);
    }
  }
}
