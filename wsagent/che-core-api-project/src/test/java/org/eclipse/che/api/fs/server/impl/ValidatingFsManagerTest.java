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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.BiConsumer;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.fs.server.PathTransformer;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/** Tests for {@link ValidatingFsManager} */
@Listeners(MockitoTestNGListener.class)
public class ValidatingFsManagerTest {

  private static final String WS_PATH = "/ws/path";
  private static final Path FS_PATH = Paths.get("/fs/path");

  private static final String PARENT_WS_PATH = "/ws";
  private static final Path PARENT_FS_PATH = FS_PATH.getParent();

  private static final String SRC_WS_PATH = "/ws/path/src";
  private static final Path SRC_FS_PATH = Paths.get("/fs/path/src");

  private static final String PARENT_SRC_WS_PATH = "/ws/path";
  private static final Path PARENT_SRC_FS_PATH = SRC_FS_PATH.getParent();

  private static final String DST_WS_PATH = "/ws/path/dst";
  private static final Path DST_FS_PATH = Paths.get("/fs/path/dst");

  private static final String PARENT_DST_WS_PATH = "/ws/path";
  private static final Path PARENT_DST_FS_PATH = DST_FS_PATH.getParent();

  private static final InputStream INPUT_STREAM =
      new InputStream() {
        @Override
        public int read() throws IOException {
          return -1;
        }
      };

  @Mock private FsOperations fsOperations;
  @Mock private PathTransformer pathTransformer;
  @Mock private SuspendingFsManager suspendingFsManager;
  @InjectMocks private ValidatingFsManager validatingFsManager;

  @Mock private BiConsumer<InputStream, OutputStream> updater;

  @BeforeMethod
  public void setUp() throws Exception {
    when(pathTransformer.transform(WS_PATH)).thenReturn(FS_PATH);
    when(pathTransformer.transform(FS_PATH)).thenReturn(WS_PATH);

    when(pathTransformer.transform(PARENT_WS_PATH)).thenReturn(PARENT_FS_PATH);
    when(pathTransformer.transform(PARENT_FS_PATH)).thenReturn(PARENT_WS_PATH);

    when(pathTransformer.transform(SRC_WS_PATH)).thenReturn(SRC_FS_PATH);
    when(pathTransformer.transform(SRC_FS_PATH)).thenReturn(SRC_WS_PATH);

    when(pathTransformer.transform(PARENT_SRC_WS_PATH)).thenReturn(PARENT_SRC_FS_PATH);
    when(pathTransformer.transform(PARENT_SRC_FS_PATH)).thenReturn(PARENT_SRC_WS_PATH);

    when(pathTransformer.transform(DST_WS_PATH)).thenReturn(DST_FS_PATH);
    when(pathTransformer.transform(DST_FS_PATH)).thenReturn(DST_WS_PATH);

    when(pathTransformer.transform(PARENT_DST_WS_PATH)).thenReturn(PARENT_DST_FS_PATH);
    when(pathTransformer.transform(PARENT_DST_FS_PATH)).thenReturn(PARENT_DST_WS_PATH);
  }

  @Test
  public void shouldCallSuspendingFsManagerOnCreateFile() throws Exception {
    when(fsOperations.exists(PARENT_FS_PATH)).thenReturn(true);
    when(fsOperations.exists(FS_PATH)).thenReturn(false);

    validatingFsManager.createFile(WS_PATH, false, false);

    verify(suspendingFsManager).createFile(WS_PATH, false, false);
  }

  @Test(expectedExceptions = ConflictException.class)
  public void shouldThrowConflictExceptionOnCreateFile() throws Exception {
    when(fsOperations.exists(PARENT_FS_PATH)).thenReturn(true);
    when(fsOperations.exists(FS_PATH)).thenReturn(true);

    validatingFsManager.createFile(WS_PATH, false, false);
  }

  @Test
  public void shouldNotThrowConflictExceptionOnCreateFile() throws Exception {
    when(fsOperations.exists(PARENT_FS_PATH)).thenReturn(true);
    when(fsOperations.exists(FS_PATH)).thenReturn(true);

    validatingFsManager.createFile(WS_PATH, true, false);
  }

  @Test(expectedExceptions = NotFoundException.class)
  public void shouldThrowNotFoundExceptionOnCreateFile() throws Exception {
    when(fsOperations.exists(FS_PATH)).thenReturn(false);
    when(fsOperations.exists(PARENT_FS_PATH)).thenReturn(false);

    validatingFsManager.createFile(WS_PATH, false, false);
  }

