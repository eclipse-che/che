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
package org.eclipse.che.plugin.csharp.languageserver;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.annotation.PostConstruct;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.fs.server.PathTransformer;
import org.eclipse.che.api.languageserver.DefaultInstanceProvider;
import org.eclipse.che.api.languageserver.LanguageServerConfig;
import org.eclipse.che.api.languageserver.LanguageServerException;
import org.eclipse.che.api.languageserver.LanguageServerInitializedEvent;
import org.eclipse.che.api.languageserver.ProcessCommunicationProvider;
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.api.project.server.impl.RootDirPathProvider;
import org.eclipse.che.api.project.shared.RegisteredProject;
import org.eclipse.che.commons.lang.IoUtil;
import org.eclipse.che.plugin.csharp.inject.CSharpModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Evgen Vidolob */
@Singleton
public class CSharpLanguageServerConfig implements LanguageServerConfig {
  private static final Logger LOG = LoggerFactory.getLogger(CSharpLanguageServerConfig.class);

  private static final String REGEX = ".*\\.(cs|csx)$";

  private final RootDirPathProvider rootDirPathProvider;
  private final EventService eventService;
  private final PathTransformer pathTransformer;
  private final ProjectManager projectManager;

  private final Path launchScript;

  @Inject
  public CSharpLanguageServerConfig(
      RootDirPathProvider rootDirPathProvider,
      EventService eventService,
      PathTransformer pathTransformer,
      ProjectManager projectManager) {
    this.rootDirPathProvider = rootDirPathProvider;
    this.eventService = eventService;
    this.pathTransformer = pathTransformer;
    this.projectManager = projectManager;

    this.launchScript = Paths.get(System.getenv("HOME"), "che/ls-csharp/launch.sh");
  }

  @PostConstruct
  private void subscribe() {
    eventService.subscribe(this::onLSProxyInitialized, LanguageServerInitializedEvent.class);
  }

  private void onLSProxyInitialized(LanguageServerInitializedEvent event) {
    try {
      if ("org.eclipse.che.plugin.csharp.languageserver".equals(event.getId())) {
        Optional<RegisteredProject> project = projectManager.getClosest(event.getWsPath());
        if (project.isPresent()) {
          restoreDependencies(pathTransformer.transform(project.get().getPath()));
        }
      }
    } catch (LanguageServerException e) {
      LOG.error(e.getMessage(), e);
    }
  }

  private void restoreDependencies(Path workspaceRootFsPath) throws LanguageServerException {
    File workspaceRootFile = workspaceRootFsPath.toFile();
    if (workspaceRootFile.isDirectory()) {
      ProcessBuilder processBuilder = new ProcessBuilder("dotnet", "restore");
      processBuilder.directory(workspaceRootFile);

      try {
        Process process = processBuilder.start();
        int resultCode = process.waitFor();
        if (resultCode != 0) {
          String err = IoUtil.readStream(process.getErrorStream());
          String in = IoUtil.readStream(process.getInputStream());
          throw new LanguageServerException(
              "Can't restore dependencies. Error: " + err + ". Output: " + in);
        }
      } catch (IOException | InterruptedException e) {
        throw new LanguageServerException("Can't start CSharp language server", e);
      }
    }
  }

  @Override
  public RegexProvider getRegexpProvider() {
    return new RegexProvider() {
      @Override
      public Map<String, String> getLanguageRegexes() {
        return ImmutableMap.of(CSharpModule.LANGUAGE_ID, REGEX);
      }

      @Override
      public Set<String> getFileWatchPatterns() {
        return ImmutableSet.of();
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
    processBuilder.redirectInput(ProcessBuilder.Redirect.PIPE);
    processBuilder.redirectOutput(ProcessBuilder.Redirect.PIPE);

    return new ProcessCommunicationProvider(processBuilder, CSharpModule.LANGUAGE_ID);
  }

  @Override
  public InstanceProvider getInstanceProvider() {
    return DefaultInstanceProvider.getInstance();
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
}
