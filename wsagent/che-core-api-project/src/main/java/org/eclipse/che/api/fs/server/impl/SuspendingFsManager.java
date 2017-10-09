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

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.function.BiConsumer;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.fs.server.FsManager;
import org.eclipse.che.api.watcher.server.FileWatcherManager;

@Singleton
public class SuspendingFsManager implements FsManager {

  private final FileWatcherManager fileWatcherManager;
  private final ExecutiveFsManager executiveFsManager;

  @Inject
  public SuspendingFsManager(
      FileWatcherManager fileWatcherManager, ExecutiveFsManager executiveFsManager) {
    this.fileWatcherManager = fileWatcherManager;
    this.executiveFsManager = executiveFsManager;
  }

  @Override
  public void createFile(String wsPath, boolean overwrite, boolean withParents)
      throws NotFoundException, ConflictException, ServerException {
    fileWatcherManager.suspend();
    try {
      executiveFsManager.createFile(wsPath, overwrite, withParents);
    } finally {
      fileWatcherManager.resume();
    }
  }

  @Override
  public void createDir(String wsPath, boolean overwrite, boolean withParents)
      throws NotFoundException, ConflictException, ServerException {
    fileWatcherManager.suspend();
    try {
      executiveFsManager.createDir(wsPath, overwrite, withParents);
    } finally {
      fileWatcherManager.resume();
    }
  }

  @Override
  public InputStream read(String wsPath)
      throws NotFoundException, ConflictException, ServerException {
    fileWatcherManager.suspend();
    try {
      return executiveFsManager.read(wsPath);
    } finally {
      fileWatcherManager.resume();
    }
  }

  @Override
  public InputStream zip(String wsPath)
      throws NotFoundException, ConflictException, ServerException {
    fileWatcherManager.suspend();
    try {
      return executiveFsManager.zip(wsPath);
    } finally {
      fileWatcherManager.resume();
    }
  }

  @Override
  public void unzip(
      String wsPath, InputStream packed, boolean overwrite, boolean withParents, boolean skipRoot)
      throws NotFoundException, ServerException, ConflictException {
    fileWatcherManager.suspend();
    try {
      executiveFsManager.unzip(wsPath, packed, overwrite, withParents, skipRoot);
    } finally {
      fileWatcherManager.resume();
    }
  }

  @Override
  public void update(String wsPath, BiConsumer<InputStream, OutputStream> updater)
      throws NotFoundException, ConflictException, ServerException {
    fileWatcherManager.suspend();
    try {
      executiveFsManager.update(wsPath, updater);
    } finally {
      fileWatcherManager.resume();
    }
  }

  @Override
  public void update(String wsPath, InputStream content)
      throws NotFoundException, ConflictException, ServerException {
    fileWatcherManager.suspend();
    try {
      executiveFsManager.update(wsPath, content);
    } finally {
      fileWatcherManager.resume();
    }
  }

  @Override
  public void delete(String wsPath, boolean quietly)
      throws NotFoundException, ConflictException, ServerException {
    fileWatcherManager.suspend();
    try {
      executiveFsManager.delete(wsPath, quietly);
    } finally {
      fileWatcherManager.resume();
    }
  }

  @Override
  public void copy(String srcWsPath, String dstWsPath, boolean overwrite, boolean withParents)
      throws NotFoundException, ConflictException, ServerException {
    fileWatcherManager.suspend();
    try {
      executiveFsManager.copy(srcWsPath, dstWsPath, overwrite, withParents);
    } finally {
      fileWatcherManager.resume();
    }
  }

  @Override
  public void move(String srcWsPath, String dstWsPath, boolean overwrite, boolean withParents)
      throws NotFoundException, ConflictException, ServerException {
    fileWatcherManager.suspend();
    try {
      executiveFsManager.move(srcWsPath, dstWsPath, overwrite, withParents);
    } finally {
      fileWatcherManager.resume();
    }
  }

  @Override
  public Set<String> getFileNames(String wsPath) {
    return executiveFsManager.getFileNames(wsPath);
  }

  @Override
  public Set<String> getDirNames(String wsPath) {
    return executiveFsManager.getDirNames(wsPath);
  }

  @Override
  public Set<String> getFileWsPaths(String wsPath) {
    return executiveFsManager.getFileWsPaths(wsPath);
  }

  @Override
  public Set<String> getDirWsPaths(String wsPath) {
    return executiveFsManager.getDirWsPaths(wsPath);
  }

  @Override
  public Set<String> getAllChildrenNames(String wsPath) {
    return executiveFsManager.getAllChildrenNames(wsPath);
  }

  @Override
  public Set<String> getAllChildrenWsPaths(String wsPath) {
    return executiveFsManager.getAllChildrenWsPaths(wsPath);
  }

  @Override
  public boolean isDir(String wsPath) {
    return executiveFsManager.isDir(wsPath);
  }

  @Override
  public boolean isFile(String wsPath) {
    return executiveFsManager.isFile(wsPath);
  }

  @Override
  public boolean exists(String wsPath) {
    return executiveFsManager.exists(wsPath);
  }

  @Override
  public boolean existsAsFile(String wsPath) {
    return executiveFsManager.existsAsFile(wsPath);
  }

  @Override
  public boolean existsAsDir(String wsPath) {
    return executiveFsManager.existsAsDir(wsPath);
  }

  @Override
  public long lastModified(String wsPath) {
    return executiveFsManager.lastModified(wsPath);
  }

  @Override
  public long length(String wsPath) {
    return executiveFsManager.length(wsPath);
  }

  @Override
  public File toIoFile(String wsPath) {
    return executiveFsManager.toIoFile(wsPath);
  }
}