  @Test
  public void shouldNotThrowNotFoundExceptionOnCreateFile() throws Exception {
    when(fsOperations.exists(FS_PATH)).thenReturn(false);
    when(fsOperations.exists(PARENT_FS_PATH)).thenReturn(false);

    validatingFsManager.createFile(WS_PATH, false, true);
  }

  @Test
  public void shouldCallSuspendingFsManagerOnCreateDir() throws Exception {
    when(fsOperations.exists(PARENT_FS_PATH)).thenReturn(true);
    when(fsOperations.exists(FS_PATH)).thenReturn(false);

    validatingFsManager.createDir(WS_PATH, false, false);

    verify(suspendingFsManager).createDir(WS_PATH, false, false);
  }

  @Test(expectedExceptions = ConflictException.class)
  public void shouldThrowConflictExceptionOnCreateDir() throws Exception {
    when(fsOperations.exists(PARENT_FS_PATH)).thenReturn(true);
    when(fsOperations.exists(FS_PATH)).thenReturn(true);

    validatingFsManager.createDir(WS_PATH, false, false);
  }

  @Test
  public void shouldNotThrowConflictExceptionOnCreateDir() throws Exception {
    when(fsOperations.exists(PARENT_FS_PATH)).thenReturn(true);
    when(fsOperations.exists(FS_PATH)).thenReturn(true);

    validatingFsManager.createDir(WS_PATH, true, false);
  }

  @Test(expectedExceptions = NotFoundException.class)
  public void shouldThrowNotFoundExceptionOnCreateDir() throws Exception {
    when(fsOperations.exists(PARENT_FS_PATH)).thenReturn(false);
    when(fsOperations.exists(FS_PATH)).thenReturn(false);

    validatingFsManager.createDir(WS_PATH, false, false);
  }

  @Test
  public void shouldNotThrowNotFoundExceptionOnCreateDir() throws Exception {
    when(fsOperations.exists(PARENT_FS_PATH)).thenReturn(false);
    when(fsOperations.exists(FS_PATH)).thenReturn(false);

    validatingFsManager.createDir(WS_PATH, false, true);
  }

  @Test
  public void shouldCallSuspendingFsManagerOnRead() throws Exception {
    when(fsOperations.exists(FS_PATH)).thenReturn(true);
    when(fsOperations.isFile(FS_PATH)).thenReturn(true);

    validatingFsManager.read(WS_PATH);

    verify(suspendingFsManager).read(WS_PATH);
  }

  @Test(expectedExceptions = NotFoundException.class)
  public void shouldThrowNotFoundExceptionOnRead() throws Exception {
    when(fsOperations.exists(FS_PATH)).thenReturn(false);

    validatingFsManager.read(WS_PATH);
  }

  @Test(expectedExceptions = ConflictException.class)
  public void shouldThrowConflictExceptionOnRead() throws Exception {
    when(fsOperations.exists(FS_PATH)).thenReturn(true);
    when(fsOperations.isFile(FS_PATH)).thenReturn(false);

    validatingFsManager.read(WS_PATH);
  }

  @Test
  public void shouldCallSuspendingFsManagerOnZip() throws Exception {
    when(fsOperations.exists(FS_PATH)).thenReturn(true);

    validatingFsManager.zip(WS_PATH);

    verify(suspendingFsManager).zip(WS_PATH);
  }

  @Test(expectedExceptions = NotFoundException.class)
  public void shouldThrowNotFoundExceptionOnZip() throws Exception {
    when(fsOperations.exists(FS_PATH)).thenReturn(false);

    validatingFsManager.zip(WS_PATH);
  }

  @Test
  public void shouldCallSuspendingFsManagerOnUnzip() throws Exception {
    when(fsOperations.exists(FS_PATH)).thenReturn(true);
    when(fsOperations.isDir(FS_PATH)).thenReturn(true);

    validatingFsManager.unzip(WS_PATH, INPUT_STREAM, false, false, false);

    verify(suspendingFsManager).unzip(WS_PATH, INPUT_STREAM, false, false, false);
  }

  @Test(expectedExceptions = NotFoundException.class)
  public void shouldThrowNotFoundExceptionOnUnzip() throws Exception {
    when(fsOperations.exists(FS_PATH)).thenReturn(false);

    validatingFsManager.unzip(WS_PATH, INPUT_STREAM, false, false, false);
  }

