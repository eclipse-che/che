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
package org.eclipse.che.ide.api.event.ng;

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonMap;
import static org.eclipse.che.api.project.shared.dto.event.ProjectTreeTrackingOperationDto.Type.START;

import com.google.gwt.user.client.Timer;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import java.util.Set;
import java.util.function.Consumer;
import javax.inject.Singleton;
import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
import org.eclipse.che.api.machine.shared.dto.execagent.event.DtoWithPid;
import org.eclipse.che.api.project.shared.dto.event.ProjectTreeTrackingOperationDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.machine.DevMachine;
import org.eclipse.che.ide.api.machine.ExecAgentCommandManager;
import org.eclipse.che.ide.api.machine.MachineEntity;
import org.eclipse.che.ide.api.machine.events.WsAgentStateEvent;
import org.eclipse.che.ide.api.machine.events.WsAgentStateHandler;
import org.eclipse.che.ide.api.workspace.event.EnvironmentOutputEvent;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.jsonrpc.JsonRpcInitializer;
import org.eclipse.che.ide.util.loging.Log;

/** @author Dmitry Kuleshov */
@Singleton
public class JsonRpcWebSocketAgentEventListener implements WsAgentStateHandler {
  private final JsonRpcInitializer initializer;
  private final AppContext appContext;
  private final EventBus eventBus;
  private final RequestTransmitter requestTransmitter;
  private final DtoFactory dtoFactory;
  private final ExecAgentCommandManager execAgentCommandManager;

  @Inject
  public JsonRpcWebSocketAgentEventListener(
      JsonRpcInitializer initializer,
      AppContext appContext,
      EventBus eventBus,
      RequestTransmitter requestTransmitter,
      DtoFactory dtoFactory,
      ExecAgentCommandManager execAgentCommandManager) {
    this.appContext = appContext;
    this.initializer = initializer;
    this.eventBus = eventBus;
    this.requestTransmitter = requestTransmitter;
    this.dtoFactory = dtoFactory;
    this.execAgentCommandManager = execAgentCommandManager;

    eventBus.addHandler(WsAgentStateEvent.TYPE, this);
  }

  @Override
  public void onWsAgentStarted(WsAgentStateEvent event) {
    initializeJsonRpc();
    initializeTreeExplorerFileWatcher();
    initializeGitCheckoutWatcher();
    initializeGitIndexWatcher();
    subscribeToGitEvents();
  }

  private void initializeJsonRpc() {
    Log.debug(JsonRpcWebSocketAgentEventListener.class, "Web socket agent started event caught.");
    try {
      internalInitialize();
    } catch (Exception e) {
      Log.debug(JsonRpcWebSocketAgentEventListener.class, "Failed, will try one more time.");
      new Timer() {
        @Override
        public void run() {
          internalInitialize();
        }
      }.schedule(1_000);
    }
  }

  private void internalInitialize() {
    DevMachine devMachine = appContext.getDevMachine();
    String devMachineId = devMachine.getId();
    String wsAgentWebSocketUrl = devMachine.getWsAgentWebSocketUrl();
    String wsAgentUrl = wsAgentWebSocketUrl.replaceFirst("api/ws", "wsagent");
    String execAgentUrl = devMachine.getExecAgentUrl();
    String separator = wsAgentUrl.contains("?") ? "&" : "?";
    String queryParams =
        appContext.getApplicationWebsocketId().map(id -> separator + "clientId=" + id).orElse("");
    Set<Runnable> initActions =
        appContext.getApplicationWebsocketId().isPresent()
            ? emptySet()
            : singleton(this::processWsId);

    initializer.initialize("ws-agent", singletonMap("url", wsAgentUrl + queryParams), initActions);
    initializer.initialize(devMachineId, singletonMap("url", execAgentUrl));

    for (MachineEntity machineEntity : appContext.getActiveRuntime().getMachines()) {
      if (!machineEntity.isDev()) {
        initializer.initialize(
            machineEntity.getId(), singletonMap("url", machineEntity.getExecAgentUrl()));
        execAgentCommandManager
            .getProcesses(machineEntity.getId(), false)
            .onSuccess(
                processes -> {
                  Consumer<Integer> pidConsumer =
                      pid ->
                          execAgentCommandManager
                              .getProcessLogs(machineEntity.getId(), pid, null, null, 50, 0)
                              .onSuccess(
                                  logs ->
                                      logs.forEach(
                                          log -> {
                                            String fixedLog =
                                                log.getText().replaceAll("\\[STDOUT\\] ", "");
                                            String machineName = machineEntity.getDisplayName();
                                            eventBus.fireEvent(
                                                new EnvironmentOutputEvent(fixedLog, machineName));
                                          }));

                  processes
                      .stream()
                      .filter(it -> "CheWsAgent".equals(it.getName()))
                      .map(DtoWithPid::getPid)
                      .forEach(pidConsumer);
                });
      }
    }
  }

  private void processWsId() {
    requestTransmitter
        .newRequest()
        .endpointId("ws-agent")
        .methodName("websocketIdService/getId")
        .noParams()
        .sendAndReceiveResultAsString()
        .onSuccess(appContext::setApplicationWebsocketId);
  }

  private void initializeTreeExplorerFileWatcher() {
    ProjectTreeTrackingOperationDto params =
        dtoFactory.createDto(ProjectTreeTrackingOperationDto.class).withPath("/").withType(START);

    requestTransmitter
        .newRequest()
        .endpointId("ws-agent")
        .methodName("track/project-tree")
        .paramsAsDto(params)
        .sendAndSkipResult();
  }

  private void initializeGitCheckoutWatcher() {
    requestTransmitter
        .newRequest()
        .endpointId("ws-agent")
        .methodName("track/git-checkout")
        .noParams()
        .sendAndSkipResult();
  }

  private void initializeGitIndexWatcher() {
    requestTransmitter
        .newRequest()
        .endpointId("ws-agent")
        .methodName("track/git-index")
        .noParams()
        .sendAndSkipResult();
  }

  private void subscribeToGitEvents() {
    requestTransmitter
        .newRequest()
        .endpointId("ws-agent")
        .methodName("event/git/subscribe")
        .noParams()
        .sendAndSkipResult();
  }

  @Override
  public void onWsAgentStopped(WsAgentStateEvent event) {
    DevMachine devMachine = appContext.getDevMachine();
    String devMachineId = devMachine.getId();

    Log.debug(JsonRpcWebSocketAgentEventListener.class, "Web socket agent stopped event caught.");

    initializer.terminate("ws-agent");
    initializer.terminate(devMachineId);
  }
}
