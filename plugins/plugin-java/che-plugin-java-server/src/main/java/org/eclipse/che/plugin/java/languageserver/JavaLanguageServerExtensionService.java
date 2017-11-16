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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcException;
import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;
import org.eclipse.che.api.languageserver.registry.LanguageServerRegistry;
import org.eclipse.che.api.languageserver.service.LanguageServiceUtils;
import org.eclipse.che.jdt.ls.extension.api.Commands;
import org.eclipse.che.jdt.ls.extension.api.dto.ExtendedSymbolInformation;
import org.eclipse.che.jdt.ls.extension.api.dto.FileStructureCommandParameters;
import org.eclipse.che.plugin.java.languageserver.dto.DtoServerImpls.ExtendedSymbolInformationDto;
import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.lsp4j.jsonrpc.json.adapters.CollectionTypeAdapterFactory;
import org.eclipse.lsp4j.jsonrpc.json.adapters.EitherTypeAdapterFactory;
import org.eclipse.lsp4j.jsonrpc.json.adapters.EnumTypeAdapterFactory;
import org.eclipse.lsp4j.services.LanguageServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This service makes custom commands in our jdt.ls extension available to clients.
 *
 * @author Thomas Mäder
 */
public class JavaLanguageServerExtensionService {
  private static final Logger LOG =
      LoggerFactory.getLogger(JavaLanguageServerExtensionService.class);
  private final LanguageServerRegistry registry;
  private final RequestHandlerConfigurator requestHandler;
  private final Gson gson;

  @Inject
  public JavaLanguageServerExtensionService(
      LanguageServerRegistry registry, RequestHandlerConfigurator requestHandler) {
    this.registry = registry;
    this.requestHandler = requestHandler;
    this.gson =
        new GsonBuilder()
            .registerTypeAdapterFactory(new CollectionTypeAdapterFactory())
            .registerTypeAdapterFactory(new EitherTypeAdapterFactory())
            .registerTypeAdapterFactory(new EnumTypeAdapterFactory())
            .create();
  }

  @PostConstruct
  public void configureMethods() {
    requestHandler
        .newConfiguration()
        .methodName("java/filestructure")
        .paramsAsDto(FileStructureCommandParameters.class)
        .resultAsListOfDto(ExtendedSymbolInformationDto.class)
        .withFunction(this::fileStructure);
  }

  private LanguageServer getLanguageServer() {
    return registry
        .findServer(server -> (server.getLauncher() instanceof JavaLanguageServerLauncher))
        .get()
        .getServer();
  }

  private List<ExtendedSymbolInformationDto> fileStructure(FileStructureCommandParameters params) {
    LOG.info("Requesting files structure for {}", params);
    params.setUri(LanguageServiceUtils.prefixURI(params.getUri()));
    CompletableFuture<Object> result =
        getLanguageServer()
            .getWorkspaceService()
            .executeCommand(
                new ExecuteCommandParams(
                    Commands.FILE_STRUCTURE_COMMAND, Collections.singletonList(params)));
    Type targetClassType = new TypeToken<ArrayList<ExtendedSymbolInformation>>() {}.getType();
    try {
      List<ExtendedSymbolInformation> symbols =
          gson.fromJson(gson.toJson(result.get(10, TimeUnit.SECONDS)), targetClassType);
      return symbols
          .stream()
          .map(
              symbol -> {
                fixLocation(symbol);
                return symbol;
              })
          .map(ExtendedSymbolInformationDto::new)
          .collect(Collectors.toList());
    } catch (JsonSyntaxException | InterruptedException | ExecutionException | TimeoutException e) {
      throw new JsonRpcException(-27000, e.getMessage());
    }
  }

  private void fixLocation(ExtendedSymbolInformation symbol) {
    LanguageServiceUtils.fixLocation(symbol.getInfo().getLocation());
    for (ExtendedSymbolInformation child : symbol.getChildren()) {
      fixLocation(child);
    }
  }
}