  @Test(expectedExceptions = ConflictException.class)
  public void shouldThrowConflictExceptionOnUnzip() throws Exception {
    when(fsOperations.exists(FS_PATH)).thenReturn(true);
    when(fsOperations.isDir(FS_PATH)).thenReturn(false);

    validatingFsManager.unzip(WS_PATH, INPUT_STREAM, false, false, false);
  }

  @Test
  public void shouldCallSuspendingFsManagerOnUpdateWithContent() throws Exception {
    when(fsOperations.exists(FS_PATH)).thenReturn(true);
    when(fsOperations.isFile(FS_PATH)).thenReturn(true);

    validatingFsManager.update(WS_PATH, INPUT_STREAM);

    verify(suspendingFsManager).update(WS_PATH, INPUT_STREAM);
  }

  @Test(expectedExceptions = NotFoundException.class)
  public void shouldThrowNotFoundExceptionOnUpdateWithContent() throws Exception {
    when(fsOperations.exists(FS_PATH)).thenReturn(false);
    when(fsOperations.isFile(FS_PATH)).thenReturn(true);

    validatingFsManager.update(WS_PATH, INPUT_STREAM);
  }

  @Test(expectedExceptions = ConflictException.class)
  public void shouldThrowConflictExceptionOnUpdateWithContent() throws Exception {
    when(fsOperations.exists(FS_PATH)).thenReturn(true);
    when(fsOperations.isFile(FS_PATH)).thenReturn(false);

    validatingFsManager.update(WS_PATH, INPUT_STREAM);
  }

  @Test(expectedExceptions = ConflictException.class)
  public void shouldThrowAnotherConflictExceptionOnUpdateWithContent() throws Exception {
    when(fsOperations.exists(FS_PATH)).thenReturn(true);
    when(fsOperations.isFile(FS_PATH)).thenReturn(true);

    validatingFsManager.update(WS_PATH, (InputStream) null);
  }

  @Test
  public void shouldCallSuspendingFsManagerOnUpdateWithUpdater() throws Exception {
    when(fsOperations.exists(FS_PATH)).thenReturn(true);
    when(fsOperations.isFile(FS_PATH)).thenReturn(true);

    validatingFsManager.update(WS_PATH, updater);

    verify(suspendingFsManager).update(WS_PATH, updater);
  }

  @Test(expectedExceptions = NotFoundException.class)
  public void shouldThrowNotFoundExceptionOnUpdateWithUpdate() throws Exception {
    when(fsOperations.exists(FS_PATH)).thenReturn(false);
    when(fsOperations.isFile(FS_PATH)).thenReturn(true);

    validatingFsManager.update(WS_PATH, updater);
  }

  @Test(expectedExceptions = ConflictException.class)
  public void shouldThrowConflictExceptionOnUpdateWithUpdate() throws Exception {
    when(fsOperations.exists(FS_PATH)).thenReturn(true);
    when(fsOperations.isFile(FS_PATH)).thenReturn(false);

    validatingFsManager.update(WS_PATH, updater);
  }

  @Test(expectedExceptions = ConflictException.class)
  public void shouldThrowAnotherConflictExceptionOnUpdateWithUpdate() throws Exception {
    when(fsOperations.exists(FS_PATH)).thenReturn(true);
    when(fsOperations.isFile(FS_PATH)).thenReturn(true);

    validatingFsManager.update(WS_PATH, (BiConsumer<InputStream, OutputStream>) null);
  }

  @Test
  public void shouldCallSuspendingFsManagerOnDelete() throws Exception {
    when(fsOperations.exists(FS_PATH)).thenReturn(true);

    validatingFsManager.delete(WS_PATH, false);

    verify(suspendingFsManager).delete(WS_PATH, false);
  }

  @Test(expectedExceptions = NotFoundException.class)
  public void shouldThrowNotFoundExceptionOnDelete() throws Exception {
    when(fsOperations.exists(FS_PATH)).thenReturn(false);

    validatingFsManager.delete(WS_PATH, false);
  }

  @Test
  public void shouldNotThrowNotFoundExceptionOnDelete() throws Exception {
    when(fsOperations.exists(FS_PATH)).thenReturn(false);

    validatingFsManager.delete(WS_PATH, true);
  }

