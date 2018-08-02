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
package org.eclipse.che.ide.part.editor.actions;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Collections.singletonList;
import static org.eclipse.che.ide.part.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

import com.google.common.annotations.Beta;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;

/**
 * @author Vlad Zhukovskiy
 * @author Roman Nikitenko
 */
@Beta
abstract class EditorSwitchAction extends AbstractPerspectiveAction {

  protected final EditorAgent editorAgent;

  public EditorSwitchAction(String text, String description, EditorAgent editorAgent) {
    super(singletonList(PROJECT_PERSPECTIVE_ID), text, description, null, null);
    this.editorAgent = editorAgent;
  }

  @Override
  public void updateInPerspective(@NotNull ActionEvent event) {
    event.getPresentation().setVisible(true);
    event.getPresentation().setEnabled(editorAgent.getOpenedEditors().size() > 1);
  }

  protected EditorPartPresenter getPreviousEditorBaseOn(EditorPartPresenter editor) {
    checkArgument(editor != null);

    return editorAgent.getPreviousFor(editor);
  }

  protected EditorPartPresenter getNextEditorBaseOn(EditorPartPresenter editor) {
    checkArgument(editor != null);

    return editorAgent.getNextFor(editor);
  }
}
