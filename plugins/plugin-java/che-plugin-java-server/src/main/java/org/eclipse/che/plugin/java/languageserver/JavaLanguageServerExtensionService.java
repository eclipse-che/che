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
package org.eclipse.che.plugin.java.languageserver;

import com.google.inject.Inject;
import javax.annotation.PostConstruct;
import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;
import org.eclipse.che.jdt.ls.extension.api.dto.FileStructureCommandParameters;
import org.eclipse.che.plugin.java.languageserver.dto.DtoServerImpls.ExtendedSymbolInformationDto;

/**
 * This service makes custom commands in our jdt.ls extension available to clients.
 *
 * @author Thomas Mäder
 */
public class JavaLanguageServerExtensionService {
  private JavaLanguageServerExtensionManager extensionManager;
  private final RequestHandlerConfigurator requestHandler;

  @Inject
  public JavaLanguageServerExtensionService(
      JavaLanguageServerExtensionManager extensionManager,
      RequestHandlerConfigurator requestHandler) {
    this.extensionManager = extensionManager;
    this.requestHandler = requestHandler;
  }

  @PostConstruct
  public void configureMethods() {
    requestHandler
        .newConfiguration()
        .methodName("java/filestructure")
        .paramsAsDto(FileStructureCommandParameters.class)
        .resultAsListOfDto(ExtendedSymbolInformationDto.class)
        .withFunction(extensionManager::executeFileStructure);
  }
}
