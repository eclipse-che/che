/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.java.languageserver;

import static org.eclipse.che.api.languageserver.LanguageServiceUtils.removePrefixUri;
import static org.eclipse.che.api.languageserver.util.JsonUtil.convertToJson;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ProcessBuilder.Redirect;
import java.lang.reflect.Proxy;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.PreDestroy;
import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.languageserver.LanguageServerConfig;
import org.eclipse.che.api.languageserver.ProcessCommunicationProvider;
import org.eclipse.che.api.languageserver.service.FileContentAccess;
import org.eclipse.che.api.languageserver.util.DynamicWrapper;
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.api.project.server.impl.RootDirPathProvider;
import org.eclipse.che.api.project.shared.Constants.Services;
import org.eclipse.che.commons.lang.concurrent.LoggingUncaughtExceptionHandler;
import org.eclipse.che.ide.ext.java.shared.Constants;
import org.eclipse.che.jdt.ls.extension.api.Notifications;
import org.eclipse.che.jdt.ls.extension.api.dto.ProgressReport;
import org.eclipse.che.jdt.ls.extension.api.dto.StatusReport;
import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Evgen Vidolob
 * @author Anatolii Bazko
 * @author Thomas Mäder
 */
@Singleton
public class JavaLanguageServerLauncher implements LanguageServerConfig {
  private static final Logger LOG = LoggerFactory.getLogger(JavaLanguageServerLauncher.class);

  private final Path launchScript;
  private final RootDirPathProvider rootDirPathProvider;
  private final ProcessorJsonRpcCommunication processorJsonRpcCommunication;
  private final ExecuteClientCommandJsonRpcTransmitter executeCliendCommandTransmitter;
  private final NotifyJsonRpcTransmitter notifyTransmitter;
  private final EventService eventService;
  private final ProjectManager projectManager;
  private final ProjectsSynchronizer projectSynchronizer;
  private final AtomicBoolean isStarted = new AtomicBoolean(false);
  private final ExecutorService notificationHandler;

  @Inject
  public JavaLanguageServerLauncher(
      RootDirPathProvider rootDirPathProvider,
      ProcessorJsonRpcCommunication processorJsonRpcCommunication,
      ExecuteClientCommandJsonRpcTransmitter executeCliendCommandTransmitter,
      NotifyJsonRpcTransmitter notifyTransmitter,
      EventService eventService,
      ProjectManager projectManager,
      ProjectsSynchronizer projectSynchronizer) {
    this.rootDirPathProvider = rootDirPathProvider;
    this.processorJsonRpcCommunication = processorJsonRpcCommunication;
    this.executeCliendCommandTransmitter = executeCliendCommandTransmitter;
    this.notifyTransmitter = notifyTransmitter;
    this.eventService = eventService;
    this.projectManager = projectManager;
    this.projectSynchronizer = projectSynchronizer;
    this.notificationHandler =
        Executors.newSingleThreadScheduledExecutor(
            new ThreadFactoryBuilder()
                .setNameFormat("Jdtls Notification Handler")
                .setUncaughtExceptionHandler(LoggingUncaughtExceptionHandler.getInstance())
                .setDaemon(true)
                .build());

    launchScript = Paths.get(System.getenv("HOME"), "che/ls-java/launch.sh");
  }

  @PreDestroy
  public void shutDown() {
    notificationHandler.shutdownNow();
  }

  public void sendStatusReport(StatusReport report) {
    notificationHandler.execute(() -> handleStatusReport(report));
  }

  private void handleStatusReport(StatusReport report) {
    LOG.info("{}: {}", report.getType(), report.getMessage());
    if ("Started".equals(report.getType())) {
      isStarted.set(true);
      updateWorkspaceOnLSStarted();
    }
  }

  /** @return {@code true} if jd.ls has started, otherwise {@code false}. */
  public boolean isStarted() {
    return isStarted.get();
  }

  private void updateWorkspaceOnLSStarted() {

    projectManager
        .getAll()
        .forEach(
            registeredProject -> {
              if (new StringTokenizer(registeredProject.getPath(), "/", false).countTokens() == 1) {
                // only do this for root paths
                CompletableFuture.runAsync(
                    () -> projectSynchronizer.synchronize(registeredProject.getPath()));
              }
              try {
                projectManager.update(registeredProject);
                eventService.publish(new ProjectClassPathChangedEvent(registeredProject.getPath()));
                notifyTransmitter.sendNotification(
                    new ExecuteCommandParams(
                        Notifications.UPDATE_PROJECTS_CLASSPATH,
                        Collections.singletonList(registeredProject.getPath())));
              } catch (ForbiddenException
                  | ServerException
                  | NotFoundException
                  | ConflictException
                  | BadRequestException e) {
                LOG.error(
                    String.format(
                        "Failed to update project '%s' configuration", registeredProject.getName()),
                    e);
              }
            });
  }

  /**
   * The show message notification is sent from a server to a client to ask the client to display a
   * particular message in the user interface.
   *
   * @param report information about report
   */
  public void sendProgressReport(ProgressReport report) {
    processorJsonRpcCommunication.sendProgressNotification(report);
  }

  public CompletableFuture<Object> executeClientCommand(ExecuteCommandParams params) {
    return executeCliendCommandTransmitter.executeClientCommand(params);
  }

  public void sendNotification(ExecuteCommandParams params) {
    // reading from language server is blocked while processing this method: if we
    // want to do calls to langauge server, need to handle notifications async
    notificationHandler.execute(() -> handleNotification(params));
  }

