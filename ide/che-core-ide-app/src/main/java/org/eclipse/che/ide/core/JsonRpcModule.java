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
package org.eclipse.che.ide.core;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.assistedinject.GinFactoryModuleBuilder;

import org.eclipse.che.ide.api.event.ng.JsonRpcWebSocketAgentEventListener;
import org.eclipse.che.ide.jsonrpc.BuildingRequestTransmitter;
import org.eclipse.che.ide.jsonrpc.JsonRpcFactory;
import org.eclipse.che.ide.jsonrpc.JsonRpcInitializer;
import org.eclipse.che.ide.jsonrpc.RequestHandlerConfigurator;
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
        bind(JsonRpcInitializer.class).to(WebSocketJsonRpcInitializer.class);

        install(new GinFactoryModuleBuilder().build(JsonRpcFactory.class));
        install(new GinFactoryModuleBuilder().build(RequestHandlerConfigurator.class));
        install(new GinFactoryModuleBuilder().build(BuildingRequestTransmitter.class));
    }
}