  @Test
  public void shouldCallSuspendingFsManagerOnCopy() throws Exception {
    when(fsOperations.exists(SRC_FS_PATH)).thenReturn(true);
    when(fsOperations.exists(PARENT_DST_FS_PATH)).thenReturn(true);
    when(fsOperations.exists(DST_FS_PATH)).thenReturn(false);

    validatingFsManager.copy(SRC_WS_PATH, DST_WS_PATH, false, false);

    verify(suspendingFsManager).copy(SRC_WS_PATH, DST_WS_PATH, false, false);
  }

  @Test(expectedExceptions = NotFoundException.class)
  public void shouldThrowNotFoundExceptionOnCopy() throws Exception {
    when(fsOperations.exists(SRC_FS_PATH)).thenReturn(false);
    when(fsOperations.exists(PARENT_DST_FS_PATH)).thenReturn(true);
    when(fsOperations.exists(DST_FS_PATH)).thenReturn(false);

    validatingFsManager.copy(SRC_WS_PATH, DST_WS_PATH, false, false);
  }

  @Test(expectedExceptions = ConflictException.class)
  public void shouldThrowConflictExceptionOnCopy() throws Exception {
    when(fsOperations.exists(SRC_FS_PATH)).thenReturn(true);
    when(fsOperations.exists(PARENT_DST_FS_PATH)).thenReturn(true);
    when(fsOperations.exists(DST_FS_PATH)).thenReturn(true);

    validatingFsManager.copy(SRC_WS_PATH, DST_WS_PATH, false, false);
  }

  @Test
  public void shouldNotThrowConflictExceptionOnCopy() throws Exception {
    when(fsOperations.exists(SRC_FS_PATH)).thenReturn(true);
    when(fsOperations.exists(PARENT_DST_FS_PATH)).thenReturn(true);
    when(fsOperations.exists(DST_FS_PATH)).thenReturn(true);

    validatingFsManager.copy(SRC_WS_PATH, DST_WS_PATH, true, false);
  }

  @Test(expectedExceptions = NotFoundException.class)
  public void shouldThrowAnotherNotFoundOnCopy() throws Exception {
    when(fsOperations.exists(SRC_FS_PATH)).thenReturn(true);
    when(fsOperations.exists(PARENT_DST_FS_PATH)).thenReturn(false);
    when(fsOperations.exists(DST_FS_PATH)).thenReturn(false);

    validatingFsManager.copy(SRC_WS_PATH, DST_WS_PATH, false, false);
  }

  @Test
  public void shouldNotThrowAnotherNotFoundOnCopy() throws Exception {
    when(fsOperations.exists(SRC_FS_PATH)).thenReturn(true);
    when(fsOperations.exists(PARENT_DST_FS_PATH)).thenReturn(false);
    when(fsOperations.exists(DST_FS_PATH)).thenReturn(false);

    validatingFsManager.copy(SRC_WS_PATH, DST_WS_PATH, false, true);
  }

  @Test
  public void shouldCallSuspendingFsManagerOnMove() throws Exception {
    when(fsOperations.exists(SRC_FS_PATH)).thenReturn(true);
    when(fsOperations.exists(PARENT_DST_FS_PATH)).thenReturn(true);
    when(fsOperations.exists(DST_FS_PATH)).thenReturn(false);

    validatingFsManager.move(SRC_WS_PATH, DST_WS_PATH, false, false);

    verify(suspendingFsManager).move(SRC_WS_PATH, DST_WS_PATH, false, false);
  }

  @Test(expectedExceptions = NotFoundException.class)
  public void shouldThrowNotFoundExceptionOnMove() throws Exception {
    when(fsOperations.exists(SRC_FS_PATH)).thenReturn(false);
    when(fsOperations.exists(PARENT_DST_FS_PATH)).thenReturn(true);
    when(fsOperations.exists(DST_FS_PATH)).thenReturn(false);

    validatingFsManager.move(SRC_WS_PATH, DST_WS_PATH, false, false);
  }

  @Test(expectedExceptions = ConflictException.class)
  public void shouldThrowConflictExceptionOnMove() throws Exception {
    when(fsOperations.exists(SRC_FS_PATH)).thenReturn(true);
    when(fsOperations.exists(PARENT_DST_FS_PATH)).thenReturn(true);
    when(fsOperations.exists(DST_FS_PATH)).thenReturn(true);

    validatingFsManager.move(SRC_WS_PATH, DST_WS_PATH, false, false);
  }

  @Test
  public void shouldNotThrowConflictExceptionOnMove() throws Exception {
    when(fsOperations.exists(SRC_FS_PATH)).thenReturn(true);
    when(fsOperations.exists(PARENT_DST_FS_PATH)).thenReturn(true);
    when(fsOperations.exists(DST_FS_PATH)).thenReturn(true);

    validatingFsManager.move(SRC_WS_PATH, DST_WS_PATH, true, false);
  }

