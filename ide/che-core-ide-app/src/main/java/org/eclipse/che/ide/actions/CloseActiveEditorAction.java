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

import static java.util.Collections.singletonList;
import static org.eclipse.che.ide.part.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.editor.EditorAgent;

/**
 * General action which listens current active editor and closes it if need.
 *
 * @author Vlad Zhukovskiy
 */
@Singleton
public class CloseActiveEditorAction extends AbstractPerspectiveAction {

  private final EditorAgent editorAgent;

  @Inject
  public CloseActiveEditorAction(CoreLocalizationConstant locale, EditorAgent editorAgent) {
    super(
        singletonList(PROJECT_PERSPECTIVE_ID),
        locale.editorTabClose(),
        locale.editorTabCloseDescription());
    this.editorAgent = editorAgent;
  }

  /** {@inheritDoc} */
  @Override
  public void updateInPerspective(@NotNull ActionEvent event) {
    event.getPresentation().setEnabledAndVisible(editorAgent.getActiveEditor() != null);
  }

  /** {@inheritDoc} */
  @Override
  public void actionPerformed(ActionEvent e) {
    editorAgent.closeEditor(editorAgent.getActiveEditor());
  }
}
