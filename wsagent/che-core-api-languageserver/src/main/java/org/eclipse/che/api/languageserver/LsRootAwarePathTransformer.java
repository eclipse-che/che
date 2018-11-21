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

import com.google.inject.Singleton;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.inject.Inject;
import org.eclipse.che.api.languageserver.RegistryContainer.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Path transformer that uses project root registry data to transform workspace paths to language
 * server known path, takes into account congigured language server projects root directory path.
 */
@Singleton
class LsRootAwarePathTransformer {
  private static final Logger LOG = LoggerFactory.getLogger(LsRootAwarePathTransformer.class);

  private final Registry<String> projectsRootRegistry;

  @Inject
  LsRootAwarePathTransformer(RegistryContainer registryContainer) {
    this.projectsRootRegistry = registryContainer.projectsRootRegistry;
  }

  /**
   * Transform workspace path into language server path represented URI taking into account project
   * root location for the specified language server.
   *
   * @param lsId - language server ID
   * @param wsPath - workspace path
   * @return URI
   */
  URI toFsUri(String lsId, String wsPath) {
    return toFsPath(lsId, wsPath).toUri();
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
    String projectsRoot = projectsRootRegistry.getOrNull(lsId);
    if (projectsRoot == null) {
      LOG.error("There is no root path for a language with id: {}", lsId);
      return null;
    }

    Path projectsRootPath = Paths.get(projectsRoot);
    if (wsPath.equals(ROOT)) {
      return projectsRootPath;
    }

    wsPath = wsPath.startsWith(ROOT) ? wsPath.substring(1) : wsPath;

    return projectsRootPath.resolve(wsPath).normalize().toAbsolutePath();
  }

  /**
   * Transform file system path represented by a URI into workspace path. Transformation respects
   * langauge server project root location
   *
   * @param lsId - language server ID
   * @param uri - file system item URI
   * @return workspace path
   */
  String toWsPath(String lsId, URI uri) {
    String projectsRoot = projectsRootRegistry.getOrNull(lsId);
    if (projectsRoot == null) {
      LOG.error("There is no root path for a language with id: {}", lsId);
      return null;
    }

    Path path = Paths.get(uri);

    return ROOT + Paths.get(projectsRoot).relativize(path);
  }
}
