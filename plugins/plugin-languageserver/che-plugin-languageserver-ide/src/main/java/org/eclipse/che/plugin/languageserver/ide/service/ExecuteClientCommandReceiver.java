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
import org.eclipse.lsp4j.ExecuteCommandParams;

/**
 * Subscribes and receives JSON-RPC messages related to ClassPath Notification
 * 'workspace/executeClientCommand' events
 */
@Singleton
public class ExecuteClientCommandReceiver {
  public static final String EXECUTE_CLIENT_COMMAND = "workspace/executeClientCommand";
  public static final String EXECUTE_CLIENT_COMMAND_SUBSCRIBE =
      "workspace/executeClientCommand/subscribe";
  private final RequestTransmitter transmitter;

  @Inject
  public ExecuteClientCommandReceiver(RequestTransmitter transmitter) {
    this.transmitter = transmitter;
  }

  public void subscribe() {
    subscribe(transmitter);
  }

  @Inject
  private void configureReceiver(
      Provider<ExecuteClientCommandProcessor> provider, RequestHandlerConfigurator configurator) {
    configurator
        .newConfiguration()
        .methodName(EXECUTE_CLIENT_COMMAND)
        .paramsAsDto(ExecuteCommandParams.class)
        .noResult()
        .withConsumer(params -> provider.get().execute(params));
  }

  private void subscribe(RequestTransmitter transmitter) {
    transmitter
        .newRequest()
        .endpointId(WS_AGENT_JSON_RPC_ENDPOINT_ID)
        .methodName(EXECUTE_CLIENT_COMMAND_SUBSCRIBE)
        .noParams()
        .sendAndSkipResult();
  }
}
