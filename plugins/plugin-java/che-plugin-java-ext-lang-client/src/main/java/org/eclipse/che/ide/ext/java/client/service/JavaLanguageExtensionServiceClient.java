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
package org.eclipse.che.ide.ext.java.client.service;

import static org.eclipse.che.ide.api.jsonrpc.Constants.WS_AGENT_JSON_RPC_ENDPOINT_ID;

import com.google.gwt.jsonp.client.TimeoutException;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.che.jdt.ls.extension.api.dto.ExtendedSymbolInformation;
import org.eclipse.che.jdt.ls.extension.api.dto.FileStructureCommandParameters;
import org.eclipse.che.plugin.languageserver.ide.service.ServiceUtil;

@Singleton
public class JavaLanguageExtensionServiceClient {
  private final RequestTransmitter requestTransmitter;

  @Inject
  public JavaLanguageExtensionServiceClient(RequestTransmitter requestTransmitter) {
    this.requestTransmitter = requestTransmitter;
  }

  public Promise<List<ExtendedSymbolInformation>> fileStructure(
      FileStructureCommandParameters params) {
    return Promises.create(
        (resolve, reject) -> {
          requestTransmitter
              .newRequest()
              .endpointId(WS_AGENT_JSON_RPC_ENDPOINT_ID)
              .methodName("java/filestructure")
              .paramsAsDto(params)
              .sendAndReceiveResultAsListOfDto(ExtendedSymbolInformation.class, 10000)
              .onSuccess(resolve::apply)
              .onTimeout(
                  () -> {
                    reject.apply(
                        new PromiseError() {
                          TimeoutException t = new TimeoutException("Timeout");

                          @Override
                          public String getMessage() {
                            return t.getMessage();
                          }

                          @Override
                          public Throwable getCause() {
                            return t;
                          }
                        });
                  })
              .onFailure(
                  error -> {
                    reject.apply(ServiceUtil.getPromiseError(error));
                  });
        });
  }
}
