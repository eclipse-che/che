/*******************************************************************************
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.languageserver.messager;

import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;
import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.languageserver.server.dto.DtoServerImpls.MessageParamsDto;
import org.eclipse.lsp4j.MessageParams;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Transmits 'textDocument/publishDiagnostics' over the JSON-RPC
 */
@Singleton
public class ShowMessageJsonRpcTransmitter {
    private final Set<String> endpointIds = new CopyOnWriteArraySet<>();

    @Inject
    private void subscribe(EventService eventService, RequestTransmitter requestTransmitter) {
        eventService.subscribe(event -> endpointIds.forEach(endpointId -> requestTransmitter.newRequest()
                                                                                            .endpointId(endpointId)
                                                                                            .methodName("window/showMessage")
                                                                                            .paramsAsDto(new MessageParamsDto(event))
                                                                                            .sendAndSkipResult()),
                               MessageParams.class);
    }

    @Inject
    private void configureSubscribeHandler(RequestHandlerConfigurator requestHandler) {
        requestHandler.newConfiguration()
                      .methodName("window/showMessage/subscribe")
                      .noParams()
                      .noResult()
                      .withConsumer(endpointIds::add);
    }

    @Inject
    private void configureUnSubscribeHandler(RequestHandlerConfigurator requestHandler) {
        requestHandler.newConfiguration()
                      .methodName("window/showMessage/unsubscribe")
                      .noParams()
                      .noResult()
                      .withConsumer(endpointIds::remove);
    }
}
