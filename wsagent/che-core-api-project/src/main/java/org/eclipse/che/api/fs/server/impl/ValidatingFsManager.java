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

import static org.eclipse.che.api.fs.server.WsPathUtils.parentOf;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Set;
import java.util.function.BiConsumer;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.fs.server.FsManager;
import org.eclipse.che.api.fs.server.PathTransformer;

@Singleton
public class ValidatingFsManager implements FsManager {

  private final FsOperations fsOperations;
  private final PathTransformer pathTransformer;
  private final FsManager suspendingFsManager;

  @Inject
  public ValidatingFsManager(
      FsOperations fsOperations,
      PathTransformer pathTransformer,
      ExecutiveFsManager suspendingFsManager) {
    this.fsOperations = fsOperations;
    this.pathTransformer = pathTransformer;
    this.suspendingFsManager = suspendingFsManager;
  }

  public void createFile(String wsPath, boolean overwrite, boolean withParents)
      throws NotFoundException, ConflictException, ServerException {
    Path fsPath = pathTransformer.transform(wsPath);
    boolean exists = fsOperations.exists(fsPath);
    if (!overwrite && exists) {
      throw new ConflictException("Can't create file, item already exists: " + wsPath);
    }

    String parentWsPath = parentOf(wsPath);
    Path parentFsPath = pathTransformer.transform(parentWsPath);
    boolean parentExists = fsOperations.exists(parentFsPath);
    if (!withParents && !parentExists) {
      throw new NotFoundException("Can't create file, parent does not exist: " + parentWsPath);
    }

    suspendingFsManager.createFile(wsPath, overwrite, withParents);
  }

  public void createDir(String wsPath, boolean overwrite, boolean withParents)
      throws NotFoundException, ConflictException, ServerException {
    Path fsPath = pathTransformer.transform(wsPath);
    boolean exists = fsOperations.exists(fsPath);
    if (!overwrite && exists) {
      throw new ConflictException("Can't create directory, item already exists: " + wsPath);
    }

    String parentWsPath = parentOf(wsPath);
    Path parentFsPath = pathTransformer.transform(parentWsPath);
    boolean parentExists = fsOperations.exists(parentFsPath);
    if (!withParents && !parentExists) {
      throw new NotFoundException("Can't create directory, parent does not exist: " + parentWsPath);
    }

    suspendingFsManager.createDir(wsPath, overwrite, withParents);
  }

  public InputStream zip(String wsPath)
      throws NotFoundException, ServerException, ConflictException {
    Path fsPath = pathTransformer.transform(wsPath);

    boolean exists = fsOperations.exists(fsPath);
    if (!exists) {
      throw new NotFoundException("Can't zip item, it does not exist: " + wsPath);
    }

    return suspendingFsManager.zip(wsPath);
  }

  public void unzip(
      String wsPath, InputStream packed, boolean overwrite, boolean withParents, boolean skipRoot)
      throws NotFoundException, ConflictException, ServerException {
    Path fsPath = pathTransformer.transform(wsPath);

    boolean exists = fsOperations.exists(fsPath);
    if (!withParents && !exists) {
      throw new NotFoundException("Can't unzip item, parent does not exist: " + wsPath);
    }

    boolean isDirectory = fsOperations.isDir(fsPath);
    if (exists && !isDirectory) {
      throw new ConflictException("Can't unzip item, parent is not directory: " + wsPath);
    }

    suspendingFsManager.unzip(wsPath, packed, overwrite, withParents, skipRoot);
  }

  public void delete(String wsPath, boolean quietly)
      throws NotFoundException, ConflictException, ServerException {
    Path fsPath = pathTransformer.transform(wsPath);

    boolean exists = fsOperations.exists(fsPath);
    if (!quietly && !exists) {
      throw new NotFoundException("Can't delete item, it does not exist: " + wsPath);
    }

    suspendingFsManager.delete(wsPath, quietly);
  }

  public void copy(String srcWsPath, String dstWsPath, boolean overwrite, boolean withParents)
      throws NotFoundException, ConflictException, ServerException {
    Path srcFsPath = pathTransformer.transform(srcWsPath);
    Path dstFsPath = pathTransformer.transform(dstWsPath);

    boolean srcExists = fsOperations.exists(srcFsPath);
    if (!srcExists) {
      throw new NotFoundException("Can't copy item, it does not exist: " + srcWsPath);
    }

    boolean dstExists = fsOperations.exists(dstFsPath);
    if (!overwrite && dstExists) {
      throw new ConflictException("Can't copy item, it already exists: " + dstWsPath);
    }

    String dstParentWsPath = parentOf(dstWsPath);
    Path dstParentFsPath = pathTransformer.transform(dstParentWsPath);
    boolean dstParentExists = fsOperations.exists(dstParentFsPath);
    if (!withParents && !dstParentExists) {
      throw new NotFoundException("Can't copy item, destination doesn't exist: " + dstParentWsPath);
    }

    suspendingFsManager.copy(srcWsPath, dstWsPath, overwrite, withParents);
  }

