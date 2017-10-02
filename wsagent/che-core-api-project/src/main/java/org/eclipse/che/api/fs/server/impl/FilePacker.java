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

import static java.nio.file.Files.createTempFile;
import static java.nio.file.Files.newInputStream;
import static org.eclipse.che.api.fs.server.impl.FsConditionChecker.mustExist;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.apache.commons.io.IOUtils;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class FilePacker {

  private static final Logger LOG = LoggerFactory.getLogger(FilePacker.class);

  private final FsPathResolver pathResolver;

  @Inject
  public FilePacker(FsPathResolver pathResolver) {
    this.pathResolver = pathResolver;
  }

  public InputStream zipToInputStream(String wsPath) throws NotFoundException, ServerException {
    return zipInternally(wsPath, fsPath -> newInputStream(fsPath));
  }

  public String zipToString(String wsPath) throws NotFoundException, ServerException {
    return zipInternally(wsPath, fsPath -> new String(Files.readAllBytes(fsPath)));
  }

  public byte[] zipToByteArray(String wsPath) throws NotFoundException, ServerException {
    return zipInternally(wsPath, Files::readAllBytes);
  }

  public Optional<InputStream> zipToInputStreamQuietly(String wsPath) {
    return zipInternallyAndQuietly(wsPath, fsPath -> newInputStream(fsPath));
  }

  public Optional<String> zipToStringQuietly(String wsPath) {
    return zipInternallyAndQuietly(wsPath, fsPath -> new String(Files.readAllBytes(fsPath)));
  }

  public Optional<byte[]> zipToByteArrayQuietly(String wsPath) {
    return zipInternallyAndQuietly(wsPath, Files::readAllBytes);
  }

  public InputStream tarToInputStream(String wsPath) throws NotFoundException, ServerException {
    String msg = "Tar archives are yet not supported";
    LOG.error(msg);
    throw new UnsupportedOperationException(msg);
  }

  public String tarToString(String wsPath) throws NotFoundException, ServerException {
    String msg = "Tar archives are yet not supported";
    LOG.error(msg);
    throw new UnsupportedOperationException(msg);
  }

  public byte[] tarToByteArray(String wsPath) throws NotFoundException, ServerException {
    String msg = "Tar archives are yet not supported";
    LOG.error(msg);
    throw new UnsupportedOperationException(msg);
  }

  public Optional<InputStream> tarToInputStreamQuietly(String wsPath) {
    String msg = "Tar archives are yet not supported";
    LOG.error(msg);
    throw new UnsupportedOperationException(msg);
  }

  public Optional<String> tarToStringQuietly(String wsPath) {
    String msg = "Tar archives are yet not supported";
    LOG.error(msg);
    throw new UnsupportedOperationException(msg);
  }

  public Optional<byte[]> tarToByteArrayQuietly(String wsPath) {
    String msg = "Tar archives are yet not supported";
    LOG.error(msg);
    throw new UnsupportedOperationException(msg);
  }

  private <R> R zipInternally(String wsPath, FunctionWithException<Path, R, IOException> function)
      throws NotFoundException, ServerException {
    try {
      Path fsPath = pathResolver.toFsPath(wsPath);
      File inFile = fsPath.toFile();

      mustExist(fsPath);

      File outFile = createTempFile(fsPath.getFileName().toString(), ".zip").toFile();

      try (FileOutputStream fos = new FileOutputStream(outFile);
          FileInputStream fis = new FileInputStream(inFile);
          ZipOutputStream zos = new ZipOutputStream(fos)) {
        ZipEntry zipEntry = new ZipEntry(inFile.getName());
        zos.putNextEntry(zipEntry);
        IOUtils.copy(fis, zos);
      }
      return function.apply(outFile.toPath());
    } catch (IOException e) {
      String msg = "Failed to zip file: " + wsPath;
      LOG.error(msg);
      throw new ServerException(msg, e);
    }
  }

  private <R> Optional<R> zipInternallyAndQuietly(
      String wsPath, FunctionWithException<Path, R, IOException> function) {
    try {
      return Optional.ofNullable(zipInternally(wsPath, function));
    } catch (ServerException | NotFoundException e) {
      return Optional.empty();
    }
  }
}
