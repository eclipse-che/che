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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
class DirectoryCopier {

  private static final Logger LOG = LoggerFactory.getLogger(DirectoryCopier.class);

  private final StandardFsPaths pathResolver;

  @Inject
  public DirectoryCopier(StandardFsPaths pathResolver) {
    this.pathResolver = pathResolver;
  }

  void copy(String srcWsPath, String dstWsPath)
      throws NotFoundException, ConflictException, ServerException {
    File srcFile = pathResolver.toFsPath(srcWsPath).toFile();
    File dstFile = pathResolver.toFsPath(dstWsPath).toFile();

    try {
      FileUtils.copyDirectory(srcFile, dstFile);
    } catch (IOException e) {
      throw new ServerException("Failed to copy directory " + srcWsPath + " to " + dstWsPath, e);
    }
  }

  boolean copyQuietly(String srcWsPath, String dstWsPath) {
    Path srcFsPath = pathResolver.toFsPath(srcWsPath);
    Path dstFsPath = pathResolver.toFsPath(dstWsPath);

    try {
      FileUtils.deleteDirectory(dstFsPath.toFile());
      Files.createDirectories(dstFsPath.getParent());

      FileUtils.copyDirectory(srcFsPath.toFile(), dstFsPath.toFile());
      return true;
    } catch (IOException e) {
      LOG.error("Failed to quietly copy directory {} to {}", srcWsPath, dstWsPath, e);
      return false;
    }
  }
}