  public void move(String srcWsPath, String dstWsPath, boolean overwrite, boolean withParents)
      throws NotFoundException, ConflictException, ServerException {
    Path srcFsPath = pathTransformer.transform(srcWsPath);
    Path dstFsPath = pathTransformer.transform(dstWsPath);

    boolean srcExists = fsOperations.exists(srcFsPath);
    if (!srcExists) {
      throw new NotFoundException("Can't move item, it does not exist: " + srcWsPath);
    }

    boolean dstExists = fsOperations.exists(dstFsPath);
    if (!overwrite && dstExists) {
      throw new ConflictException("Can't move item, it already exists: " + dstWsPath);
    }

    String dstParentWsPath = parentOf(dstWsPath);
    Path dstParentFsPath = pathTransformer.transform(dstParentWsPath);
    boolean dstParentExists = fsOperations.exists(dstParentFsPath);
    if (!withParents && !dstParentExists) {
      throw new NotFoundException("Can't move item, destination doesn't exist: " + dstParentWsPath);
    }

    suspendingFsManager.move(srcWsPath, dstWsPath, overwrite, withParents);
  }

  public InputStream read(String wsPath)
      throws NotFoundException, ServerException, ConflictException {
    Path fsPath = pathTransformer.transform(wsPath);

    boolean exists = fsOperations.exists(fsPath);
    if (!exists) {
      throw new NotFoundException("Can't read file, it does not exist: " + wsPath);
    }

    boolean isFile = fsOperations.isFile(fsPath);
    if (!isFile) {
      throw new ConflictException("Can't read file, it is not a file: " + wsPath);
    }

    return suspendingFsManager.read(wsPath);
  }

  public void update(String wsPath, BiConsumer<InputStream, OutputStream> updater)
      throws NotFoundException, ConflictException, ServerException {
    Path fsPath = pathTransformer.transform(wsPath);

    boolean exists = fsOperations.exists(fsPath);
    if (!exists) {
      throw new NotFoundException("Can't update file, item does not exist: " + wsPath);
    }

    boolean isFile = fsOperations.isFile(fsPath);
    if (!isFile) {
      throw new ConflictException("Can't update file, item is not a file: " + wsPath);
    }

    if (updater == null) {
      throw new ConflictException("Can't update file, updater is null");
    }

    suspendingFsManager.update(wsPath, updater);
  }

  public void update(String wsPath, InputStream content)
      throws NotFoundException, ConflictException, ServerException {
    Path fsPath = pathTransformer.transform(wsPath);

    boolean exists = fsOperations.exists(fsPath);
    if (!exists) {
      throw new NotFoundException("Can't update file, item does not exist: " + wsPath);
    }

    boolean isFile = fsOperations.isFile(fsPath);
    if (!isFile) {
      throw new ConflictException("Can't update file, item is not a file: " + wsPath);
    }

    if (content == null) {
      throw new ConflictException("Can't update file, content is null");
    }

    suspendingFsManager.update(wsPath, content);
  }

  @Override
  public Set<String> getFileNames(String wsPath) {
    return suspendingFsManager.getFileNames(wsPath);
  }

  @Override
  public Set<String> getDirNames(String wsPath) {
    return suspendingFsManager.getDirNames(wsPath);
  }

  @Override
  public Set<String> getFileWsPaths(String wsPath) {
    return suspendingFsManager.getFileWsPaths(wsPath);
  }

  @Override
  public Set<String> getDirWsPaths(String wsPath) {
    return suspendingFsManager.getDirWsPaths(wsPath);
  }

  @Override
  public Set<String> getAllChildrenNames(String wsPath) {
    return suspendingFsManager.getAllChildrenNames(wsPath);
  }

  @Override
  public Set<String> getAllChildrenWsPaths(String wsPath) {
    return suspendingFsManager.getAllChildrenWsPaths(wsPath);
  }

  @Override
  public boolean isDir(String wsPath) {
    return suspendingFsManager.isDir(wsPath);
  }

  @Override
  public boolean isFile(String wsPath) {
    return suspendingFsManager.isFile(wsPath);
  }

  @Override
  public boolean exists(String wsPath) {
    return suspendingFsManager.exists(wsPath);
  }

  @Override
  public boolean existsAsFile(String wsPath) {
    return suspendingFsManager.existsAsFile(wsPath);
  }

  @Override
  public boolean existsAsDir(String wsPath) {
    return suspendingFsManager.existsAsDir(wsPath);
  }

  @Override
  public long lastModified(String wsPath) {
    return suspendingFsManager.lastModified(wsPath);
  }

  @Override
  public long length(String wsPath) {
    return suspendingFsManager.length(wsPath);
  }

  @Override
  public File toIoFile(String wsPath) {
    return suspendingFsManager.toIoFile(wsPath);
  }
}
