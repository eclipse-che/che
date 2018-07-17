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
package org.eclipse.che.api.languageserver;

import static com.google.common.collect.Lists.newLinkedList;
import static java.util.concurrent.TimeUnit.MINUTES;

import com.google.inject.Singleton;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcException;
import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;
import org.eclipse.che.api.languageserver.RegistryContainer.Registry;
import org.eclipse.che.api.languageserver.server.dto.DtoServerImpls.LanguageRegexDto;
import org.eclipse.che.api.languageserver.server.dto.DtoServerImpls.ServerCapabilitiesDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Language server service that handles JSON-RPC requests related to language server initialization
 * and matching.
 *
 * @author Dmytro Kulieshov
 */
@Singleton
class LanguageServerService {
  private static final Logger LOG = LoggerFactory.getLogger(LanguageServerService.class);

  private final RequestHandlerConfigurator configurator;
  private final LanguageServerInitializer languageServerInitializer;
  private final Registry<String> languageFilterRegistry;
  private final LanguageServerConfigInitializer languageServerConfigInitializer;

  @Inject
  LanguageServerService(
      RequestHandlerConfigurator configurator,
      LanguageServerInitializer languageServerInitializer,
      RegistryContainer registryContainer,
      LanguageServerConfigInitializer languageServerConfigInitializer) {
    this.configurator = configurator;
    this.languageServerInitializer = languageServerInitializer;
    this.languageFilterRegistry = registryContainer.languageFilterRegistry;
    this.languageServerConfigInitializer = languageServerConfigInitializer;
  }

  @PostConstruct
  private void configureMethodHandlers() {
    configurator
        .newConfiguration()
        .methodName("languageServer/initialize")
        .paramsAsString()
        .resultAsDto(ServerCapabilitiesDto.class)
        .withFunction(this::initialize);

    configurator
        .newConfiguration()
        .methodName("languageServer/getLanguageRegexes")
        .noParams()
        .resultAsListOfDto(LanguageRegexDto.class)
        .withSupplier(this::getLanguageRegexes);
  }

  private List<LanguageRegexDto> getLanguageRegexes() {
    LOG.debug("Received 'languageServer/getLanguageRegexes' request");
    List<LanguageRegexDto> languageRegexes = newLinkedList();

    languageServerConfigInitializer.initialize();

    for (Entry<String, String> entry : languageFilterRegistry.getAll().entrySet()) {
      LanguageRegexDto languageRegexDto = new LanguageRegexDto();
      languageRegexDto.setNamePattern(entry.getValue());
      languageRegexDto.setLanguageId(entry.getKey());
      languageRegexes.add(languageRegexDto);
    }

    LOG.debug("Responding: {}", languageRegexes);
    return languageRegexes;
  }

  private ServerCapabilitiesDto initialize(String wsPath) {
    try {
      LOG.debug("Received 'languageServer/initialize' request for path: {}", wsPath);

      languageServerConfigInitializer.initialize();

      ServerCapabilitiesDto serverCapabilitiesDto =
          new ServerCapabilitiesDto(languageServerInitializer.initialize(wsPath).get(1, MINUTES));

      LOG.debug("Responding: {}", serverCapabilitiesDto);
      return serverCapabilitiesDto;
    } catch (CompletionException e) {
      LOG.error("Language server initialization procedure failed", e.getCause());
      throw new JsonRpcException(-27000, e.getCause().getMessage());
    } catch (InterruptedException | ExecutionException | TimeoutException e) {
      LOG.error("Language server initialization procedure failed", e);
      throw new JsonRpcException(-27000, e.getMessage());
    }
  }
}
