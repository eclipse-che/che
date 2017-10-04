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
import static java.nio.file.Files.readAllBytes;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.apache.commons.io.IOUtils;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class DirectoryPacker {

  private static final Logger LOG = LoggerFactory.getLogger(DirectoryPacker.class);

  private final SimpleFsPathResolver pathResolver;

  @Inject
  public DirectoryPacker(SimpleFsPathResolver pathResolver) {
    this.pathResolver = pathResolver;
  }

  private static void zip(File zipInFile, String fileName, ZipOutputStream zos) throws IOException {
    if (zipInFile.isDirectory()) {
      File[] files = zipInFile.listFiles();
      for (File file : files == null ? new File[0] : files) {
        zip(file, file.getAbsolutePath(), zos);
      }
      return;
    }

    try (FileInputStream fis = new FileInputStream(zipInFile); ) {
      ZipEntry zipEntry = new ZipEntry(fileName);
      zos.putNextEntry(zipEntry);
      IOUtils.copy(fis, zos);
    }
  }

  public InputStream zipToInputStream(String wsPath) throws NotFoundException, ServerException {
    return zipInternally(wsPath, fsPath -> newInputStream(fsPath));
  }

  public String zipToString(String wsPath) throws NotFoundException, ServerException {
    return zipInternally(wsPath, fsPath -> new String(readAllBytes(fsPath)));
  }

  public byte[] zipToByteArray(String wsPath) throws NotFoundException, ServerException {
    return zipInternally(wsPath, Files::readAllBytes);
  }

  public Optional<InputStream> zipToInputStreamQuietly(String wsPath)
      throws NotFoundException, ServerException {
    return zipInternallyAndQuietly(wsPath, fsPath -> newInputStream(fsPath));
  }

  public Optional<String> zipToStringQuietly(String wsPath)
      throws NotFoundException, ServerException {
    return zipInternallyAndQuietly(wsPath, fsPath -> new String(readAllBytes(fsPath)));
  }

  public Optional<byte[]> zipToByteArrayQuietly(String wsPath)
      throws NotFoundException, ServerException {
    return zipInternallyAndQuietly(wsPath, Files::readAllBytes);
  }

  public void unzip(String wsPath, InputStream content)
      throws NotFoundException, ConflictException, ServerException {
    unzip(wsPath, content, false);
  }

  public void unzip(String wsPath, InputStream content, boolean skipRoot)
      throws NotFoundException, ConflictException, ServerException {
    try {
      Path fsPath = pathResolver.toFsPath(wsPath);

      if (!fsPath.toFile().exists()) {
        throw new NotFoundException("FS item '" + fsPath.toString() + "' does not exist");
      }

      unzipInternally(content, skipRoot, fsPath);
    } catch (IOException e) {
      throw new ServerException("Failed to unzip directory: " + wsPath, e);
    }
  }

  public boolean unzipQuietly(String wsPath, InputStream content) {
    return unzipQuietly(wsPath, content, false);
  }

  public boolean unzipQuietly(String wsPath, InputStream content, boolean skipRoot) {
    try {
      Path fsPath = pathResolver.toFsPath(wsPath);

      Files.createDirectories(fsPath);

      unzipInternally(content, skipRoot, fsPath);
      return true;
    } catch (IOException e) {
      LOG.error("Failed to quietly unzip directory: " + wsPath, e);
      return false;
    }
  }

  private void unzipInternally(InputStream content, boolean skipRoot, Path fsPath)
      throws IOException {
    try (ZipInputStream zis = new ZipInputStream(content)) {
      ZipEntry zipEntry = zis.getNextEntry();

      if (zipEntry.isDirectory() && skipRoot) {
        zipEntry = zis.getNextEntry();
      }

      while (zipEntry != null) {
        String name = zipEntry.getName();
        Path path = fsPath.resolve(name);
        Files.createDirectories(path.getParent());

        try (FileOutputStream fos = new FileOutputStream(path.toFile())) {
          IOUtils.copy(zis, fos);
        }

        zipEntry = zis.getNextEntry();
      }
    }
  }

  private <R> R zipInternally(String wsPath, FunctionWithException<Path, R, IOException> function)
      throws ServerException, NotFoundException {
    Path fsPath = pathResolver.toFsPath(wsPath);
    if (!fsPath.toFile().exists()) {
      throw new NotFoundException("FS item '" + fsPath.toString() + "' does not exist");
    }

    try {
      File inFile = fsPath.toFile();
      File outFile = createTempFile(fsPath.getFileName().toString(), ".zip").toFile();

      try (FileOutputStream fos = new FileOutputStream(outFile);
          ZipOutputStream zos = new ZipOutputStream(fos)) {
        zip(inFile, inFile.getName(), zos);
      }

      return function.apply(outFile.toPath());
    } catch (IOException e) {
      throw new ServerException("Failed to zip directory: " + wsPath, e);
    }
  }

  private <R> Optional<R> zipInternallyAndQuietly(
      String wsPath, FunctionWithException<Path, R, IOException> function)
      throws ServerException, NotFoundException {
    Path fsPath = pathResolver.toFsPath(wsPath);

    try {
      Files.createDirectories(fsPath);

      File inFile = fsPath.toFile();
      File outFile = createTempFile(fsPath.getFileName().toString(), ".zip").toFile();

      try (FileOutputStream fos = new FileOutputStream(outFile);
          ZipOutputStream zos = new ZipOutputStream(fos)) {
        zip(inFile, inFile.getName(), zos);
      }

      R apply = function.apply(outFile.toPath());
      return Optional.of(apply);
    } catch (IOException e) {
      LOG.error("Failed to quietly zip directory: " + wsPath, e);
      return Optional.empty();
    }
  }
}
