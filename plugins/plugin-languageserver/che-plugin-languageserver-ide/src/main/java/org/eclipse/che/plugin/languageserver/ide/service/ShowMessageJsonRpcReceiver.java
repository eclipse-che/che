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
package org.eclipse.che.plugin.languageserver.ide.service;

import static org.eclipse.che.ide.api.jsonrpc.Constants.WS_AGENT_JSON_RPC_ENDPOINT_ID;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;
import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
import org.eclipse.che.plugin.languageserver.ide.window.ShowMessageProcessor;
import org.eclipse.che.plugin.languageserver.ide.window.ShowMessageRequestProcessor;
import org.eclipse.lsp4j.MessageActionItem;
import org.eclipse.lsp4j.ShowMessageRequestParams;

/** Subscribes and receives JSON-RPC messages related to 'window/showMessage' events */
@Singleton
public class ShowMessageJsonRpcReceiver {

  private final RequestTransmitter transmitter;

  @Inject
  public ShowMessageJsonRpcReceiver(RequestTransmitter transmitter) {
    this.transmitter = transmitter;
  }

  public void subscribe() {
    subscribe(transmitter);
    subscribeShowMessageRequest(transmitter);
  }

  @Inject
  private void configureReceiver(
      Provider<ShowMessageProcessor> provider, RequestHandlerConfigurator configurator) {
    configurator
        .newConfiguration()
        .methodName("window/showMessage")
        .paramsAsDto(ShowMessageRequestParams.class)
        .noResult()
        .withConsumer(params -> provider.get().processNotification(params));
  }

  private void subscribe(RequestTransmitter transmitter) {
    transmitter
        .newRequest()
        .endpointId(WS_AGENT_JSON_RPC_ENDPOINT_ID)
        .methodName("window/showMessage/subscribe")
        .noParams()
        .sendAndSkipResult();
  }

  @Inject
  private void configureShowMessageRequestReceiver(
      Provider<ShowMessageRequestProcessor> provider, RequestHandlerConfigurator configurator) {
    configurator
        .newConfiguration()
        .methodName("window/showMessageRequest")
        .paramsAsDto(ShowMessageRequestParams.class)
        .resultAsPromiseDto(MessageActionItem.class)
        .withPromise(params -> provider.get().processNotificationRequest(params));
  }

  private void subscribeShowMessageRequest(RequestTransmitter transmitter) {
    transmitter
        .newRequest()
        .endpointId("ws-agent")
        .methodName("window/showMessageRequest/subscribe")
        .noParams()
        .sendAndSkipResult();
  }
}
