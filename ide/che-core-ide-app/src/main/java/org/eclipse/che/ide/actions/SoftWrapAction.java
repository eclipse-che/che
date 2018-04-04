/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.actions;

import com.google.inject.Inject;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.ToggleAction;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.texteditor.CanWrapLines;

/**
 * Toggle wrapping long lines action
 *
 * @author Vitaliy Guliy
 */
public class SoftWrapAction extends ToggleAction {

  private EditorAgent editorAgent;

  @Inject
  public SoftWrapAction(EditorAgent editorAgent, CoreLocalizationConstant localization) {
    super(localization.softWrap());
    this.editorAgent = editorAgent;
  }

  @Override
  public boolean isSelected(ActionEvent e) {
    EditorPartPresenter activeEditor = editorAgent.getActiveEditor();
    if (activeEditor != null && activeEditor instanceof CanWrapLines) {
      return ((CanWrapLines) activeEditor).isWrapLines();
    }

    return false;
  }

  @Override
  public void setSelected(ActionEvent e, boolean state) {
    EditorPartPresenter activeEditor = editorAgent.getActiveEditor();
    if (activeEditor != null && activeEditor instanceof CanWrapLines) {
      ((CanWrapLines) activeEditor).toggleWrapLines();
    }
  }

  @Override
  public void update(ActionEvent e) {
    EditorPartPresenter activeEditor = editorAgent.getActiveEditor();

    if (activeEditor != null && activeEditor instanceof CanWrapLines) {
      e.getPresentation().setEnabled(true);
    } else {
      e.getPresentation().setEnabled(false);
    }
  }
}
