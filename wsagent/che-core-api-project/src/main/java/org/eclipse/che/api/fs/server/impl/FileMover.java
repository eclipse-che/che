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
import org.eclipse.che.api.fs.server.FsPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
class FileMover {

  private static final Logger LOG = LoggerFactory.getLogger(FileMover.class);

  private final FsPaths fsPaths;

  @Inject
  FileMover(FsPaths fsPaths) {
    this.fsPaths = fsPaths;
  }

  void move(String srcWsPath, String dstWsPath)
      throws NotFoundException, ConflictException, ServerException {
    try {
      Path srcFsPath = fsPaths.toFsPath(srcWsPath);
      Path dstFsPath = fsPaths.toFsPath(dstWsPath);

      Files.move(srcFsPath, dstFsPath);
    } catch (IOException e) {
      throw new ServerException("Failed to move file " + srcWsPath + " to " + dstWsPath, e);
    }
  }

  boolean moveQuietly(String srcWsPath, String dstWsPath) {
    try {
      Path dstFsPath = fsPaths.toFsPath(dstWsPath);
      Path srcFsPath = fsPaths.toFsPath(srcWsPath);

      Files.createDirectories(dstFsPath.getParent());
      Files.deleteIfExists(dstFsPath.getParent());

      Files.move(srcFsPath, dstFsPath);
      return true;
    } catch (IOException e) {
      LOG.error("Failed to move file {} to {}", srcWsPath, dstWsPath);
      return false;
    }
  }
}
