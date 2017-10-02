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
package org.eclipse.che.api.fs.server;

import static java.util.Collections.unmodifiableSet;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import org.apache.commons.fileupload.FileItem;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;

public interface FsManager {

  void createFile(String wsPath) throws NotFoundException, ConflictException, ServerException;

  void createFile(String wsPath, InputStream content)
      throws NotFoundException, ConflictException, ServerException;

  void createFile(String wsPath, String content)
      throws NotFoundException, ConflictException, ServerException;

  void createFile(String wsPath, byte[] content)
      throws NotFoundException, ConflictException, ServerException;

  void createFile(String parentWsPath, Iterator<FileItem> content)
      throws NotFoundException, ConflictException, ServerException;

  boolean createFileQuietly(String wsPath);

  boolean createFileQuietly(String wsPath, InputStream content);

  boolean createFileQuietly(String wsPath, String content);

  boolean createFileQuietly(String wsPath, byte[] content);

  boolean createFileQuietly(String parentWsPath, Iterator<FileItem> content);

  InputStream readFileAsInputStream(String wsPath) throws NotFoundException, ServerException;

  String readFileAsString(String wsPath) throws NotFoundException, ServerException;

  byte[] readFileAsByteArray(String wsPath) throws NotFoundException, ServerException;

  Optional<InputStream> readFileAsInputStreamQuietly(String wsPath);

  Optional<String> readFileAsStringQuietly(String wsPath);

  Optional<byte[]> readFileAsByteArrayQuietly(String wsPath);

  InputStream zipFileToInputStream(String wsPath) throws NotFoundException, ServerException;

  String zipFileToString(String wsPath) throws NotFoundException, ServerException;

  byte[] zipFileToByteArray(String wsPath) throws NotFoundException, ServerException;

  Optional<InputStream> zipFileToInputStreamQuietly(String wsPath);

  Optional<String> zipFileToStringQuietly(String wsPath);

  Optional<byte[]> zipFileToByteArrayQuietly(String wsPath);

  InputStream tarFileToInputStream(String wsPath) throws NotFoundException, ServerException;

  String tarFileToString(String wsPath) throws NotFoundException, ServerException;

  byte[] tarFileToByteArray(String wsPath) throws NotFoundException, ServerException;

  Optional<InputStream> tarFileToInputStreamQuietly(String wsPath);

  Optional<String> tarFileToStringQuietly(String wsPath);

  Optional<byte[]> tarFileToByteArrayQuietly(String wsPath);

  void updateFile(String wsPath, BiConsumer<InputStream, OutputStream> updater)
      throws NotFoundException, ConflictException, ServerException;

  void updateFile(String wsPath, InputStream content)
      throws NotFoundException, ConflictException, ServerException;

  void updateFile(String wsPath, String content)
      throws NotFoundException, ConflictException, ServerException;

  void updateFile(String wsPath, byte[] content)
      throws NotFoundException, ConflictException, ServerException;

  boolean updateFileQuietly(String wsPath, InputStream content);

  boolean updateFileQuietly(String wsPath, String content);

  boolean updateFileQuietly(String wsPath, byte[] content);

  void deleteFile(String wsPath) throws NotFoundException, ServerException;

  boolean deleteFileQuietly(String wsPath);

  void copyFile(String srcWsPath, String dstWsPath)
      throws NotFoundException, ConflictException, ServerException;

  boolean copyFileQuietly(String srcWsPath, String dstWsPath);

  void moveFile(String srcWsPath, String dstWsPath)
      throws NotFoundException, ConflictException, ServerException;

  boolean moveFileQuietly(String srcWsPath, String dstWsPath);

  void createDirectory(String wsPath) throws NotFoundException, ConflictException, ServerException;

  boolean createDirectoryQuietly(String wsPath);

  void createDirectory(String wsPath, Iterator<FileItem> formData)
      throws NotFoundException, ConflictException, ServerException;

  boolean createDirectoryQuietly(String wsPath, Iterator<FileItem> formData);

  InputStream zipDirectoryToInputStream(String wsPath) throws NotFoundException, ServerException;

  String zipDirectoryToString(String wsPath) throws NotFoundException, ServerException;

  byte[] zipDirectoryToByteArray(String wsPath) throws NotFoundException, ServerException;

  Optional<InputStream> zipDirectoryToInputStreamQuietly(String wsPath)
      throws NotFoundException, ServerException;

  Optional<String> zipDirectoryToStringQuietly(String wsPath)
      throws NotFoundException, ServerException;

  Optional<byte[]> zipDirectoryToByteArrayQuietly(String wsPath)
      throws NotFoundException, ServerException;

  void unzipDirectory(String wsPath, InputStream content)
      throws NotFoundException, ConflictException, ServerException;

  void unzipDirectory(String wsPath, InputStream content, boolean skipRoot)
      throws NotFoundException, ConflictException, ServerException;

  boolean unzipDirectoryQuietly(String wsPath, InputStream content);

  boolean unzipDirectoryQuietly(String wsPath, InputStream content, boolean skipRoot);

  void deleteDirectory(String wsPath) throws NotFoundException, ServerException;

  boolean deleteDirectoryQuietly(String wsPath);

  void copyDirectory(String srcWsPath, String dstWsPath)
      throws NotFoundException, ConflictException, ServerException;

  boolean copyDirectoryQuietly(String srcWsPath, String dstWsPath);

  void moveDirectory(String srcWsPath, String dstWsPath)
      throws NotFoundException, ConflictException, ServerException;

  boolean moveDirectoryQuietly(String srcWsPath, String dstWsPath);

  Set<String> getFileNames(String wsPath);

  Set<String> getFileWsPaths(String wsPath);

  Set<String> getDirectoryNames(String wsPath);

  Set<String> getDirectoryWsPaths(String wsPath);

  default Set<String> getAllChildren(String wsPath) {
    Set<String> files = new HashSet<>(getFileNames(wsPath));
    files.addAll(getDirectoryNames(wsPath));
    return unmodifiableSet(files);
  }

  default Set<String> getAllChildrenWsPaths(String wsPath) {
    Set<String> files = new HashSet<>(getFileWsPaths(wsPath));
    files.addAll(getDirectoryWsPaths(wsPath));
    return unmodifiableSet(files);
  }

  boolean isDirectory(String wsPath);

  boolean isFile(String wsPath);

  boolean isRoot(String wsPath);

  boolean exists(String wsPath);

  default boolean existsAsFile(String wsPath) {
    return exists(wsPath) && isFile(wsPath);
  }

  default boolean existsAsDirectory(String wsPath) {
    return exists(wsPath) && isDirectory(wsPath);
  }

  long lastModified(String wsPath);

  long length(String wsPath);

  File toIoFile(String wsPath) throws NotFoundException;

  File toIoFileQuietly(String wsPath);
}
