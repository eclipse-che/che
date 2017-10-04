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
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.apache.commons.fileupload.FileItem;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.fs.server.FsManager;
import org.eclipse.che.api.watcher.server.FileWatcherManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class SuspendingFsManager implements FsManager {
  private static final Logger LOG = LoggerFactory.getLogger(ValidatingFsManager.class);

  private final FileWatcherManager fileWatcherManager;
  private final ExecutiveFsManager executiveFsManager;

  @Inject
  public SuspendingFsManager(
      FileWatcherManager fileWatcherManager, ExecutiveFsManager executiveFsManager) {
    this.fileWatcherManager = fileWatcherManager;
    this.executiveFsManager = executiveFsManager;
  }

  @Override
  public void createDirectory(String wsPath)
      throws NotFoundException, ConflictException, ServerException {
    try {
      fileWatcherManager.suspend();
      executiveFsManager.createDirectory(wsPath);
    } finally {
      fileWatcherManager.resume();
    }
  }

  @Override
  public boolean createDirectoryQuietly(String wsPath) {
    try {
      fileWatcherManager.suspend();
      return executiveFsManager.createDirectoryQuietly(wsPath);
    } finally {
      fileWatcherManager.resume();
    }
  }

  @Override
  public void createDirectory(String wsPath, Iterator<FileItem> formData)
      throws NotFoundException, ConflictException, ServerException {
    try {
      fileWatcherManager.suspend();
      executiveFsManager.createDirectory(wsPath, formData);
    } finally {
      fileWatcherManager.resume();
    }
  }

  @Override
  public boolean createDirectoryQuietly(String wsPath, Iterator<FileItem> formData) {
    try {
      fileWatcherManager.suspend();
      return executiveFsManager.createDirectoryQuietly(wsPath, formData);
    } finally {
      fileWatcherManager.resume();
    }
  }

  @Override
  public InputStream zipDirectoryToInputStream(String wsPath)
      throws NotFoundException, ServerException {
    return executiveFsManager.zipDirectoryToInputStream(wsPath);
  }

  @Override
  public String zipDirectoryToString(String wsPath) throws NotFoundException, ServerException {
    return executiveFsManager.zipDirectoryToString(wsPath);
  }

  @Override
  public byte[] zipDirectoryToByteArray(String wsPath) throws NotFoundException, ServerException {
    return executiveFsManager.zipDirectoryToByteArray(wsPath);
  }

  @Override
  public Optional<InputStream> zipDirectoryToInputStreamQuietly(String wsPath)
      throws NotFoundException, ServerException {
    return executiveFsManager.zipDirectoryToInputStreamQuietly(wsPath);
  }

  @Override
  public Optional<String> zipDirectoryToStringQuietly(String wsPath)
      throws NotFoundException, ServerException {
    return executiveFsManager.zipDirectoryToStringQuietly(wsPath);
  }

  @Override
  public Optional<byte[]> zipDirectoryToByteArrayQuietly(String wsPath)
      throws NotFoundException, ServerException {
    return executiveFsManager.zipDirectoryToByteArrayQuietly(wsPath);
  }

  @Override
  public void unzipDirectory(String wsPath, InputStream content)
      throws NotFoundException, ConflictException, ServerException {
    try {
      fileWatcherManager.suspend();
      executiveFsManager.unzipDirectory(wsPath, content);
    } finally {
      fileWatcherManager.resume();
    }
  }

  @Override
  public void unzipDirectory(String wsPath, InputStream content, boolean skipRoot)
      throws NotFoundException, ConflictException, ServerException {
    try {
      fileWatcherManager.suspend();
      executiveFsManager.unzipDirectory(wsPath, content, skipRoot);
    } finally {
      fileWatcherManager.resume();
    }
  }

  @Override
  public boolean unzipDirectoryQuietly(String wsPath, InputStream content) {
    try {
      fileWatcherManager.suspend();
      return executiveFsManager.unzipDirectoryQuietly(wsPath, content, false);
    } finally {
      fileWatcherManager.resume();
    }
  }

  @Override
  public boolean unzipDirectoryQuietly(String wsPath, InputStream content, boolean skipRoot) {
    try {
      fileWatcherManager.suspend();
      return executiveFsManager.unzipDirectoryQuietly(wsPath, content, skipRoot);
    } finally {
      fileWatcherManager.resume();
    }
  }

  @Override
  public void deleteDirectory(String wsPath) throws NotFoundException, ServerException {
    try {
      fileWatcherManager.suspend();
      executiveFsManager.deleteDirectory(wsPath);
    } finally {
      fileWatcherManager.resume();
    }
  }

  @Override
  public boolean deleteDirectoryQuietly(String wsPath) {
    try {
      fileWatcherManager.suspend();
      return executiveFsManager.deleteDirectoryQuietly(wsPath);
    } finally {
      fileWatcherManager.resume();
    }
  }

  @Override
  public void copyDirectory(String srcWsPath, String dstWsPath)
      throws NotFoundException, ConflictException, ServerException {
    try {
      fileWatcherManager.suspend();
      executiveFsManager.copyDirectory(srcWsPath, dstWsPath);
    } finally {
      fileWatcherManager.resume();
    }
  }

  @Override
  public boolean copyDirectoryQuietly(String srcWsPath, String dstWsPath) {
    try {
      fileWatcherManager.suspend();
      return executiveFsManager.copyDirectoryQuietly(srcWsPath, dstWsPath);
    } finally {
      fileWatcherManager.resume();
    }
  }

  @Override
  public void moveDirectory(String srcWsPath, String dstWsPath)
      throws NotFoundException, ConflictException, ServerException {
    try {
      fileWatcherManager.suspend();
      executiveFsManager.moveDirectory(srcWsPath, dstWsPath);
    } finally {
      fileWatcherManager.resume();
    }
  }

  @Override
  public boolean moveDirectoryQuietly(String srcWsPath, String dstWsPath) {
    try {
      fileWatcherManager.suspend();
      return executiveFsManager.moveDirectoryQuietly(srcWsPath, dstWsPath);
    } finally {
      fileWatcherManager.resume();
    }
  }

  @Override
  public void createFile(String wsPath)
      throws ConflictException, NotFoundException, ServerException {
    try {
      fileWatcherManager.suspend();
      executiveFsManager.createFile(wsPath);
    } finally {
      fileWatcherManager.resume();
    }
  }

  @Override
  public void createFile(String wsPath, InputStream content)
      throws NotFoundException, ConflictException, ServerException {
    try {
      fileWatcherManager.suspend();
      executiveFsManager.createFile(wsPath, content);
    } finally {
      fileWatcherManager.resume();
    }
  }

  @Override
  public void createFile(String wsPath, String content)
      throws NotFoundException, ConflictException, ServerException {
    try {
      fileWatcherManager.suspend();
      executiveFsManager.createFile(wsPath, content);
    } finally {
      fileWatcherManager.resume();
    }
  }

  @Override
  public void createFile(String wsPath, byte[] content)
      throws NotFoundException, ConflictException, ServerException {
    try {
      fileWatcherManager.resume();
      executiveFsManager.createFile(wsPath, content);
    } finally {
      fileWatcherManager.suspend();
    }
  }

  @Override
  public void createFile(String wsPath, Iterator<FileItem> content)
      throws NotFoundException, ConflictException, ServerException {
    try {
      fileWatcherManager.suspend();
      executiveFsManager.createFile(wsPath, content);
    } finally {
      fileWatcherManager.resume();
    }
  }

  @Override
  public boolean createFileQuietly(String wsPath) {
    try {
      fileWatcherManager.suspend();
      return executiveFsManager.createFileQuietly(wsPath);
    } finally {
      fileWatcherManager.resume();
    }
  }

  @Override
  public boolean createFileQuietly(String wsPath, InputStream content) {
    try {
      fileWatcherManager.suspend();
      return executiveFsManager.createFileQuietly(wsPath, content);
    } finally {
      fileWatcherManager.resume();
    }
  }

  @Override
  public boolean createFileQuietly(String wsPath, String content) {
    try {
      fileWatcherManager.resume();
      return executiveFsManager.createFileQuietly(wsPath, content);
    } finally {
      fileWatcherManager.suspend();
    }
  }

  @Override
  public boolean createFileQuietly(String wsPath, byte[] content) {
    try {
      fileWatcherManager.suspend();
      return executiveFsManager.createFileQuietly(wsPath, content);
    } finally {
      fileWatcherManager.resume();
    }
  }

  @Override
  public boolean createFileQuietly(String wsPath, Iterator<FileItem> content) {
    try {
      fileWatcherManager.suspend();
      return executiveFsManager.createFileQuietly(wsPath, content);
    } finally {
      fileWatcherManager.resume();
    }
  }

  @Override
  public InputStream readFileAsInputStream(String wsPath)
      throws NotFoundException, ServerException, ConflictException {
    return executiveFsManager.readFileAsInputStream(wsPath);
  }

  @Override
  public String readFileAsString(String wsPath)
      throws NotFoundException, ServerException, ConflictException {
    return executiveFsManager.readFileAsString(wsPath);
  }

  @Override
  public byte[] readFileAsByteArray(String wsPath)
      throws NotFoundException, ServerException, ConflictException {
    return executiveFsManager.readFileAsByteArray(wsPath);
  }

  @Override
  public Optional<InputStream> readFileAsInputStreamQuietly(String wsPath) {
    return executiveFsManager.readFileAsInputStreamQuietly(wsPath);
  }

  @Override
  public Optional<String> readFileAsStringQuietly(String wsPath) {
    return executiveFsManager.readFileAsStringQuietly(wsPath);
  }

  @Override
  public Optional<byte[]> readFileAsByteArrayQuietly(String wsPath) {
    return executiveFsManager.readFileAsByteArrayQuietly(wsPath);
  }

  @Override
  public InputStream zipFileToInputStream(String wsPath)
      throws NotFoundException, ServerException, ConflictException {
    return executiveFsManager.zipFileToInputStream(wsPath);
  }

  @Override
  public String zipFileToString(String wsPath)
      throws NotFoundException, ServerException, ConflictException {
    return executiveFsManager.zipFileToString(wsPath);
  }

  @Override
  public byte[] zipFileToByteArray(String wsPath)
      throws NotFoundException, ServerException, ConflictException {
    return executiveFsManager.zipFileToByteArray(wsPath);
  }

  @Override
  public Optional<InputStream> zipFileToInputStreamQuietly(String wsPath) {
    return executiveFsManager.zipFileToInputStreamQuietly(wsPath);
  }

  @Override
  public Optional<String> zipFileToStringQuietly(String wsPath) {
    return executiveFsManager.zipFileToStringQuietly(wsPath);
  }

  @Override
  public Optional<byte[]> zipFileToByteArrayQuietly(String wsPath) {
    return executiveFsManager.zipFileToByteArrayQuietly(wsPath);
  }

  @Override
  public InputStream tarFileToInputStream(String wsPath)
      throws NotFoundException, ServerException, ConflictException {
    return executiveFsManager.tarFileToInputStream(wsPath);
  }

  @Override
  public String tarFileToString(String wsPath)
      throws NotFoundException, ServerException, ConflictException {
    return executiveFsManager.tarFileToString(wsPath);
  }

  @Override
  public byte[] tarFileToByteArray(String wsPath)
      throws NotFoundException, ServerException, ConflictException {
    return executiveFsManager.tarFileToByteArray(wsPath);
  }

  @Override
  public Optional<InputStream> tarFileToInputStreamQuietly(String wsPath) {
    return executiveFsManager.tarFileToInputStreamQuietly(wsPath);
  }

  @Override
  public Optional<String> tarFileToStringQuietly(String wsPath) {
    return executiveFsManager.tarFileToStringQuietly(wsPath);
  }

  @Override
  public Optional<byte[]> tarFileToByteArrayQuietly(String wsPath) {
    return executiveFsManager.tarFileToByteArrayQuietly(wsPath);
  }

  @Override
  public void updateFile(String wsPath, BiConsumer<InputStream, OutputStream> updater)
      throws NotFoundException, ConflictException, ServerException {
    try {
      fileWatcherManager.suspend();
      executiveFsManager.updateFile(wsPath, updater);
    } finally {
      fileWatcherManager.resume();
    }
  }

  @Override
  public void updateFile(String wsPath, InputStream content)
      throws NotFoundException, ConflictException, ServerException {
    try {
      fileWatcherManager.suspend();
      executiveFsManager.updateFile(wsPath, content);
    } finally {
      fileWatcherManager.resume();
    }
  }

  @Override
  public void updateFile(String wsPath, String content)
      throws NotFoundException, ConflictException, ServerException {
    try {
      fileWatcherManager.suspend();
      executiveFsManager.updateFile(wsPath, content);
    } finally {
      fileWatcherManager.resume();
    }
  }

  @Override
  public void updateFile(String wsPath, byte[] content)
      throws NotFoundException, ConflictException, ServerException {
    try {
      fileWatcherManager.suspend();
      executiveFsManager.updateFile(wsPath, content);
    } finally {
      fileWatcherManager.resume();
    }
  }

  @Override
  public boolean updateFileQuietly(String wsPath, InputStream content) {
    try {
      fileWatcherManager.suspend();
      return executiveFsManager.updateFileQuietly(wsPath, content);
    } finally {
      fileWatcherManager.resume();
    }
  }

  @Override
  public boolean updateFileQuietly(String wsPath, String content) {
    try {
      fileWatcherManager.suspend();
      return executiveFsManager.updateFileQuietly(wsPath, content);
    } finally {
      fileWatcherManager.resume();
    }
  }

  @Override
  public boolean updateFileQuietly(String wsPath, byte[] content) {
    try {
      fileWatcherManager.suspend();
      return executiveFsManager.updateFileQuietly(wsPath, content);
    } finally {
      fileWatcherManager.resume();
    }
  }

  @Override
  public void deleteFile(String wsPath) throws NotFoundException, ServerException {
    try {
      fileWatcherManager.suspend();
      executiveFsManager.deleteFile(wsPath);
    } finally {
      fileWatcherManager.resume();
    }
  }

  @Override
  public boolean deleteFileQuietly(String wsPath) {
    try {
      fileWatcherManager.suspend();
      return executiveFsManager.deleteFileQuietly(wsPath);
    } finally {
      fileWatcherManager.resume();
    }
  }

  @Override
  public void copyFile(String srcWsPath, String dstWsPath)
      throws ConflictException, NotFoundException, ServerException {
    try {
      fileWatcherManager.suspend();
      executiveFsManager.copyFile(srcWsPath, dstWsPath);
    } finally {
      fileWatcherManager.resume();
    }
  }

  @Override
  public boolean copyFileQuietly(String srcWsPath, String dstWsPath) {
    try {
      fileWatcherManager.resume();
      return executiveFsManager.copyFileQuietly(srcWsPath, dstWsPath);
    } finally {
      fileWatcherManager.suspend();
    }
  }

  @Override
  public void moveFile(String srcWsPath, String dstWsPath)
      throws NotFoundException, ConflictException, ServerException {
    try {
      fileWatcherManager.suspend();
      executiveFsManager.moveFile(srcWsPath, dstWsPath);
    } finally {
      fileWatcherManager.resume();
    }
  }

  @Override
  public boolean moveFileQuietly(String srcWsPath, String dstWsPath) {
    try {
      fileWatcherManager.suspend();
      return executiveFsManager.moveFileQuietly(srcWsPath, dstWsPath);
    } finally {
      fileWatcherManager.resume();
    }
  }

  @Override
  public boolean isFile(String wsPath) {
    return executiveFsManager.isFile(wsPath);
  }

  @Override
  public boolean isDirectory(String wsPath) {
    return executiveFsManager.isDirectory(wsPath);
  }

  @Override
  public boolean isRoot(String wsPath) {
    return executiveFsManager.isRoot(wsPath);
  }

  @Override
  public boolean exists(String wsPath) {
    return executiveFsManager.exists(wsPath);
  }

  @Override
  public long length(String wsPath) {
    return executiveFsManager.length(wsPath);
  }

  @Override
  public long lastModified(String wsPath) {
    return executiveFsManager.lastModified(wsPath);
  }

  @Override
  public Set<String> getFileNames(String wsPath) {
    return executiveFsManager.getFileNames(wsPath);
  }

  @Override
  public Set<String> getFileWsPaths(String wsPath) {
    return executiveFsManager.getFileWsPaths(wsPath);
  }

  @Override
  public Set<String> getDirectoryNames(String wsPath) {
    return executiveFsManager.getDirectoryNames(wsPath);
  }

  @Override
  public Set<String> getDirectoryWsPaths(String wsPath) {
    return executiveFsManager.getDirectoryWsPaths(wsPath);
  }

  @Override
  public File toIoFile(String wsPath) throws NotFoundException {
    return executiveFsManager.toIoFile(wsPath);
  }

  @Override
  public Optional<File> toIoFileQuietly(String wsPath) {
    return executiveFsManager.toIoFileQuietly(wsPath);
  }

  @Override
  public File toIoFileQuietlyOrNull(String wsPath) {
    return executiveFsManager.toIoFileQuietlyOrNull(wsPath);
  }
}
