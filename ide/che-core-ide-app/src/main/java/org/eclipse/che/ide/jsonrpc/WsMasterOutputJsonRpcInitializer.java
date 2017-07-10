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

import org.eclipse.che.ide.api.app.AppContext;

import javax.inject.Singleton;

import static com.google.gwt.user.client.Window.Location.getHost;
import static com.google.gwt.user.client.Window.Location.getProtocol;
import static java.util.Collections.singletonMap;

@Singleton
public class WsMasterOutputJsonRpcInitializer {

    private final JsonRpcInitializer initializer;
    private final AppContext         appContext;

    @Inject
    public WsMasterOutputJsonRpcInitializer(JsonRpcInitializer initializer, AppContext appContext) {
        this.initializer = initializer;
        this.appContext = appContext;

        internalInitialize();
    }

    private void internalInitialize() {
        String workspaceMasterUrl = getWsMasterURL();

        initializer.initialize("ws-master-output", singletonMap("url", workspaceMasterUrl));
    }

    private String getWsMasterURL() {
        String protocol = "https:".equals(getProtocol()) ? "wss://" : "ws://";
        String host = getHost();
        String context = "/wsmaster/output/websocket/";

        return protocol + host + context + appContext.getAppId();
    }

    public void terminate() {
        initializer.terminate("ws-master");
    }
}
