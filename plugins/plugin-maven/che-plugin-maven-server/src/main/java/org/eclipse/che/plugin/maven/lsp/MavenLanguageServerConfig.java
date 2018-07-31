/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.maven.lsp;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Map;
import java.util.Set;
import org.eclipse.che.api.languageserver.EmptyCommunicationProvider;
import org.eclipse.che.api.languageserver.LanguageServerConfig;
import org.eclipse.lsp4j.services.LanguageServer;

@Singleton
public class MavenLanguageServerConfig implements LanguageServerConfig {

  private final LanguageServer mavenLanguageServer;

  @Inject
  public MavenLanguageServerConfig(MavenLanguageServer mavenLanguageServer) {
    this.mavenLanguageServer = mavenLanguageServer;
  }

  @Override
  public RegexProvider getRegexpProvider() {
    return new RegexProvider() {
      @Override
      public Map<String, String> getLanguageRegexes() {
        return ImmutableMap.of("maven", ".*[/\\\\]?pom\\.xml$");
      }

      @Override
      public Set<String> getFileWatchPatterns() {
        return ImmutableSet.of();
      }
    };
  }

  @Override
  public CommunicationProvider getCommunicationProvider() {
    return EmptyCommunicationProvider.getInstance();
  }

  @Override
  public InstanceProvider getInstanceProvider() {
    return (client, in, out) -> mavenLanguageServer;
  }

  @Override
  public InstallerStatusProvider getInstallerStatusProvider() {
    return new InstallerStatusProvider() {
      @Override
      public boolean isSuccessfullyInstalled() {
        return true;
      }

      @Override
      public String getCause() {
        return null;
      }
    };
  }
}
