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

import static java.util.stream.Collectors.toSet;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import javax.inject.Singleton;
import org.apache.commons.io.FileUtils;
import org.eclipse.che.api.core.ServerException;

@Singleton
class FsOperations {

  void createFile(Path fsPath) throws ServerException {
    try {
      Files.createFile(fsPath);
    } catch (IOException e) {
      throw new ServerException("Failed to create file: " + fsPath, e);
    }
  }

  void createFileWithParents(Path fsPath) throws ServerException {
    try {
      Files.createDirectories(fsPath.getParent());

      Files.createFile(fsPath);
    } catch (IOException e) {
      throw new ServerException("Failed to create file: " + fsPath, e);
    }
  }

  void createDir(Path fsPath) throws ServerException {
    try {
      Files.createDirectory(fsPath);
    } catch (IOException e) {
      throw new ServerException("Failed to create item: " + fsPath, e);
    }
  }

  void createDirWithParents(Path fsPath) throws ServerException {
    try {
      Files.createDirectories(fsPath.getParent());

      Files.createDirectory(fsPath);
    } catch (IOException e) {
      throw new ServerException("Failed to create item: " + fsPath, e);
    }
  }

  void copy(Path srcFsPath, Path dstFsPath) throws ServerException {
    try {
      if (Files.isDirectory(srcFsPath)) {
        FileUtils.copyDirectory(srcFsPath.toFile(), dstFsPath.toFile());
      } else {
        FileUtils.copyFile(srcFsPath.toFile(), dstFsPath.toFile());
      }
    } catch (IOException e) {
      throw new ServerException("Failed to copy item " + srcFsPath + " to " + dstFsPath, e);
    }
  }

  void copyWithParents(Path srcFsPath, Path dstFsPath) throws ServerException {
    try {
      Files.createDirectories(dstFsPath.getParent());

      if (Files.isDirectory(srcFsPath)) {
        FileUtils.copyDirectory(srcFsPath.toFile(), dstFsPath.toFile());
      } else {
        FileUtils.copyFile(srcFsPath.toFile(), dstFsPath.toFile());
      }
    } catch (IOException e) {
      throw new ServerException("Failed to copy item " + srcFsPath + " to " + dstFsPath, e);
    }
  }

  void move(Path srcFsPath, Path dstFsPath) throws ServerException {
    try {
      if (Files.isDirectory(srcFsPath)) {
        FileUtils.moveDirectory(srcFsPath.toFile(), dstFsPath.toFile());
      } else {
        FileUtils.moveFile(srcFsPath.toFile(), dstFsPath.toFile());
      }
    } catch (IOException e) {
      throw new ServerException("Failed to move item " + srcFsPath + " to " + dstFsPath, e);
    }
  }

  void moveWithParents(Path srcFsPath, Path dstFsPath) throws ServerException {
    try {
      Files.createDirectories(dstFsPath.getParent());

      if (Files.isDirectory(srcFsPath)) {
        FileUtils.moveDirectory(srcFsPath.toFile(), dstFsPath.toFile());
      } else {
        FileUtils.moveFile(srcFsPath.toFile(), dstFsPath.toFile());
      }
    } catch (IOException e) {
      throw new ServerException("Failed to move item " + srcFsPath + " to " + dstFsPath, e);
    }
  }

  void delete(Path fsPath) throws ServerException {
    try {
      if (Files.isDirectory(fsPath)) {
        FileUtils.deleteDirectory(fsPath.toFile());
      } else {
        Files.delete(fsPath);
      }
    } catch (IOException e) {
      throw new ServerException("Failed to delete item: " + fsPath, e);
    }
  }

  void deleteIfExists(Path fsPath) throws ServerException {
    if (!Files.exists(fsPath)) {
      return;
    }

    try {
      if (Files.isDirectory(fsPath)) {
        FileUtils.deleteDirectory(fsPath.toFile());
      } else {
        Files.deleteIfExists(fsPath);
      }
    } catch (IOException e) {
      throw new ServerException("Failed to delete item: " + fsPath, e);
    }
  }

  OutputStream write(Path fsPath) throws ServerException {
    try {
      File file = fsPath.toFile();
      return new BufferedOutputStream(new FileOutputStream(file));
    } catch (IOException e) {
      throw new ServerException("Failed to create file: " + fsPath, e);
    }
  }

  InputStream read(Path fsPath) throws ServerException {
    try {
      File file = fsPath.toFile();
      return new BufferedInputStream(new FileInputStream(file));
    } catch (IOException e) {
      throw new ServerException("Can't read content of file " + fsPath, e);
    }
  }

  void update(Path fsPath, InputStream content) throws ServerException {
    try {
      byte[] bytes = readStream(content).getBytes();
      Files.write(fsPath, bytes);
      content.close();
    } catch (IOException e) {
      throw new ServerException("Failed to update file: " + fsPath, e);
    }
  }

  boolean exists(Path fsPath) {
    return fsPath.toFile().exists();
  }

  boolean isFile(Path fsPath) {
    return fsPath.toFile().isFile();
  }

  boolean isDir(Path fsPath) {
    return fsPath.toFile().isDirectory();
  }

  File toIoFile(Path fsPath) {
    return fsPath.toFile();
  }

  Set<String> getFileNames(Path fsPath) {
    File[] files = fsPath.toFile().listFiles();

    if (files == null) {
      return Collections.emptySet();
    }

    Set<String> fileNames =
        Arrays.stream(files).filter(File::isFile).map(File::getName).collect(toSet());

    return Collections.unmodifiableSet(fileNames);
  }

  Set<Path> getFilePaths(Path fsPath) {
    File[] files = fsPath.toFile().listFiles();

    if (files == null) {
      return Collections.emptySet();
    }

    Set<Path> fileNames =
        Arrays.stream(files).filter(File::isFile).map(File::toPath).collect(toSet());

    return Collections.unmodifiableSet(fileNames);
  }

  Set<String> getDirNames(Path fsPath) {
    File[] files = fsPath.toFile().listFiles();

    if (files == null) {
      return Collections.emptySet();
    }

    Set<String> dirNames =
        Arrays.stream(files).filter(File::isDirectory).map(File::getName).collect(toSet());

    return Collections.unmodifiableSet(dirNames);
  }

  Set<Path> getDirPaths(Path fsPath) {
    File[] files = fsPath.toFile().listFiles();

    if (files == null) {
      return Collections.emptySet();
    }

    Set<Path> dirNames =
        Arrays.stream(files).filter(File::isDirectory).map(File::toPath).collect(toSet());

    return Collections.unmodifiableSet(dirNames);
  }

  long length(Path fsPath) {
    return fsPath.toFile().length();
  }

  long lastModified(Path fsPath) {
    return fsPath.toFile().lastModified();
  }
}
