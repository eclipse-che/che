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
package org.eclipse.che.api.core.jsonrpc.impl;

import com.google.gson.JsonParser;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.assistedinject.FactoryModuleBuilder;

import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcComposer;
import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcMarshaller;
import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcQualifier;
import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcUnmarshaller;
import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;

import javax.inject.Singleton;

public class JsonRpcModule extends AbstractModule {
    @Override
    protected void configure() {
        install(new FactoryModuleBuilder().build(RequestHandlerConfigurator.class));
        install(new FactoryModuleBuilder().build(RequestTransmitter.class));

        bind(JsonRpcMarshaller.class).to(GsonJsonRpcMarshaller.class);
        bind(JsonRpcUnmarshaller.class).to(GsonJsonRpcUnmarshaller.class);
        bind(JsonRpcQualifier.class).to(GsonJsonRpcQualifier.class);
        bind(JsonRpcComposer.class).to(GsonJsonRpcComposer.class);
    }

    @Provides
    @Singleton
    public JsonParser jsonParser() {
        return new JsonParser();
    }
}
