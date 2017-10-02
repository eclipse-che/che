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
package org.eclipse.che.api.fs.impl;

import static org.eclipse.che.api.fs.impl.FsConditionChecker.mustExist;
import static org.eclipse.che.api.fs.impl.FsConditionChecker.mustNotExist;

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
public class FileCopier {

  private static final Logger LOG = LoggerFactory.getLogger(FileCopier.class);

  private final PathResolver pathResolver;

  @Inject
  public FileCopier(PathResolver pathResolver) {
    this.pathResolver = pathResolver;
  }

  public void copy(String srcWsPath, String dstWsPath)
      throws NotFoundException, ConflictException, ServerException {
    Path srcFsPath = pathResolver.toFsPath(srcWsPath);
    Path dstFsPath = pathResolver.toFsPath(dstWsPath);

    mustExist(srcFsPath);
    mustNotExist(dstFsPath);

    Path fileName = srcFsPath.getFileName();

    mustNotExist(dstFsPath.resolve(fileName));

    try {
      Files.copy(srcFsPath, dstFsPath);
    } catch (IOException e) {
      String msg = "Failed to copy file: " + srcWsPath;
      LOG.error(msg);
      throw new ServerException(msg, e);
    }
  }

  public boolean copyQuietly(String srcWsPath, String dstWsPath) {
    try {
      Path srcFsPath = pathResolver.toFsPath(srcWsPath);
      Path dstFsPath = pathResolver.toFsPath(dstWsPath);

      Path fileName = srcFsPath.getFileName();

      Files.createDirectories(dstFsPath);
      Files.deleteIfExists(dstFsPath.resolve(fileName));

      Files.copy(srcFsPath, dstFsPath);
      return true;
    } catch (IOException e) {
      LOG.error("Failed to quietly delete file: " + srcWsPath);
      return false;
    }
  }
}
