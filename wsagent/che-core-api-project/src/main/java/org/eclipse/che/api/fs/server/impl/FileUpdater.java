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

import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.eclipse.che.commons.lang.IoUtil.readStream;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.BiConsumer;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
class FileUpdater {

  private static final Logger LOG = LoggerFactory.getLogger(FileUpdater.class);

  private final StandardFsPaths pathResolver;

  @Inject
  FileUpdater(StandardFsPaths pathResolver) {
    this.pathResolver = pathResolver;
  }

  void update(String wsPath, BiConsumer<InputStream, OutputStream> updater)
      throws NotFoundException, ConflictException, ServerException {
    try {
      String parentWsPath = pathResolver.getParentWsPath(wsPath);
      Path parentFsPath = pathResolver.toFsPath(parentWsPath);
      String name = pathResolver.getName(wsPath);

      File tempFile = Files.createTempFile(parentFsPath, name, "tmp").toFile();
      File updatedFile = pathResolver.toFsPath(wsPath).toFile();

      try (BufferedInputStream input = new BufferedInputStream(new FileInputStream(updatedFile));
          BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(tempFile))) {
        updater.accept(input, output);
      }

      Files.move(tempFile.toPath(), updatedFile.toPath(), REPLACE_EXISTING, ATOMIC_MOVE);
      Files.deleteIfExists(tempFile.toPath());
    } catch (IOException e) {
      throw new ServerException(e);
    }
  }

  void update(String wsPath, InputStream content)
      throws NotFoundException, ConflictException, ServerException {
    updateInternally(wsPath, () -> readStream(content).getBytes());
  }

  void update(String wsPath, String content)
      throws NotFoundException, ConflictException, ServerException {
    updateInternally(wsPath, content::getBytes);
  }

  void update(String wsPath, byte[] content)
      throws NotFoundException, ConflictException, ServerException {
    updateInternally(wsPath, () -> content);
  }

  boolean updateQuietly(String wsPath, InputStream content) {
    return updateInternallyAndQuietly(wsPath, () -> readStream(content).getBytes());
  }

  boolean updateQuietly(String wsPath, String content) {
    return updateInternallyAndQuietly(wsPath, content::getBytes);
  }

  boolean updateQuietly(String wsPath, byte[] content) {
    return updateInternallyAndQuietly(wsPath, () -> content);
  }

  private void updateInternally(String wsPath, SupplierWithException<byte[], IOException> supplier)
      throws NotFoundException, ConflictException, ServerException {
    Path fsPath = pathResolver.toFsPath(wsPath);

    try {
      Files.write(fsPath, supplier.get());
    } catch (IOException e) {
      throw new ServerException("Failed to update file " + wsPath, e);
    }
  }

  private boolean updateInternallyAndQuietly(
      String wsPath, SupplierWithException<byte[], IOException> supplier) {
    Path fsPath = pathResolver.toFsPath(wsPath);

    try {
      Files.createDirectories(fsPath.getParent());

      if (!fsPath.toFile().exists()) {
        Files.createFile(fsPath);
      }

      updateInternally(wsPath, supplier);

      return true;
    } catch (IOException | NotFoundException | ConflictException | ServerException e) {
      LOG.error("Failed to quietly update file {}", wsPath, e);
    }
    return false;
  }
}
