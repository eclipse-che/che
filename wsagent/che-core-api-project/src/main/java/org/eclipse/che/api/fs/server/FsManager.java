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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;

public interface FsManager {

  /**
   * Create file
   *
   * @param wsPath absolute workspace path of a file
   * @param overwrite overwrite on create marker
   * @param withParents create parents on create marker
   * @throws NotFoundException is thrown if file's parent does not exist and with parents is
   *     disabled
   * @throws ConflictException is thrown if file already exists and overwrite is disavbled
   * @throws ServerException is thrown if an error occurred during operation execution
   */
  void createFile(String wsPath, boolean overwrite, boolean withParents)
      throws NotFoundException, ConflictException, ServerException;

  /**
   * Create directory
   *
   * @param wsPath absolute workspace path of a directory
   * @param overwrite overwrite on create marker
   * @param withParents create parents on create marker
   * @throws NotFoundException is thrown if directory's parent does not exist and with parents is
   *     disabled
   * @throws ConflictException is thrown if directory already exists and overwrite is disavbled
   * @throws ServerException is thrown if an error occurred during operation execution
   */
  void createDir(String wsPath, boolean overwrite, boolean withParents)
      throws NotFoundException, ConflictException, ServerException;

  /**
   * Reads a file denoted by the path to an input stream
   *
   * @param wsPath absolute workspace file path
   * @return
   * @throws NotFoundException is thrown if the file does not exist
   * @throws ConflictException is thrown if the item is not a file
   * @throws ServerException is thrown if an error occurred during operation execution
   */
  InputStream read(String wsPath) throws NotFoundException, ConflictException, ServerException;

  /**
   * Zips a file denoted by the path to an input stream
   *
   * @param wsPath absolute workspace file path
   * @return
   * @throws NotFoundException is thrown if the file does not exist
   * @throws ConflictException is thrown if the item is not a file
   * @throws ServerException is thrown if an error occurred during operation execution
   */
  InputStream zip(String wsPath) throws NotFoundException, ConflictException, ServerException;

  /**
   * Unzips an input stream to a specified workspace path
   *
   * @param wsPath absolute workspace directory path
   * @return
   * @throws NotFoundException is thrown if destination does not exist
   * @throws ConflictException is thrown if destination is not a directory
   * @throws ServerException is thrown if an error occurred during operation execution
   */
  void unzip(
      String wsPath, InputStream packed, boolean overwrite, boolean withParents, boolean skipRoot)
      throws NotFoundException, ServerException, ConflictException;

  /**
   * Update a file with an updater
   *
   * @param wsPath absolute workspace file path
   * @param updater file updater
   * @throws NotFoundException is thrown if file does not exist
   * @throws ConflictException is thrown if item is not a file
   * @throws ServerException is thrown is an error occurred during operation execution
   */
  void update(String wsPath, BiConsumer<InputStream, OutputStream> updater)
      throws NotFoundException, ConflictException, ServerException;

  /**
   * Update a file with an input stream
   *
   * @param wsPath absolute workspace file path
   * @param content imput stream
   * @throws NotFoundException is thrown if file does not exist
   * @throws ConflictException is thrown if item is not a file
   * @throws ServerException is thrown is an error occurred during operation execution
   */
  void update(String wsPath, InputStream content)
      throws NotFoundException, ConflictException, ServerException;

  /**
   * Delete file system item
   *
   * @param wsPath absolute workspace file path
   * @param quietly quietly delete marker
   * @throws NotFoundException is thrown if item does not exist and quietly is disabled
   * @throws ConflictException is thrown if parent does not exist and quietly is disabled
   * @throws ServerException is thrown is an error occurred during operation execution
   */
  void delete(String wsPath, boolean quietly)
      throws NotFoundException, ConflictException, ServerException;

  /**
   * Copy an item from source to destination
   *
   * @param srcWsPath absolute workspace source path
   * @param dstWsPath absolute workspace destination path
   * @param overwrite overwrite on copy marker
   * @param withParents create parents on copy marker
   * @throws NotFoundException is thrown when item at source path does not exist or destination
   *     parent does not exist and with parents is disabled
   * @throws ConflictException is thrown when destination item already exists and overwrite is
   *     disabled
   * @throws ServerException is thrown is an error occurred during operation execution
   */
  void copy(String srcWsPath, String dstWsPath, boolean overwrite, boolean withParents)
      throws NotFoundException, ConflictException, ServerException;

  /**
   * Move an item from source to destination
   *
   * @param srcWsPath absolute workspace source path
   * @param dstWsPath absolute workspace destination path
   * @param overwrite overwrite on move marker
   * @param withParents create parents on copy marker
   * @throws NotFoundException is thrown when item at source path does not exist or destination
   *     parent does not exist and with parents is disabled
   * @throws ConflictException is thrown when destination item already exists and overwrite is
   *     disabled
   * @throws ServerException is thrown is an error occurred during operation execution
   */
  void move(String srcWsPath, String dstWsPath, boolean overwrite, boolean withParents)
      throws NotFoundException, ConflictException, ServerException;

  /**
   * Get names of all files in a directory
   *
   * @param wsPath absolute workspace path
   * @return
   */
  Set<String> getFileNames(String wsPath);

