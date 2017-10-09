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
import org.eclipse.che.api.languageserver.shared.model.ExtendedPublishDiagnosticsParams;
import org.eclipse.che.ide.api.machine.events.WsAgentStateEvent;
import org.eclipse.che.ide.api.machine.events.WsAgentStateHandler;
import org.eclipse.che.plugin.languageserver.ide.editor.PublishDiagnosticsProcessor;

/** Subscribes and receives JSON-RPC messages related to 'textDocument/publishDiagnostics' events */
@Singleton
public class PublishDiagnosticsReceiver {

  @Inject
  public PublishDiagnosticsReceiver(RequestTransmitter transmitter, EventBus eventBus) {
    eventBus.addHandler(
        WsAgentStateEvent.TYPE,
        new WsAgentStateHandler() {
          @Override
          public void onWsAgentStarted(WsAgentStateEvent event) {
            subscribe(transmitter);
          }

          @Override
          public void onWsAgentStopped(WsAgentStateEvent event) {}
        });
  }

  @Inject
  private void configureReceiver(
      Provider<PublishDiagnosticsProcessor> provider, RequestHandlerConfigurator configurator) {
    configurator
        .newConfiguration()
        .methodName("textDocument/publishDiagnostics")
        .paramsAsDto(ExtendedPublishDiagnosticsParams.class)
        .noResult()
        .withConsumer(params -> provider.get().processDiagnostics(params));
  }

  private void subscribe(RequestTransmitter transmitter) {
    transmitter
        .newRequest()
        .endpointId("ws-agent")
        .methodName("textDocument/publishDiagnostics/subscribe")
        .noParams()
        .sendAndSkipResult();
  }
}
