/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.api.event.ng;

import com.google.gwt.user.client.Timer;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
import org.eclipse.che.api.machine.shared.dto.execagent.event.DtoWithPid;
import org.eclipse.che.api.project.shared.dto.event.ProjectTreeTrackingOperationDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.machine.ExecAgentCommandManager;
import org.eclipse.che.ide.api.machine.events.WsAgentStateEvent;
import org.eclipse.che.ide.api.machine.events.WsAgentStateHandler;
import org.eclipse.che.ide.api.workspace.event.EnvironmentOutputEvent;
import org.eclipse.che.ide.api.workspace.model.MachineImpl;
import org.eclipse.che.ide.api.workspace.model.RuntimeImpl;
import org.eclipse.che.ide.api.workspace.model.ServerImpl;
import org.eclipse.che.ide.api.workspace.model.WorkspaceImpl;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.jsonrpc.JsonRpcInitializer;
import org.eclipse.che.ide.util.loging.Log;

import javax.inject.Singleton;
import java.util.Optional;
import java.util.function.Consumer;

import static java.util.Collections.singletonMap;
import static org.eclipse.che.api.machine.shared.Constants.EXEC_AGENT_REFERENCE;
import static org.eclipse.che.api.machine.shared.Constants.WSAGENT_REFERENCE;
import static org.eclipse.che.api.project.shared.dto.event.ProjectTreeTrackingOperationDto.Type.START;

/**
 * @author Dmitry Kuleshov
 */
@Singleton
public class JsonRpcWebSocketAgentEventListener implements WsAgentStateHandler {
    private final JsonRpcInitializer      initializer;
    private final AppContext              appContext;
    private final EventBus                eventBus;
    private final RequestTransmitter      requestTransmitter;
    private final DtoFactory              dtoFactory;
    private final ExecAgentCommandManager execAgentCommandManager;

    @Inject
    public JsonRpcWebSocketAgentEventListener(JsonRpcInitializer initializer,
                                              AppContext appContext, EventBus eventBus,
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
        final WorkspaceImpl workspace = appContext.getWorkspace();
        final RuntimeImpl runtime = workspace.getRuntime();

        if (runtime == null) {
            return;
        }

        for (MachineImpl machine : runtime.getMachines().values()) {
            machine.getServerByName(EXEC_AGENT_REFERENCE)
                   .ifPresent(server -> {
                       String execAgentServerURL = server.getUrl();
                       execAgentServerURL = execAgentServerURL.replaceFirst("http", "ws") + "/connect"; // FIXME: spi ide
                       initializer.initialize(machine.getName(), singletonMap("url", execAgentServerURL));
                   });

            final Optional<ServerImpl> wsAgentServer = machine.getServerByName(WSAGENT_REFERENCE);

            if (wsAgentServer.isPresent()) {
                final String wsAgentBaseUrl = wsAgentServer.get().getUrl() + "/api"; // FIXME: spi ide
                final String wsAgentWebSocketUrl = wsAgentBaseUrl.replaceFirst("http", "ws") + "/ws"; // FIXME: spi ide
                final String wsAgentUrl = wsAgentWebSocketUrl.replaceFirst("(api)(/)(ws)", "websocket" + "$2" + appContext.getAppId());

                initializer.initialize("ws-agent", singletonMap("url", wsAgentUrl));
            } else {
                execAgentCommandManager.getProcesses(machine.getName(), false)
                                       .onSuccess(processes -> {
                                           Consumer<Integer> pidConsumer = pid -> execAgentCommandManager
                                                   .getProcessLogs(machine.getName(), pid, null, null, 50, 0)
                                                   .onSuccess(logs -> logs.forEach(log -> {
                                                       String fixedLog = log.getText().replaceAll("\\[STDOUT\\] ", "");
                                                       String machineName = machine.getName();
                                                       eventBus.fireEvent(new EnvironmentOutputEvent(fixedLog, machineName));
                                                   }));

                                           processes.stream()
                                                    .filter(it -> "CheWsAgent".equals(it.getName()))
                                                    .map(DtoWithPid::getPid)
                                                    .forEach(pidConsumer);
                                       });
            }
        }
    }

    private void initializeTreeExplorerFileWatcher() {
        ProjectTreeTrackingOperationDto params = dtoFactory.createDto(ProjectTreeTrackingOperationDto.class)
                                                           .withPath("/")
                                                           .withType(START);

        requestTransmitter.newRequest()
                          .endpointId("ws-agent")
                          .methodName("track:project-tree")
                          .paramsAsDto(params)
                          .sendAndSkipResult();

    }

    private void initializeGitCheckoutWatcher() {
        requestTransmitter.newRequest()
                          .endpointId("ws-agent")
                          .methodName("track:git-checkout")
                          .noParams()
                          .sendAndSkipResult();
    }

    @Override
    public void onWsAgentStopped(WsAgentStateEvent event) {
        final WorkspaceImpl workspace = appContext.getWorkspace();
        final Optional<MachineImpl> devMachine = workspace.getDevMachine();

        if (!devMachine.isPresent()) {
            return;
        }

        final String devMachineName = devMachine.get().getName();

        Log.debug(JsonRpcWebSocketAgentEventListener.class, "Web socket agent stopped event caught.");

        initializer.terminate("ws-agent");
        initializer.terminate(devMachineName);
    }
}
