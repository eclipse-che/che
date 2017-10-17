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

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.BiConsumer;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.watcher.server.FileWatcherManager;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/** Tests for {@link SuspendingFsManager} */
@Listeners(MockitoTestNGListener.class)
public class SuspendingFsManagerTest {

  private static final String WS_PATH = "/ws/path";
  private static final String SRC_WS_PATH = "/ws/path/src";
  private static final String DST_WS_PATH = "/ws/path/dst";
  private static final InputStream INPUT_STREAM =
      new InputStream() {
        @Override
        public int read() throws IOException {
          return -1;
        }
      };

  @Mock private FileWatcherManager fileWatcherManager;
  @Mock private ExecutiveFsManager executiveFsManager;
  @InjectMocks private SuspendingFsManager suspendingFsManager;

  @Mock private BiConsumer<InputStream, OutputStream> updater;

  @Test
  public void shouldSuspendFileWatchersOnCreateFile() throws Exception {
    suspendingFsManager.createFile(WS_PATH, false, false);

    verify(fileWatcherManager).suspend();
  }

  @Test
  public void shouldCallExecutiveFsManagerOnCreateFile() throws Exception {
    suspendingFsManager.createFile(WS_PATH, false, false);

    verify(executiveFsManager).createFile(WS_PATH, false, false);
  }

  @Test
  public void shouldResumeFileWatchersOnCreateFile() throws Exception {
    suspendingFsManager.createFile(WS_PATH, false, false);

    verify(fileWatcherManager).resume();
  }

  @Test(expectedExceptions = ServerException.class)
  public void shouldResumeFileWatchersAfterExceptionOnCreateFile() throws Exception {
    doThrow(new ServerException("")).when(executiveFsManager).createFile(WS_PATH, false, false);

    suspendingFsManager.createFile(WS_PATH, false, false);

    verify(fileWatcherManager).resume();
  }

  @Test
  public void shouldSuspendFileWatchersOnCreateDir() throws Exception {
    suspendingFsManager.createDir(WS_PATH, false, false);

    verify(fileWatcherManager).suspend();
  }

  @Test
  public void shouldCallExecutiveFsManagerOnCreateDir() throws Exception {
    suspendingFsManager.createDir(WS_PATH, false, false);

    verify(executiveFsManager).createDir(WS_PATH, false, false);
  }

  @Test
  public void shouldResumeFileWatchersOnCreateDir() throws Exception {
    suspendingFsManager.createDir(WS_PATH, false, false);

    verify(fileWatcherManager).resume();
  }

  @Test(expectedExceptions = ServerException.class)
  public void shouldResumeFileWatchersAfterExceptionOnCreateDir() throws Exception {
    doThrow(new ServerException("")).when(executiveFsManager).createDir(WS_PATH, false, false);

    suspendingFsManager.createDir(WS_PATH, false, false);

    verify(fileWatcherManager).resume();
  }

  @Test
  public void shouldSuspendFileWatchersOnRead() throws Exception {
    suspendingFsManager.read(WS_PATH);

    verify(fileWatcherManager).suspend();
  }

  @Test
  public void shouldCallExecutiveFsManagerOnRead() throws Exception {
    suspendingFsManager.read(WS_PATH);

    verify(executiveFsManager).read(WS_PATH);
  }

  @Test
  public void shouldResumeFileWatchersOnRead() throws Exception {
    suspendingFsManager.read(WS_PATH);

    verify(fileWatcherManager).resume();
  }

  @Test(expectedExceptions = ServerException.class)
  public void shouldResumeFileWatchersAfterExceptionOnRead() throws Exception {
    doThrow(new ServerException("")).when(executiveFsManager).read(WS_PATH);

    suspendingFsManager.read(WS_PATH);

    verify(fileWatcherManager).resume();
  }

  @Test
  public void shouldSuspendFileWatchersOnZip() throws Exception {
    suspendingFsManager.zip(WS_PATH);

    verify(fileWatcherManager).suspend();
  }

  @Test
  public void shouldCallExecutiveFsManagerOnZip() throws Exception {
    suspendingFsManager.zip(WS_PATH);

    verify(executiveFsManager).zip(WS_PATH);
  }

  @Test
  public void shouldResumeFileWatchersOnZip() throws Exception {
    suspendingFsManager.zip(WS_PATH);

    verify(fileWatcherManager).resume();
  }

  @Test(expectedExceptions = ServerException.class)
  public void shouldResumeFileWatchersAfterExceptionOnZip() throws Exception {
    doThrow(new ServerException("")).when(executiveFsManager).zip(WS_PATH);

    suspendingFsManager.zip(WS_PATH);

    verify(fileWatcherManager).resume();
  }

  @Test
  public void shouldSuspendFileWatchersOnUnzip() throws Exception {
    suspendingFsManager.unzip(WS_PATH, INPUT_STREAM, false, false, false);

    verify(fileWatcherManager).suspend();
  }

