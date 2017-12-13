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
package org.eclipse.che.ide.actions.switching;

import static java.util.Collections.singletonList;
import static org.eclipse.che.ide.api.parts.PartStackType.NAVIGATION;
import static org.eclipse.che.ide.part.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.parts.PartPresenter;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;

/**
 * Switches Project Explorer display mode depends on the current state of IDE:
 *
 * <ul>
 *   <li>Project Explorer is invisible -> make it visible and active
 *   <li>Project Explorer is active -> make it invisible
 *   <li>Project Explorer is inactive -> make it active
 * </ul>
 *
 * @author Roman Nikitenko
 */
@Singleton
public class ProjectExplorerDisplayingModeAction extends AbstractPerspectiveAction {
  private EditorAgent editorAgent;
  private WorkspaceAgent workspaceAgent;
  private Provider<ProjectExplorerPresenter> projectExplorerPresenterProvider;

  @Inject
  public ProjectExplorerDisplayingModeAction(
      Resources resources,
      EditorAgent editorAgent,
      CoreLocalizationConstant localizedConstant,
      WorkspaceAgent workspaceAgent,
      Provider<ProjectExplorerPresenter> projectExplorerPresenterProvider) {
    super(
        singletonList(PROJECT_PERSPECTIVE_ID),
        localizedConstant.switchProjectExplorerDisplayingTitle(),
        localizedConstant.switchProjectExplorerDisplayingDescription(),
        resources.projectExplorerPartIcon());
    this.editorAgent = editorAgent;
    this.workspaceAgent = workspaceAgent;
    this.projectExplorerPresenterProvider = projectExplorerPresenterProvider;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    ProjectExplorerPresenter projectExplorerPresenter = projectExplorerPresenterProvider.get();
    PartPresenter activePart = workspaceAgent.getActivePart();
    if (activePart != null && activePart instanceof ProjectExplorerPresenter) {
      workspaceAgent.hidePart(projectExplorerPresenter);

      EditorPartPresenter activeEditor = editorAgent.getActiveEditor();
      if (activeEditor != null) {
        workspaceAgent.setActivePart(activeEditor);
      }
      return;
    }

    workspaceAgent.openPart(projectExplorerPresenter, NAVIGATION);
    workspaceAgent.setActivePart(projectExplorerPresenter);
  }

  @Override
  public void updateInPerspective(@NotNull ActionEvent event) {
    event.getPresentation().setEnabledAndVisible(true);
  }
}
