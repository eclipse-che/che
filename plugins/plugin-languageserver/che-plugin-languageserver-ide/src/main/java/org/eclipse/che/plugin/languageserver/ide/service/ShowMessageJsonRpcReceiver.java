/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.languageserver.ide.service;

import com.google.web.bindery.event.shared.EventBus;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;
import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
import org.eclipse.che.ide.api.machine.events.WsAgentStateEvent;
import org.eclipse.che.ide.api.machine.events.WsAgentStateHandler;
import org.eclipse.che.plugin.languageserver.ide.window.ShowMessageProcessor;
import org.eclipse.che.plugin.languageserver.ide.window.ShowMessageRequestProcessor;
import org.eclipse.lsp4j.MessageActionItem;
import org.eclipse.lsp4j.ShowMessageRequestParams;

/** Subscribes and receives JSON-RPC messages related to 'window/showMessage' events */
@Singleton
public class ShowMessageJsonRpcReceiver {

  @Inject
  public ShowMessageJsonRpcReceiver(RequestTransmitter transmitter, EventBus eventBus) {
    eventBus.addHandler(
        WsAgentStateEvent.TYPE,
        new WsAgentStateHandler() {
          @Override
          public void onWsAgentStarted(WsAgentStateEvent event) {
            subscribe(transmitter);
            subscribeShowMessageRequest(transmitter);
          }

          @Override
          public void onWsAgentStopped(WsAgentStateEvent event) {}
        });
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
        .endpointId("ws-agent")
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
