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
package org.eclipse.che.ide.jsonrpc;

import com.google.gwt.user.client.Timer;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.machine.events.WsAgentServerRunningEvent;
import org.eclipse.che.ide.api.machine.events.WsAgentServerStoppedEvent;
import org.eclipse.che.ide.api.workspace.model.RuntimeImpl;
import org.eclipse.che.ide.api.workspace.model.WorkspaceImpl;
import org.eclipse.che.ide.bootstrap.BasicIDEInitializedEvent;
import org.eclipse.che.ide.util.loging.Log;

import javax.inject.Singleton;
import java.util.Set;

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonMap;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.RUNNING;
import static org.eclipse.che.ide.api.workspace.Constants.WS_AGENT_JSON_RPC_ENDPOINT_ID;

/** Initializes JSON-RPC connection to the ws-agent server. */
@Singleton
public class WsAgentJsonRpcInitializer {

    private final AppContext         appContext;
    private final JsonRpcInitializer initializer;
    private final RequestTransmitter requestTransmitter;

    @Inject
    public WsAgentJsonRpcInitializer(JsonRpcInitializer initializer,
                                     AppContext appContext,
                                     EventBus eventBus,
                                     RequestTransmitter requestTransmitter) {
        this.appContext = appContext;
        this.initializer = initializer;
        this.requestTransmitter = requestTransmitter;

        eventBus.addHandler(WsAgentServerRunningEvent.TYPE, event -> initializeJsonRpcService());
        eventBus.addHandler(WsAgentServerStoppedEvent.TYPE, event -> initializer.terminate(WS_AGENT_JSON_RPC_ENDPOINT_ID));

        // in case ws-agent is already running
        eventBus.addHandler(BasicIDEInitializedEvent.TYPE, event -> {
            if (appContext.getWorkspace().getStatus() == RUNNING) {
                initializeJsonRpcService();
            }
        });
    }

    private void initializeJsonRpcService() {
        Log.debug(WsAgentJsonRpcInitializer.class, "Web socket agent started event caught.");

        try {
            internalInitialize();
        } catch (Exception e) {
            Log.debug(WsAgentJsonRpcInitializer.class, "Failed, will try one more time.");

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
            return; // workspace is stopped
        }

        runtime.getWsAgentServer().ifPresent(server -> {
            String wsAgentWebSocketUrl =
                    server.getUrl().replaceFirst("http", "ws") + "/ws"; // TODO (spi ide): remove path when it comes with URL
            String wsAgentUrl = wsAgentWebSocketUrl.replaceFirst("api/ws", "wsagent");

            String separator = wsAgentUrl.contains("?") ? "&" : "?";
            String queryParams = appContext.getApplicationWebsocketId().map(id -> separator + "clientId=" + id).orElse("");
            Set<Runnable> initActions = appContext.getApplicationWebsocketId().isPresent() ? emptySet() : singleton(this::processWsId);

            initializer.initialize(WS_AGENT_JSON_RPC_ENDPOINT_ID, singletonMap("url", wsAgentUrl + queryParams), initActions);
        });
    }

    private void processWsId() {
        requestTransmitter.newRequest()
                          .endpointId(WS_AGENT_JSON_RPC_ENDPOINT_ID)
                          .methodName("websocketIdService/getId")
                          .noParams()
                          .sendAndReceiveResultAsString()
                          .onSuccess(appContext::setApplicationWebsocketId);
    }
}
