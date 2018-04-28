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

import static java.util.concurrent.CompletableFuture.supplyAsync;
import static java.util.concurrent.Executors.newCachedThreadPool;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.Collectors.toSet;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.*;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.languageserver.LanguageServerConfig.CommunicationProvider;
import org.eclipse.che.api.languageserver.LanguageServerConfig.CommunicationProvider.StatusChecker;
import org.eclipse.che.api.languageserver.LanguageServerConfig.InstanceProvider;
import org.eclipse.che.api.languageserver.RegistryContainer.Registry;
import org.eclipse.che.commons.lang.Pair;
import org.eclipse.che.commons.lang.concurrent.LoggingUncaughtExceptionHandler;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.services.LanguageServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages the whole language server initialization process that includes process launch/socket
 * connection establishing, language server instance creation and initialization, language server
 * capabilities accumulation.
 *
 * @author Dmytro Kulieshov
 */
@Singleton
class LanguageServerInitializer {
  private static Logger LOG = LoggerFactory.getLogger(LanguageServerInitializer.class);

  private final ExecutorService executor;

  private final EventService eventService;
  private final CheLanguageClientFactory cheLanguageClientFactory;
  private final InitializeParamsProvider initializeParamsProvider;
  private final ServerCapabilitiesAccumulator serverCapabilitiesAccumulator;
  private final FindId findId;

  private final Registry<String> idRegistry;
  private final Registry<LanguageServer> languageServerRegistry;
  private final Registry<InstanceProvider> instanceProviderRegistry;
  private final Registry<ServerCapabilities> serverCapabilitiesRegistry;
  private final Registry<Pair<InputStream, OutputStream>> ioStreamRegistry;
  private final Registry<CommunicationProvider> communicationProviderRegistry;

  @Inject
  public LanguageServerInitializer(
      ServerCapabilitiesAccumulator serverCapabilitiesAccumulator,
      RegistryContainer registryContainer,
      FindId findId,
      EventService eventService,
      CheLanguageClientFactory cheLanguageClientFactory,
      InitializeParamsProvider initializeParamsProvider) {
    this.executor = newCachedThreadPool(getFactory());

    this.eventService = eventService;
    this.cheLanguageClientFactory = cheLanguageClientFactory;
    this.initializeParamsProvider = initializeParamsProvider;
    this.serverCapabilitiesAccumulator = serverCapabilitiesAccumulator;
    this.findId = findId;

    this.idRegistry = registryContainer.idRegistry;
    this.languageServerRegistry = registryContainer.languageServerRegistry;
    this.instanceProviderRegistry = registryContainer.instanceProviderRegistry;
    this.serverCapabilitiesRegistry = registryContainer.serverCapabilitiesRegistry;
    this.ioStreamRegistry = registryContainer.ioStreamRegistry;
    this.communicationProviderRegistry = registryContainer.communicationProviderRegistry;
  }

  private static ThreadFactory getFactory() {
    return new ThreadFactoryBuilder()
        .setUncaughtExceptionHandler(LoggingUncaughtExceptionHandler.getInstance())
        .setNameFormat(LanguageServerInitializer.class.getSimpleName())
        .setDaemon(true)
        .build();
  }

