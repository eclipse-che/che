/*
 * Copyright (c) 2012-2019 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.core.jsonrpc.impl;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import javax.inject.Singleton;
import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcComposer;
import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcMarshaller;
import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcQualifier;
import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcUnmarshaller;
import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;
import org.eclipse.che.api.core.jsonrpc.commons.RequestProcessor;
import org.eclipse.che.api.core.jsonrpc.commons.RequestProcessorConfigurationProvider;
import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
import org.eclipse.che.api.core.jsonrpc.commons.TimeoutActionRunner;
import org.eclipse.che.dto.server.DtoFactory;

public class JsonRpcModule extends AbstractModule {
  @Override
  protected void configure() {
    install(new FactoryModuleBuilder().build(RequestHandlerConfigurator.class));
    install(new FactoryModuleBuilder().build(RequestTransmitter.class));

    bind(JsonRpcMarshaller.class).to(GsonJsonRpcMarshaller.class);
    bind(JsonRpcUnmarshaller.class).to(GsonJsonRpcUnmarshaller.class);
    bind(JsonRpcQualifier.class).to(GsonJsonRpcQualifier.class);
    bind(JsonRpcComposer.class).to(GsonJsonRpcComposer.class);

    bind(RequestProcessor.class).to(ServerSideRequestProcessor.class);
    bind(RequestProcessorConfigurationProvider.class)
        .to(ServerSideRequestProcessorConfigurator.class);
    bind(TimeoutActionRunner.class).to(ServerSideTimeoutActionRunner.class);
  }

  @Provides
  @Singleton
  public JsonParser jsonParser() {
    return new JsonParser();
  }

  @Provides
  @Singleton
  protected Gson gson() {
    return DtoFactory.getInstance().getGson();
  }
}
