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

import static com.google.common.collect.ImmutableSet.copyOf;
import static java.util.Arrays.stream;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toSet;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.apache.commons.fileupload.FileItem;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class ExecutiveFsManager implements org.eclipse.che.api.fs.server.FsManager {

  private static final Logger LOG = LoggerFactory.getLogger(ExecutiveFsManager.class);

  private final StandardFsPaths pathResolver;
  private final DirectoryCreator directoryCreator;
  private final DirectoryCopier directoryCopier;
  private final DirectoryDeleter directoryDeleter;
  private final DirectoryMover directoryMover;
  private final DirectoryPacker directoryPacker;
  private final FileCreator fileCreator;
  private final FileDeleter fileDeleter;
  private final FileReader fileReader;
  private final FileUpdater fileUpdater;
  private final FileCopier fileCopier;
  private final FilePacker filePacker;
  private final FileMover fileMover;

  @Inject
  public ExecutiveFsManager(
      StandardFsPaths pathResolver,
      DirectoryCreator directoryCreator,
      DirectoryCopier directoryCopier,
      DirectoryDeleter directoryDeleter,
      DirectoryPacker directoryPacker,
      DirectoryMover directoryMover,
      FileCreator fileCreator,
      FileDeleter fileDeleter,
      FileReader fileReader,
      FileUpdater fileUpdater,
      FileCopier fileCopier,
      FilePacker filePacker,
      FileMover fileMover) {
    this.pathResolver = pathResolver;
    this.directoryCreator = directoryCreator;
    this.directoryCopier = directoryCopier;
    this.directoryDeleter = directoryDeleter;
    this.directoryPacker = directoryPacker;
    this.directoryMover = directoryMover;
    this.fileCreator = fileCreator;
    this.fileDeleter = fileDeleter;
    this.fileReader = fileReader;
    this.fileUpdater = fileUpdater;
    this.fileCopier = fileCopier;
    this.filePacker = filePacker;
    this.fileMover = fileMover;
  }

  @Override
  public void createDirectory(String wsPath)
      throws NotFoundException, ConflictException, ServerException {
    directoryCreator.create(wsPath);
  }

  @Override
  public boolean createDirectoryQuietly(String wsPath) {
    return directoryCreator.createQuietly(wsPath);
  }

  @Override
  public void createDirectory(String wsPath, Iterator<FileItem> formData)
      throws NotFoundException, ConflictException, ServerException {
    directoryCreator.create(wsPath, formData);
  }

  @Override
  public boolean createDirectoryQuietly(String wsPath, Iterator<FileItem> formData) {
    return directoryCreator.createQuietly(wsPath, formData);
  }

  @Override
  public InputStream zipDirectoryToInputStream(String wsPath)
      throws NotFoundException, ServerException {
    return directoryPacker.zipToInputStream(wsPath);
  }

  @Override
  public String zipDirectoryToString(String wsPath) throws NotFoundException, ServerException {
    return directoryPacker.zipToString(wsPath);
  }

  @Override
  public byte[] zipDirectoryToByteArray(String wsPath) throws NotFoundException, ServerException {
    return directoryPacker.zipToByteArray(wsPath);
  }

  @Override
  public Optional<InputStream> zipDirectoryToInputStreamQuietly(String wsPath)
      throws NotFoundException, ServerException {
    return directoryPacker.zipToInputStreamQuietly(wsPath);
  }

  @Override
  public Optional<String> zipDirectoryToStringQuietly(String wsPath)
      throws NotFoundException, ServerException {
    return directoryPacker.zipToStringQuietly(wsPath);
  }

  @Override
  public Optional<byte[]> zipDirectoryToByteArrayQuietly(String wsPath)
      throws NotFoundException, ServerException {
    return directoryPacker.zipToByteArrayQuietly(wsPath);
  }

  @Override
  public void unzipDirectory(String wsPath, InputStream content)
      throws NotFoundException, ConflictException, ServerException {
    directoryPacker.unzip(wsPath, content);
  }

  @Override
  public void unzipDirectory(String wsPath, InputStream content, boolean skipRoot)
      throws NotFoundException, ConflictException, ServerException {
    directoryPacker.unzip(wsPath, content, skipRoot);
  }

  @Override
  public boolean unzipDirectoryQuietly(String wsPath, InputStream content) {
    return unzipDirectoryQuietly(wsPath, content, false);
  }

  @Override
  public boolean unzipDirectoryQuietly(String wsPath, InputStream content, boolean skipRoot) {
    return directoryPacker.unzipQuietly(wsPath, content, skipRoot);
  }

  @Override
  public void deleteDirectory(String wsPath) throws NotFoundException, ServerException {
    directoryDeleter.delete(wsPath);
  }

  @Override
  public boolean deleteDirectoryQuietly(String wsPath) {
    return directoryDeleter.deleteQuietly(wsPath);
  }

  @Override
  public void copyDirectory(String srcWsPath, String dstWsPath)
      throws NotFoundException, ConflictException, ServerException {
    directoryCopier.copy(srcWsPath, dstWsPath);
  }

  @Override
  public boolean copyDirectoryQuietly(String srcWsPath, String dstWsPath) {
    return directoryCopier.copyQuietly(srcWsPath, dstWsPath);
  }

  @Override
  public void moveDirectory(String srcWsPath, String dstWsPath)
      throws NotFoundException, ConflictException, ServerException {
    directoryMover.move(srcWsPath, dstWsPath);
  }

  @Override
  public boolean moveDirectoryQuietly(String srcWsPath, String dstWsPath) {
    return directoryMover.moveQuietly(srcWsPath, dstWsPath);
  }

  @Override
  public void createFile(String wsPath)
      throws NotFoundException, ConflictException, ServerException {
    fileCreator.create(wsPath);
  }

  @Override
  public void createFile(String wsPath, InputStream content)
      throws NotFoundException, ConflictException, ServerException {
    fileCreator.create(wsPath, content);
  }

  @Override
  public void createFile(String wsPath, String content)
      throws NotFoundException, ConflictException, ServerException {
    fileCreator.create(wsPath, content);
  }

  @Override
  public void createFile(String wsPath, byte[] content)
      throws NotFoundException, ConflictException, ServerException {
    fileCreator.create(wsPath, content);
  }

  @Override
  public void createFile(String wsPath, Iterator<FileItem> content)
      throws NotFoundException, ConflictException, ServerException {
    fileCreator.create(wsPath, content);
  }

  @Override
  public boolean createFileQuietly(String wsPath) {
    return fileCreator.createQuietly(wsPath);
  }

  @Override
  public boolean createFileQuietly(String wsPath, InputStream content) {
    return fileCreator.createQuietly(wsPath, content);
  }

  @Override
  public boolean createFileQuietly(String wsPath, String content) {
    return fileCreator.createQuietly(wsPath, content);
  }

  @Override
  public boolean createFileQuietly(String wsPath, byte[] content) {
    return fileCreator.createQuietly(wsPath, content);
  }

  @Override
  public boolean createFileQuietly(String wsPath, Iterator<FileItem> content) {
    return fileCreator.createQuietly(wsPath, content);
  }

  @Override
  public InputStream readFileAsInputStream(String wsPath)
      throws NotFoundException, ServerException {
    return fileReader.readAsInputStream(wsPath);
  }

  @Override
  public String readFileAsString(String wsPath) throws NotFoundException, ServerException {
    return fileReader.readAsString(wsPath);
  }

  @Override
  public byte[] readFileAsByteArray(String wsPath) throws NotFoundException, ServerException {
    return fileReader.readAsByteArray(wsPath);
  }

  @Override
  public Optional<InputStream> readFileAsInputStreamQuietly(String wsPath) {
    return fileReader.readAsInputStreamQuietly(wsPath);
  }

  @Override
  public Optional<String> readFileAsStringQuietly(String wsPath) {
    return fileReader.readAsStringQuietly(wsPath);
  }

  @Override
  public Optional<byte[]> readFileAsByteArrayQuietly(String wsPath) {
    return fileReader.readAsByteArrayQuietly(wsPath);
  }

  @Override
  public InputStream zipFileToInputStream(String wsPath) throws NotFoundException, ServerException {
    return filePacker.zipToInputStream(wsPath);
  }

  @Override
  public String zipFileToString(String wsPath) throws NotFoundException, ServerException {
    return filePacker.zipToString(wsPath);
  }

  @Override
  public byte[] zipFileToByteArray(String wsPath) throws NotFoundException, ServerException {
    return filePacker.zipToByteArray(wsPath);
  }

  @Override
  public Optional<InputStream> zipFileToInputStreamQuietly(String wsPath) {
    return filePacker.zipToInputStreamQuietly(wsPath);
  }

  @Override
  public Optional<String> zipFileToStringQuietly(String wsPath) {
    return filePacker.zipToStringQuietly(wsPath);
  }

  @Override
  public Optional<byte[]> zipFileToByteArrayQuietly(String wsPath) {
    return filePacker.zipToByteArrayQuietly(wsPath);
  }

  @Override
  public InputStream tarFileToInputStream(String wsPath) throws NotFoundException, ServerException {
    return filePacker.tarToInputStream(wsPath);
  }

  @Override
  public String tarFileToString(String wsPath) throws NotFoundException, ServerException {
    return filePacker.tarToString(wsPath);
  }

  @Override
  public byte[] tarFileToByteArray(String wsPath) throws NotFoundException, ServerException {
    return filePacker.tarToByteArray(wsPath);
  }

  @Override
  public Optional<InputStream> tarFileToInputStreamQuietly(String wsPath) {
    return filePacker.tarToInputStreamQuietly(wsPath);
  }

  @Override
  public Optional<String> tarFileToStringQuietly(String wsPath) {
    return filePacker.tarToStringQuietly(wsPath);
  }

  @Override
  public Optional<byte[]> tarFileToByteArrayQuietly(String wsPath) {
    return filePacker.tarToByteArrayQuietly(wsPath);
  }

  @Override
  public void updateFile(String wsPath, BiConsumer<InputStream, OutputStream> updater)
      throws NotFoundException, ConflictException, ServerException {
    fileUpdater.update(wsPath, updater);
  }

  @Override
  public void updateFile(String wsPath, InputStream content)
      throws NotFoundException, ConflictException, ServerException {
    fileUpdater.update(wsPath, content);
  }

  @Override
  public void updateFile(String wsPath, String content)
      throws NotFoundException, ConflictException, ServerException {
    fileUpdater.update(wsPath, content);
  }

  @Override
  public void updateFile(String wsPath, byte[] content)
      throws NotFoundException, ConflictException, ServerException {
    fileUpdater.update(wsPath, content);
  }

  @Override
  public boolean updateFileQuietly(String wsPath, InputStream content) {
    return fileUpdater.updateQuietly(wsPath, content);
  }

  @Override
  public boolean updateFileQuietly(String wsPath, String content) {
    return fileUpdater.updateQuietly(wsPath, content);
  }

  @Override
  public boolean updateFileQuietly(String wsPath, byte[] content) {
    return fileUpdater.updateQuietly(wsPath, content);
  }

  @Override
  public void deleteFile(String wsPath) throws NotFoundException, ServerException {
    fileDeleter.delete(wsPath);
  }

  @Override
  public boolean deleteFileQuietly(String wsPath) {
    return fileDeleter.deleteQuietly(wsPath);
  }

  @Override
  public void copyFile(String srcWsPath, String dstWsPath)
      throws NotFoundException, ConflictException, ServerException {
    fileCopier.copy(srcWsPath, dstWsPath);
  }

  @Override
  public boolean copyFileQuietly(String srcWsPath, String dstWsPath) {
    return fileCopier.copyQuietly(srcWsPath, dstWsPath);
  }

  @Override
  public void moveFile(String srcWsPath, String dstWsPath)
      throws NotFoundException, ConflictException, ServerException {
    fileMover.move(srcWsPath, dstWsPath);
  }

  @Override
  public boolean moveFileQuietly(String srcWsPath, String dstWsPath) {
    return fileMover.moveQuietly(srcWsPath, dstWsPath);
  }

  @Override
  public boolean isFile(String wsPath) {
    return pathResolver.toFsPath(wsPath).toFile().isFile();
  }

  @Override
  public boolean isDirectory(String wsPath) {
    return pathResolver.toFsPath(wsPath).toFile().isDirectory();
  }

  @Override
  public boolean isRoot(String wsPath) {
    return pathResolver.isRoot(wsPath);
  }

  @Override
  public boolean exists(String wsPath) {
    return pathResolver.toFsPath(wsPath).toFile().exists();
  }

  @Override
  public long length(String wsPath) {
    return pathResolver.toFsPath(wsPath).toFile().length();
  }

  @Override
  public long lastModified(String wsPath) {
    return pathResolver.toFsPath(wsPath).toFile().lastModified();
  }

  @Override
  public Set<String> getFileNames(String wsPath) {
    return getItemNamesByFilter(wsPath, File::isFile);
  }

  @Override
  public Set<String> getFileWsPaths(String wsPath) {
    return getItemWsPathsByFilter(wsPath, File::isFile);
  }

  @Override
  public Set<String> getDirectoryNames(String wsPath) {
    return getItemNamesByFilter(wsPath, File::isDirectory);
  }

  @Override
  public Set<String> getDirectoryWsPaths(String wsPath) {
    return getItemWsPathsByFilter(wsPath, File::isDirectory);
  }

  @Override
  public File toIoFile(String wsPath) throws NotFoundException {
    return pathResolver.toFsPath(wsPath).toFile();
  }

  @Override
  public Optional<File> toIoFileQuietly(String wsPath) {
    Path fsPath = pathResolver.toFsPath(wsPath);
    return Files.exists(fsPath) ? Optional.of(fsPath.toFile()) : Optional.empty();
  }

  @Override
  public File toIoFileQuietlyOrNull(String wsPath) {
    return toIoFileQuietly(wsPath).orElse(null);
  }

  private Set<String> getItemNamesByFilter(String wsPath, Predicate<File> predicate) {
    File[] files = pathResolver.toFsPath(wsPath).toFile().listFiles();
    return files == null
        ? emptySet()
        : copyOf(stream(files).filter(predicate).map(File::getName).collect(toSet()));
  }

  private Set<String> getItemWsPathsByFilter(String wsPath, Predicate<File> predicate) {
    File[] files = pathResolver.toFsPath(wsPath).toFile().listFiles();
    return files == null
        ? emptySet()
        : copyOf(
            stream(files)
                .filter(predicate)
                .map(File::toPath)
                .map(Path::toAbsolutePath)
                .map(pathResolver::toWsPath)
                .collect(toSet()));
  }
}