  @Test
  public void shouldCallExecutiveFsManagerOnUnzip() throws Exception {
    suspendingFsManager.unzip(WS_PATH, INPUT_STREAM, false, false, false);

    verify(executiveFsManager).unzip(WS_PATH, INPUT_STREAM, false, false, false);
  }

  @Test
  public void shouldResumeFileWatchersOnUnzip() throws Exception {
    suspendingFsManager.unzip(WS_PATH, INPUT_STREAM, false, false, false);

    verify(fileWatcherManager).resume();
  }

  @Test(expectedExceptions = ServerException.class)
  public void shouldResumeFileWatchersAfterExceptionOnUnzip() throws Exception {
    doThrow(new ServerException(""))
        .when(executiveFsManager)
        .unzip(WS_PATH, INPUT_STREAM, false, false, false);

    suspendingFsManager.unzip(WS_PATH, INPUT_STREAM, false, false, false);

    verify(fileWatcherManager).resume();
  }

  @Test
  public void shouldSuspendFileWatchersOnUpdateWithContent() throws Exception {
    suspendingFsManager.update(WS_PATH, INPUT_STREAM);

    verify(fileWatcherManager).suspend();
  }

  @Test
  public void shouldCallExecutiveFsManagerOnUpdateWithContent() throws Exception {
    suspendingFsManager.update(WS_PATH, INPUT_STREAM);

    verify(executiveFsManager).update(WS_PATH, INPUT_STREAM);
  }

  @Test
  public void shouldResumeFileWatchersOnUpdateWithContent() throws Exception {
    suspendingFsManager.update(WS_PATH, INPUT_STREAM);

    verify(fileWatcherManager).resume();
  }

  @Test(expectedExceptions = ServerException.class)
  public void shouldResumeFileWatchersAfterExceptionOnUpdateWithContent() throws Exception {
    doThrow(new ServerException("")).when(executiveFsManager).update(WS_PATH, INPUT_STREAM);

    suspendingFsManager.update(WS_PATH, INPUT_STREAM);

    verify(fileWatcherManager).resume();
  }

  @Test
  public void shouldSuspendFileWatchersOnUpdateWithUpdater() throws Exception {
    suspendingFsManager.update(WS_PATH, updater);

    verify(fileWatcherManager).suspend();
  }

  @Test
  public void shouldCallExecutiveFsManagerOnUpdateWithUpdater() throws Exception {
    suspendingFsManager.update(WS_PATH, updater);

    verify(executiveFsManager).update(WS_PATH, updater);
  }

  @Test
  public void shouldResumeFileWatchersOnUpdateWithUpdater() throws Exception {
    suspendingFsManager.update(WS_PATH, updater);

    verify(fileWatcherManager).resume();
  }

  @Test(expectedExceptions = ServerException.class)
  public void shouldResumeFileWatchersAfterExceptionOnUpdateWithUpdater() throws Exception {
    doThrow(new ServerException("")).when(executiveFsManager).update(WS_PATH, updater);

    suspendingFsManager.update(WS_PATH, updater);

    verify(fileWatcherManager).resume();
  }

  @Test
  public void shouldSuspendFileWatchersOnDelete() throws Exception {
    suspendingFsManager.delete(WS_PATH, false);

    verify(fileWatcherManager).suspend();
  }

  @Test
  public void shouldCallExecutiveFsManagerOnDelete() throws Exception {
    suspendingFsManager.delete(WS_PATH, false);

    verify(executiveFsManager).delete(WS_PATH, false);
  }

  @Test
  public void shouldResumeFileWatchersOnDelete() throws Exception {
    suspendingFsManager.delete(WS_PATH, false);

    verify(fileWatcherManager).resume();
  }

  @Test(expectedExceptions = ServerException.class)
  public void shouldResumeFileWatchersAfterExceptionOnDelete() throws Exception {
    doThrow(new ServerException("")).when(executiveFsManager).delete(WS_PATH, false);

    suspendingFsManager.delete(WS_PATH, false);

    verify(fileWatcherManager).resume();
  }

  @Test
  public void shouldSuspendFileWatchersOnCopy() throws Exception {
    suspendingFsManager.copy(SRC_WS_PATH, DST_WS_PATH, false, false);

    verify(fileWatcherManager).suspend();
  }

  @Test
  public void shouldCallExecutiveFsManagerOnCopy() throws Exception {
    suspendingFsManager.copy(SRC_WS_PATH, DST_WS_PATH, false, false);

    verify(executiveFsManager).copy(SRC_WS_PATH, DST_WS_PATH, false, false);
  }

  @Test
  public void shouldResumeFileWatchersOnCopy() throws Exception {
    suspendingFsManager.copy(SRC_WS_PATH, DST_WS_PATH, false, false);

    verify(fileWatcherManager).resume();
  }

