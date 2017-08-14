/*******************************************************************************
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.core;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.assistedinject.GinFactoryModuleBuilder;

import org.eclipse.che.api.core.jsonrpc.commons.RequestProcessor;
import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcComposer;
import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcMarshaller;
import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcQualifier;
import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcUnmarshaller;
import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;
import org.eclipse.che.api.core.jsonrpc.commons.TimeoutActionRunner;
import org.eclipse.che.ide.api.event.ng.JsonRpcWebSocketAgentEventListener;
import org.eclipse.che.ide.api.jsonrpc.WorkspaceMasterJsonRpcInitializer;
import org.eclipse.che.ide.jsonrpc.ClientSideRequestProcessor;
import org.eclipse.che.ide.jsonrpc.ClientSideTimeoutActionRunner;
import org.eclipse.che.ide.jsonrpc.ElementalJsonRpcComposer;
import org.eclipse.che.ide.jsonrpc.ElementalJsonRpcMarshaller;
import org.eclipse.che.ide.jsonrpc.ElementalJsonRpcQualifier;
import org.eclipse.che.ide.jsonrpc.ElementalJsonRpcUnmarshaller;
import org.eclipse.che.ide.jsonrpc.JsonRpcInitializer;
import org.eclipse.che.ide.jsonrpc.WebSocketJsonRpcInitializer;

/**
 * GIN module for configuring Json RPC protocol implementation components.
 *
 * @author Artem Zatsarynnyi
 */
public class JsonRpcModule extends AbstractGinModule {

    @Override
    protected void configure() {
        bind(JsonRpcWebSocketAgentEventListener.class).asEagerSingleton();
        bind(WorkspaceMasterJsonRpcInitializer.class).asEagerSingleton();

        bind(JsonRpcInitializer.class).to(WebSocketJsonRpcInitializer.class);

        install(new GinFactoryModuleBuilder().build(RequestHandlerConfigurator.class));
        install(new GinFactoryModuleBuilder().build(RequestTransmitter.class));

        bind(JsonRpcMarshaller.class).to(ElementalJsonRpcMarshaller.class);
        bind(JsonRpcUnmarshaller.class).to(ElementalJsonRpcUnmarshaller.class);
        bind(JsonRpcComposer.class).to(ElementalJsonRpcComposer.class);
        bind(JsonRpcQualifier.class).to(ElementalJsonRpcQualifier.class);

        bind(RequestProcessor.class).to(ClientSideRequestProcessor.class);
        bind(TimeoutActionRunner.class).to(ClientSideTimeoutActionRunner.class);
    }
}
