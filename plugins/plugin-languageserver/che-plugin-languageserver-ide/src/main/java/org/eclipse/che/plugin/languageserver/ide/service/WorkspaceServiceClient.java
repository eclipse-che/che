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
import static org.eclipse.che.ide.jsonrpc.JsonRpcErrorUtils.getPromiseError;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
import org.eclipse.che.api.languageserver.shared.model.FileEditParams;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.WorkspaceSymbolParams;
import org.eclipse.lsp4j.services.WorkspaceService;

/** @author Evgen Vidolob */
@Singleton
public class WorkspaceServiceClient {
  private RequestTransmitter requestTransmitter;

  @Inject
  public WorkspaceServiceClient(RequestTransmitter requestTransmitter) {
    this.requestTransmitter = requestTransmitter;
  }

  /**
   * GWT client implementation of {@link WorkspaceService#symbol(WorkspaceSymbolParams)}
   *
   * @param params
   * @return
   */
  public Promise<List<SymbolInformation>> symbol(WorkspaceSymbolParams params) {
    return Promises.create(
        (resolve, reject) ->
            requestTransmitter
                .newRequest()
                .endpointId(WS_AGENT_JSON_RPC_ENDPOINT_ID)
                .methodName("workspace/symbol")
                .paramsAsDto(params)
                .sendAndReceiveResultAsListOfDto(SymbolInformation.class)
                .onSuccess(resolve::apply)
                .onFailure(error -> reject.apply(getPromiseError(error))));
  }

  public Promise<List<TextEdit>> editFile(FileEditParams params) {
    return Promises.create(
        (resolve, reject) ->
            requestTransmitter
                .newRequest()
                .endpointId(WS_AGENT_JSON_RPC_ENDPOINT_ID)
                .methodName("workspace/editFile")
                .paramsAsDto(params)
                .sendAndReceiveResultAsListOfDto(TextEdit.class)
                .onSuccess(resolve::apply)
                .onFailure(error -> reject.apply(getPromiseError(error))));
  }
}
