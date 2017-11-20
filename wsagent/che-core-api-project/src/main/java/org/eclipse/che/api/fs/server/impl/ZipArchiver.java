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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.inject.Singleton;
import org.apache.commons.io.IOUtils;
import org.eclipse.che.api.core.ServerException;

@Singleton
class ZipArchiver {

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

  InputStream zip(Path fsPath) throws ServerException {
    try {
      File inFile = fsPath.toFile();
      File outFile = createTempFile(fsPath.getFileName().toString(), ".zip").toFile();

      try (FileOutputStream fos = new FileOutputStream(outFile);
          ZipOutputStream zos = new ZipOutputStream(fos)) {
        zip(inFile, inFile.getName(), zos);
      }

      return newInputStream(outFile.toPath());
    } catch (IOException e) {
      throw new ServerException("Failed to zip item: " + fsPath, e);
    }
  }

  void unzip(
      Path fsPath, InputStream content, boolean overwrite, boolean withParents, boolean skipRoot)
      throws ServerException {
    try {
      if (withParents) {
        Files.createDirectories(fsPath);
      }

      try (ZipInputStream zis = new ZipInputStream(content)) {
        ZipEntry zipEntry = zis.getNextEntry();

        String prefixToSkip = null;
        if (zipEntry.isDirectory() && skipRoot) {
          prefixToSkip = zipEntry.getName();
          zipEntry = zis.getNextEntry();
        }

        while (zipEntry != null) {
          String name =
              prefixToSkip != null
                  ? zipEntry.getName().replaceFirst(prefixToSkip, "")
                  : zipEntry.getName();
          Path path = fsPath.resolve(name);

          if (overwrite) {
            Files.deleteIfExists(path);
          }
          if (zipEntry.isDirectory()) {
            Files.createDirectory(path);
          } else {
            try (FileOutputStream fos = new FileOutputStream(path.toFile())) {
              IOUtils.copy(zis, fos);
            }
          }

          zipEntry = zis.getNextEntry();
        }
      }
    } catch (IOException e) {
      throw new ServerException("Failed to unzip item " + fsPath, e);
    }
  }
}
