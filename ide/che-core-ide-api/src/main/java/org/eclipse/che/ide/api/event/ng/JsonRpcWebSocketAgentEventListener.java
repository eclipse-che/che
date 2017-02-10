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

import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.Timer;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.project.shared.dto.event.ProjectTreeTrackingOperationDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.machine.DevMachine;
import org.eclipse.che.ide.api.machine.MachineEntity;
import org.eclipse.che.ide.api.machine.events.WsAgentStateEvent;
import org.eclipse.che.ide.api.machine.events.WsAgentStateHandler;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.jsonrpc.JsonRpcInitializer;
import org.eclipse.che.ide.jsonrpc.RequestTransmitter;
import org.eclipse.che.ide.util.loging.Log;

import javax.inject.Singleton;

import static java.util.Collections.singletonMap;
import static org.eclipse.che.api.project.shared.dto.event.ProjectTreeTrackingOperationDto.Type.START;
import static org.eclipse.che.api.project.shared.dto.event.ProjectTreeTrackingOperationDto.Type.STOP;

/**
 * @author Dmitry Kuleshov
 */
@Singleton
public class JsonRpcWebSocketAgentEventListener implements WsAgentStateHandler {
    private static final int ENDPOINT_ID = Random.nextInt(Integer.MAX_VALUE);

    private final JsonRpcInitializer initializer;
    private final AppContext         appContext;
    private final RequestTransmitter requestTransmitter;
    private final DtoFactory         dtoFactory;

    @Inject
    public JsonRpcWebSocketAgentEventListener(JsonRpcInitializer initializer, AppContext appContext, EventBus eventBus,
                                              RequestTransmitter requestTransmitter, DtoFactory dtoFactory) {
        this.appContext = appContext;
        this.initializer = initializer;
        this.requestTransmitter = requestTransmitter;
        this.dtoFactory = dtoFactory;

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
        DevMachine devMachine = appContext.getDevMachine();
        String devMachineId = devMachine.getId();
        String wsAgentWebSocketUrl = devMachine.getWsAgentWebSocketUrl();

        String wsAgentUrl = wsAgentWebSocketUrl.replaceFirst("(api)(/)(ws)", "websocket" + "$2" + ENDPOINT_ID);
        String execAgentUrl = devMachine.getExecAgentUrl();

        initializer.initialize("ws-agent", singletonMap("url", wsAgentUrl));
        initializer.initialize(devMachineId, singletonMap("url", execAgentUrl));

        for(MachineEntity machineEntity : appContext.getActiveRuntime().getMachines()) {
            if (!machineEntity.isDev()) {
                initializer.initialize(machineEntity.getId(), singletonMap("url", machineEntity.getExecAgentUrl()));
            }
        }
    }

    private void initializeTreeExplorerFileWatcher() {
        ProjectTreeTrackingOperationDto params = dtoFactory.createDto(ProjectTreeTrackingOperationDto.class)
                                                           .withPath("/")
                                                           .withType(START);

        requestTransmitter.transmitOneToNone("ws-agent", "track:project-tree", params);
    }

    private void initializeGitCheckoutWatcher() {
        requestTransmitter.transmitNoneToNone("ws-agent", "track:git-checkout");
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
