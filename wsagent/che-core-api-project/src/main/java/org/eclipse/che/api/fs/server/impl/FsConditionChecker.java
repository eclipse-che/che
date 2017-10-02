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

import java.nio.file.Path;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FsConditionChecker {

  private static final Logger LOG = LoggerFactory.getLogger(FsConditionChecker.class);

  public static void mustExist(Path fsPath) throws NotFoundException {
    if (!fsPath.toFile().exists()) {
      String message = "FS item '" + fsPath.toString() + "' does not exist";
      LOG.error(message);
      throw new NotFoundException(message);
    }
  }

  public static void mustBeAFile(Path fsPath) throws ConflictException {
    if (!fsPath.toFile().isFile()) {
      String message = "FS item '" + fsPath.toString() + "' must be a file";
      LOG.error(message);
      throw new ConflictException(message);
    }
  }

  public static void mustBeADirectory(Path fsPath) throws ConflictException {
    if (!fsPath.toFile().isDirectory()) {
      String message = "FS item '" + fsPath.toString() + "' must be a directory";
      LOG.error(message);
      throw new ConflictException(message);
    }
  }

  public static void mustNotExist(Path fsPath) throws ConflictException {
    if (fsPath.toFile().exists()) {
      String message = "FS item '" + fsPath.toString() + "' already exists";
      LOG.error(message);
      throw new ConflictException(message);
    }
  }
}
