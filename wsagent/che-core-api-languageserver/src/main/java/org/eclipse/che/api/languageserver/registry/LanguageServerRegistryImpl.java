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
package org.eclipse.che.api.languageserver.registry;

import static org.eclipse.che.api.fs.server.WsPathUtils.absolutize;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.languageserver.exception.LanguageServerException;
import org.eclipse.che.api.languageserver.launcher.LanguageServerLauncher;
import org.eclipse.che.api.languageserver.service.LanguageServiceUtils;
import org.eclipse.che.api.languageserver.shared.model.LanguageDescription;
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.api.project.server.impl.RegisteredProject;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.services.LanguageServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class LanguageServerRegistryImpl implements LanguageServerRegistry {

  private static final Logger LOG = LoggerFactory.getLogger(LanguageServerRegistryImpl.class);
  private final List<LanguageDescription> languages;
  private final List<LanguageServerLauncher> launchers;
  private final AtomicInteger serverId = new AtomicInteger();

  /** Started {@link LanguageServer} by project. */
  private final Map<String, List<LanguageServerLauncher>> launchedServers;

  private final Map<String, List<InitializedLanguageServer>> initializedServers;

  private final Provider<ProjectManager> projectManagerProvider;
  private final ServerInitializer initializer;
  private EventService eventService;
  private CheLanguageClientFactory clientFactory;

  @Inject
  public LanguageServerRegistryImpl(
      Set<LanguageServerLauncher> languageServerLaunchers,
      Set<LanguageDescription> languages,
      Provider<ProjectManager> projectManagerProvider,
      ServerInitializer initializer,
      EventService eventService,
      CheLanguageClientFactory clientFactory) {
    this.languages = new ArrayList<>(languages);
    this.launchers = new ArrayList<>(languageServerLaunchers);
    this.projectManagerProvider = projectManagerProvider;
    this.initializer = initializer;
    this.eventService = eventService;
    this.clientFactory = clientFactory;
    this.launchedServers = new HashMap<>();
    this.initializedServers = new HashMap<>();
  }

  private LanguageDescription findLanguage(String path) {
    for (LanguageDescription language : languages) {
      if (matchesFilenames(language, path) || matchesExtensions(language, path)) {
        return language;
      }
    }
    return null;
  }

  private boolean matchesExtensions(LanguageDescription language, String path) {
    return language.getFileExtensions().stream().anyMatch(extension -> path.endsWith(extension));
  }

  private boolean matchesFilenames(LanguageDescription language, String path) {
    return language.getFileNames().stream().anyMatch(name -> path.endsWith(name));
  }

  @Override
  public ServerCapabilities getCapabilities(String fileUri) throws LanguageServerException {
    return getApplicableLanguageServers(fileUri)
        .stream()
        .flatMap(Collection::stream)
        .map(s -> s.getInitializeResult().getCapabilities())
        .reduce(
            null,
            (left, right) ->
                left == null ? right : new ServerCapabilitiesOverlay(left, right).compute());
  }

  public ServerCapabilities initialize(String fileUri) throws LanguageServerException {
    String projectPath = extractProjectPath(fileUri);
    if (projectPath == null) {
      return null;
    }
    List<LanguageServerLauncher> requiredToLaunch = findLaunchers(projectPath, fileUri);
    // launchers is the set of things we need to have initialized

    for (LanguageServerLauncher launcher : new ArrayList<>(requiredToLaunch)) {
      synchronized (initializedServers) {
        List<LanguageServerLauncher> servers =
            launchedServers.computeIfAbsent(projectPath, k -> new ArrayList<>());

        if (!servers.contains(launcher)) {
          servers.add(launcher);
          String id = String.valueOf(serverId.incrementAndGet());
          initializer
              .initialize(launcher, clientFactory.create(id), projectPath)
              .thenAccept(
                  pair -> {
                    synchronized (initializedServers) {
                      List<InitializedLanguageServer> initialized =
                          initializedServers.computeIfAbsent(projectPath, k -> new ArrayList<>());
                      initialized.add(
                          new InitializedLanguageServer(id, pair.first, pair.second, launcher));
                      requiredToLaunch.remove(launcher);
                      initializedServers.notifyAll();
                    }
                  })
              .exceptionally(
                  t -> {
                    eventService.publish(
                        new MessageParams(
                            MessageType.Error,
                            "Failed to initialized LS "
                                + launcher.getDescription().getId()
                                + ": "
                                + t.getMessage()));
                    LOG.error("Error launching language server " + launcher, t);
                    synchronized (initializedServers) {
                      requiredToLaunch.remove(launcher);
                      servers.remove(launcher);
                      initializedServers.notifyAll();
                    }
                    return null;
                  });
        }
      }
    }

    // now wait for all launchers to arrive at initialized
    // eventually, all launchers will either fail or succeed, regardless of
    // which request thread started them. Thus the loop below will
    // end.
    synchronized (initializedServers) {
      List<InitializedLanguageServer> initForProject = initializedServers.get(projectPath);
      if (initForProject != null) {
        for (InitializedLanguageServer initialized : initForProject) {
          requiredToLaunch.remove(initialized.getLauncher());
        }
      }
      while (!requiredToLaunch.isEmpty()) {
        try {
          initializedServers.wait();
          initForProject = initializedServers.get(projectPath);
          if (initForProject != null) {
            for (InitializedLanguageServer initialized : initForProject) {
              requiredToLaunch.remove(initialized.getLauncher());
            }
          }
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          return null;
        }
      }
    }
    return getCapabilities(fileUri);
  }

  private List<LanguageServerLauncher> findLaunchers(String projectPath, String fileUri) {
    LanguageDescription language = findLanguage(fileUri);
    if (language == null) {
      return Collections.emptyList();
    }
    List<LanguageServerLauncher> result = new ArrayList<>();
    for (LanguageServerLauncher launcher : launchers) {
      if (launcher.isAbleToLaunch()) {
        int score = matchScore(launcher.getDescription(), fileUri, language.getLanguageId());
        if (score > 0) {
          result.add(launcher);
        }
      }
    }
    return result;
  }

  @Override
  public List<LanguageDescription> getSupportedLanguages() {
    return Collections.unmodifiableList(languages);
  }

  protected String extractProjectPath(String filePath) throws LanguageServerException {
    if (!LanguageServiceUtils.isProjectUri(filePath)) {
      throw new LanguageServerException("Project not found for " + filePath);
    }

    String wsPath = absolutize(LanguageServiceUtils.removePrefixUri(filePath));
    RegisteredProject project =
        projectManagerProvider
            .get()
            .getClosest(wsPath)
            .orElseThrow(() -> new LanguageServerException("Project not found for " + filePath));

    return LanguageServiceUtils.prefixURI(project.getPath());
  }

  public List<Collection<InitializedLanguageServer>> getApplicableLanguageServers(String fileUri)
      throws LanguageServerException {
    String projectPath = extractProjectPath(fileUri);
    LanguageDescription language = findLanguage(fileUri);
    if (projectPath == null || language == null) {
      return Collections.emptyList();
    }

    Map<Integer, List<InitializedLanguageServer>> result = new HashMap<>();

    List<InitializedLanguageServer> servers = null;
    synchronized (initializedServers) {
      List<InitializedLanguageServer> list = initializedServers.get(projectPath);
      if (list == null) {
        return Collections.emptyList();
      }
      servers = new ArrayList<InitializedLanguageServer>(list);
    }
    for (InitializedLanguageServer server : servers) {
      int score =
          matchScore(server.getLauncher().getDescription(), fileUri, language.getLanguageId());
      if (score > 0) {
        List<InitializedLanguageServer> list = result.get(score);
        if (list == null) {
          list = new ArrayList<>();
          result.put(score, list);
        }
        list.add(server);
      }
    }
    // sort lists highest score first
    return result
        .entrySet()
        .stream()
        .sorted((left, right) -> right.getKey() - left.getKey())
        .map(entry -> entry.getValue())
        .collect(Collectors.toList());
  }

  private int matchScore(LanguageServerDescription desc, String path, String languageId) {
    int match = matchLanguageId(desc, languageId);
    if (match == 10) {
      return 10;
    }

    for (DocumentFilter filter : desc.getDocumentFilters()) {
      if (filter.getLanguageId() != null && filter.getLanguageId().length() > 0) {
        match = Math.max(match, matchLanguageId(filter.getLanguageId(), languageId));
        if (match == 10) {
          return 10;
        }
      }
      if (filter.getScheme() != null && path.startsWith(filter.getScheme() + ":")) {
        return 10;
      }
      String pattern = filter.getPathRegex();
      if (pattern != null) {
        if (pattern.equals(path)) {
          return 10;
        }
        Pattern regex = Pattern.compile(pattern);
        if (regex.matcher(path).matches()) {
          match = Math.max(match, 5);
        }
      }
    }
    return match;
  }

  private int matchLanguageId(String id, String languageId) {
    if (id.equals(languageId)) {
      return 10;
    } else if ("*".equals(id)) {
      return 5;
    }
    return 0;
  }

  private int matchLanguageId(LanguageServerDescription desc, String languageId) {
    int match = 0;
    List<String> languageIds = desc.getLanguageIds();
    if (languageIds == null) {
      return 0;
    }
    for (String id : languageIds) {
      if (id.equals(languageId)) {
        match = 10;
        break;
      } else if ("*".equals(id)) {
        match = 5;
      }
    }
    return match;
  }

  @PreDestroy
  protected void shutdown() {
    List<LanguageServer> allServers;
    synchronized (initializedServers) {
      allServers =
          initializedServers
              .values()
              .stream()
              .flatMap(l -> l.stream())
              .map(s -> s.getServer())
              .collect(Collectors.toList());
    }
    for (LanguageServer server : allServers) {
      server.shutdown();
      server.exit();
    }
  }

  @Override
  public InitializedLanguageServer getServer(String id) {
    for (List<InitializedLanguageServer> list : initializedServers.values()) {
      for (InitializedLanguageServer initializedLanguageServer : list) {
        if (initializedLanguageServer.getId().equals(id)) {
          return initializedLanguageServer;
        }
      }
    }
    return null;
  }
}