  /**
   * Get names of all directories in a directory
   *
   * @param wsPath absolute workspace path
   * @return
   */
  Set<String> getDirNames(String wsPath);

  /**
   * Get workspace paths of all files in a directory
   *
   * @param wsPath absolute workspace path
   * @return
   */
  Set<String> getFileWsPaths(String wsPath);

  /**
   * Get workspace paths of all directories in a directory
   *
   * @param wsPath absolute workspace path
   * @return
   */
  Set<String> getDirWsPaths(String wsPath);

  /**
   * Get names of all files and directories in a directory
   *
   * @param wsPath absolute workspace path
   * @return
   */
  Set<String> getAllChildrenNames(String wsPath);

  /**
   * Get workspace paths of all files and directories in a directory
   *
   * @param wsPath absolute workspace path
   * @return
   */
  Set<String> getAllChildrenWsPaths(String wsPath);

  /**
   * Shows if an item is a directory
   *
   * @param wsPath absolute workspace path
   * @return
   */
  boolean isDir(String wsPath);

  /**
   * Shows if an item is a file
   *
   * @param wsPath absolute workspace path
   * @return
   */
  boolean isFile(String wsPath);

  /**
   * Shows if an item exists
   *
   * @param wsPath absolute workspace path
   * @return
   */
  boolean exists(String wsPath);

  /**
   * Shows if an item exists and is a file
   *
   * @param wsPath absolute workspace path
   * @return
   */
  boolean existsAsFile(String wsPath);

  /**
   * Shows if an item exists and is a directory
   *
   * @param wsPath
   * @return
   */
  boolean existsAsDir(String wsPath);

  /**
   * Get last modified property
   *
   * @param wsPath absolute workspace path
   * @return
   */
  long lastModified(String wsPath);

  /**
   * Get content length property
   *
   * @param wsPath absolute workspace path
   * @return
   */
  long length(String wsPath);

  /**
   * Transform to java.io.File
   *
   * @param wsPath absolute workspace path
   * @return
   */
  File toIoFile(String wsPath);

  /** Shortcut to createFile(wsPath, true, true); */
  default void createFile(String wsPath)
      throws NotFoundException, ConflictException, ServerException {
    createFile(wsPath, true, true);
  }

  /** Shortcut to createFile(wsPath, overwrite, withParents) and then update(wsPath, content) */
  default void createFile(
      String wsPath, InputStream content, boolean overwrite, boolean withParents)
      throws NotFoundException, ConflictException, ServerException {
    createFile(wsPath, overwrite, withParents);
    update(wsPath, content);
  }

  /** Shortcut to createFile(wsPath, overwrite, withParents) and then update(wsPath, content) */
  default void createFile(String wsPath, String content, boolean overwrite, boolean withParents)
      throws NotFoundException, ConflictException, ServerException {
    createFile(wsPath, overwrite, withParents);
    update(wsPath, content);
  }

  /** Shortcut to createFile(wsPath, true, true) and then update(wsPath, content) */
  default void createFile(String wsPath, InputStream content)
      throws NotFoundException, ConflictException, ServerException {
    createFile(wsPath, true, true);
    update(wsPath, content);
  }

  /** Shortcut to createFile(wsPath, true, true) and then update(wsPath, content) */
  default void createFile(String wsPath, String content)
      throws NotFoundException, ConflictException, ServerException {
    createFile(wsPath, true, true);
    update(wsPath, content);
  }

  /** Shortcut to createDir(wsPath, true, true) */
  default void createDir(String wsPath)
      throws NotFoundException, ConflictException, ServerException {
    createDir(wsPath, true, true);
  }

  /** Read file as String */
  default String readAsString(String wsPath)
      throws NotFoundException, ConflictException, ServerException {
    try (InputStream inputStream = read(wsPath)) {
      InputStreamReader isr = new InputStreamReader(inputStream);
      BufferedReader br = new BufferedReader(isr);
      return br.lines().collect(Collectors.joining("\n"));
    } catch (IOException e) {
      throw new ServerException(e);
    }
  }

  /** Update file with String */
  default void update(String wsPath, String content)
      throws NotFoundException, ConflictException, ServerException {
    try (InputStream inputStream = new ByteArrayInputStream(content.getBytes())) {
      update(wsPath, inputStream);
    } catch (IOException e) {
      throw new ServerException(e);
    }
  }

  /** Shortcut to delete(wsPath, true) */
  default void delete(String wsPath) throws NotFoundException, ConflictException, ServerException {
    delete(wsPath, true);
  }

  /** Shortcut to copy(srcWsPath, dstWsPath, true, true) */
  default void copy(String srcWsPath, String dstWsPath)
      throws NotFoundException, ConflictException, ServerException {
    copy(srcWsPath, dstWsPath, true, true);
  }

  /** Shortcut to move(srcWsPath, dstWsPath, true, true) */
  default void move(String srcWsPath, String dstWsPath)
      throws NotFoundException, ConflictException, ServerException {
    move(srcWsPath, dstWsPath, true, true);
  }

  /** Shortcut to unzip(wsPath, packed, true, true, skipRoot) */
  default void unzip(String wsPath, InputStream packed, boolean skipRoot)
      throws NotFoundException, ServerException, ConflictException {
    unzip(wsPath, packed, true, true, skipRoot);
  }
}
