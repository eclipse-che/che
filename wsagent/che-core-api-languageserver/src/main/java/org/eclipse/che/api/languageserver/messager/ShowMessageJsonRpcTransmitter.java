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
import org.eclipse.che.api.languageserver.server.dto.DtoServerImpls.MessageParamsDto;
import org.eclipse.lsp4j.MessageActionItem;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.ShowMessageRequestParams;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Transmits 'textDocument/publishDiagnostics' over the JSON-RPC
 */
@Singleton
public class ShowMessageJsonRpcTransmitter {
    private final Set<String> endpointIds                   = new CopyOnWriteArraySet<>();
    private final Set<String> showMessageRequestEndpointIds = new CopyOnWriteArraySet<>();

    private final RequestTransmitter requestTransmitter;


    @Inject
    public ShowMessageJsonRpcTransmitter(RequestTransmitter requestTransmitter) {
        this.requestTransmitter = requestTransmitter;
    }

    @Inject
    private void subscribe(EventService eventService) {
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

    @Inject
    private void configureShowMessageRequestSubscribeHandler(RequestHandlerConfigurator requestHandler) {
        requestHandler.newConfiguration()
                      .methodName("window/showMessageRequest/subscribe")
                      .noParams()
                      .noResult()
                      .withConsumer(showMessageRequestEndpointIds::add);
    }

    @Inject
    private void configureShowMessageRequestUnSubscribeHandler(RequestHandlerConfigurator requestHandler) {
        requestHandler.newConfiguration()
                      .methodName("window/showMessageRequest/unsubscribe")
                      .noParams()
                      .noResult()
                      .withConsumer(showMessageRequestEndpointIds::remove);
    }

    public CompletableFuture<MessageActionItem> sendShowMessageRequest(ShowMessageRequestParams requestParams) {
        CompletableFuture<MessageActionItem> result = new CompletableFuture<>();
        if (showMessageRequestEndpointIds.isEmpty()) {
            result.complete(null);
        }
        if (showMessageRequestEndpointIds.size() > 1) {
            result.completeExceptionally(new Exception("Can't send show message request, too meany clients"));
        } else {
            String endpoint = showMessageRequestEndpointIds.iterator().next();
            requestTransmitter.newRequest()
                              .endpointId(endpoint)
                              .methodName("window/showMessageRequest")
                              .paramsAsDto(requestParams)
                              .sendAndReceiveResultAsDto(MessageActionItem.class)
                              .onSuccess(result::complete)
                              .onFailure(jsonRpcError -> result.completeExceptionally(new Exception(jsonRpcError.getMessage())));
        }

        return result;
    }
}
