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
package org.eclipse.che.ide.api.jsonrpc;

import com.google.gwt.user.client.Timer;
import com.google.inject.Inject;

import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.jsonrpc.JsonRpcInitializer;
import org.eclipse.che.ide.util.loging.Log;

import javax.inject.Singleton;

import static com.google.gwt.user.client.Window.Location.getHost;
import static com.google.gwt.user.client.Window.Location.getProtocol;
import static java.util.Collections.singletonMap;

/**
 * Initializes json rpc connection to workspace master
 */
@Singleton
public class WorkspaceMasterJsonRpcInitializer {
    private final JsonRpcInitializer initializer;
    private final AppContext         appContext;

    @Inject
    public WorkspaceMasterJsonRpcInitializer(JsonRpcInitializer initializer, AppContext appContext) {
        this.initializer = initializer;
        this.appContext = appContext;
        internalInitialize();
    }

    private static native String getWebsocketContext() /*-{
        if ($wnd.IDE && $wnd.IDE.config) {
            return $wnd.IDE.config.websocketContext;
        } else {
            return null;
        }
    }-*/;

    public void initialize() {
        Log.debug(WorkspaceMasterJsonRpcInitializer.class, "Initializing JSON RPC websocket connection to workspace master");
        try {
            internalInitialize();
        } catch (Exception e) {
            Log.debug(WorkspaceMasterJsonRpcInitializer.class, "Failed, will try one more time.");
            new Timer() {
                @Override
                public void run() {
                    internalInitialize();
                }
            }.schedule(1_000);
        }
    }

    private void internalInitialize() {
        String protocol = "https:".equals(getProtocol()) ? "wss://" : "ws://";
        String host = getHost();
        String context = getWebsocketContext() + "/";
        String workspaceMasterUrl = protocol + host + context + appContext.getAppId();

        initializer.initialize("ws-master", singletonMap("url", workspaceMasterUrl));
    }

    public void terminate() {
        initializer.terminate("ws-master");
    }
}
