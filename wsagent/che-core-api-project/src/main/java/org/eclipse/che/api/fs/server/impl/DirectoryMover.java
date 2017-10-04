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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.apache.commons.io.FileUtils;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.fs.server.FsPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
class DirectoryMover {

  private static final Logger LOG = LoggerFactory.getLogger(DirectoryMover.class);

  private final FsPaths fsPaths;

  @Inject
  DirectoryMover(FsPaths fsPaths) {
    this.fsPaths = fsPaths;
  }

  void move(String srcWsPath, String dstWsPath)
      throws NotFoundException, ConflictException, ServerException {
    File srcFile = fsPaths.toFsPath(srcWsPath).toFile();
    File dstFile = fsPaths.toFsPath(dstWsPath).toFile();

    try {
      FileUtils.moveDirectory(srcFile, dstFile);
    } catch (IOException e) {
      throw new ServerException("Failed to move directory " + srcWsPath + " to " + dstWsPath, e);
    }
  }

  boolean moveQuietly(String srcWsPath, String dstWsPath) {
    Path srcFsPath = fsPaths.toFsPath(srcWsPath);
    Path dstFsPath = fsPaths.toFsPath(dstWsPath);

    try {
      Files.createDirectories(dstFsPath.getParent());
      FileUtils.moveDirectory(srcFsPath.toFile(), dstFsPath.toFile());
      return true;
    } catch (IOException e) {
      LOG.error("Failed to quietly move directory {} to {}", srcWsPath, dstFsPath, e);
      return false;
    }
  }
}
