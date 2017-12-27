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
package org.eclipse.che.ide.workspace.events;

import static org.eclipse.che.api.workspace.shared.Constants.MACHINE_STATUS_CHANGED_METHOD;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import java.util.Optional;
import java.util.function.BiConsumer;
import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;
import org.eclipse.che.api.workspace.shared.dto.event.MachineStatusEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.workspace.event.MachineFailedEvent;
import org.eclipse.che.ide.api.workspace.event.MachineRunningEvent;
import org.eclipse.che.ide.api.workspace.event.MachineStartingEvent;
import org.eclipse.che.ide.api.workspace.event.MachineStoppedEvent;
import org.eclipse.che.ide.api.workspace.model.MachineImpl;
import org.eclipse.che.ide.api.workspace.model.RuntimeImpl;
import org.eclipse.che.ide.api.workspace.model.WorkspaceImpl;
import org.eclipse.che.ide.context.AppContextImpl;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.ide.workspace.WorkspaceServiceClient;

/**
 * Receives notifications about changing machines' statuses. After a notification is received it is
 * processed and an appropriate event is fired on the {@link EventBus}.
 */
@Singleton
class MachineStatusEventHandler {

  private AppContext appContext;

  @Inject
  MachineStatusEventHandler(
      RequestHandlerConfigurator configurator,
      EventBus eventBus,
      AppContext appContext,
      WorkspaceServiceClient workspaceServiceClient) {
    this.appContext = appContext;

    BiConsumer<String, MachineStatusEvent> operation =
        (String endpointId, MachineStatusEvent event) -> {
          Log.debug(getClass(), "Received notification from endpoint: " + endpointId);

          final String machineName = event.getMachineName();
          final String workspaceId = event.getIdentity().getWorkspaceId();

          eventBus.fireEvent(new MachineStatusChangedEvent(machineName, event.getEventType()));

          workspaceServiceClient
              .getWorkspace(workspaceId)
              .then(
                  workspace -> {
                    RuntimeImpl workspaceRuntime = workspace.getRuntime();
                    if (workspaceRuntime == null) {
                      return;
                    }

                    // Update workspace model in AppContext before firing an event.
                    // Because AppContext always must return an actual workspace model.
                    ((AppContextImpl) appContext).setWorkspace(workspace);

                    switch (event.getEventType()) {
                      case STARTING:
                        getMachineByName(machineName)
                            .ifPresent(m -> eventBus.fireEvent(new MachineStartingEvent(m)));
                        break;
                      case RUNNING:
                        getMachineByName(machineName)
                            .ifPresent(m -> eventBus.fireEvent(new MachineRunningEvent(m)));
                        break;
                      case STOPPED:
                        getMachineByName(machineName)
                            .ifPresent(m -> eventBus.fireEvent(new MachineStoppedEvent(m)));
                        break;
                      case FAILED:
                        getMachineByName(machineName)
                            .ifPresent(
                                m ->
                                    eventBus.fireEvent(
                                        new MachineFailedEvent(m, event.getError())));
                        break;
                    }
                  });
        };

    configurator
        .newConfiguration()
        .methodName(MACHINE_STATUS_CHANGED_METHOD)
        .paramsAsDto(MachineStatusEvent.class)
        .noResult()
        .withBiConsumer(operation);
  }

  private Optional<MachineImpl> getMachineByName(String machineName) {
    final WorkspaceImpl workspace = appContext.getWorkspace();
    final RuntimeImpl runtime = workspace.getRuntime();

    if (runtime == null) {
      return Optional.empty();
    }

    return runtime.getMachineByName(machineName);
  }
}
