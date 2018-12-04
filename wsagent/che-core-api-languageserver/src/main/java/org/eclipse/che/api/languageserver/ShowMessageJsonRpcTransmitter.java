/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.languageserver;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArraySet;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;
import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.languageserver.server.dto.DtoServerImpls.MessageParamsDto;
import org.eclipse.lsp4j.MessageActionItem;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.ShowMessageRequestParams;

/** Transmits 'textDocument/publishDiagnostics' over the JSON-RPC */
@Singleton
public class ShowMessageJsonRpcTransmitter {
  private final Set<String> endpointIds = new CopyOnWriteArraySet<>();
  private final Set<String> showMessageRequestEndpointIds = new CopyOnWriteArraySet<>();

  private final RequestTransmitter requestTransmitter;

  @Inject
  public ShowMessageJsonRpcTransmitter(RequestTransmitter requestTransmitter) {
    this.requestTransmitter = requestTransmitter;
  }

  @Inject
  private void subscribe(EventService eventService) {
    eventService.subscribe(
        event ->
            endpointIds.forEach(
                endpointId ->
                    requestTransmitter
                        .newRequest()
                        .endpointId(endpointId)
                        .methodName("window/showMessage")
                        .paramsAsDto(new MessageParamsDto(event))
                        .sendAndSkipResult()),
        MessageParams.class);
  }

  @Inject
  private void configureSubscribeHandler(RequestHandlerConfigurator requestHandler) {
    requestHandler
        .newConfiguration()
        .methodName("window/showMessage/subscribe")
        .noParams()
        .noResult()
        .withConsumer(endpointIds::add);
  }

  @Inject
  private void configureUnSubscribeHandler(RequestHandlerConfigurator requestHandler) {
    requestHandler
        .newConfiguration()
        .methodName("window/showMessage/unsubscribe")
        .noParams()
        .noResult()
        .withConsumer(endpointIds::remove);
  }

  @Inject
  private void configureShowMessageRequestSubscribeHandler(
      RequestHandlerConfigurator requestHandler) {
    requestHandler
        .newConfiguration()
        .methodName("window/showMessageRequest/subscribe")
        .noParams()
        .noResult()
        .withConsumer(showMessageRequestEndpointIds::add);
  }

  @Inject
  private void configureShowMessageRequestUnSubscribeHandler(
      RequestHandlerConfigurator requestHandler) {
    requestHandler
        .newConfiguration()
        .methodName("window/showMessageRequest/unsubscribe")
        .noParams()
        .noResult()
        .withConsumer(showMessageRequestEndpointIds::remove);
  }

  public CompletableFuture<MessageActionItem> sendShowMessageRequest(
      ShowMessageRequestParams requestParams) {
    CompletableFuture<MessageActionItem> result = new CompletableFuture<>();
    if (showMessageRequestEndpointIds.isEmpty()) {
      result.complete(null);
    }
    for (String endpointId : endpointIds) {
      requestTransmitter
          .newRequest()
          .endpointId(endpointId)
          .methodName("window/showMessageRequest")
          .paramsAsDto(requestParams)
          .sendAndReceiveResultAsDto(MessageActionItem.class)
          .onSuccess(
              actionItem -> {
                if (!result.isDone()) {
                  result.complete(actionItem);
                }
              })
          .onFailure(
              jsonRpcError ->
                  result.completeExceptionally(new Exception(jsonRpcError.getMessage())));
    }

    return result;
  }
}
