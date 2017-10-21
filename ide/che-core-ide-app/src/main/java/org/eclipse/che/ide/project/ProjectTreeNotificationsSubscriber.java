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
package org.eclipse.che.ide.project;

import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.RUNNING;
import static org.eclipse.che.api.project.shared.dto.event.ProjectTreeTrackingOperationDto.Type.START;
import static org.eclipse.che.ide.api.jsonrpc.Constants.WS_AGENT_JSON_RPC_ENDPOINT_ID;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
import org.eclipse.che.api.project.shared.dto.event.ProjectTreeTrackingOperationDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.workspace.event.WsAgentServerRunningEvent;
import org.eclipse.che.ide.bootstrap.BasicIDEInitializedEvent;
import org.eclipse.che.ide.dto.DtoFactory;

/** Subscribes on receiving notifications about any changes in the project tree. */
@Singleton
public class ProjectTreeNotificationsSubscriber {

  private final RequestTransmitter requestTransmitter;
  private final DtoFactory dtoFactory;

  @Inject
  public ProjectTreeNotificationsSubscriber(
      EventBus eventBus,
      AppContext appContext,
      RequestTransmitter requestTransmitter,
      DtoFactory dtoFactory) {
    this.requestTransmitter = requestTransmitter;
    this.dtoFactory = dtoFactory;

    eventBus.addHandler(WsAgentServerRunningEvent.TYPE, event -> subscribe());

    // in case ws-agent is already running
    eventBus.addHandler(
        BasicIDEInitializedEvent.TYPE,
        event -> {
          if (appContext.getWorkspace().getStatus() == RUNNING) {
            subscribe();
          }
        });
  }

  private void subscribe() {
    ProjectTreeTrackingOperationDto params =
        dtoFactory.createDto(ProjectTreeTrackingOperationDto.class).withPath("/").withType(START);

    requestTransmitter
        .newRequest()
        .endpointId(WS_AGENT_JSON_RPC_ENDPOINT_ID)
        .methodName("track/project-tree")
        .paramsAsDto(params)
        .sendAndSkipResult();
  }
}
