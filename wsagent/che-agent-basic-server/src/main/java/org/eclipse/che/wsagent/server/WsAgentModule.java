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
package org.eclipse.che.wsagent.server;

import com.google.gson.JsonParser;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.assistedinject.FactoryModuleBuilder;

import org.eclipse.che.api.core.jsonrpc.BuildingRequestTransmitter;
import org.eclipse.che.api.core.jsonrpc.JsonRpcFactory;
import org.eclipse.che.api.core.jsonrpc.JsonRpcMessageReceiver;
import org.eclipse.che.api.core.jsonrpc.RequestHandlerConfigurator;
import org.eclipse.che.api.core.rest.ApiInfoService;
import org.eclipse.che.api.core.websocket.WebSocketMessageReceiver;
import org.eclipse.che.api.core.websocket.WebSocketMessageTransmitter;
import org.eclipse.che.api.core.websocket.impl.BasicWebSocketMessageTransmitter;
import org.eclipse.che.api.core.websocket.impl.GuiceInjectorEndpointConfigurator;
import org.eclipse.che.commons.lang.Pair;
import org.eclipse.che.inject.DynaModule;

import javax.inject.Named;
import javax.inject.Singleton;

;

/**
 * Mandatory modules of workspace agent
 *
 * @author Evgen Vidolob
 * @author Sergii Kabashniuk
 */
@DynaModule
public class WsAgentModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(ApiInfoService.class);
        install(new org.eclipse.che.api.auth.client.OAuthAgentModule());
        install(new org.eclipse.che.api.core.rest.CoreRestModule());
        install(new org.eclipse.che.api.core.util.FileCleaner.FileCleanerModule());
        install(new org.eclipse.che.api.project.server.ProjectApiModule());
        install(new org.eclipse.che.commons.schedule.executor.ScheduleModule());
        install(new org.eclipse.che.plugin.ssh.key.SshModule());
        install(new org.eclipse.che.api.languageserver.LanguageServerModule());
        install(new org.eclipse.che.api.debugger.server.DebuggerModule());
        install(new org.eclipse.che.api.git.GitModule());
        install(new org.eclipse.che.git.impl.jgit.JGitModule());

        configureJsonRpc();
        configureWebSocket();
    }

    //it's need for WSocketEventBusClient and in the future will be replaced with the property
    @Named("notification.client.event_subscriptions")
    @Provides
    @SuppressWarnings("unchecked")
    Pair<String, String>[] eventSubscriptionsProvider(@Named("event.bus.url") String eventBusURL) {
        return new Pair[]{Pair.of(eventBusURL, "")};
    }

    //it's need for EventOriginClientPropagationPolicy and in the future will be replaced with the property
    @Named("notification.client.propagate_events")
    @Provides
    @SuppressWarnings("unchecked")
    Pair<String, String>[] propagateEventsProvider(@Named("event.bus.url") String eventBusURL) {
        return new Pair[]{Pair.of(eventBusURL, "")};
    }

    private void configureWebSocket() {
        requestStaticInjection(GuiceInjectorEndpointConfigurator.class);
        bind(WebSocketMessageTransmitter.class).to(BasicWebSocketMessageTransmitter.class);

        bind(WebSocketMessageReceiver.class).to(JsonRpcMessageReceiver.class);
    }

    private void configureJsonRpc() {
        install(new FactoryModuleBuilder().build(JsonRpcFactory.class));
        install(new FactoryModuleBuilder().build(RequestHandlerConfigurator.class));
        install(new FactoryModuleBuilder().build(BuildingRequestTransmitter.class));
    }

    @Provides
    @Singleton
    public JsonParser jsonParser() {
        return new JsonParser();
    }
}
