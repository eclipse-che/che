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
package org.eclipse.che.ide.projectimport;

import static org.eclipse.che.api.core.model.workspace.runtime.ServerStatus.RUNNING;
import static org.eclipse.che.api.project.shared.Constants.EVENT_IMPORT_OUTPUT_SUBSCRIBE;
import static org.eclipse.che.api.project.shared.Constants.EVENT_IMPORT_OUTPUT_UN_SUBSCRIBE;
import static org.eclipse.che.api.workspace.shared.Constants.SERVER_WS_AGENT_HTTP_REFERENCE;
import static org.eclipse.che.ide.api.jsonrpc.Constants.WS_AGENT_JSON_RPC_ENDPOINT_ID;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.workspace.event.WsAgentServerRunningEvent;
import org.eclipse.che.ide.api.workspace.event.WsAgentServerStoppedEvent;
import org.eclipse.che.ide.api.workspace.model.ServerImpl;
import org.eclipse.che.ide.bootstrap.BasicIDEInitializedEvent;

/**
 * Subscriber that register and deregister a listener for import project progress.
 *
 * @author Vlad Zhukovskyi
 */
@Singleton
public class ProjectImportNotificationSubscriber {

  private RequestTransmitter transmitter;

  @Inject
  public ProjectImportNotificationSubscriber(
      EventBus eventBus, AppContext appContext, RequestTransmitter transmitter) {
    this.transmitter = transmitter;

    eventBus.addHandler(WsAgentServerRunningEvent.TYPE, event -> subscribe());
    eventBus.addHandler(WsAgentServerStoppedEvent.TYPE, event -> unsubscribe());

    eventBus.addHandler(
        BasicIDEInitializedEvent.TYPE,
        event ->
            appContext
                .getWorkspace()
                .getDevMachine()
                .flatMap(machine -> machine.getServerByName(SERVER_WS_AGENT_HTTP_REFERENCE))
                .map(ServerImpl::getStatus)
                .filter(RUNNING::equals)
                .ifPresent(it -> subscribe()));
  }

  private void subscribe() {
    transmitter
        .newRequest()
        .endpointId(WS_AGENT_JSON_RPC_ENDPOINT_ID)
        .methodName(EVENT_IMPORT_OUTPUT_SUBSCRIBE)
        .noParams()
        .sendAndSkipResult();
  }

  private void unsubscribe() {
    transmitter
        .newRequest()
        .endpointId(WS_AGENT_JSON_RPC_ENDPOINT_ID)
        .methodName(EVENT_IMPORT_OUTPUT_UN_SUBSCRIBE)
        .noParams()
        .sendAndSkipResult();
  }
}
