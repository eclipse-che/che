/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.languageserver.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcException;
import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;
import org.eclipse.che.api.languageserver.exception.LanguageServerException;
import org.eclipse.che.api.languageserver.registry.LanguageServerRegistry;
import org.eclipse.che.api.languageserver.server.dto.DtoServerImpls.ServerCapabilitiesDto;
import org.eclipse.lsp4j.ServerCapabilities;

@Singleton
public class LanguageServerInitializationHandler {

  @Inject
  public LanguageServerInitializationHandler(
      RequestHandlerConfigurator requestHandlerConfigurator, LanguageServerRegistry registry) {
    requestHandlerConfigurator
        .newConfiguration()
        .methodName("languageServer/initialize")
        .paramsAsString()
        .resultAsDto(ServerCapabilitiesDto.class)
        .withFunction(
            path -> {
              try {
                ServerCapabilities capabilities =
                    registry.initialize(LanguageServiceUtils.prefixURI(path));
                return capabilities == null ? null : new ServerCapabilitiesDto(capabilities);
              } catch (LanguageServerException e) {
                throw new JsonRpcException(-27000, e.getMessage());
              }
            });
  }
}
