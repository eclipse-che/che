/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.core.websocket.impl;

import com.google.inject.AbstractModule;
import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcMessageReceiver;
import org.eclipse.che.api.core.websocket.commons.WebSocketMessageReceiver;
import org.eclipse.che.api.core.websocket.commons.WebSocketMessageTransmitter;

public class WebSocketModule extends AbstractModule {
  @Override
  protected void configure() {
    requestStaticInjection(GuiceInjectorEndpointConfigurator.class);

    bind(WebSocketMessageReceiver.class).to(JsonRpcMessageReceiver.class);
    bind(WebSocketMessageTransmitter.class).to(BasicWebSocketMessageTransmitter.class);
  }
}
