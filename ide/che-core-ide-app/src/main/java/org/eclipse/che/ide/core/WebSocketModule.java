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
import com.google.gwt.inject.client.multibindings.GinMapBinder;

import org.eclipse.che.ide.jsonrpc.impl.WebSocketJsonRpcDispatcher;
import org.eclipse.che.ide.websocket.ng.WebSocketMessageReceiver;
import org.eclipse.che.ide.websocket.ng.WebSocketMessageTransmitter;
import org.eclipse.che.ide.websocket.ng.impl.BasicWebSocketEndpoint;
import org.eclipse.che.ide.websocket.ng.impl.BasicWebSocketMessageTransmitter;
import org.eclipse.che.ide.websocket.ng.impl.BasicWebSocketTransmissionValidator;
import org.eclipse.che.ide.websocket.ng.impl.DelayableWebSocket;
import org.eclipse.che.ide.websocket.ng.impl.SessionWebSocketInitializer;
import org.eclipse.che.ide.websocket.ng.impl.WebSocket;
import org.eclipse.che.ide.websocket.ng.impl.WebSocketCreator;
import org.eclipse.che.ide.websocket.ng.impl.WebSocketEndpoint;
import org.eclipse.che.ide.websocket.ng.impl.WebSocketInitializer;
import org.eclipse.che.ide.websocket.ng.impl.WebSocketTransmissionValidator;

/**
 * GIN module for configuring WebSocket components.
 *
 * @author Artem Zatsarynnyi
 */
public class WebSocketModule extends AbstractGinModule {

    @Override
    protected void configure() {
        bind(WebSocketInitializer.class).to(SessionWebSocketInitializer.class);

        bind(WebSocketEndpoint.class).to(BasicWebSocketEndpoint.class);

        bind(WebSocketTransmissionValidator.class).to(BasicWebSocketTransmissionValidator.class);

        bind(WebSocketMessageTransmitter.class).to(BasicWebSocketMessageTransmitter.class);

        install(new GinFactoryModuleBuilder()
                        .implement(WebSocket.class, DelayableWebSocket.class)
                        .build(WebSocketCreator.class));

        GinMapBinder<String, WebSocketMessageReceiver> receivers =
                GinMapBinder.newMapBinder(binder(), String.class, WebSocketMessageReceiver.class);

        receivers.addBinding("jsonrpc-2.0").to(WebSocketJsonRpcDispatcher.class);
    }
}
