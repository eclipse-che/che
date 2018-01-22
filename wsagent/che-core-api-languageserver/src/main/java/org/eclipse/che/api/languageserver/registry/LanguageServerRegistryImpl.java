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
package org.eclipse.che.api.languageserver.registry;

import static javax.ws.rs.core.UriBuilder.fromUri;
import static org.eclipse.che.api.fs.server.WsPathUtils.absolutize;
import static org.eclipse.che.api.languageserver.registry.LanguageRecognizer.UNIDENTIFIED;

import java.lang.management.ManagementFactory;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.languageserver.exception.LanguageServerException;
import org.eclipse.che.api.languageserver.launcher.LanguageServerLauncher;
import org.eclipse.che.api.languageserver.launcher.LaunchingStrategy;
import org.eclipse.che.api.languageserver.remote.RemoteLsLauncherProvider;
import org.eclipse.che.api.languageserver.service.LanguageServiceUtils;
import org.eclipse.che.api.languageserver.shared.model.LanguageDescription;
import org.eclipse.lsp4j.ClientCapabilities;
import org.eclipse.lsp4j.CodeActionCapabilities;
import org.eclipse.lsp4j.CodeLensCapabilities;
import org.eclipse.lsp4j.CompletionCapabilities;
import org.eclipse.lsp4j.CompletionItemCapabilities;
import org.eclipse.lsp4j.DefinitionCapabilities;
import org.eclipse.lsp4j.DidChangeConfigurationCapabilities;
import org.eclipse.lsp4j.DidChangeWatchedFilesCapabilities;
import org.eclipse.lsp4j.DocumentHighlightCapabilities;
import org.eclipse.lsp4j.DocumentLinkCapabilities;
import org.eclipse.lsp4j.DocumentSymbolCapabilities;
import org.eclipse.lsp4j.ExecuteCommandCapabilities;
import org.eclipse.lsp4j.FormattingCapabilities;
import org.eclipse.lsp4j.HoverCapabilities;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.api.project.server.impl.RegisteredProject;
import org.eclipse.che.api.workspace.server.WorkspaceService;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.OnTypeFormattingCapabilities;
import org.eclipse.lsp4j.RangeFormattingCapabilities;
import org.eclipse.lsp4j.ReferencesCapabilities;
import org.eclipse.lsp4j.RenameCapabilities;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.SignatureHelpCapabilities;
import org.eclipse.lsp4j.SymbolCapabilities;
import org.eclipse.lsp4j.SynchronizationCapabilities;
import org.eclipse.lsp4j.TextDocumentClientCapabilities;
import org.eclipse.lsp4j.WorkspaceClientCapabilities;
import org.eclipse.lsp4j.WorkspaceEditCapabilities;
import org.eclipse.lsp4j.services.LanguageServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class LanguageServerRegistryImpl implements LanguageServerRegistry {

  private static final Logger LOG = LoggerFactory.getLogger(LanguageServerRegistryImpl.class);

  private final String workspaceId;
  private final String apiEndpoint;
  private final HttpJsonRequestFactory httpJsonRequestFactory;
  private final Set<RemoteLsLauncherProvider> launcherProviders;
  private final List<LanguageDescription> languages;
  private final List<LanguageServerLauncher> launchers;
  private final AtomicInteger serverId = new AtomicInteger();

  /** Started {@link LanguageServer} by project. */
  private final Set<String> launchedServers;

  private final Set<InitializedLanguageServer> initializedServers;

  private final Provider<ProjectManager> projectManagerProvider;
  private EventService eventService;
  private CheLanguageClientFactory clientFactory;
  private LanguageRecognizer languageRecognizer;
  private Workspace workspace;

  private static ClientCapabilities CLIENT_CAPABILITIES = initClientCapabilities();
  private static final int PROCESS_ID = getProcessId();

  private final List<ServerInitializerObserver> observers = new ArrayList<>();

    private static int getProcessId() {
        String name = ManagementFactory.getRuntimeMXBean().getName();
        int prefixEnd = name.indexOf('@');
        if (prefixEnd != -1) {
            String prefix = name.substring(0, prefixEnd);
            try {
                return Integer.parseInt(prefix);
            } catch (NumberFormatException ignored) {
            }
        }

        LOG.error("Failed to recognize the pid of the process");
        return -1;
    }

  @Inject
  public LanguageServerRegistryImpl(
      @Named("env.CHE_WORKSPACE_ID") String workspaceId,
      @Named("che.api") String apiEndpoint,
      HttpJsonRequestFactory httpJsonRequestFactory,
      Set<RemoteLsLauncherProvider> launcherProviders,
      Set<LanguageServerLauncher> languageServerLaunchers,
      Set<LanguageDescription> languages,
      Provider<ProjectManager> projectManagerProvider,
      EventService eventService,
      CheLanguageClientFactory clientFactory,
      LanguageRecognizer languageRecognizer) {
    this.workspaceId = workspaceId;
    this.apiEndpoint = apiEndpoint;
    this.httpJsonRequestFactory = httpJsonRequestFactory;
    this.launcherProviders = launcherProviders;
    this.languages = new ArrayList<>(languages);
    this.launchers = new ArrayList<>(languageServerLaunchers);
    this.projectManagerProvider = projectManagerProvider;
    this.eventService = eventService;
    this.clientFactory = clientFactory;
    this.languageRecognizer = languageRecognizer;
    this.launchedServers = new HashSet<>();
    this.initializedServers = new HashSet<>();
    launchers.forEach(this::registerCallbacks);
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
    long thread = Thread.currentThread().getId();
    List<LanguageServerLauncher> requiredToLaunch = findApplicableLaunchers(fileUri);
    LOG.info("required to launch for thread " + thread + ": " + requiredToLaunch);
    // launchers is the set of things we need to have initialized
    Set<String> requiredServers = new HashSet<>();

    for (LanguageServerLauncher launcher : requiredToLaunch) {
      LaunchingStrategy launchingStrategy = launcher.getLaunchingStrategy();
      String key = launchingStrategy.getLaunchKey(fileUri);
      requiredServers.add(key);
      synchronized (initializedServers) {
        if (!launchedServers.contains(key)) {
          launchedServers.add(key);
          String id = String.valueOf(serverId.incrementAndGet());
          String launcherId = launcher.getDescription().getId();
          long threadId = Thread.currentThread().getId();
          try {
            LanguageServer server = launcher.launch(fileUri, clientFactory.create(id));
            LOG.info(
                "Launched language server {} on thread {} and fileUri {}",
                launcherId,
                threadId,
                fileUri);
            registerCallbacks(server);

            LOG.info(
                "Initializing language server {} on thread {} and fileUri {}",
                launcherId,
                threadId,
                fileUri);
            String rootUri = launchingStrategy.getRootUri(fileUri);
            InitializeParams initializeParams = prepareInitializeParams(rootUri);
            CompletableFuture<InitializeResult> completableFuture =
                server.initialize(initializeParams);
            completableFuture
                .thenAccept(
                    (InitializeResult res) -> {
                      LOG.info(
                          "Initialized language server {} on thread {} and fileUri {}",
                          launcherId,
                          threadId,
                          fileUri);
                      fireServerInitialized(launcher, server, res.getCapabilities(), fileUri);
                      synchronized (initializedServers) {
                        initializedServers.add(
                            new InitializedLanguageServer(id, server, res, launcher, key));
                        LOG.info("launched for  {} : {}", thread, key);
                        requiredServers.remove(key);
                        initializedServers.notifyAll();
                      }
                    })
                .exceptionally(
                    (Throwable e) -> {
                      synchronized (initializedServers) {
                        requiredServers.remove(key);
                        launchedServers.remove(key);
                        initializedServers.notifyAll();
                      }
                      server.shutdown();
                      server.exit(); // TODO: WAIT FOR SHUTDOWN TO COMPLETE
                      return null;
                    });
          } catch (LanguageServerException e) {
            eventService.publish(
                new MessageParams(
                    MessageType.Error,
                    "Failed to initialized LS "
                        + launcher.getDescription().getId()
                        + ": "
                        + e.getMessage()));
            LOG.error("Error launching language server for thread  {}, {}", thread, launcher, e);
            synchronized (initializedServers) {
              requiredServers.remove(key);
              launchedServers.remove(key);
              initializedServers.notifyAll();
            }
          }
        }
      }
    }

    // now wait for all launchers to arrive at initialized
    // eventually, all launchers will either fail or succeed, regardless of
    // which request thread started them. Thus the loop below will
    // end.
    synchronized (initializedServers) {
      for (InitializedLanguageServer initialized : initializedServers) {
        requiredServers.remove(initialized.getLaunchKey());
      }
      while (!requiredServers.isEmpty()) {
        LOG.info("waiting for launched servers on thread {} : {}", thread, requiredServers);
        try {
          initializedServers.wait();
          for (InitializedLanguageServer initialized : initializedServers) {
            requiredServers.remove(initialized.getLaunchKey());
          }
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          return null;
        }
      }
    }
    return getCapabilities(fileUri);
  }

  private List<LanguageServerLauncher> findApplicableLaunchers(String fileUri) {
    String wsPath = absolutize(LanguageServiceUtils.removePrefixUri(fileUri));
    LanguageDescription language = languageRecognizer.recognizeByPath(wsPath);
    if (language == null) {
      return Collections.emptyList();
    }
    List<LanguageServerLauncher> combinedLaunchers = new LinkedList<>(launchers);
    initWorkspaceConfiguration();
    if (workspace != null) {
      for (RemoteLsLauncherProvider launcherProvider : launcherProviders) {
        combinedLaunchers.addAll(launcherProvider.getAll(workspace));
      }
    }

    List<LanguageServerLauncher> result = new ArrayList<>();
    for (LanguageServerLauncher launcher : combinedLaunchers) {
      if (launcher.isAbleToLaunch()) {
        int score = matchScore(launcher.getDescription(), fileUri, language.getLanguageId());
        if (score > 0 || matchesWatchPatterns(launcher, fileUri)) {
          result.add(launcher);
        }
      }
    }
    return result;
  }

  private boolean matchesWatchPatterns(LanguageServerLauncher launcher, String fileUri) {
    // we need to launch servers that are interested in didChangeWatchedFiles notifications.
    try {
      URI uri = new URI(fileUri);
      Path path = FileSystems.getDefault().getPath(uri.getPath());
      return launcher
          .getDescription()
          .getFileWatchPatterns()
          .stream()
          .map(LanguageServerFileWatcher.patternToMatcher())
          .anyMatch(matcher -> matcher.matches(path));
    } catch (URISyntaxException e) {
      LOG.error("Could not parse URI", e);
    }
    return false;
  }

  @Override
  public List<LanguageDescription> getSupportedLanguages() {
      initWorkspaceConfiguration();

      if (workspace == null) {
          return Collections.unmodifiableList(languages);
      }

      List<LanguageDescription> languageDescriptions = new LinkedList<>(languages);

      for (RemoteLsLauncherProvider launcherProvider : launcherProviders) {
          for (LanguageServerLauncher launcher : launcherProvider.getAll(workspace)) {
              for (String languageId : launcher.getDescription().getLanguageIds()) {
                  LanguageDescription language = languageRecognizer.recognizeById(languageId);
                  if (language.equals(UNIDENTIFIED)) {
                      continue;
                  }
                  languageDescriptions.add(language);
              }
          }
      }

      return Collections.unmodifiableList(languageDescriptions);
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
    String wsPath = absolutize(LanguageServiceUtils.removePrefixUri(fileUri));
    LanguageDescription language = languageRecognizer.recognizeByPath(wsPath);
    String languageId = language == null ? null : language.getLanguageId();
    if (projectPath == null || language == null) {
      return Collections.emptyList();
    }

    Map<Integer, List<InitializedLanguageServer>> result = new HashMap<>();

    for (InitializedLanguageServer server : initializedServers) {
      if (server
          .getLauncher()
          .getLaunchingStrategy()
          .isApplicable(server.getLaunchKey(), fileUri)) {
        int score = matchScore(server.getLauncher().getDescription(), fileUri, languageId);
        if (score > 0) {
          List<InitializedLanguageServer> list = result.get(score);
          if (list == null) {
            list = new ArrayList<>();
            result.put(score, list);
          }
          list.add(server);
        }
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

  private int matchScore(LanguageServerDescription desc, String fileUri, String languageId) {
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
      if (filter.getScheme() != null && fileUri.startsWith(filter.getScheme() + ":")) {
        return 10;
      }
      String pattern = filter.getPathRegex();
      if (pattern != null) {
        if (pattern.equals(fileUri)) {
          return 10;
        }
        Pattern regex = Pattern.compile(pattern);
        if (regex.matcher(fileUri).matches()) {
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
      allServers = initializedServers.stream().map(s -> s.getServer()).collect(Collectors.toList());
    }
    for (LanguageServer server : allServers) {
      server.shutdown();
      server.exit();
    }
  }

  @Override
  public InitializedLanguageServer getServer(String id) {
    for (InitializedLanguageServer initializedLanguageServer : initializedServers) {
      if (initializedLanguageServer.getId().equals(id)) {
        return initializedLanguageServer;
      }
    }
    return null;
  }

  private void initWorkspaceConfiguration() {
    if (workspace != null) {
      return;
    }

    String href =
        fromUri(apiEndpoint)
            .path(WorkspaceService.class)
            .path(WorkspaceService.class, "getByKey")
            .queryParam("includeInternalServers", true)
            .build(workspaceId)
            .toString();
    try {
      workspace =
          httpJsonRequestFactory.fromUrl(href).useGetMethod().request().asDto(WorkspaceDto.class);
    } catch (IOException | ApiException e) {
      LOG.error("Did not manage to get workspace configuration: {}", workspaceId, e);
    }
  }

  public Optional<InitializedLanguageServer> findServer(
      Predicate<InitializedLanguageServer> condition) {
    ArrayList<InitializedLanguageServer> arrayList = new ArrayList<>(initializedServers.size());
    synchronized (initializedServers) {
      arrayList.addAll(initializedServers);
    }
    return arrayList.stream().filter(condition).findFirst();
  }

  public void addObserver(ServerInitializerObserver observer) {
    observers.add(observer);
  }

  public void removeObserver(ServerInitializerObserver observer) {
    observers.remove(observer);
  }

  private static ClientCapabilities initClientCapabilities() {
    ClientCapabilities result = new ClientCapabilities();
    WorkspaceClientCapabilities workspace = new WorkspaceClientCapabilities();
    workspace.setApplyEdit(false); // Change when support added
    workspace.setDidChangeConfiguration(new DidChangeConfigurationCapabilities());
    workspace.setDidChangeWatchedFiles(new DidChangeWatchedFilesCapabilities());
    workspace.setExecuteCommand(new ExecuteCommandCapabilities());
    workspace.setSymbol(new SymbolCapabilities());
    workspace.setWorkspaceEdit(new WorkspaceEditCapabilities());
    result.setWorkspace(workspace);

    TextDocumentClientCapabilities textDocument = new TextDocumentClientCapabilities();
    textDocument.setCodeAction(new CodeActionCapabilities(false));
    textDocument.setCodeLens(new CodeLensCapabilities(false));
    textDocument.setCompletion(new CompletionCapabilities(new CompletionItemCapabilities(false)));
    textDocument.setDefinition(new DefinitionCapabilities(false));
    textDocument.setDocumentHighlight(new DocumentHighlightCapabilities(false));
    textDocument.setDocumentLink(new DocumentLinkCapabilities(false));
    textDocument.setDocumentSymbol(new DocumentSymbolCapabilities(false));
    textDocument.setFormatting(new FormattingCapabilities(false));
    textDocument.setHover(new HoverCapabilities(false));
    textDocument.setOnTypeFormatting(new OnTypeFormattingCapabilities(false));
    textDocument.setRangeFormatting(new RangeFormattingCapabilities(false));
    textDocument.setReferences(new ReferencesCapabilities(false));
    textDocument.setRename(new RenameCapabilities(false));
    textDocument.setSignatureHelp(new SignatureHelpCapabilities(false));
    textDocument.setSynchronization(new SynchronizationCapabilities(true, false, true));
    result.setTextDocument(textDocument);
    return result;
  }

  protected void registerCallbacks(Object o) {
    if (o instanceof ServerInitializerObserver) {
      addObserver((ServerInitializerObserver) o);
    }
  }

  private void fireServerInitialized(
      LanguageServerLauncher launcher,
      LanguageServer server,
      ServerCapabilities capabilities,
      String rootPath) {
    observers.forEach(
        observer -> observer.onServerInitialized(launcher, server, capabilities, rootPath));
  }

  @SuppressWarnings("deprecation")
  private InitializeParams prepareInitializeParams(String rootUri) {
    InitializeParams initializeParams = new InitializeParams();
    initializeParams.setProcessId(PROCESS_ID);
    initializeParams.setRootPath(LanguageServiceUtils.removePrefixUri(rootUri));
    initializeParams.setRootUri(rootUri);

    initializeParams.setCapabilities(CLIENT_CAPABILITIES);
    return initializeParams;
  }
}
