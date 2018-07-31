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
package org.eclipse.che.ide.actions;

import static org.eclipse.che.ide.part.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

import com.google.inject.Inject;
import java.util.Arrays;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.texteditor.HandlesUndoRedo;
import org.eclipse.che.ide.api.editor.texteditor.UndoableEditor;

/**
 * Undo Action
 *
 * @author Roman Nikitenko
 * @author Dmitry Shnurenko
 */
public class UndoAction extends AbstractPerspectiveAction {

  private EditorAgent editorAgent;

  @Inject
  public UndoAction(
      EditorAgent editorAgent, CoreLocalizationConstant localization, Resources resources) {
    super(
        Arrays.asList(PROJECT_PERSPECTIVE_ID),
        localization.undoName(),
        localization.undoDescription(),
        resources.undo());
    this.editorAgent = editorAgent;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    EditorPartPresenter activeEditor = editorAgent.getActiveEditor();

    if (activeEditor != null && activeEditor instanceof UndoableEditor) {
      final HandlesUndoRedo undoRedo = ((UndoableEditor) activeEditor).getUndoRedo();
      if (undoRedo != null) {
        undoRedo.undo();
      }
    }
  }

  @Override
  public void updateInPerspective(@NotNull ActionEvent event) {
    EditorPartPresenter activeEditor = editorAgent.getActiveEditor();

    boolean mustEnable = false;
    if (activeEditor != null && activeEditor instanceof UndoableEditor) {
      final HandlesUndoRedo undoRedo = ((UndoableEditor) activeEditor).getUndoRedo();
      if (undoRedo != null) {
        mustEnable = undoRedo.undoable();
      }
    }
    event.getPresentation().setEnabled(mustEnable);
  }
}