  /**
   * Initialize all language servers that match a specified workspace path. If some servers are
   * already initialized does nothing. If some servers can't be initialized due to some errors at
   * any point - skips them and proceeds with the rest.
   *
   * @param wsPath absolute workspace path
   * @return accumulated server capabilities of all initialized language servers
   * @throws CompletionException if no server initialized throws {@link LanguageServerException}
   *     wrapped by {@link CompletionException}
   */
  CompletableFuture<ServerCapabilities> initialize(String wsPath) {
    return supplyAsync(
        () -> {
          LOG.info("Started language servers initialization, file path '{}'", wsPath);

          Set<ServerCapabilities> serverCapabilitiesSet =
              findId
                  .byPath(wsPath)
                  .stream()
                  .map(this::initializeIOStreams)
                  .filter(Objects::nonNull)
                  .map(this::createServerInstance)
                  .filter(Objects::nonNull)
                  .map(this::initializeServerInstance)
                  .filter(Objects::nonNull)
                  .map(serverCapabilitiesRegistry::getOrNull)
                  .filter(Objects::nonNull)
                  .collect(toSet());

          LOG.info("Finished language servers initialization, file path '{}'", wsPath);

          LOG.debug("Calculating number of initialized servers and accumulating capabilities");
          if (serverCapabilitiesSet.isEmpty()) {
            String message = String.format("Could not initialize any server for '%s'", wsPath);
            LOG.error(message);
            LanguageServerException cause = new LanguageServerException(message);
            throw new CompletionException(cause);
          } else {
            return serverCapabilitiesSet
                .stream()
                .reduce(new ServerCapabilities(), serverCapabilitiesAccumulator);
          }
        },
        executor);
  }

  private String initializeIOStreams(String id) {
    try {
      LOG.debug("Initializing of IO streams for server '{}': started", id);
      synchronized (idRegistry.get(id)) {
        if (ioStreamRegistry.contains(id)) {
          LOG.debug("Already initialized");
          return id;
        }

        CommunicationProvider communicationProvider = communicationProviderRegistry.get(id);
        StatusChecker statusChecker = communicationProvider.getStatusChecker();
        InputStream inputStream = communicationProvider.getInputStream();
        OutputStream outputStream = communicationProvider.getOutputStream();

        if (!statusChecker.isAlive()) {
          throw new LanguageServerException(statusChecker.getCause());
        }

        LOG.debug("Initializing of IO streams for server '{}': finished", id);
        return ioStreamRegistry.add(id, Pair.of(inputStream, outputStream));
      }
    } catch (LanguageServerException e) {
      LOG.error("Can't initialize IO streams for '{}'", id, e);
    }

    return null;
  }

  private String createServerInstance(String id) {
    try {
      LOG.debug("Creation of a language server instance for server '{}': started", id);
      synchronized (idRegistry.get(id)) {
        if (languageServerRegistry.contains(id)) {
          LOG.debug("Already exists");
          return id;
        }

        CheLanguageClient client = cheLanguageClientFactory.create(id);
        InstanceProvider instanceProvider = instanceProviderRegistry.get(id);
        InputStream inputStream = ioStreamRegistry.get(id).first;
        OutputStream outputStream = ioStreamRegistry.get(id).second;
        LanguageServer languageServer = instanceProvider.get(client, inputStream, outputStream);

        LOG.debug("Creation of a language server instance for server '{}': finished", id);
        return languageServerRegistry.add(id, languageServer);
      }
    } catch (LanguageServerException e) {
      LOG.error("Can't create language server for '{}'", id, e);
    }

    return null;
  }

  private String initializeServerInstance(String id) {
    try {
      LOG.debug("Initializing of a language server instance for server '{}': started", id);
      synchronized (idRegistry.get(id)) {
        if (serverCapabilitiesRegistry.contains(id)) {
          LOG.debug("Already initialized");
          return id;
        }

        LanguageServer languageServer = languageServerRegistry.get(id);
        InitializeParams initializeParams = initializeParamsProvider.get(id);
        InitializeResult initializeResult =
            languageServer.initialize(initializeParams).get(30, SECONDS);

        LanguageServerInitializedEvent event =
            new LanguageServerInitializedEvent(id, languageServer);
        eventService.publish(event);
        LOG.debug("Published a corresponding event: {}", event);

        LOG.debug("Initializing of a language server instance for server '{}': finished", id);

        LOG.info("Initialized language server '{}'", id);
        return serverCapabilitiesRegistry.add(id, initializeResult.getCapabilities());
      }
    } catch (LanguageServerException
        | InterruptedException
        | ExecutionException
        | TimeoutException e) {
      LOG.error("Can't initialize language server for '{}'", id, e);
    }

    return null;
  }
}
