/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.jsonrpc;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.assistedinject.GinFactoryModuleBuilder;
import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcComposer;
import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcMarshaller;
import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcQualifier;
import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcUnmarshaller;
import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;
import org.eclipse.che.api.core.jsonrpc.commons.RequestProcessor;
import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
import org.eclipse.che.api.core.jsonrpc.commons.TimeoutActionRunner;
import org.eclipse.che.ide.core.ServerSubscriptionBroadcaster;

/** GIN module for configuring JSON-RPC protocol implementation components. */
public class JsonRpcModule extends AbstractGinModule {

  @Override
  protected void configure() {
    bind(WsMasterJsonRpcInitializer.class).asEagerSingleton();
    bind(WsAgentJsonRpcInitializer.class).asEagerSingleton();
    bind(ExecAgentJsonRpcInitializer.class).asEagerSingleton();

    bind(JsonRpcInitializer.class).to(WebSocketJsonRpcInitializer.class);

    install(new GinFactoryModuleBuilder().build(RequestHandlerConfigurator.class));
    install(new GinFactoryModuleBuilder().build(RequestTransmitter.class));

    bind(JsonRpcMarshaller.class).to(ElementalJsonRpcMarshaller.class);
    bind(JsonRpcUnmarshaller.class).to(ElementalJsonRpcUnmarshaller.class);
    bind(JsonRpcComposer.class).to(ElementalJsonRpcComposer.class);
    bind(JsonRpcQualifier.class).to(ElementalJsonRpcQualifier.class);

    bind(RequestProcessor.class).to(ClientSideRequestProcessor.class);
    bind(TimeoutActionRunner.class).to(ClientSideTimeoutActionRunner.class);

    bind(ServerSubscriptionBroadcaster.class).asEagerSingleton();
  }
}
