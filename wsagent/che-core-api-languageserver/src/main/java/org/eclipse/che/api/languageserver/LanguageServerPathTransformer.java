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

import static org.eclipse.che.api.fs.server.WsPathUtils.ROOT;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.inject.Inject;
import javax.inject.Singleton;
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
class LanguageServerPathTransformer {
  private static final Logger LOG = LoggerFactory.getLogger(LanguageServerPathTransformer.class);

  private final RootDirPathProvider rootDirPathProvider;
  private final Registry<String> projectsRootRegistry;

  @Inject
  LanguageServerPathTransformer(
      RootDirPathProvider rootDirPathProvider, RegistryContainer registryContainer) {
    this.rootDirPathProvider = rootDirPathProvider;
    this.projectsRootRegistry = registryContainer.projectsRootRegistry;
  }

  /**
   * Transform workspace path into language server URI represented path taking into account project
   * root location for the specified language server.
   *
   * @param lsId - language server ID
   * @param wsPath - workspace path
   * @return path
   */
  URI toFsURI(String lsId, String wsPath) {
    if (wsPath.startsWith(ROOT)) {
      wsPath = wsPath.substring(1);
    }

    String projectsRoot = projectsRootRegistry.getOrDefault(lsId, rootDirPathProvider.get());

    return Paths.get(projectsRoot).resolve(wsPath).toUri();
  }

  /**
   * Transform workspace path into language server path represented path taking into account project
   * root location for the specified language server.
   *
   * @param lsId - language server ID
   * @param wsPath - workspace path
   * @return path
   */
  Path toFsPath(String lsId, String wsPath) {
    return Paths.get(toFsURI(lsId, wsPath));
  }

  /**
   * Transform file system path represented by a URI into workspace path. Transformation respects
   * language server project root location
   *
   * @param lsId - language server ID
   * @param fsUri - file system item URI
   * @return workspace path
   */
  String toWsPath(String lsId, String fsUri) {
    String projectsRoot = projectsRootRegistry.getOrDefault(lsId, rootDirPathProvider.get());

    URI uri;
    try {
      uri = new URI(fsUri);
    } catch (URISyntaxException e) {
      LOG.error("Can't parse uri: {}", fsUri, e);
      return null;
    }
    return ROOT + Paths.get(projectsRoot).toUri().relativize(uri).getPath();
  }

  /**
   * Transform file system path represented by a URI into workspace path. Transformation respects
   * language server project root location
   *
   * @param lsId - language server ID
   * @param fsUri - file system item URI
   * @return workspace path
   */
  String toWsPath(String lsId, URI fsUri) {
    String projectsRoot = projectsRootRegistry.getOrDefault(lsId, rootDirPathProvider.get());

    return ROOT + Paths.get(projectsRoot).toUri().relativize(fsUri).getPath();
  }

  String toPath(URI uri) {
    return uri.getPath();
  }

  String toPath(String stringUri) {
    try {
      URI uri = new URI(stringUri);
      return toPath(uri);
    } catch (URISyntaxException e) {
      LOG.error("Can't parse URI: {}", stringUri, e);
      return null;
    }
  }

  /**
   * Check if location represented by a URI is absolute for specified language server
   *
   * @param lsId - language server ID
   * @param fsUri - file system URI
   * @return true if the path segment of the URI is absolute
   */
  boolean isAbsolute(String lsId, URI fsUri) {
    String projectsRoot = projectsRootRegistry.getOrDefault(lsId, rootDirPathProvider.get());

    return fsUri.getScheme().equals("file") && fsUri.getPath().startsWith(projectsRoot);
  }

  /**
   * Check if location represented by a stringified path is absolute for specified language server
   *
   * @param lsId - language server ID
   * @param fsPath - stringified file system path
   * @return true if the path segment of the path is absolute
   */
  boolean isAbsolute(String lsId, String fsPath) {
    String projectsRoot = projectsRootRegistry.getOrDefault(lsId, rootDirPathProvider.get());

    return fsPath.startsWith(projectsRoot);
  }
}
