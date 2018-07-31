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
package org.eclipse.che.ide.part.editor.actions;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;

/**
 * Switch to previous opened editor based on current active.
 *
 * @author Vlad Zhukovskyi
 * @author Roman Nikitenko
 */
@Singleton
public class SwitchPreviousEditorAction extends EditorSwitchAction {

  @Inject
  public SwitchPreviousEditorAction(CoreLocalizationConstant constant, EditorAgent editorAgent) {
    super(
        constant.switchToLeftEditorAction(),
        constant.switchToLeftEditorActionDescription(),
        editorAgent);
  }

  /** {@inheritDoc} */
  @Override
  public void actionPerformed(ActionEvent event) {
    final EditorPartPresenter activeEditor = editorAgent.getActiveEditor();

    checkNotNull(activeEditor, "Null editor occurred");

    final EditorPartPresenter previousEditor = getPreviousEditorBaseOn(activeEditor);
    editorAgent.activateEditor(previousEditor);
  }
}
