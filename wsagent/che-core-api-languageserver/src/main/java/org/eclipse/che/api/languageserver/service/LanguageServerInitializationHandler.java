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
package org.eclipse.che.api.languageserver.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcException;
import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;
import org.eclipse.che.api.languageserver.exception.LanguageServerException;
import org.eclipse.che.api.languageserver.registry.LanguageServerRegistry;
import org.eclipse.che.api.languageserver.server.dto.DtoServerImpls.ServerCapabilitiesDto;

@Singleton
public class LanguageServerInitializationHandler {

    @Inject
    public LanguageServerInitializationHandler(RequestHandlerConfigurator requestHandlerConfigurator, LanguageServerRegistry registry) {
        requestHandlerConfigurator.newConfiguration()
                                  .methodName("languageServer/initialize")
                                  .paramsAsString()
                                  .resultAsDto(ServerCapabilitiesDto.class)
                                  .withFunction(path -> {
                                      try {
                                          return new ServerCapabilitiesDto(registry.initialize(TextDocumentServiceUtils.prefixURI(path)));
                                      } catch (LanguageServerException e) {
                                          throw new JsonRpcException(-27000, e.getMessage());
                                      }
                                  });
    }
}
