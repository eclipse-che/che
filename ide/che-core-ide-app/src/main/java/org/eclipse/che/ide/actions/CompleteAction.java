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

import com.google.inject.Inject;
import com.google.inject.Singleton;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.texteditor.HandlesTextOperations;
import org.eclipse.che.ide.api.editor.texteditor.TextEditorOperations;

/**
 * Calls editor complete(Ctrl+Space)
 *
 * @author Evgen Vidolob
 */
@Singleton
public class CompleteAction extends AbstractPerspectiveAction {

  private EditorAgent editorAgent;

  @Inject
  public CompleteAction(
      CoreLocalizationConstant coreLocalizationConstant, EditorAgent editorAgent) {
    super(null, coreLocalizationConstant.actionCompetitionsTitle());
    this.editorAgent = editorAgent;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    EditorPartPresenter activeEditor = editorAgent.getActiveEditor();
    if (activeEditor instanceof HandlesTextOperations) {
      ((HandlesTextOperations) activeEditor).doOperation(TextEditorOperations.CODEASSIST_PROPOSALS);
    }
  }

  @Override
  public void updateInPerspective(@NotNull ActionEvent e) {
    EditorPartPresenter activeEditor = editorAgent.getActiveEditor();
    if (activeEditor != null) {
      if (activeEditor instanceof HandlesTextOperations) {
        e.getPresentation().setVisible(true);
        if (((HandlesTextOperations) activeEditor)
            .canDoOperation(TextEditorOperations.CODEASSIST_PROPOSALS)) {
          e.getPresentation().setEnabled(true);
        } else {
          e.getPresentation().setEnabled(false);
        }
      }
    } else {
      e.getPresentation().setEnabledAndVisible(false);
    }
  }
}
