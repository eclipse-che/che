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
package org.eclipse.che.api.core.websocket;

import com.google.inject.AbstractModule;

import org.eclipse.che.api.core.jsonrpc.JsonRpcMessageReceiver;
import org.eclipse.che.api.core.websocket.impl.BasicWebSocketMessageTransmitter;
import org.eclipse.che.api.core.websocket.impl.GuiceInjectorEndpointConfigurator;

public class WebSocketModule extends AbstractModule {
    @Override
    protected void configure() {
        requestStaticInjection(GuiceInjectorEndpointConfigurator.class);
        bind(WebSocketMessageTransmitter.class).to(BasicWebSocketMessageTransmitter.class);
        bind(WebSocketMessageReceiver.class).to(JsonRpcMessageReceiver.class);
    }
}