  @Test(expectedExceptions = ServerException.class)
  public void shouldResumeFileWatchersAfterExceptionOnCopy() throws Exception {
    doThrow(new ServerException(""))
        .when(executiveFsManager)
        .copy(SRC_WS_PATH, DST_WS_PATH, false, false);

    suspendingFsManager.copy(SRC_WS_PATH, DST_WS_PATH, false, false);

    verify(fileWatcherManager).resume();
  }

  @Test
  public void shouldSuspendFileWatchersOnMove() throws Exception {
    suspendingFsManager.move(SRC_WS_PATH, DST_WS_PATH, false, false);

    verify(fileWatcherManager).suspend();
  }

  @Test
  public void shouldCallExecutiveFsManagerOnMove() throws Exception {
    suspendingFsManager.move(SRC_WS_PATH, DST_WS_PATH, false, false);

    verify(executiveFsManager).move(SRC_WS_PATH, DST_WS_PATH, false, false);
  }

  @Test
  public void shouldResumeFileWatchersOnMove() throws Exception {
    suspendingFsManager.move(SRC_WS_PATH, DST_WS_PATH, false, false);

    verify(fileWatcherManager).resume();
  }

  @Test(expectedExceptions = ServerException.class)
  public void shouldResumeFileWatchersAfterExceptionOnMove() throws Exception {
    doThrow(new ServerException(""))
        .when(executiveFsManager)
        .move(SRC_WS_PATH, DST_WS_PATH, false, false);

    suspendingFsManager.move(SRC_WS_PATH, DST_WS_PATH, false, false);

    verify(fileWatcherManager).resume();
  }

  @Test
  public void shouldCallExecutiveFsManagerOnGetFileNames() throws Exception {
    suspendingFsManager.getFileWsPaths(WS_PATH);

    verify(executiveFsManager).getFileWsPaths(WS_PATH);
  }

  @Test
  public void shouldCallExecutiveFsManagerOnGetDirNames() throws Exception {
    suspendingFsManager.getDirNames(WS_PATH);

    verify(executiveFsManager).getDirNames(WS_PATH);
  }

  @Test
  public void shouldCallExecutiveFsManagerOnGetFileWsPaths() throws Exception {
    suspendingFsManager.getFileWsPaths(WS_PATH);

    verify(executiveFsManager).getFileWsPaths(WS_PATH);
  }

  @Test
  public void shouldCallExecutiveFsManagerOnGetDirWsPaths() throws Exception {
    suspendingFsManager.getDirWsPaths(WS_PATH);

    verify(executiveFsManager).getDirWsPaths(WS_PATH);
  }

  @Test
  public void shouldCallExecutiveFsManagerOnGetAllChildrenNames() throws Exception {
    suspendingFsManager.getAllChildrenNames(WS_PATH);

    verify(executiveFsManager).getAllChildrenNames(WS_PATH);
  }

  @Test
  public void shouldCallExecutiveFsManagerOnGetAllChildrenWsPaths() throws Exception {
    suspendingFsManager.getAllChildrenWsPaths(WS_PATH);

    verify(executiveFsManager).getAllChildrenWsPaths(WS_PATH);
  }

  @Test
  public void shouldCallExecutiveFsManagerOnIsDir() throws Exception {
    suspendingFsManager.isDir(WS_PATH);

    verify(executiveFsManager).isDir(WS_PATH);
  }

  @Test
  public void shouldCallExecutiveFsManagerOnIsFile() throws Exception {
    suspendingFsManager.isFile(WS_PATH);

    verify(executiveFsManager).isFile(WS_PATH);
  }

  @Test
  public void shouldCallExecutiveFsManagerOnExists() throws Exception {
    suspendingFsManager.exists(WS_PATH);

    verify(executiveFsManager).exists(WS_PATH);
  }

  @Test
  public void shouldCallExecutiveFsManagerOnExistsAsDir() throws Exception {
    suspendingFsManager.existsAsDir(WS_PATH);

    verify(executiveFsManager).existsAsDir(WS_PATH);
  }

  @Test
  public void shouldCallExecutiveFsManagerOnExistsAsFile() throws Exception {
    suspendingFsManager.existsAsFile(WS_PATH);

    verify(executiveFsManager).existsAsFile(WS_PATH);
  }

  @Test
  public void shouldCallExecutiveFsManagerOnLastModified() throws Exception {
    suspendingFsManager.lastModified(WS_PATH);

    verify(executiveFsManager).lastModified(WS_PATH);
  }

  @Test
  public void shouldCallExecutiveFsManagerOnLength() throws Exception {
    suspendingFsManager.length(WS_PATH);

    verify(executiveFsManager).length(WS_PATH);
  }

  @Test
  public void shouldCallExecutiveFsManagerOnToIoFile() throws Exception {
    suspendingFsManager.toIoFile(WS_PATH);

    verify(executiveFsManager).toIoFile(WS_PATH);
  }
}
