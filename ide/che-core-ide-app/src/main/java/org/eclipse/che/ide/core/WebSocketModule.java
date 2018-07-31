/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.core;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.assistedinject.GinFactoryModuleBuilder;
import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcMessageReceiver;
import org.eclipse.che.api.core.websocket.commons.WebSocketMessageReceiver;
import org.eclipse.che.api.core.websocket.commons.WebSocketMessageTransmitter;
import org.eclipse.che.ide.websocket.impl.BasicWebSocketEndpoint;
import org.eclipse.che.ide.websocket.impl.BasicWebSocketMessageTransmitter;
import org.eclipse.che.ide.websocket.impl.DelayableWebSocketConnection;
import org.eclipse.che.ide.websocket.impl.WebSocketConnection;
import org.eclipse.che.ide.websocket.impl.WebSocketEndpoint;
import org.eclipse.che.ide.websocket.impl.WebSocketFactory;

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
    bind(WebSocketMessageReceiver.class).to(JsonRpcMessageReceiver.class);

    install(
        new GinFactoryModuleBuilder()
            .implement(WebSocketConnection.class, DelayableWebSocketConnection.class)
            .build(WebSocketFactory.class));
  }
}
