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

import static org.eclipse.che.api.fs.server.impl.FsConditionChecker.mustExist;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class FileDeleter {

  private static final Logger LOG = LoggerFactory.getLogger(FileDeleter.class);

  private final FsPathResolver pathResolver;

  @Inject
  public FileDeleter(FsPathResolver pathResolver) {
    this.pathResolver = pathResolver;
  }

  public void delete(String wsPath) throws NotFoundException, ServerException {
    Path fsPath = pathResolver.toFsPath(wsPath);

    mustExist(fsPath);

    try {
      Files.delete(fsPath);
    } catch (IOException e) {
      String msg = "Failed to delete file: " + wsPath;
      LOG.error(msg);
      throw new ServerException(msg, e);
    }
  }

  public boolean deleteQuietly(String wsPath) {
    try {
      Path fsPath = pathResolver.toFsPath(wsPath);
      return Files.deleteIfExists(fsPath);
    } catch (IOException e) {
      LOG.error("Failed to quietly delete file: " + wsPath);
      return false;
    }
  }
}
