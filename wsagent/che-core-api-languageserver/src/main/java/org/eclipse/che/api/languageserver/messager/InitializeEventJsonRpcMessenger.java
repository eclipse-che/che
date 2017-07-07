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
package org.eclipse.che.api.languageserver.messager;

import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;
import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
import org.eclipse.che.api.languageserver.registry.LanguageServerRegistryImpl;
import org.eclipse.che.api.languageserver.registry.ServerInitializer;
import org.eclipse.che.api.languageserver.registry.ServerInitializerObserver;
import org.eclipse.che.api.languageserver.server.dto.DtoServerImpls;
import org.eclipse.che.api.languageserver.server.dto.DtoServerImpls.LanguageServerInitializeEventDto;
import org.eclipse.che.api.languageserver.shared.model.LanguageDescription;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.services.LanguageServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@Singleton
public class InitializeEventJsonRpcMessenger implements ServerInitializerObserver {
    private final Set<String> endpointIds = new CopyOnWriteArraySet<>();

    private final RequestTransmitter requestTransmitter;
    private final ServerInitializer  initializer;

    @Inject
    public InitializeEventJsonRpcMessenger(ServerInitializer initializer, RequestTransmitter requestTransmitter) {
        this.initializer = initializer;
        this.requestTransmitter = requestTransmitter;
    }

    @Override
    public void onServerInitialized(LanguageServer server,
                                    ServerCapabilities serverCapabilities,
                                    LanguageDescription languageDescription,
                                    String projectPath) {

        LanguageServerInitializeEventDto initializeEventDto = new DtoServerImpls.LanguageServerInitializeEventDto();
        initializeEventDto.setSupportedLanguages(new DtoServerImpls.LanguageDescriptionDto(languageDescription));
        initializeEventDto.setServerCapabilities(new DtoServerImpls.ServerCapabilitiesDto(serverCapabilities));
        initializeEventDto.setProjectPath(projectPath.substring(LanguageServerRegistryImpl.PROJECT_FOLDER_PATH.length()));

        endpointIds.forEach(endpointId -> requestTransmitter.newRequest()
                                                            .endpointId(endpointId)
                                                            .methodName("languageServer/initialize/notify")
                                                            .paramsAsDto(initializeEventDto)
                                                            .sendAndSkipResult());
    }

    @Inject
    private void configureSubscribeHandler(RequestHandlerConfigurator configurator) {
        configurator.newConfiguration()
                    .methodName("languageServer/initialize/subscribe")
                    .noParams()
                    .noResult()
                    .withConsumer(endpointIds::add);
    }

    @Inject
    private void configureUnSubscribeHandler(RequestHandlerConfigurator configurator) {
        configurator.newConfiguration()
                    .methodName("languageServer/initialize/unsubscribe")
                    .noParams()
                    .noResult()
                    .withConsumer(endpointIds::remove);

    }

    @PostConstruct
    public void addObserver() {
        initializer.addObserver(this);
    }

    @PreDestroy
    public void removeObserver() {
        initializer.removeObserver(this);
    }

}
