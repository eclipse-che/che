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
import org.eclipse.che.jdt.ls.extension.api.dto.GetEffectivePomParameters;
import org.eclipse.che.plugin.languageserver.ide.service.ServiceUtil;

@Singleton
public class JavaLanguageExtensionServiceClient {

  private static final String FILE_STRUCTURE = "java/file-structure";
  private static final int FILE_STRUCTURE_REQUEST_TIMEOUT = 10_000;
  private static final String EFFECTIVE_POM = "java/effective-pom";
  private static final int EFFECTIVE_POM_REQUEST_TIMEOUT = 30_000;

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
              .methodName(FILE_STRUCTURE)
              .paramsAsDto(params)
              .sendAndReceiveResultAsListOfDto(
                  ExtendedSymbolInformation.class, FILE_STRUCTURE_REQUEST_TIMEOUT)
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

  public Promise<String> effectivePom(GetEffectivePomParameters params) {
    return Promises.create(
        (resolve, reject) ->
            requestTransmitter
                .newRequest()
                .endpointId(WS_AGENT_JSON_RPC_ENDPOINT_ID)
                .methodName(EFFECTIVE_POM)
                .paramsAsDto(params)
                .sendAndReceiveResultAsString(EFFECTIVE_POM_REQUEST_TIMEOUT)
                .onSuccess(resolve::apply)
                .onTimeout(
                    () ->
                        reject.apply(
                            new PromiseError() {
                              TimeoutException te =
                                  new TimeoutException("Timeout while getting effective pom.");

                              @Override
                              public String getMessage() {
                                return te.getMessage();
                              }

                              @Override
                              public Throwable getCause() {
                                return te;
                              }
                            }))
                .onFailure(error -> reject.apply(ServiceUtil.getPromiseError(error))));
  }
}
