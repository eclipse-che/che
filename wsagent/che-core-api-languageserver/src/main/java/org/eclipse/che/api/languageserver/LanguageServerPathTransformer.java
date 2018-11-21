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
package org.eclipse.che.api.languageserver;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.fs.server.WsPathUtils;
import org.eclipse.che.api.languageserver.RegistryContainer.Registry;
import org.eclipse.che.api.project.server.impl.RootDirPathProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Language server path transformer
 *
 * @author Dmytro Kulieshov
 */
@Singleton
public class LanguageServerPathTransformer {
  private static final Logger LOG = LoggerFactory.getLogger(LanguageServerPathTransformer.class);

  private final RootDirPathProvider rootDirPathProvider;
  private final Registry<String> projectsRootRegistry;

  @Inject
  public LanguageServerPathTransformer(
      RootDirPathProvider rootDirPathProvider, RegistryContainer registryContainer) {
    this.rootDirPathProvider = rootDirPathProvider;
    this.projectsRootRegistry = registryContainer.projectsRootRegistry;
  }

  public URI resolve(String path, String id) {
    if (path.startsWith("/")) {
      path = path.substring(1);
    }

    String projectsRoot = projectsRootRegistry.getOrDefault(id, rootDirPathProvider.get());

    return Paths.get(projectsRoot).resolve(path).toUri();
  }

  public String toWsPath(String stringUri, String id) {
    String projectsRoot = projectsRootRegistry.getOrDefault(id, rootDirPathProvider.get());

    URI uri;
    try {
      uri = new URI(stringUri);
    } catch (URISyntaxException e) {
      LOG.error("Can't parse uri: {}", stringUri, e);
      return null;
    }
    return "/" + Paths.get(projectsRoot).toUri().relativize(uri).getPath();
  }

  public String toWsPath(URI uri, String id) {
    String projectsRoot = projectsRootRegistry.getOrDefault(id, rootDirPathProvider.get());

    return "/" + Paths.get(projectsRoot).toUri().relativize(uri).getPath();
  }

  public String toPath(URI uri) {
    return uri.getPath();
  }

  public String toPath(String stringUri) {
    URI uri;
    try {
      uri = new URI(stringUri);
    } catch (URISyntaxException e) {
      LOG.error("Can't parse uri: {}", stringUri, e);
      return null;
    }
    return toPath(uri);
  }

  public boolean isAbsolute(URI uri, String id) {
    String projectsRoot = projectsRootRegistry.getOrDefault(id, rootDirPathProvider.get());

    return uri.getScheme().equals("file") && uri.getPath().startsWith(projectsRoot);
  }

  public boolean isAbsolute(String path, String id) {
    String projectsRoot = projectsRootRegistry.getOrDefault(id, rootDirPathProvider.get());

    return path.startsWith(projectsRoot);
  }

  public String addProjectsRoot(String path, String id) {
    if (path.startsWith("/")) {
      path = path.substring(1);
    }

    String projectsRoot = projectsRootRegistry.getOrDefault(id, rootDirPathProvider.get());

    return WsPathUtils.resolve(projectsRoot, path);
  }
}