  private void handleNotification(ExecuteCommandParams params) {
    String command = params.getCommand();
    List<Object> arguments = params.getArguments();
    switch (command) {
      case Notifications.UPDATE_PROJECTS_CLASSPATH:
        {
          List<Object> fixedPathList = new ArrayList<>(arguments.size());
          for (Object uri : arguments) {
            String uriString = convertToJson(uri).getAsString();
            String projectPath = removePrefixUri(uriString);
            fixedPathList.add(projectPath);
            projectManager
                .get(projectPath)
                .ifPresent(
                    project -> {
                      try {
                        LOG.info("updating projectconfig for {}", projectPath);
                        eventService.publish(new ProjectClassPathChangedEvent(project.getPath()));
                        projectManager.update(project);
                      } catch (ForbiddenException
                          | ServerException
                          | NotFoundException
                          | ConflictException
                          | BadRequestException e) {
                        throw toJsonRpcException(e);
                      }
                    });
          }
          params.setArguments(fixedPathList);
          notifyClient(params);

          break;
        }
      case Notifications.MAVEN_PROJECT_CREATED:
        {
          List<Object> fixedPathList = new ArrayList<>(arguments.size());
          for (Object uri : arguments) {
            String uriString = convertToJson(uri).getAsString();
            String projectPath = removePrefixUri(uriString);
            fixedPathList.add(projectPath);
            projectSynchronizer.ensureMavenProject(projectPath);
          }
          params.setArguments(fixedPathList);
          notifyClient(params);

          break;
        }
    }
  }

  void notifyClient(ExecuteCommandParams params) {
    notifyTransmitter.sendNotification(params);
  }

  private JsonRpcException toJsonRpcException(ApiException e) {
    if (e instanceof ForbiddenException) {
      return new JsonRpcException(Services.FORBIDDEN, e.getMessage());
    } else if (e instanceof ServerException) {
      return new JsonRpcException(Services.SERVER_ERROR, e.getMessage());
    } else if (e instanceof NotFoundException) {
      return new JsonRpcException(Services.NOT_FOUND, e.getMessage());
    } else if (e instanceof ConflictException) {
      return new JsonRpcException(Services.CONFLICT, e.getMessage());
    } else if (e instanceof BadRequestException) {
      return new JsonRpcException(Services.BAD_REQUEST, e.getMessage());
    } else {
      return new JsonRpcException(-27000, e.getMessage());
    }
  }

  @Override
  public RegexProvider getRegexpProvider() {
    return new RegexProvider() {

      @Override
      public Map<String, String> getLanguageRegexes() {
        HashMap<String, String> regex = new HashMap<>();
        regex.put("java", "(^jdt://.*|^chelib://.*|.*\\.java|.*\\.class)");
        return regex;
      }

      @Override
      public Set<String> getFileWatchPatterns() {
        Set<String> regex = new HashSet<>();
        regex.add("glob:**/*.java");
        regex.add("glob:**/pom.xml");
        regex.add("glob:**/*.gradle");
        regex.add("glob:**/.project");
        regex.add("glob:**/.classpath");
        regex.add("glob:**/settings/*.prefs");

        return regex;
      }
    };
  }

  @Override
  public String getProjectsRoot() {
    return rootDirPathProvider.get();
  }

  @Override
  public CommunicationProvider getCommunicationProvider() {
    ProcessBuilder processBuilder = new ProcessBuilder(launchScript.toString());
    processBuilder.directory(launchScript.getParent().toFile());
    processBuilder.redirectInput(ProcessBuilder.Redirect.PIPE);
    processBuilder.redirectOutput(ProcessBuilder.Redirect.PIPE);
    processBuilder.redirectError(Redirect.INHERIT);

    return new ProcessCommunicationProvider(processBuilder, "Che-LS-JDT");
  }

  @Override
  public InstallerStatusProvider getInstallerStatusProvider() {
    return new InstallerStatusProvider() {
      @Override
      public boolean isSuccessfullyInstalled() {
        return launchScript.toFile().exists();
      }

      @Override
      public String getCause() {
        return isSuccessfullyInstalled() ? null : "Launch script file does not exist";
      }
    };
  }

  @Override
  public InstanceProvider getInstanceProvider() {
    return new InstanceProvider() {

      @Override
      public LanguageServer get(LanguageClient client, InputStream in, OutputStream out) {
        Object javaLangClient =
            Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class[] {LanguageClient.class, JavaLanguageClient.class},
                new DynamicWrapper(JavaLanguageServerLauncher.this, client));

        Launcher<JavaLanguageServer> launcher =
            Launcher.createLauncher(javaLangClient, JavaLanguageServer.class, in, out);
        launcher.startListening();
        JavaLanguageServer proxy = launcher.getRemoteProxy();
        LanguageServer wrapped =
            (LanguageServer)
                Proxy.newProxyInstance(
                    getClass().getClassLoader(),
                    new Class[] {LanguageServer.class, FileContentAccess.class},
                    new DynamicWrapper(
                        new JavaLSWrapper(JavaLanguageServerLauncher.this, proxy), proxy));
        return wrapped;
      }
    };
  }

  public void pomChanged(String uri) {
    String pomPath = removePrefixUri(uri);
    projectManager
        .getClosest(pomPath)
        .ifPresent(
            project -> {
              try {
                LOG.info("updating projectconfig for {}", project.getPath());
                projectManager.update(project);
                notifyClient(
                    new ExecuteCommandParams(
                        Constants.POM_CHANGED, Collections.singletonList(pomPath)));
              } catch (ForbiddenException
                  | ServerException
                  | NotFoundException
                  | ConflictException
                  | BadRequestException e) {
                throw toJsonRpcException(e);
              }
            });
  }
}
