/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.fs.server.impl;

import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.toSet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.fs.server.FsManager;
import org.eclipse.che.api.fs.server.PathTransformer;

/** Performs workspace file system related operations after method parameters are validated. */
@Singleton
public class ExecutiveFsManager implements FsManager {

  private final FsOperations fsOperations;
  private final ZipArchiver zipArchiver;
  private final PathTransformer pathTransformer;

  @Inject
  public ExecutiveFsManager(
      FsOperations fsOperations, ZipArchiver zipArchiver, PathTransformer pathTransformer) {
    this.fsOperations = fsOperations;
    this.zipArchiver = zipArchiver;
    this.pathTransformer = pathTransformer;
  }

  @Override
  public void createFile(String wsPath, boolean overwrite, boolean withParents)
      throws NotFoundException, ConflictException, ServerException {
    Path fsPath = pathTransformer.transform(wsPath);

    if (overwrite) {
      fsOperations.deleteIfExists(fsPath);
    }

    if (withParents) {
      fsOperations.createFileWithParents(fsPath);
    } else {
      fsOperations.createFile(fsPath);
    }
  }

  @Override
  public void createDir(String wsPath, boolean overwrite, boolean withParents)
      throws NotFoundException, ConflictException, ServerException {
    Path fsPath = pathTransformer.transform(wsPath);

    if (overwrite) {
      fsOperations.deleteIfExists(fsPath);
    }

    if (withParents) {
      fsOperations.createDirWithParents(fsPath);
    } else {
      fsOperations.createDir(fsPath);
    }
  }

  @Override
  public InputStream read(String wsPath)
      throws NotFoundException, ConflictException, ServerException {
    Path fsPath = pathTransformer.transform(wsPath);

    return fsOperations.read(fsPath);
  }

  @Override
  public InputStream zip(String wsPath)
      throws NotFoundException, ConflictException, ServerException {
    Path fsPath = pathTransformer.transform(wsPath);

    return zipArchiver.zip(fsPath);
  }

  @Override
  public void unzip(
      String wsPath, InputStream packed, boolean overwrite, boolean withParents, boolean skipRoot)
      throws NotFoundException, ServerException, ConflictException {
    Path fsPath = pathTransformer.transform(wsPath);

    zipArchiver.unzip(fsPath, packed, overwrite, withParents, skipRoot);
  }

  @Override
  public void update(String wsPath, BiConsumer<InputStream, OutputStream> updater)
      throws NotFoundException, ConflictException, ServerException {
    Path fsPath = pathTransformer.transform(wsPath);
    Path tmpFsPath = pathTransformer.transform(wsPath + ".tmp");

    try {
      try (InputStream input = fsOperations.read(fsPath);
          OutputStream output = fsOperations.write(tmpFsPath)) {
        updater.accept(input, output);
      }

      fsOperations.deleteIfExists(fsPath);
      fsOperations.move(tmpFsPath, fsPath);
    } catch (IOException e) {
      throw new ServerException(e);
    }
  }

  @Override
  public void update(String wsPath, InputStream content)
      throws NotFoundException, ConflictException, ServerException {
    Path fsPath = pathTransformer.transform(wsPath);

    fsOperations.update(fsPath, content);
  }

  @Override
  public void delete(String wsPath, boolean quietly)
      throws NotFoundException, ConflictException, ServerException {
    Path fsPath = pathTransformer.transform(wsPath);

    if (quietly) {
      fsOperations.deleteIfExists(fsPath);
    } else {
      fsOperations.delete(fsPath);
    }
  }

  @Override
  public void copy(String srcWsPath, String dstWsPath, boolean overwrite, boolean withParents)
      throws NotFoundException, ConflictException, ServerException {
    Path srcFsPath = pathTransformer.transform(srcWsPath);
    Path dstFsPath = pathTransformer.transform(dstWsPath);

    if (overwrite) {
      fsOperations.deleteIfExists(dstFsPath);
    }

    if (withParents) {
      fsOperations.copyWithParents(srcFsPath, dstFsPath);
    } else {
      fsOperations.copy(srcFsPath, dstFsPath);
    }
  }

  @Override
  public void move(String srcWsPath, String dstWsPath, boolean overwrite, boolean withParents)
      throws NotFoundException, ConflictException, ServerException {
    Path srcFsPath = pathTransformer.transform(srcWsPath);
    Path dstFsPath = pathTransformer.transform(dstWsPath);

    if (overwrite) {
      fsOperations.deleteIfExists(dstFsPath);
    }

    if (withParents) {
      fsOperations.moveWithParents(srcFsPath, dstFsPath);
    } else {
      fsOperations.move(srcFsPath, dstFsPath);
    }
  }

  @Override
  public boolean isFile(String wsPath) {
    Path fsPath = pathTransformer.transform(wsPath);

    return fsOperations.isFile(fsPath);
  }

  @Override
  public boolean isDir(String wsPath) {
    Path fsPath = pathTransformer.transform(wsPath);

    return fsOperations.isDir(fsPath);
  }

  @Override
  public boolean exists(String wsPath) {
    Path fsPath = pathTransformer.transform(wsPath);

    return fsOperations.exists(fsPath);
  }

  @Override
  public long length(String wsPath) {
    Path fsPath = pathTransformer.transform(wsPath);

    return fsOperations.length(fsPath);
  }

  @Override
  public long lastModified(String wsPath) {
    Path fsPath = pathTransformer.transform(wsPath);

    return fsOperations.lastModified(fsPath);
  }

  @Override
  public Set<String> getFileNames(String wsPath) {
    Path fsPath = pathTransformer.transform(wsPath);

    return fsOperations.getFileNames(fsPath);
  }

  @Override
  public Set<String> getFileWsPaths(String wsPath) {
    Path fsPath = pathTransformer.transform(wsPath);

    Set<String> wsPaths =
        fsOperations.getFilePaths(fsPath).stream().map(pathTransformer::transform).collect(toSet());

    return unmodifiableSet(wsPaths);
  }

  @Override
  public Set<String> getDirNames(String wsPath) {
    Path fsPath = pathTransformer.transform(wsPath);

    return fsOperations.getDirNames(fsPath);
  }

  @Override
  public Set<String> getDirWsPaths(String wsPath) {
    Path fsPath = pathTransformer.transform(wsPath);

    Set<String> wsPaths =
        fsOperations.getDirPaths(fsPath).stream().map(pathTransformer::transform).collect(toSet());

    return unmodifiableSet(wsPaths);
  }

  @Override
  public Set<String> getAllChildrenNames(String wsPath) {
    Set<String> combined = new HashSet<>();
    combined.addAll(getFileNames(wsPath));
    combined.addAll(getDirNames(wsPath));
    return unmodifiableSet(combined);
  }

  @Override
  public Set<String> getAllChildrenWsPaths(String wsPath) {
    Set<String> combined = new HashSet<>();
    combined.addAll(getFileWsPaths(wsPath));
    combined.addAll(getDirWsPaths(wsPath));
    return unmodifiableSet(combined);
  }

  @Override
  public boolean existsAsFile(String wsPath) {
    return exists(wsPath) && isFile(wsPath);
  }

  @Override
  public boolean existsAsDir(String wsPath) {
    return exists(wsPath) && isDir(wsPath);
  }

  @Override
  public File toIoFile(String wsPath) {
    Path fsPath = pathTransformer.transform(wsPath);

    return fsOperations.toIoFile(fsPath);
  }
}
