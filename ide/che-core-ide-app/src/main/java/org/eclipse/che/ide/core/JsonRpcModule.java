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
import com.google.gwt.inject.client.multibindings.GinMapBinder;

import org.eclipse.che.ide.api.event.ng.JsonRpcWebSocketAgentEventListener;
import org.eclipse.che.ide.jsonrpc.JsonRpcRequestReceiver;
import org.eclipse.che.ide.jsonrpc.JsonRpcRequestTransmitter;
import org.eclipse.che.ide.jsonrpc.JsonRpcResponseReceiver;
import org.eclipse.che.ide.jsonrpc.JsonRpcResponseTransmitter;
import org.eclipse.che.ide.jsonrpc.impl.BasicJsonRpcObjectValidator;
import org.eclipse.che.ide.jsonrpc.impl.JsonRpcDispatcher;
import org.eclipse.che.ide.jsonrpc.impl.JsonRpcInitializer;
import org.eclipse.che.ide.jsonrpc.impl.JsonRpcObjectValidator;
import org.eclipse.che.ide.jsonrpc.impl.WebSocketJsonRpcInitializer;
import org.eclipse.che.ide.jsonrpc.impl.WebSocketJsonRpcRequestDispatcher;
import org.eclipse.che.ide.jsonrpc.impl.WebSocketJsonRpcRequestTransmitter;
import org.eclipse.che.ide.jsonrpc.impl.WebSocketJsonRpcResponseDispatcher;
import org.eclipse.che.ide.jsonrpc.impl.WebSocketJsonRpcResponseTransmitter;

/**
 * GIN module for configuring Json RPC protocol implementation components.
 *
 * @author Artem Zatsarynnyi
 */
public class JsonRpcModule extends AbstractGinModule {

    @Override
    protected void configure() {
        bind(JsonRpcWebSocketAgentEventListener.class).asEagerSingleton();

        bind(JsonRpcInitializer.class).to(WebSocketJsonRpcInitializer.class);

        bind(JsonRpcRequestTransmitter.class).to(WebSocketJsonRpcRequestTransmitter.class);
        bind(JsonRpcResponseTransmitter.class).to(WebSocketJsonRpcResponseTransmitter.class);

        bind(JsonRpcObjectValidator.class).to(BasicJsonRpcObjectValidator.class);

        GinMapBinder<String, JsonRpcDispatcher> dispatchers = GinMapBinder.newMapBinder(binder(), String.class, JsonRpcDispatcher.class);
        dispatchers.addBinding("request").to(WebSocketJsonRpcRequestDispatcher.class);
        dispatchers.addBinding("response").to(WebSocketJsonRpcResponseDispatcher.class);

        GinMapBinder<String, JsonRpcRequestReceiver> requestReceivers =
                GinMapBinder.newMapBinder(binder(), String.class, JsonRpcRequestReceiver.class);

        GinMapBinder<String, JsonRpcResponseReceiver> responseReceivers =
                GinMapBinder.newMapBinder(binder(), String.class, JsonRpcResponseReceiver.class);
    }
}
