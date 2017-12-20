/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
 * Redo Action
 *
 * @author Roman Nikitenko
 * @author Dmitry Shnurenko
 */
public class RedoAction extends AbstractPerspectiveAction {

  private EditorAgent editorAgent;

  @Inject
  public RedoAction(
      EditorAgent editorAgent, CoreLocalizationConstant localization, Resources resources) {
    super(
        Arrays.asList(PROJECT_PERSPECTIVE_ID),
        localization.redoName(),
        localization.redoDescription(),
        resources.redo());
    this.editorAgent = editorAgent;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    EditorPartPresenter activeEditor = editorAgent.getActiveEditor();

    if (activeEditor != null && activeEditor instanceof UndoableEditor) {
      final HandlesUndoRedo undoRedo = ((UndoableEditor) activeEditor).getUndoRedo();
      if (undoRedo != null) {
        undoRedo.redo();
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
        mustEnable = undoRedo.redoable();
      }
    }
    event.getPresentation().setEnabled(mustEnable);
  }
}
