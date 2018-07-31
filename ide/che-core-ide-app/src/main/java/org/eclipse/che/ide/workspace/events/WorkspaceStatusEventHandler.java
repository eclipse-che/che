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
package org.eclipse.che.ide.workspace.events;

import static com.google.common.base.Strings.nullToEmpty;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.RUNNING;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.STARTING;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.STOPPED;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.STOPPING;
import static org.eclipse.che.api.workspace.shared.Constants.WORKSPACE_STATUS_CHANGED_METHOD;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;
import org.eclipse.che.api.workspace.shared.dto.event.WorkspaceStatusEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.workspace.event.WorkspaceRunningEvent;
import org.eclipse.che.ide.api.workspace.event.WorkspaceStartingEvent;
import org.eclipse.che.ide.api.workspace.event.WorkspaceStoppedEvent;
import org.eclipse.che.ide.api.workspace.event.WorkspaceStoppingEvent;
import org.eclipse.che.ide.context.AppContextImpl;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.ide.workspace.WorkspaceServiceClient;

/**
 * Receives notifications about changing workspace's status. After a notification is received it is
 * processed and an appropriate event is fired on the {@link EventBus}.
 */
@Singleton
class WorkspaceStatusEventHandler {

  private final WorkspaceServiceClient workspaceServiceClient;
  private final AppContext appContext;
  private final EventBus eventBus;

  @Inject
  WorkspaceStatusEventHandler(
      RequestHandlerConfigurator configurator,
      WorkspaceServiceClient workspaceServiceClient,
      AppContext appContext,
      EventBus eventBus) {
    this.workspaceServiceClient = workspaceServiceClient;
    this.appContext = appContext;
    this.eventBus = eventBus;

    configurator
        .newConfiguration()
        .methodName(WORKSPACE_STATUS_CHANGED_METHOD)
        .paramsAsDto(WorkspaceStatusEvent.class)
        .noResult()
        .withBiConsumer((endpointId, event) -> processStatus(event));
  }

  private void processStatus(WorkspaceStatusEvent event) {
    workspaceServiceClient
        .getWorkspace(appContext.getWorkspaceId())
        .then(
            workspace -> {
              try {
                // Update workspace model in AppContext before firing an event.
                // Because AppContext always must return an actual workspace model.
                ((AppContextImpl) appContext).setWorkspace(workspace);

                if (event.getStatus() == STARTING) {
                  eventBus.fireEvent(new WorkspaceStartingEvent());
                } else if (event.getStatus() == RUNNING) {
                  eventBus.fireEvent(new WorkspaceRunningEvent());
                } else if (event.getStatus() == STOPPING) {
                  eventBus.fireEvent(new WorkspaceStoppingEvent());
                } else if (event.getStatus() == STOPPED) {
                  eventBus.fireEvent(
                      new WorkspaceStoppedEvent(
                          event.getError() != null, nullToEmpty(event.getError())));
                }
              } catch (Exception e) {
                Log.error(WorkspaceStatusEventHandler.class, "Error: " + e.getMessage(), e);
              }
            });
  }
}
