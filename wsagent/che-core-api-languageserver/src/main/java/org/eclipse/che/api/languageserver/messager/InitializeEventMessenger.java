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

import org.eclipse.che.api.languageserver.registry.LanguageServerRegistryImpl;
import org.eclipse.che.api.languageserver.registry.ServerInitializer;
import org.eclipse.che.api.languageserver.registry.ServerInitializerObserver;
import org.eclipse.che.api.languageserver.server.dto.DtoServerImpls;
import org.eclipse.che.api.languageserver.server.dto.DtoServerImpls.LanguageServerInitializeEventDto;
import org.eclipse.che.api.languageserver.shared.model.LanguageDescription;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.services.LanguageServer;
import org.everrest.websockets.WSConnectionContext;
import org.everrest.websockets.message.ChannelBroadcastMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.websocket.EncodeException;
import java.io.IOException;

/**
 * @author Anatolii Bazko
 */
@Singleton
public class InitializeEventMessenger implements ServerInitializerObserver {
    private final static Logger LOG = LoggerFactory.getLogger(InitializeEventMessenger.class);

    private ServerInitializer initializer;

    @Inject
    public InitializeEventMessenger(ServerInitializer initializer) {
        this.initializer = initializer;
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

        send(initializeEventDto);
    }

    @PostConstruct
    public void addObserver() {
        initializer.addObserver(this);
    }

    @PreDestroy
    public void removeObserver() {
        initializer.removeObserver(this);
    }

    protected void send(final LanguageServerInitializeEventDto message) {
        try {
            final ChannelBroadcastMessage bm = new ChannelBroadcastMessage();
            bm.setChannel("languageserver");
            bm.setBody(message.toJson());
            WSConnectionContext.sendMessage(bm);
        } catch (EncodeException | IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }

}
