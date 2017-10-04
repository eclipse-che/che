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
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.apache.commons.io.FileUtils;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
class FileReader {

  private static final Logger LOG = LoggerFactory.getLogger(FileReader.class);

  private final StandardFsPaths pathResolver;

  @Inject
  FileReader(StandardFsPaths pathResolver) {
    this.pathResolver = pathResolver;
  }

  InputStream readAsInputStream(String wsPath) throws NotFoundException, ServerException {
    return readInternally(wsPath, fsPath -> FileUtils.openInputStream(fsPath.toFile()));
  }

  String readAsString(String wsPath) throws NotFoundException, ServerException {
    return readInternally(wsPath, fsPath -> new String(Files.readAllBytes(fsPath)));
  }

  byte[] readAsByteArray(String wsPath) throws NotFoundException, ServerException {
    return readInternally(wsPath, Files::readAllBytes);
  }

  Optional<InputStream> readAsInputStreamQuietly(String wsPath) {
    return readInternallyAndQuietly(wsPath, fsPath -> FileUtils.openInputStream(fsPath.toFile()));
  }

  Optional<String> readAsStringQuietly(String wsPath) {
    return readInternallyAndQuietly(wsPath, fsPath -> new String(Files.readAllBytes(fsPath)));
  }

  Optional<byte[]> readAsByteArrayQuietly(String wsPath) {
    return readInternallyAndQuietly(wsPath, Files::readAllBytes);
  }

  private <R> Optional<R> readInternallyAndQuietly(
      String wsPath, FunctionWithException<Path, R, IOException> function) {
    try {
      return Optional.ofNullable(readInternally(wsPath, function));
    } catch (NotFoundException | ServerException e) {
      LOG.error("Can't read content of file {}", wsPath, e);
      return Optional.empty();
    }
  }

  private <R> R readInternally(String wsPath, FunctionWithException<Path, R, IOException> function)
      throws NotFoundException, ServerException {
    Path fsPath = pathResolver.toFsPath(wsPath);

    try {
      return function.apply(fsPath);
    } catch (IOException e) {
      throw new ServerException("Can't read content of file " + wsPath, e);
    }
  }
}
