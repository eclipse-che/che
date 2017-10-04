/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.fs.server.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
class FileCopier {

  private static final Logger LOG = LoggerFactory.getLogger(FileCopier.class);

  private final StandardFsPaths pathResolver;

  @Inject
  FileCopier(StandardFsPaths pathResolver) {
    this.pathResolver = pathResolver;
  }

  void copy(String srcWsPath, String dstWsPath)
      throws NotFoundException, ConflictException, ServerException {
    Path srcFsPath = pathResolver.toFsPath(srcWsPath);
    Path dstFsPath = pathResolver.toFsPath(dstWsPath);

    try {
      Files.copy(srcFsPath, dstFsPath);
    } catch (IOException e) {
      throw new ServerException("Failed to copy file " + srcWsPath + " to " + dstWsPath, e);
    }
  }

  boolean copyQuietly(String srcWsPath, String dstWsPath) {
    try {
      Path srcFsPath = pathResolver.toFsPath(srcWsPath);
      Path dstFsPath = pathResolver.toFsPath(dstWsPath);

      Files.createDirectories(dstFsPath.getParent());
      Files.deleteIfExists(dstFsPath);

      Files.copy(srcFsPath, dstFsPath);
      return true;
    } catch (IOException e) {
      LOG.error("Failed to quietly copy file {}", srcWsPath, e);
      return false;
    }
  }
}
