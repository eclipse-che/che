/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.core;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.assistedinject.GinFactoryModuleBuilder;

import org.eclipse.che.ide.jsonrpc.impl.WebSocketToJsonRpcDispatcher;
import org.eclipse.che.ide.websocket.ng.WebSocketMessageReceiver;
import org.eclipse.che.ide.websocket.ng.WebSocketMessageTransmitter;
import org.eclipse.che.ide.websocket.ng.impl.BasicWebSocketEndpoint;
import org.eclipse.che.ide.websocket.ng.impl.BasicWebSocketMessageTransmitter;
import org.eclipse.che.ide.websocket.ng.impl.DelayableWebSocketConnection;
import org.eclipse.che.ide.websocket.ng.impl.WebSocketConnection;
import org.eclipse.che.ide.websocket.ng.impl.WebSocketEndpoint;
import org.eclipse.che.ide.websocket.ng.impl.WebSocketFactory;

/**
 * GIN module for configuring WebSocket components.
 *
 * @author Artem Zatsarynnyi
 */
public class WebSocketModule extends AbstractGinModule {

    @Override
    protected void configure() {
        bind(WebSocketEndpoint.class).to(BasicWebSocketEndpoint.class);
        bind(WebSocketMessageTransmitter.class).to(BasicWebSocketMessageTransmitter.class);
        bind(WebSocketMessageReceiver.class).to(WebSocketToJsonRpcDispatcher.class);

        install(new GinFactoryModuleBuilder().implement(WebSocketConnection.class, DelayableWebSocketConnection.class)
                                             .build(WebSocketFactory.class));
    }
}
