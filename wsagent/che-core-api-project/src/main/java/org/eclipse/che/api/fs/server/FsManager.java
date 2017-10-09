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

  void createFile(String wsPath, boolean overwrite, boolean withParents)
      throws NotFoundException, ConflictException, ServerException;

  void createDir(String wsPath, boolean overwrite, boolean withParents)
      throws NotFoundException, ConflictException, ServerException;

  InputStream read(String wsPath) throws NotFoundException, ConflictException, ServerException;

  InputStream zip(String wsPath) throws NotFoundException, ConflictException, ServerException;

  void unzip(
      String wsPath, InputStream packed, boolean overwrite, boolean withParents, boolean skipRoot)
      throws NotFoundException, ServerException, ConflictException;

  void update(String wsPath, BiConsumer<InputStream, OutputStream> updater)
      throws NotFoundException, ConflictException, ServerException;

  void update(String wsPath, InputStream content)
      throws NotFoundException, ConflictException, ServerException;

  void delete(String wsPath, boolean quietly)
      throws NotFoundException, ConflictException, ServerException;

  void copy(String srcWsPath, String dstWsPath, boolean overwrite, boolean withParents)
      throws NotFoundException, ConflictException, ServerException;

  void move(String srcWsPath, String dstWsPath, boolean overwrite, boolean withParents)
      throws NotFoundException, ConflictException, ServerException;

  Set<String> getFileNames(String wsPath);

  Set<String> getDirNames(String wsPath);

  Set<String> getFileWsPaths(String wsPath);

  Set<String> getDirWsPaths(String wsPath);

  Set<String> getAllChildrenNames(String wsPath);

  Set<String> getAllChildrenWsPaths(String wsPath);

  boolean isDir(String wsPath);

  boolean isFile(String wsPath);

  boolean exists(String wsPath);

  boolean existsAsFile(String wsPath);

  boolean existsAsDir(String wsPath);

  long lastModified(String wsPath);

  long length(String wsPath);

  File toIoFile(String wsPath);

  default void createFile(String wsPath)
      throws NotFoundException, ConflictException, ServerException {
    createFile(wsPath, true, true);
  }

  default void createFile(
      String wsPath, InputStream content, boolean overwrite, boolean withParents)
      throws NotFoundException, ConflictException, ServerException {
    createFile(wsPath, overwrite, withParents);
    update(wsPath, content);
  }

  default void createFile(String wsPath, String content, boolean overwrite, boolean withParents)
      throws NotFoundException, ConflictException, ServerException {
    createFile(wsPath, overwrite, withParents);
    update(wsPath, content);
  }

  default void createFile(String wsPath, InputStream content)
      throws NotFoundException, ConflictException, ServerException {
    createFile(wsPath, true, true);
    update(wsPath, content);
  }

  default void createFile(String wsPath, String content)
      throws NotFoundException, ConflictException, ServerException {
    createFile(wsPath, true, true);
    update(wsPath, content);
  }

  default void createDir(String wsPath)
      throws NotFoundException, ConflictException, ServerException {
    createDir(wsPath, true, true);
  }

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

  default void update(String wsPath, String content)
      throws NotFoundException, ConflictException, ServerException {
    try (InputStream inputStream = new ByteArrayInputStream(content.getBytes())) {
      update(wsPath, inputStream);
    } catch (IOException e) {
      throw new ServerException(e);
    }
  }

  default void delete(String wsPath) throws NotFoundException, ConflictException, ServerException {
    delete(wsPath, true);
  }

  default void copy(String srcWsPath, String dstWsPath)
      throws NotFoundException, ConflictException, ServerException {
    copy(srcWsPath, dstWsPath, true, true);
  }

  default void move(String srcWsPath, String dstWsPath)
      throws NotFoundException, ConflictException, ServerException {
    move(srcWsPath, dstWsPath, true, true);
  }

  default void unzip(String wsPath, InputStream packed, boolean skipRoot)
      throws NotFoundException, ServerException, ConflictException {
    unzip(wsPath, packed, true, true, skipRoot);
  }
}
