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
package org.eclipse.che.plugin.pullrequest.client.actions;

import static java.util.Collections.singletonList;
import static org.eclipse.che.ide.api.parts.PartStackType.TOOLING;
import static org.eclipse.che.ide.part.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.parts.PartPresenter;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;
import org.eclipse.che.plugin.pullrequest.client.ContributeMessages;
import org.eclipse.che.plugin.pullrequest.client.ContributeResources;
import org.eclipse.che.plugin.pullrequest.client.parts.contribute.ContributePartPresenter;

/**
 * Switches Contribute part display mode depends on the current state of IDE:
 *
 * <ul>
 *   <li>Contribute part is invisible -> make it visible and active
 *   <li>Contribute part is active -> make it invisible
 *   <li>Contribute part is inactive -> make it active
 * </ul>
 *
 * @author Roman Nikitenko
 */
@Singleton
public class ContributePartDisplayingModeAction extends AbstractPerspectiveAction {
  private EditorAgent editorAgent;
  private WorkspaceAgent workspaceAgent;
  private Provider<ContributePartPresenter> contributePartPresenterProvider;

  @Inject
  public ContributePartDisplayingModeAction(
      ContributeResources resources,
      EditorAgent editorAgent,
      WorkspaceAgent workspaceAgent,
      Provider<ContributePartPresenter> contributePartPresenterProvider,
      ContributeMessages localizedConstant) {
    super(
        singletonList(PROJECT_PERSPECTIVE_ID),
        localizedConstant.switchContributePartDisplayingTitle(),
        localizedConstant.switchContributePartDisplayingDescription(),
        resources.titleIcon());
    this.editorAgent = editorAgent;
    this.workspaceAgent = workspaceAgent;
    this.contributePartPresenterProvider = contributePartPresenterProvider;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    ContributePartPresenter contributePartPresenter = contributePartPresenterProvider.get();
    PartPresenter activePart = workspaceAgent.getActivePart();
    if (activePart != null && activePart instanceof ContributePartPresenter) {
      workspaceAgent.hidePart(contributePartPresenter);

      EditorPartPresenter activeEditor = editorAgent.getActiveEditor();
      if (activeEditor != null) {
        workspaceAgent.setActivePart(activeEditor);
      }
      return;
    }

    workspaceAgent.openPart(contributePartPresenter, TOOLING);
    workspaceAgent.setActivePart(contributePartPresenter);
  }

  @Override
  public void updateInPerspective(@NotNull ActionEvent event) {
    event.getPresentation().setEnabledAndVisible(true);
  }
}
