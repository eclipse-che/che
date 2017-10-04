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
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.fs.server.FsPathResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class FileMover {

  private static final Logger LOG = LoggerFactory.getLogger(FileMover.class);

  private final FsPathResolver fsPathResolver;

  @Inject
  public FileMover(FsPathResolver fsPathResolver) {
    this.fsPathResolver = fsPathResolver;
  }

  public void move(String srcWsPath, String dstWsPath)
      throws NotFoundException, ConflictException, ServerException {
    try {
      Files.move(fsPathResolver.toFsPath(srcWsPath), fsPathResolver.toFsPath(dstWsPath));
    } catch (IOException e) {
      throw new ServerException("Failed to move file: " + srcWsPath, e);
    }
  }

  public boolean moveQuietly(String srcWsPath, String dstWsPath) {
    try {
      Files.createDirectories(fsPathResolver.toFsPath(dstWsPath).getParent());
      Files.move(fsPathResolver.toFsPath(srcWsPath), fsPathResolver.toFsPath(dstWsPath));
      return true;
    } catch (IOException e) {
      LOG.error("Failed to move file: {}", srcWsPath);
      return false;
    }
  }
}
