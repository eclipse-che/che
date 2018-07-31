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
package org.eclipse.che.ide.workspace;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Collections.singletonList;
import static org.eclipse.che.ide.part.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

import com.google.inject.Inject;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.statepersistance.AppStateManager;

/**
 * The class contains business logic to stop workspace.
 *
 * @author Dmitry Shnurenko
 */
public class StopWorkspaceAction extends AbstractPerspectiveAction {

  private AppStateManager appStateManager;
  private final CurrentWorkspaceManager workspaceManager;
  private final AppContext appContext;

  @Inject
  public StopWorkspaceAction(
      CoreLocalizationConstant locale,
      AppContext appContext,
      AppStateManager appStateManager,
      CurrentWorkspaceManager workspaceManager) {
    super(singletonList(PROJECT_PERSPECTIVE_ID), locale.stopWsTitle(), locale.stopWsDescription());
    this.appContext = appContext;
    this.appStateManager = appStateManager;
    this.workspaceManager = workspaceManager;
  }

  @Override
  public void updateInPerspective(@NotNull ActionEvent event) {
    event.getPresentation().setVisible(true);
    event.getPresentation().setEnabled(appContext.getWorkspace().getRuntime() != null);
  }

  /** {@inheritDoc} */
  @Override
  public void actionPerformed(ActionEvent event) {
    checkNotNull(appContext.getWorkspace().getId(), "Workspace id should not be null");
    appStateManager
        .persistState()
        .then(
            arg -> {
              workspaceManager.stopWorkspace();
            });
  }
}
