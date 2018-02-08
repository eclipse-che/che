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
package org.eclipse.che.ide.processes.loading;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Collections;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.part.perspectives.project.ProjectPerspective;

/** Action to show workspace status. */
@Singleton
public class ShowWorkspaceStatusAction extends AbstractPerspectiveAction {

  private WorkspaceLoadingTrackerImpl workspaceLoadingTracker;

  @Inject
  public ShowWorkspaceStatusAction(
      CoreLocalizationConstant localizationConstant,
      WorkspaceLoadingTrackerImpl workspaceLoadingTracker) {
    super(
        Collections.singletonList(ProjectPerspective.PROJECT_PERSPECTIVE_ID),
        localizationConstant.workspaceStatusTitle());
    this.workspaceLoadingTracker = workspaceLoadingTracker;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    workspaceLoadingTracker.showPanel();
  }

  @Override
  public void updateInPerspective(@NotNull ActionEvent event) {}
}