  @Test(expectedExceptions = NotFoundException.class)
  public void shouldThrowAnotherNotFoundOnMove() throws Exception {
    when(fsOperations.exists(SRC_FS_PATH)).thenReturn(true);
    when(fsOperations.exists(PARENT_DST_FS_PATH)).thenReturn(false);
    when(fsOperations.exists(DST_FS_PATH)).thenReturn(false);

    validatingFsManager.move(SRC_WS_PATH, DST_WS_PATH, false, false);
  }

  @Test
  public void shouldNotThrowAnotherNotFoundOnMove() throws Exception {
    when(fsOperations.exists(SRC_FS_PATH)).thenReturn(true);
    when(fsOperations.exists(PARENT_DST_FS_PATH)).thenReturn(false);
    when(fsOperations.exists(DST_FS_PATH)).thenReturn(false);

    validatingFsManager.move(SRC_WS_PATH, DST_WS_PATH, false, true);
  }

  @Test
  public void shouldCallSuspendingFsManagerOnGetFileNames() throws Exception {
    validatingFsManager.getFileWsPaths(WS_PATH);

    verify(suspendingFsManager).getFileWsPaths(WS_PATH);
  }

  @Test
  public void shouldCallSuspendingFsManagerOnGetDirNames() throws Exception {
    validatingFsManager.getDirNames(WS_PATH);

    verify(suspendingFsManager).getDirNames(WS_PATH);
  }

  @Test
  public void shouldCallSuspendingFsManagerOnGetFileWsPaths() throws Exception {
    validatingFsManager.getFileWsPaths(WS_PATH);

    verify(suspendingFsManager).getFileWsPaths(WS_PATH);
  }

  @Test
  public void shouldCallSuspendingFsManagerOnGetDirWsPaths() throws Exception {
    validatingFsManager.getDirWsPaths(WS_PATH);

    verify(suspendingFsManager).getDirWsPaths(WS_PATH);
  }

  @Test
  public void shouldCallSuspendingFsManagerOnGetAllChildrenNames() throws Exception {
    validatingFsManager.getAllChildrenNames(WS_PATH);

    verify(suspendingFsManager).getAllChildrenNames(WS_PATH);
  }

  @Test
  public void shouldCallSuspendingFsManagerOnGetAllChildrenWsPaths() throws Exception {
    validatingFsManager.getAllChildrenWsPaths(WS_PATH);

    verify(suspendingFsManager).getAllChildrenWsPaths(WS_PATH);
  }

  @Test
  public void shouldCallSuspendingFsManagerOnIsDir() throws Exception {
    validatingFsManager.isDir(WS_PATH);

    verify(suspendingFsManager).isDir(WS_PATH);
  }

  @Test
  public void shouldCallSuspendingFsManagerOnIsFile() throws Exception {
    validatingFsManager.isFile(WS_PATH);

    verify(suspendingFsManager).isFile(WS_PATH);
  }

  @Test
  public void shouldCallSuspendingFsManagerOnExists() throws Exception {
    validatingFsManager.exists(WS_PATH);

    verify(suspendingFsManager).exists(WS_PATH);
  }

  @Test
  public void shouldCallSuspendingFsManagerOnExistsAsDir() throws Exception {
    validatingFsManager.existsAsDir(WS_PATH);

    verify(suspendingFsManager).existsAsDir(WS_PATH);
  }

  @Test
  public void shouldCallSuspendingFsManagerOnExistsAsFile() throws Exception {
    validatingFsManager.existsAsFile(WS_PATH);

    verify(suspendingFsManager).existsAsFile(WS_PATH);
  }

  @Test
  public void shouldCallSuspendingFsManagerOnLastModified() throws Exception {
    validatingFsManager.lastModified(WS_PATH);

    verify(suspendingFsManager).lastModified(WS_PATH);
  }

  @Test
  public void shouldCallSuspendingFsManagerOnLength() throws Exception {
    validatingFsManager.length(WS_PATH);

    verify(suspendingFsManager).length(WS_PATH);
  }

  @Test
  public void shouldCallSuspendingFsManagerOnToIoFile() throws Exception {
    validatingFsManager.toIoFile(WS_PATH);

    verify(suspendingFsManager).toIoFile(WS_PATH);
  }
}
