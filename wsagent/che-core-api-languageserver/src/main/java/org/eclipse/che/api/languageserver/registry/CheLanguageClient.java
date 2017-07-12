/*******************************************************************************
 * Copyright (c) 2012-2017 Red Hat
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.languageserver.registry;

import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.languageserver.shared.model.ExtendedPublishDiagnosticsParams;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.ShowMessageRequestParams;
import org.eclipse.lsp4j.services.LanguageClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

/**
 * A LSP language client implementation for Che
 * 
 * @author Thomas Mäder
 *
 */
public class CheLanguageClient implements LanguageClient {
    private final static Logger LOG = LoggerFactory.getLogger(CheLanguageClient.class);

    private EventService eventService;
    private String       serverId;

    public CheLanguageClient(EventService eventService, String serverId) {
        this.eventService = eventService;
        this.serverId = serverId;
    }

    @Override
    public void telemetryEvent(Object object) {
        // not supported yet.
    }

    @Override
    public CompletableFuture<Void> showMessageRequest(ShowMessageRequestParams requestParams) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void showMessage(MessageParams messageParams) {
        eventService.publish(messageParams);
    }

    @Override
    public void publishDiagnostics(PublishDiagnosticsParams diagnostics) {
        eventService.publish(new ExtendedPublishDiagnosticsParams(serverId, diagnostics));
    }

    @Override
    public void logMessage(MessageParams message) {
        switch (message.getType()) {
        case Error:
            LOG.error(message.getMessage());
            break;
        case Warning:
            LOG.warn(message.getMessage());
            break;
        case Info:
            LOG.info(message.getMessage());
            break;
        case Log:
            LOG.debug(message.getMessage());
            break;
        }
    }
}
