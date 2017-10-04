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
import java.nio.file.Files;
import java.nio.file.Path;
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
import org.eclipse.che.api.fs.server.FsPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class ValidatingFsManager implements FsManager {

  private static final Logger LOG = LoggerFactory.getLogger(ValidatingFsManager.class);

  private final FsPaths fsPaths;
  private final SuspendingFsManager suspendingFsManager;

  @Inject
  public ValidatingFsManager(FsPaths fsPaths, SuspendingFsManager suspendingFsManager) {
    this.fsPaths = fsPaths;
    this.suspendingFsManager = suspendingFsManager;
  }

  @Override
  public void createDirectory(String wsPath)
      throws NotFoundException, ConflictException, ServerException {
    Path fsPath = fsPaths.toFsPath(wsPath);
    Path parentFsPath = fsPath.getParent();

    if (!Files.exists(parentFsPath)) {
      throw new NotFoundException("Can't create directory, parent does not exist: " + wsPath);
    }

    if (Files.exists(fsPath)) {
      throw new ConflictException("Can't create directory, item already exists: " + wsPath);
    }

    suspendingFsManager.createDirectory(wsPath);
  }

  @Override
  public boolean createDirectoryQuietly(String wsPath) {
    return suspendingFsManager.createDirectoryQuietly(wsPath);
  }

  @Override
  public void createDirectory(String wsPath, Iterator<FileItem> formData)
      throws NotFoundException, ConflictException, ServerException {
    suspendingFsManager.createDirectory(wsPath, formData);
  }

  @Override
  public boolean createDirectoryQuietly(String wsPath, Iterator<FileItem> formData) {
    return suspendingFsManager.createDirectoryQuietly(wsPath, formData);
  }

  @Override
  public InputStream zipDirectoryToInputStream(String wsPath)
      throws NotFoundException, ServerException, ConflictException {
    Path fsPath = fsPaths.toFsPath(wsPath);

    if (!Files.exists(fsPath)) {
      throw new NotFoundException(
          "Can't zip directory to input stream, item does not exist: " + wsPath);
    }

    if (!fsPath.toFile().isDirectory()) {
      throw new ConflictException(
          "Can't zip directory to input stream, item is not directory: " + wsPath);
    }

    return suspendingFsManager.zipDirectoryToInputStream(wsPath);
  }

  @Override
  public String zipDirectoryToString(String wsPath)
      throws NotFoundException, ServerException, ConflictException {
    Path fsPath = fsPaths.toFsPath(wsPath);

    if (!Files.exists(fsPath)) {
      throw new NotFoundException("Can't zip directory to string, item does not exist: " + wsPath);
    }

    if (!fsPath.toFile().isDirectory()) {
      throw new ConflictException(
          "Can't zip directory to string, item is not directory: " + wsPath);
    }

    return suspendingFsManager.zipDirectoryToString(wsPath);
  }

  @Override
  public byte[] zipDirectoryToByteArray(String wsPath)
      throws NotFoundException, ServerException, ConflictException {
    Path fsPath = fsPaths.toFsPath(wsPath);

    if (!Files.exists(fsPath)) {
      throw new NotFoundException(
          "Can't zip directory to byte array, item does not exist: " + wsPath);
    }

    if (!fsPath.toFile().isDirectory()) {
      throw new ConflictException(
          "Can't zip directory to byte array, item is not directory: " + wsPath);
    }

    return suspendingFsManager.zipDirectoryToByteArray(wsPath);
  }

  @Override
  public Optional<InputStream> zipDirectoryToInputStreamQuietly(String wsPath)
      throws NotFoundException, ServerException {
    return suspendingFsManager.zipDirectoryToInputStreamQuietly(wsPath);
  }

  @Override
  public Optional<String> zipDirectoryToStringQuietly(String wsPath)
      throws NotFoundException, ServerException {
    return suspendingFsManager.zipDirectoryToStringQuietly(wsPath);
  }

  @Override
  public Optional<byte[]> zipDirectoryToByteArrayQuietly(String wsPath)
      throws NotFoundException, ServerException {
    return suspendingFsManager.zipDirectoryToByteArrayQuietly(wsPath);
  }

  @Override
  public void unzipDirectory(String wsPath, InputStream content)
      throws NotFoundException, ConflictException, ServerException {
    suspendingFsManager.unzipDirectory(wsPath, content);
  }

  @Override
  public void unzipDirectory(String wsPath, InputStream content, boolean skipRoot)
      throws NotFoundException, ConflictException, ServerException {
    suspendingFsManager.unzipDirectory(wsPath, content, skipRoot);
  }

  @Override
  public boolean unzipDirectoryQuietly(String wsPath, InputStream content) {
    return suspendingFsManager.unzipDirectoryQuietly(wsPath, content, false);
  }

  @Override
  public boolean unzipDirectoryQuietly(String wsPath, InputStream content, boolean skipRoot) {
    return suspendingFsManager.unzipDirectoryQuietly(wsPath, content, skipRoot);
  }

  @Override
  public void deleteDirectory(String wsPath) throws NotFoundException, ServerException {
    Path fsPath = fsPaths.toFsPath(wsPath);

    if (!Files.exists(fsPath)) {
      throw new NotFoundException("Can't delete directory, item does not exist: " + wsPath);
    }

    if (!Files.isDirectory(fsPath)) {
      throw new NotFoundException("Can't delete directory, item is not directory: " + wsPath);
    }

    suspendingFsManager.deleteDirectory(wsPath);
  }

  @Override
  public boolean deleteDirectoryQuietly(String wsPath) {
    return suspendingFsManager.deleteDirectoryQuietly(wsPath);
  }

  @Override
  public void copyDirectory(String srcWsPath, String dstWsPath)
      throws NotFoundException, ConflictException, ServerException {

    Path srcFsPath = fsPaths.toFsPath(srcWsPath);
    if (!Files.exists(srcFsPath)) {
      throw new NotFoundException("Can't copy directory, item does not exist: " + srcWsPath);
    }

    Path dstFsPath = fsPaths.toFsPath(dstWsPath);
    if (Files.exists(dstFsPath)) {
      throw new ConflictException("Can't copy directory, item already exists: " + dstWsPath);
    }

    Path dstParentFsPath = dstFsPath.getParent();
    if (!Files.exists(dstParentFsPath)) {
      String dstParentWsPath = fsPaths.getParentWsPath(dstWsPath);
      throw new NotFoundException(
          "Can't copy directory, destination parent does not exist: " + dstParentWsPath);
    }

    suspendingFsManager.copyDirectory(srcWsPath, dstWsPath);
  }

  @Override
  public boolean copyDirectoryQuietly(String srcWsPath, String dstWsPath) {
    return suspendingFsManager.copyDirectoryQuietly(srcWsPath, dstWsPath);
  }

  @Override
  public void moveDirectory(String srcWsPath, String dstWsPath)
      throws NotFoundException, ConflictException, ServerException {
    Path srcFsPath = fsPaths.toFsPath(srcWsPath);
    if (!Files.exists(srcFsPath)) {
      throw new NotFoundException("Can't move directory, item does not exist: " + srcWsPath);
    }

    Path dstFsPath = fsPaths.toFsPath(dstWsPath);
    if (Files.exists(dstFsPath)) {
      throw new ConflictException("Can't move directory, item already exists: " + dstWsPath);
    }

    Path dstParentFsPath = dstFsPath.getParent();
    if (!Files.exists(dstParentFsPath)) {
      String dstParentWsPath = fsPaths.getParentWsPath(dstWsPath);
      throw new NotFoundException(
          "Can't move directory, destination parent does not exist: " + dstParentWsPath);
    }

    suspendingFsManager.moveDirectory(srcWsPath, dstWsPath);
  }

  @Override
  public boolean moveDirectoryQuietly(String srcWsPath, String dstWsPath) {
    return suspendingFsManager.moveDirectoryQuietly(srcWsPath, dstWsPath);
  }

  @Override
  public void createFile(String wsPath)
      throws NotFoundException, ConflictException, ServerException {
    Path fsPath = fsPaths.toFsPath(wsPath);
    if (Files.exists(fsPath)) {
      throw new ConflictException("Can't create file, item already exists: " + wsPath);
    }

    Path parentFsPath = fsPath.getParent();
    if (!Files.exists(parentFsPath)) {
      String parentWsPath = fsPaths.getParentWsPath(wsPath);
      throw new NotFoundException("Can't create file, parent does not exist: " + parentWsPath);
    }

    suspendingFsManager.createFile(wsPath);
  }

  @Override
  public void createFile(String wsPath, InputStream content)
      throws NotFoundException, ConflictException, ServerException {
    Path fsPath = fsPaths.toFsPath(wsPath);
    if (Files.exists(fsPath)) {
      throw new ConflictException("Can't create file, item already exists: " + wsPath);
    }

    Path parentFsPath = fsPath.getParent();
    if (!Files.exists(parentFsPath)) {
      String parentWsPath = fsPaths.getParentWsPath(wsPath);
      throw new NotFoundException("Can't create file, parent does not exist: " + parentWsPath);
    }

    if (content == null) {
      throw new ServerException("Can't create file, content is null");
    }

    suspendingFsManager.createFile(wsPath, content);
  }

  @Override
  public void createFile(String wsPath, String content)
      throws NotFoundException, ConflictException, ServerException {
    Path fsPath = fsPaths.toFsPath(wsPath);
    if (Files.exists(fsPath)) {
      throw new ConflictException("Can't create file, item already exists: " + wsPath);
    }

    Path parentFsPath = fsPath.getParent();
    if (!Files.exists(parentFsPath)) {
      String parentWsPath = fsPaths.getParentWsPath(wsPath);
      throw new NotFoundException("Can't create file, parent does not exist: " + parentWsPath);
    }

    if (content == null || content.isEmpty()) {
      throw new ServerException("Can't create file, content is null or empty");
    }

    suspendingFsManager.createFile(wsPath, content);
  }

  @Override
  public void createFile(String wsPath, byte[] content)
      throws NotFoundException, ConflictException, ServerException {
    Path fsPath = fsPaths.toFsPath(wsPath);
    if (Files.exists(fsPath)) {
      throw new ConflictException("Can't create file, item already exists: " + wsPath);
    }

    Path parentFsPath = fsPath.getParent();
    if (!Files.exists(parentFsPath)) {
      String parentWsPath = fsPaths.getParentWsPath(wsPath);
      throw new NotFoundException("Can't create file, parent does not exist: " + parentWsPath);
    }

    if (content == null || content.length == 0) {
      throw new ServerException("Can't create file, content is null or empty");
    }

    suspendingFsManager.createFile(wsPath, content);
  }

  @Override
  public void createFile(String wsPath, Iterator<FileItem> content)
      throws NotFoundException, ConflictException, ServerException {
    suspendingFsManager.createFile(wsPath, content);
  }

  @Override
  public boolean createFileQuietly(String wsPath) {
    return suspendingFsManager.createFileQuietly(wsPath);
  }

  @Override
  public boolean createFileQuietly(String wsPath, InputStream content) {
    return suspendingFsManager.createFileQuietly(wsPath, content);
  }

  @Override
  public boolean createFileQuietly(String wsPath, String content) {
    return suspendingFsManager.createFileQuietly(wsPath, content);
  }

  @Override
  public boolean createFileQuietly(String wsPath, byte[] content) {
    return suspendingFsManager.createFileQuietly(wsPath, content);
  }

  @Override
  public boolean createFileQuietly(String wsPath, Iterator<FileItem> content) {
    return suspendingFsManager.createFileQuietly(wsPath, content);
  }

  @Override
  public InputStream readFileAsInputStream(String wsPath)
      throws NotFoundException, ServerException, ConflictException {
    Path fsPath = fsPaths.toFsPath(wsPath);
    if (!Files.exists(fsPath)) {
      throw new NotFoundException("Can't read file as stream, item does not exist: " + wsPath);
    }

    if (!fsPath.toFile().isFile()) {
      throw new ConflictException("Can't read file as stream, item is not a file: " + wsPath);
    }

    return suspendingFsManager.readFileAsInputStream(wsPath);
  }

  @Override
  public String readFileAsString(String wsPath)
      throws NotFoundException, ServerException, ConflictException {
    Path fsPath = fsPaths.toFsPath(wsPath);
    if (!Files.exists(fsPath)) {
      throw new NotFoundException("Can't read file as string, item does not exist: " + wsPath);
    }

    if (!fsPath.toFile().isFile()) {
      throw new ConflictException("Can't read file as string, item is not a file: " + wsPath);
    }

    return suspendingFsManager.readFileAsString(wsPath);
  }

  @Override
  public byte[] readFileAsByteArray(String wsPath)
      throws NotFoundException, ServerException, ConflictException {
    Path fsPath = fsPaths.toFsPath(wsPath);
    if (!Files.exists(fsPath)) {
      throw new NotFoundException("Can't read file as byte array, item does not exist: " + wsPath);
    }

    if (!fsPath.toFile().isFile()) {
      throw new ConflictException("Can't read file as byte array, item is not a file: " + wsPath);
    }

    return suspendingFsManager.readFileAsByteArray(wsPath);
  }

  @Override
  public Optional<InputStream> readFileAsInputStreamQuietly(String wsPath) {
    return suspendingFsManager.readFileAsInputStreamQuietly(wsPath);
  }

  @Override
  public Optional<String> readFileAsStringQuietly(String wsPath) {
    return suspendingFsManager.readFileAsStringQuietly(wsPath);
  }

  @Override
  public Optional<byte[]> readFileAsByteArrayQuietly(String wsPath) {
    return suspendingFsManager.readFileAsByteArrayQuietly(wsPath);
  }

  @Override
  public InputStream zipFileToInputStream(String wsPath)
      throws NotFoundException, ServerException, ConflictException {
    Path fsPath = fsPaths.toFsPath(wsPath);
    if (!Files.exists(fsPath)) {
      throw new NotFoundException("Can't zip file to input stream, item does not exist: " + wsPath);
    }

    if (!fsPath.toFile().isFile()) {
      throw new ConflictException("Can't zip file to input stream, item is not a file: " + wsPath);
    }

    return suspendingFsManager.zipFileToInputStream(wsPath);
  }

  @Override
  public String zipFileToString(String wsPath)
      throws NotFoundException, ServerException, ConflictException {
    Path fsPath = fsPaths.toFsPath(wsPath);
    if (!Files.exists(fsPath)) {
      throw new NotFoundException("Can't zip file to string, item does not exist: " + wsPath);
    }

    if (!fsPath.toFile().isFile()) {
      throw new ConflictException("Can't zip file to string, item is not a file: " + wsPath);
    }

    return suspendingFsManager.zipFileToString(wsPath);
  }

  @Override
  public byte[] zipFileToByteArray(String wsPath)
      throws NotFoundException, ServerException, ConflictException {
    Path fsPath = fsPaths.toFsPath(wsPath);
    if (!Files.exists(fsPath)) {
      throw new NotFoundException("Can't zip file to byte array, item does not exist: " + wsPath);
    }

    if (!fsPath.toFile().isFile()) {
      throw new ConflictException("Can't zip file to byte array, item is not a file: " + wsPath);
    }

    return suspendingFsManager.zipFileToByteArray(wsPath);
  }

  @Override
  public Optional<InputStream> zipFileToInputStreamQuietly(String wsPath) {
    return suspendingFsManager.zipFileToInputStreamQuietly(wsPath);
  }

  @Override
  public Optional<String> zipFileToStringQuietly(String wsPath) {
    return suspendingFsManager.zipFileToStringQuietly(wsPath);
  }

  @Override
  public Optional<byte[]> zipFileToByteArrayQuietly(String wsPath) {
    return suspendingFsManager.zipFileToByteArrayQuietly(wsPath);
  }

  @Override
  public InputStream tarFileToInputStream(String wsPath)
      throws NotFoundException, ServerException, ConflictException {
    Path fsPath = fsPaths.toFsPath(wsPath);
    if (!Files.exists(fsPath)) {
      throw new NotFoundException("Can't tar file to input stream, item does not exist: " + wsPath);
    }

    if (!fsPath.toFile().isFile()) {
      throw new ConflictException("Can't tar file to input stream, item is not a file: " + wsPath);
    }

    return suspendingFsManager.tarFileToInputStream(wsPath);
  }

  @Override
  public String tarFileToString(String wsPath)
      throws NotFoundException, ServerException, ConflictException {
    Path fsPath = fsPaths.toFsPath(wsPath);
    if (!Files.exists(fsPath)) {
      throw new NotFoundException("Can't tar file to string, item does not exist: " + wsPath);
    }

    if (!fsPath.toFile().isFile()) {
      throw new ConflictException("Can't tar file to string, item is not a file: " + wsPath);
    }

    return suspendingFsManager.tarFileToString(wsPath);
  }

  @Override
  public byte[] tarFileToByteArray(String wsPath)
      throws NotFoundException, ServerException, ConflictException {
    Path fsPath = fsPaths.toFsPath(wsPath);
    if (!Files.exists(fsPath)) {
      throw new NotFoundException("Can't tar file to byte array, item does not exist: " + wsPath);
    }

    if (!fsPath.toFile().isFile()) {
      throw new ConflictException("Can't tar file to byte array, item is not a file: " + wsPath);
    }

    return suspendingFsManager.tarFileToByteArray(wsPath);
  }

  @Override
  public Optional<InputStream> tarFileToInputStreamQuietly(String wsPath) {
    return suspendingFsManager.tarFileToInputStreamQuietly(wsPath);
  }

  @Override
  public Optional<String> tarFileToStringQuietly(String wsPath) {
    return suspendingFsManager.tarFileToStringQuietly(wsPath);
  }

  @Override
  public Optional<byte[]> tarFileToByteArrayQuietly(String wsPath) {
    return suspendingFsManager.tarFileToByteArrayQuietly(wsPath);
  }

  @Override
  public void updateFile(String wsPath, BiConsumer<InputStream, OutputStream> updater)
      throws NotFoundException, ConflictException, ServerException {
    Path fsPath = fsPaths.toFsPath(wsPath);

    if (!Files.exists(fsPath)) {
      throw new NotFoundException("Can't update file with updater, item does not exist: " + wsPath);
    }

    if (!fsPath.toFile().isFile()) {
      throw new ConflictException("Can't update file with updater, item is not a file: " + wsPath);
    }

    if (updater == null) {
      throw new ConflictException("Can't update file with updater, updater is null");
    }

    suspendingFsManager.updateFile(wsPath, updater);
  }

  @Override
  public void updateFile(String wsPath, InputStream content)
      throws NotFoundException, ConflictException, ServerException {
    Path fsPath = fsPaths.toFsPath(wsPath);

    if (!Files.exists(fsPath)) {
      throw new NotFoundException("Can't update file with stream, item does not exist: " + wsPath);
    }

    if (!fsPath.toFile().isFile()) {
      throw new ConflictException("Can't update file with stream, item is not a file: " + wsPath);
    }

    if (content == null) {
      throw new ConflictException("Can't update file with stream, stream is null");
    }

    suspendingFsManager.updateFile(wsPath, content);
  }

  @Override
  public void updateFile(String wsPath, String content)
      throws NotFoundException, ConflictException, ServerException {
    Path fsPath = fsPaths.toFsPath(wsPath);

    if (!Files.exists(fsPath)) {
      throw new NotFoundException("Can't update file with string, item does not exist: " + wsPath);
    }

    if (!fsPath.toFile().isFile()) {
      throw new ConflictException("Can't update file with string, item is not a file: " + wsPath);
    }

    if (content == null || content.isEmpty()) {
      throw new ConflictException("Can't update file with string, string is null or empty");
    }

    suspendingFsManager.updateFile(wsPath, content);
  }

  @Override
  public void updateFile(String wsPath, byte[] content)
      throws NotFoundException, ConflictException, ServerException {
    Path fsPath = fsPaths.toFsPath(wsPath);

    if (!Files.exists(fsPath)) {
      throw new NotFoundException("Can't update file with string, item does not exist: " + wsPath);
    }

    if (!fsPath.toFile().isFile()) {
      throw new ConflictException("Can't update file with string, item is not a file: " + wsPath);
    }

    if (content == null || content.length == 0) {
      throw new ConflictException("Can't update file with byte array, byte array is null or empty");
    }
    suspendingFsManager.updateFile(wsPath, content);
  }

  @Override
  public boolean updateFileQuietly(String wsPath, InputStream content) {
    return suspendingFsManager.updateFileQuietly(wsPath, content);
  }

  @Override
  public boolean updateFileQuietly(String wsPath, String content) {
    return suspendingFsManager.updateFileQuietly(wsPath, content);
  }

  @Override
  public boolean updateFileQuietly(String wsPath, byte[] content) {
    return suspendingFsManager.updateFileQuietly(wsPath, content);
  }

  @Override
  public void deleteFile(String wsPath) throws NotFoundException, ServerException {
    Path fsPath = fsPaths.toFsPath(wsPath);

    if (!fsPath.toFile().exists()) {
      throw new NotFoundException("FS item '" + fsPath.toString() + "' does not exist");
    }

    suspendingFsManager.deleteFile(wsPath);
  }

  @Override
  public boolean deleteFileQuietly(String wsPath) {
    return suspendingFsManager.deleteFileQuietly(wsPath);
  }

  @Override
  public void copyFile(String srcWsPath, String dstWsPath)
      throws NotFoundException, ConflictException, ServerException {
    Path srcFsPath = fsPaths.toFsPath(srcWsPath);
    if (!Files.exists(srcFsPath)) {
      throw new NotFoundException("Can't copy file, item does not exist: " + srcWsPath);
    }

    if (!srcFsPath.toFile().isFile()) {
      throw new ConflictException("Can't copy file, item is not a file: " + srcWsPath);
    }

    Path dstFsPath = fsPaths.toFsPath(dstWsPath);
    if (Files.exists(dstFsPath)) {
      throw new ConflictException("Can't copy file, destination item already exists: " + dstWsPath);
    }

    Path parentDstFsPath = dstFsPath.getParent();
    if (!Files.exists(parentDstFsPath)) {
      String parentDstWsPath = fsPaths.getParentWsPath(dstWsPath);
      throw new NotFoundException(
          "Can't copy file, destination parent does not exist: " + parentDstWsPath);
    }

    suspendingFsManager.copyFile(srcWsPath, dstWsPath);
  }

  @Override
  public boolean copyFileQuietly(String srcWsPath, String dstWsPath) {
    return suspendingFsManager.copyFileQuietly(srcWsPath, dstWsPath);
  }

  @Override
  public void moveFile(String srcWsPath, String dstWsPath)
      throws NotFoundException, ConflictException, ServerException {
    Path srcFsPath = fsPaths.toFsPath(srcWsPath);
    if (!Files.exists(srcFsPath)) {
      throw new NotFoundException("Can't move file, item does not exist: " + srcWsPath);
    }

    if (!srcFsPath.toFile().isFile()) {
      throw new ConflictException("Can't move file, item is not a file: " + srcWsPath);
    }

    Path dstFsPath = fsPaths.toFsPath(dstWsPath);
    if (Files.exists(dstFsPath)) {
      throw new ConflictException("Can't move file, destination item already exists: " + dstWsPath);
    }

    Path parentDstFsPath = dstFsPath.getParent();
    if (!Files.exists(parentDstFsPath)) {
      String parentDstWsPath = fsPaths.getParentWsPath(dstWsPath);
      throw new NotFoundException(
          "Can't move file, destination parent does not exist: " + parentDstWsPath);
    }

    suspendingFsManager.moveFile(srcWsPath, dstWsPath);
  }

  @Override
  public boolean moveFileQuietly(String srcWsPath, String dstWsPath) {
    return suspendingFsManager.moveFileQuietly(srcWsPath, dstWsPath);
  }

  @Override
  public boolean isFile(String wsPath) {
    return suspendingFsManager.isFile(wsPath);
  }

  @Override
  public boolean isDirectory(String wsPath) {
    return suspendingFsManager.isDirectory(wsPath);
  }

  @Override
  public boolean isRoot(String wsPath) {
    return suspendingFsManager.isRoot(wsPath);
  }

  @Override
  public boolean exists(String wsPath) {
    return suspendingFsManager.exists(wsPath);
  }

  @Override
  public long length(String wsPath) {
    return suspendingFsManager.length(wsPath);
  }

  @Override
  public long lastModified(String wsPath) {
    return suspendingFsManager.lastModified(wsPath);
  }

  @Override
  public Set<String> getFileNames(String wsPath) {
    return suspendingFsManager.getFileNames(wsPath);
  }

  @Override
  public Set<String> getFileWsPaths(String wsPath) {
    return suspendingFsManager.getFileWsPaths(wsPath);
  }

  @Override
  public Set<String> getDirectoryNames(String wsPath) {
    return suspendingFsManager.getDirectoryNames(wsPath);
  }

  @Override
  public Set<String> getDirectoryWsPaths(String wsPath) {
    return suspendingFsManager.getDirectoryWsPaths(wsPath);
  }

  @Override
  public File toIoFile(String wsPath) throws NotFoundException {
    Path fsPath = fsPaths.toFsPath(wsPath);
    if (!Files.exists(fsPath)) {
      throw new NotFoundException("Cant convert to IO file, item does not exist: " + wsPath);
    }

    return suspendingFsManager.toIoFile(wsPath);
  }

  @Override
  public Optional<File> toIoFileQuietly(String wsPath) {
    return suspendingFsManager.toIoFileQuietly(wsPath);
  }

  @Override
  public File toIoFileQuietlyOrNull(String wsPath) {
    return suspendingFsManager.toIoFileQuietlyOrNull(wsPath);
  }
}
