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

import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.machine.events.WsAgentServerRunningEvent;
import org.eclipse.che.ide.api.machine.events.WsAgentServerStoppedEvent;
import org.eclipse.che.ide.api.workspace.model.RuntimeImpl;
import org.eclipse.che.ide.api.workspace.model.ServerImpl;
import org.eclipse.che.ide.api.workspace.model.WorkspaceImpl;
import org.eclipse.che.ide.bootstrap.BasicIDEInitializedEvent;
import org.eclipse.che.ide.util.loging.Log;

import javax.inject.Singleton;
import java.util.Optional;

import static java.util.Collections.singletonMap;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.RUNNING;
import static org.eclipse.che.api.machine.shared.Constants.WSAGENT_REFERENCE;

/** Initializes JSON-RPC connection to the ws-agent server. */
@Singleton
public class WsAgentJsonRpcInitializer {

    private final AppContext         appContext;
    private final JsonRpcInitializer initializer;

    @Inject
    public WsAgentJsonRpcInitializer(JsonRpcInitializer initializer, AppContext appContext, EventBus eventBus) {
        this.appContext = appContext;
        this.initializer = initializer;

        eventBus.addHandler(WsAgentServerRunningEvent.TYPE, event -> initializeJsonRpcService());
        eventBus.addHandler(WsAgentServerStoppedEvent.TYPE, event -> initializer.terminate("ws-agent"));

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

        runtime.getDevMachine().ifPresent(devMachine -> {
            Optional<ServerImpl> wsAgentServer = devMachine.getServerByName(WSAGENT_REFERENCE);

            wsAgentServer.ifPresent(server -> {
                final String wsAgentBaseUrl = server.getUrl() + "/api"; // FIXME: spi ide
                final String wsAgentWebSocketUrl = wsAgentBaseUrl.replaceFirst("http", "ws") + "/ws"; // FIXME: spi ide
                final String wsAgentUrl = wsAgentWebSocketUrl.replaceFirst("(api)(/)(ws)", "websocket" + "$2" + appContext.getAppId());

                initializer.initialize("ws-agent", singletonMap("url", wsAgentUrl));
            });
        });
    }
}
