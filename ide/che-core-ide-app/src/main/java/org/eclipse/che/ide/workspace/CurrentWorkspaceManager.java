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

import static java.lang.Boolean.parseBoolean;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.STOPPED;
import static org.eclipse.che.api.workspace.shared.Constants.CHE_WORKSPACE_AUTO_START;
import static org.eclipse.che.ide.workspace.WorkspaceStatusNotification.Phase.STARTING_WORKSPACE_RUNTIME;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.workspace.event.WorkspaceStartingEvent;
import org.eclipse.che.ide.api.workspace.model.WorkspaceImpl;
import org.eclipse.che.ide.bootstrap.BasicIDEInitializedEvent;
import org.eclipse.che.ide.context.AppContextImpl;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;

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
  private final DialogFactory dialogFactory;
  private final CoreLocalizationConstant messages;
  private final WorkspaceStatusNotification wsStatusNotification;
  private final StartWorkspaceNotification startWorkspaceNotification;

  @Inject
  CurrentWorkspaceManager(
      WorkspaceServiceClient workspaceServiceClient,
      AppContext appContext,
      EventBus eventBus,
      DialogFactory dialogFactory,
      CoreLocalizationConstant messages,
      WorkspaceStatusNotification wsStatusNotification,
      StartWorkspaceNotification startWorkspaceNotification) {
    this.workspaceServiceClient = workspaceServiceClient;
    this.appContext = appContext;
    this.eventBus = eventBus;
    this.dialogFactory = dialogFactory;
    this.messages = messages;
    this.wsStatusNotification = wsStatusNotification;
    this.startWorkspaceNotification = startWorkspaceNotification;

    eventBus.addHandler(BasicIDEInitializedEvent.TYPE, e -> handleWorkspaceStatus());
  }

  /** Start the current workspace with a default environment. */
  Promise<Void> startWorkspace() {
    WorkspaceImpl workspace = appContext.getWorkspace();
    String defEnvName = workspace.getConfig().getDefaultEnv();

    return workspaceServiceClient
        .startById(workspace.getId(), defEnvName)
        .then(
            ws -> {
              ((AppContextImpl) appContext).setWorkspace(ws);
              eventBus.fireEvent(new WorkspaceStartingEvent());
            })
        .then((Function<WorkspaceImpl, Void>) arg -> null)
        .catchError(
            error -> {
              dialogFactory
                  .createMessageDialog(messages.startWsErrorTitle(), error.getMessage(), null)
                  .show();

              wsStatusNotification.setError(STARTING_WORKSPACE_RUNTIME);
              startWorkspaceNotification.show();
            });
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
                  startWorkspace();
                }
              });
    }
  }
}
