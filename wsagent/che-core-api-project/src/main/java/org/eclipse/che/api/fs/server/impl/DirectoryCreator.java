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
import java.util.Iterator;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.apache.commons.fileupload.FileItem;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.fs.server.FsPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
class DirectoryCreator {

  private static final Logger LOG = LoggerFactory.getLogger(DirectoryCreator.class);

  private final FsPaths pathResolver;
  private final DirectoryPacker directoryPacker;

  @Inject
  DirectoryCreator(FsPaths pathResolver, DirectoryPacker directoryPacker) {
    this.pathResolver = pathResolver;
    this.directoryPacker = directoryPacker;
  }

  void create(String wsPath) throws NotFoundException, ConflictException, ServerException {
    Path fsPath = pathResolver.toFsPath(wsPath);

    try {
      Files.createDirectory(fsPath);
    } catch (IOException e) {
      throw new ServerException("Failed to create directory " + wsPath, e);
    }
  }

  boolean createQuietly(String wsPath) {
    Path fsPath = pathResolver.toFsPath(wsPath);

    try {
      Files.deleteIfExists(fsPath);
      Files.createDirectories(fsPath);
      return true;
    } catch (IOException e) {
      LOG.error("Failed to quietly create directory " + wsPath, e);
      return false;
    }
  }

  void create(String wsPath, Iterator<FileItem> formData)
      throws NotFoundException, ConflictException, ServerException {
    FileItem contentItem = null;

    if (formData.hasNext()) {
      FileItem item = formData.next();
      if (!item.isFormField()) {
        contentItem = item;
      }
    }

    if (formData.hasNext()) {
      throw new ServerException("More then one upload file is found, but only one is expected");
    }

    if (contentItem == null) {
      throw new ServerException("Can't find file for upload");
    }

    try {
      directoryPacker.unzip(wsPath, contentItem.getInputStream());
    } catch (IOException e) {
      throw new ServerException("Failed to create directory " + wsPath, e);
    }
  }

  boolean createQuietly(String wsPath, Iterator<FileItem> formData) {
    try {
      create(wsPath, formData);
      return true;
    } catch (ConflictException | NotFoundException | ServerException e) {
      LOG.error("Failed to create directory {}", wsPath, e);
      return false;
    }
  }
}
