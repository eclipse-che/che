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
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.languageserver.server.dto.DtoServerImpls.ExtendedPublishDiagnosticsParamsDto;
import org.eclipse.che.api.languageserver.shared.model.ExtendedPublishDiagnosticsParams;
import org.eclipse.lsp4j.PublishDiagnosticsParams;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Transmits 'textDocument/publishDiagnostics' over the JSON-RPC
 */
@Singleton
public class PublishDiagnosticsParamsJsonRpcTransmitter {
    private final Set<String> endpointIds = new CopyOnWriteArraySet<>();

    @Inject
    private void subscribe(EventService eventService, RequestTransmitter requestTransmitter) {
        eventService.subscribe(event -> {
            PublishDiagnosticsParams params = event.getParams();
            if(params.getUri() != null) {
                params.setUri(params.getUri().substring(16));
            }
            endpointIds.forEach(endpointId -> requestTransmitter.newRequest()
                                                                .endpointId(endpointId)
                                                                .methodName("textDocument/publishDiagnostics")
                                                                .paramsAsDto(new ExtendedPublishDiagnosticsParamsDto(event))
                                                                .sendAndSkipResult());
        }, ExtendedPublishDiagnosticsParams.class);
    }

    @Inject
    private void configureSubscribeHandler(RequestHandlerConfigurator requestHandler) {
        requestHandler.newConfiguration()
                      .methodName("textDocument/publishDiagnostics/subscribe")
                      .noParams()
                      .noResult()
                      .withConsumer(endpointIds::add);
    }

    @Inject
    private void configureUnSubscribeHandler(RequestHandlerConfigurator requestHandler) {
        requestHandler.newConfiguration()
                      .methodName("textDocument/publishDiagnostics/unsubscribe")
                      .noParams()
                      .noResult()
                      .withConsumer(endpointIds::remove);
    }
}
