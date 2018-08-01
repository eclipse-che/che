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
package org.eclipse.che.plugin.test.ls.languageserver;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.languageserver.DefaultInstanceProvider;
import org.eclipse.che.api.languageserver.LanguageServerConfig;
import org.eclipse.che.api.languageserver.ProcessCommunicationProvider;
import org.eclipse.che.plugin.test.ls.inject.TestLSModule;

/**
 * Launcher for simple test Language server
 *
 * @author Evgen Vidolob
 */
@Singleton
public class TestLanguageServerConfig implements LanguageServerConfig {
  private final Path launchScript;

  @Inject
  public TestLanguageServerConfig() {
    launchScript = Paths.get(System.getenv("HOME"), "che/test-ls/launch.sh");
  }

  @Override
  public RegexProvider getRegexpProvider() {
    return new RegexProvider() {
      @Override
      public Map<String, String> getLanguageRegexes() {
        return ImmutableMap.of(TestLSModule.LANGUAGE_ID, "_");
      }

      @Override
      public Set<String> getFileWatchPatterns() {
        return ImmutableSet.of();
      }
    };
  }

  @Override
  public CommunicationProvider getCommunicationProvider() {
    ProcessBuilder processBuilder = new ProcessBuilder(launchScript.toString());
    processBuilder.redirectInput(ProcessBuilder.Redirect.PIPE);
    processBuilder.redirectOutput(ProcessBuilder.Redirect.PIPE);

    return new ProcessCommunicationProvider(processBuilder, TestLSModule.LANGUAGE_ID);
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
