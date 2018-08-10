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

import static java.util.Collections.singletonList;
import static org.eclipse.che.api.languageserver.LanguageServiceUtils.removePrefixUri;
import static org.eclipse.che.api.languageserver.util.JsonUtil.convertToJson;
import static org.eclipse.che.jdt.ls.extension.api.Commands.CLIENT_UPDATE_ON_PROJECT_CLASSPATH_CHANGED;
import static org.eclipse.che.jdt.ls.extension.api.Commands.CLIENT_UPDATE_PROJECT;
import static org.eclipse.che.jdt.ls.extension.api.Commands.CLIENT_UPDATE_PROJECTS_CLASSPATH;
import static org.eclipse.che.jdt.ls.extension.api.Commands.CLIENT_UPDATE_PROJECT_CONFIG;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ProcessBuilder.Redirect;
import java.lang.reflect.Proxy;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.languageserver.LanguageServerConfig;
import org.eclipse.che.api.languageserver.ProcessCommunicationProvider;
import org.eclipse.che.api.languageserver.service.FileContentAccess;
import org.eclipse.che.api.languageserver.util.DynamicWrapper;
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.api.project.server.notification.ProjectUpdatedEvent;
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
  private ProcessorJsonRpcCommunication processorJsonRpcCommunication;
  private ExecuteClientCommandJsonRpcTransmitter executeCliendCommandTransmitter;
  private EventService eventService;
  private ProjectManager projectManager;

  @Inject
  public JavaLanguageServerLauncher(
      ProcessorJsonRpcCommunication processorJsonRpcCommunication,
      ExecuteClientCommandJsonRpcTransmitter executeCliendCommandTransmitter,
      EventService eventService,
      ProjectManager projectManager) {
    this.processorJsonRpcCommunication = processorJsonRpcCommunication;
    this.executeCliendCommandTransmitter = executeCliendCommandTransmitter;
    this.eventService = eventService;
    this.projectManager = projectManager;
    launchScript = Paths.get(System.getenv("HOME"), "che/ls-java/launch.sh");
  }

  public void sendStatusReport(StatusReport report) {
    LOG.info("{}: {}", report.getType(), report.getMessage());
    if ("Started".equals(report.getType())) {
      updateWorkspaceOnLSStarted();
    }
  }

  private void updateWorkspaceOnLSStarted() {
    projectManager
        .getAll()
        .forEach(
            registeredProject -> {
              if (!registeredProject.getProblems().isEmpty()) {
                try {

                  projectManager.update(registeredProject);
                  eventService.publish(new ProjectUpdatedEvent(registeredProject.getPath()));
                } catch (ForbiddenException
                    | ServerException
                    | NotFoundException
                    | ConflictException
                    | BadRequestException e) {
                  LOG.error(
                      String.format(
                          "Failed to update project '%s' configuration",
                          registeredProject.getName()),
                      e);
                }
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
    return executeCliendCommandTransmitter.executeClientCommand(convertParams(params));
  }

  private ExecuteCommandParams convertParams(ExecuteCommandParams params) {
    String command = params.getCommand();
    switch (command) {
      case CLIENT_UPDATE_PROJECTS_CLASSPATH:
      case CLIENT_UPDATE_ON_PROJECT_CLASSPATH_CHANGED:
        List<Object> fixedPathList = new ArrayList<>(params.getArguments().size());
        for (Object uri : params.getArguments()) {
          fixedPathList.add(removePrefixUri(convertToJson(uri).getAsString()));
        }
        params.setArguments(fixedPathList);
        break;
      case CLIENT_UPDATE_PROJECT:
      case CLIENT_UPDATE_PROJECT_CONFIG:
        Object projectUri = params.getArguments().get(0);
        params.setArguments(
            singletonList(removePrefixUri(convertToJson(projectUri).getAsString())));
        break;
      default:
        break;
    }
    return params;
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
                    new DynamicWrapper(new JavaLSWrapper(proxy), proxy));
        return wrapped;
      }
    };
  }
}
