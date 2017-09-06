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
package org.eclipse.che.ide.workspace;

import static java.lang.Boolean.parseBoolean;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.RUNNING;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.STARTING;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.STOPPED;
import static org.eclipse.che.api.workspace.shared.Constants.CHE_WORKSPACE_AUTO_START;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.workspace.event.WorkspaceRunningEvent;
import org.eclipse.che.ide.api.workspace.event.WorkspaceStartingEvent;
import org.eclipse.che.ide.api.workspace.model.WorkspaceImpl;
import org.eclipse.che.ide.bootstrap.BasicIDEInitializedEvent;
import org.eclipse.che.ide.context.AppContextImpl;
import org.eclipse.che.ide.resource.Path;

/**
 * Performs the routines required to start/stop the current workspace.
 *
 * <p>Also does an appropriate action on loading IDE depending on the current workspace's status
 * (starts stopped workspace).
 */
@Singleton
class CurrentWorkspaceManager {

  private final WorkspaceServiceClient workspaceServiceClient;
  private final AppContext appContext;
  private final EventBus eventBus;

  @Inject
  CurrentWorkspaceManager(
      WorkspaceServiceClient workspaceServiceClient, AppContext appContext, EventBus eventBus) {
    this.workspaceServiceClient = workspaceServiceClient;
    this.appContext = appContext;
    this.eventBus = eventBus;

    eventBus.addHandler(BasicIDEInitializedEvent.TYPE, e -> handleWorkspaceStatus());

    // TODO (spi ide): get from CHE_PROJECTS_ROOT environment variable
    ((AppContextImpl) appContext).setProjectsRoot(Path.valueOf("/projects"));
  }

  /** Start the current workspace with a default environment. */
  Promise<Void> startWorkspace(boolean restoreFromSnapshot) {
    WorkspaceImpl workspace = appContext.getWorkspace();
    String defEnvName = workspace.getConfig().getDefaultEnv();

    return workspaceServiceClient
        .startById(workspace.getId(), defEnvName, restoreFromSnapshot)
        .then(
            ws -> {
              ((AppContextImpl) appContext).setWorkspace(ws);

              if (ws.getStatus() == STARTING) {
                eventBus.fireEvent(new WorkspaceStartingEvent());
                // rarely possible case when workspace starts "immediately" in some infrastructures
              } else if (ws.getStatus() == RUNNING) {
                eventBus.fireEvent(new WorkspaceStartingEvent());
                eventBus.fireEvent(new WorkspaceRunningEvent());
              }
            })
        .then((Function<WorkspaceImpl, Void>) arg -> null);
  }

  /** Stop the current workspace. */
  void stopWorkspace() {
    workspaceServiceClient.stop(appContext.getWorkspaceId());
  }

  /** Does an appropriate action depending on the current workspace's status. */
  private void handleWorkspaceStatus() {
    WorkspaceImpl workspace = appContext.getWorkspace();

    if (workspace.getStatus() == STOPPED) {
      workspaceServiceClient
          .getSettings()
          .then(
              settings -> {
                if (parseBoolean(settings.getOrDefault(CHE_WORKSPACE_AUTO_START, "true"))) {
                  startWorkspace(false);
                }
              });
    }
  }
}
