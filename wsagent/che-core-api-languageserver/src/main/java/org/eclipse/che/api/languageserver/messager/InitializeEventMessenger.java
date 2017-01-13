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

import io.typefox.lsapi.ServerCapabilities;
import io.typefox.lsapi.services.LanguageServer;

import com.google.gson.Gson;

import org.eclipse.che.api.languageserver.DtoConverter;
import org.eclipse.che.api.languageserver.registry.LanguageServerRegistryImpl;
import org.eclipse.che.api.languageserver.registry.ServerInitializer;
import org.eclipse.che.api.languageserver.registry.ServerInitializerObserver;
import org.eclipse.che.api.languageserver.shared.event.LanguageServerInitializeEventDto;
import org.eclipse.che.api.languageserver.shared.model.LanguageDescription;
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

import static org.eclipse.che.dto.server.DtoFactory.newDto;

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

        LanguageServerInitializeEventDto initializeEventDto = newDto(LanguageServerInitializeEventDto.class);
        initializeEventDto.setSupportedLanguages(DtoConverter.asDto(languageDescription));
        initializeEventDto.setServerCapabilities(DtoConverter.asDto(serverCapabilities));
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
            bm.setBody(new Gson().toJson(message));
            WSConnectionContext.sendMessage(bm);
        } catch (EncodeException | IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }

}
