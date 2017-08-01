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

import com.google.inject.Inject;

import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
import org.eclipse.che.ide.api.app.AppContext;

import javax.inject.Singleton;
import java.util.Set;

import static com.google.gwt.user.client.Window.Location.getHost;
import static com.google.gwt.user.client.Window.Location.getProtocol;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonMap;
import static org.eclipse.che.ide.api.jsonrpc.Constants.WS_MASTER_JSON_RPC_ENDPOINT_ID;

/** Initializes JSON-RPC connection to the workspace master. */
@Singleton
public class WsMasterJsonRpcInitializer {

    private final JsonRpcInitializer initializer;
    private final RequestTransmitter requestTransmitter;
    private final AppContext         appContext;

    @Inject
    public WsMasterJsonRpcInitializer(JsonRpcInitializer initializer,
                                      RequestTransmitter requestTransmitter,
                                      AppContext appContext) {
        this.initializer = initializer;
        this.requestTransmitter = requestTransmitter;
        this.appContext = appContext;

        initialize();
    }

    private void initialize() {
        String protocol = "https:".equals(getProtocol()) ? "wss://" : "ws://";
        String host = getHost();
        String context = "/wsmaster/websocket";
        String url = protocol + host + context;
        String separator = url.contains("?") ? "&" : "?";
        String queryParams = appContext.getApplicationWebsocketId().map(id -> separator + "clientId=" + id).orElse("");
        String wsMasterEndpointURL = url + queryParams;

        Set<Runnable> initActions = appContext.getApplicationWebsocketId().isPresent() ? emptySet() : singleton(this::processWsId);

        initializer.initialize(WS_MASTER_JSON_RPC_ENDPOINT_ID, singletonMap("url", wsMasterEndpointURL), initActions);
    }

    private void processWsId() {
        requestTransmitter.newRequest()
                          .endpointId(WS_MASTER_JSON_RPC_ENDPOINT_ID)
                          .methodName("websocketIdService/getId")
                          .noParams()
                          .sendAndReceiveResultAsString()
                          .onSuccess(appContext::setApplicationWebsocketId);
    }

    public void terminate() {
        initializer.terminate(WS_MASTER_JSON_RPC_ENDPOINT_ID);
    }
}
