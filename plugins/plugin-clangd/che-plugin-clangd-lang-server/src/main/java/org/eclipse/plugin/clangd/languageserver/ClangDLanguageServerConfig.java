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
package org.eclipse.plugin.clangd.languageserver;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;
import org.eclipse.che.api.languageserver.DefaultInstanceProvider;
import org.eclipse.che.api.languageserver.LanguageServerConfig;
import org.eclipse.che.api.languageserver.ProcessCommunicationProvider;
import org.eclipse.che.api.project.server.impl.RootDirPathProvider;
import org.eclipse.plugin.clangd.inject.ClangModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Alexander Andrienko
 * @author Hanno Kolvenbach
 */
@Singleton
public class ClangDLanguageServerConfig implements LanguageServerConfig {
  private static final Logger LOG = LoggerFactory.getLogger(ClangDLanguageServerConfig.class);

  private static final String REGEX = ".*\\.(c|h|cc|hh|cpp|hpp|cxx|hxx|C|H|CC|HH|CPP|HPP|CXX|HXX)$";

  private final Path launchScript;
  private final RootDirPathProvider rootDirPathProvider;

  @Inject
  public ClangDLanguageServerConfig(RootDirPathProvider rootDirPathProvider) {
    this.rootDirPathProvider = rootDirPathProvider;
    launchScript = Paths.get(System.getenv("HOME"), "che/ls-clangd/launch.sh");
  }

  @Override
  public RegexProvider getRegexpProvider() {
    return new RegexProvider() {
      @Override
      public Map<String, String> getLanguageRegexes() {
        return ImmutableMap.of(ClangModule.LANGUAGE_ID, REGEX);
      }

      @Override
      public Set<String> getFileWatchPatterns() {
        return ImmutableSet.of();
      }
    };
  }

  @Override
  public CommunicationProvider getCommunicationProvider() {
    ProcessBuilder processBuilder = new ProcessBuilder();

    processBuilder.command(launchScript.toString()).inheritIO();
    processBuilder.redirectInput(ProcessBuilder.Redirect.PIPE);
    processBuilder.redirectOutput(ProcessBuilder.Redirect.PIPE);

    return new ProcessCommunicationProvider(processBuilder, ClangModule.LANGUAGE_ID);
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

  @Override
  public String getProjectsRoot() {
    return rootDirPathProvider.get();
  }
}
