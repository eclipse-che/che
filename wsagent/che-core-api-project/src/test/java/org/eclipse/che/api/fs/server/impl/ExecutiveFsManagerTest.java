/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.fs.server.impl;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.BiConsumer;
import org.eclipse.che.api.fs.server.PathTransformer;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/** Tests for {@link ExecutiveFsManager} */
@Listeners(MockitoTestNGListener.class)
public class ExecutiveFsManagerTest {

  private static final String WS_PATH = "/ws/path";
  private static final Path FS_PATH = Paths.get("/fs/path");

  private static final String SRC_WS_PATH = "/ws/path/src";
  private static final Path SRC_FS_PATH = Paths.get("/fs/path/src");

  private static final String DST_WS_PATH = "/ws/path/dst";
  private static final Path DST_FS_PATH = Paths.get("/fs/path/dst");

  private static final String TMP_WS_PATH = "/ws/path.tmp";
  private static final Path TMP_FS_PATH = Paths.get("/fs/path.tmp");

  private static final InputStream INPUT_STREAM =
      new InputStream() {
        @Override
        public int read() throws IOException {
          return -1;
        }
      };

  @Mock private FsOperations fsOperations;
  @Mock private ZipArchiver zipArchiver;
  @Mock private PathTransformer pathTransformer;
  @InjectMocks private ExecutiveFsManager executiveFsManager;

  @Mock private BiConsumer<InputStream, OutputStream> updater;

  @BeforeMethod
  public void setUp() throws Exception {
    when(pathTransformer.transform(WS_PATH)).thenReturn(FS_PATH);
    when(pathTransformer.transform(FS_PATH)).thenReturn(WS_PATH);

    when(pathTransformer.transform(SRC_WS_PATH)).thenReturn(SRC_FS_PATH);
    when(pathTransformer.transform(SRC_FS_PATH)).thenReturn(SRC_WS_PATH);

    when(pathTransformer.transform(DST_WS_PATH)).thenReturn(DST_FS_PATH);
    when(pathTransformer.transform(DST_FS_PATH)).thenReturn(DST_WS_PATH);

    when(pathTransformer.transform(TMP_WS_PATH)).thenReturn(TMP_FS_PATH);
    when(pathTransformer.transform(TMP_FS_PATH)).thenReturn(TMP_WS_PATH);
  }

  @Test
  public void shouldTransformWsPathToFsPathOnCreateFile() throws Exception {
    executiveFsManager.createFile(WS_PATH, false, false);

    verify(pathTransformer).transform(WS_PATH);
  }

  @Test
  public void shouldDeleteFileIfExistsWhenOverwriteIsEnabled() throws Exception {
    executiveFsManager.createFile(WS_PATH, true, false);

    verify(fsOperations).deleteIfExists(FS_PATH);
  }

  @Test
  public void shouldNotDeleteFileIfExistsWhenOverwriteIsDisabled() throws Exception {
    executiveFsManager.createFile(WS_PATH, false, false);

    verify(fsOperations, never()).deleteIfExists(FS_PATH);
  }

  @Test
  public void shouldCreateFileWithParentsWhenWithParentsIsEnabled() throws Exception {
    executiveFsManager.createFile(WS_PATH, false, true);

    verify(fsOperations).createFileWithParents(FS_PATH);
  }

  @Test
  public void shouldNotCreateFileWithParentsWhenWithParentsIsDisabled() throws Exception {
    executiveFsManager.createFile(WS_PATH, false, false);

    verify(fsOperations, never()).createFileWithParents(FS_PATH);
  }

  @Test
  public void shouldNotCreateFileWhenWithParentsIsEnabled() throws Exception {
    executiveFsManager.createFile(WS_PATH, false, true);

    verify(fsOperations, never()).createFile(FS_PATH);
  }

  @Test
  public void shouldCreateFileWhenWithParentsIsDisabled() throws Exception {
    executiveFsManager.createFile(WS_PATH, false, false);

    verify(fsOperations).createFile(FS_PATH);
  }

  @Test
  public void shouldTransformWsPathToFsPathOnCreateDir() throws Exception {
    executiveFsManager.createDir(WS_PATH, false, false);

    verify(pathTransformer).transform(WS_PATH);
  }

  @Test
  public void shouldDeleteDirIfExistsWhenOverwriteIsEnabled() throws Exception {
    executiveFsManager.createDir(WS_PATH, true, false);

    verify(fsOperations).deleteIfExists(FS_PATH);
  }

  @Test
  public void shouldNotDirFileIfExistsWhenOverwriteIsDisabled() throws Exception {
    executiveFsManager.createDir(WS_PATH, false, false);

    verify(fsOperations, never()).deleteIfExists(FS_PATH);
  }

  @Test
  public void shouldCreateDirWithParentsWhenWithParentsIsEnabled() throws Exception {
    executiveFsManager.createDir(WS_PATH, false, true);

    verify(fsOperations).createDirWithParents(FS_PATH);
  }

  @Test
  public void shouldNotCreateDirWithParentsWhenWithParentsIsDisabled() throws Exception {
    executiveFsManager.createDir(WS_PATH, false, false);

    verify(fsOperations, never()).createDirWithParents(FS_PATH);
  }

  @Test
  public void shouldNotCreateDirWhenWithParentsIsEnabled() throws Exception {
    executiveFsManager.createDir(WS_PATH, false, true);

    verify(fsOperations, never()).createDir(FS_PATH);
  }

  @Test
  public void shouldCreateDirWhenWithParentsIsDisabled() throws Exception {
    executiveFsManager.createDir(WS_PATH, false, false);

    verify(fsOperations).createDir(FS_PATH);
  }

  @Test
  public void shouldTransformWsPathToFsPathOnRead() throws Exception {
    executiveFsManager.read(WS_PATH);

    verify(pathTransformer).transform(WS_PATH);
  }

  @Test
  public void shouldFsOperationsReadOnRead() throws Exception {
    executiveFsManager.read(WS_PATH);

    verify(fsOperations).read(FS_PATH);
  }

  @Test
  public void shouldZipArchiverZipOnZip() throws Exception {
    executiveFsManager.zip(WS_PATH);

    verify(zipArchiver).zip(FS_PATH);
  }

  @Test
  public void shouldZipArchiverUnzipOnUnzip() throws Exception {
    executiveFsManager.unzip(WS_PATH, INPUT_STREAM, false, false, false);

    verify(zipArchiver).unzip(FS_PATH, INPUT_STREAM, false, false, false);
  }

  @Test
  public void shouldTransformWsPathToFsPathOnUpdateWithStream() throws Exception {
    executiveFsManager.update(WS_PATH, INPUT_STREAM);

    verify(pathTransformer).transform(WS_PATH);
  }

  @Test
  public void shouldFsOperationUpdateOnUpdateWithStream() throws Exception {
    executiveFsManager.update(WS_PATH, INPUT_STREAM);

    verify(fsOperations).update(FS_PATH, INPUT_STREAM);
  }

  @Test
  public void shouldTransformWsPathToFsPathOnUpdateWithUpdater() throws Exception {
    executiveFsManager.update(WS_PATH, updater);

    verify(pathTransformer).transform(WS_PATH);
  }

  @Test
  public void shouldTransformForTmpFileWsPathToFsPathOnUpdateWithUpdater() throws Exception {
    executiveFsManager.update(WS_PATH, updater);

    verify(pathTransformer).transform(TMP_WS_PATH);
  }

  @Test
  public void shouldFsOperationReadOnUpdateWithUpdater() throws Exception {
    executiveFsManager.update(WS_PATH, updater);

    verify(fsOperations).read(FS_PATH);
  }

  @Test
  public void shouldFsOperationWriteOnUpdateWithUpdater() throws Exception {
    executiveFsManager.update(WS_PATH, updater);

    verify(fsOperations).write(TMP_FS_PATH);
  }

  @Test
  public void shouldFsOperationDeleteIfExistsOnUpdateWithUpdater() throws Exception {
    executiveFsManager.update(WS_PATH, updater);

    verify(fsOperations).deleteIfExists(FS_PATH);
  }

  @Test
  public void shouldFsOperationMoveOnUpdateWithUpdater() throws Exception {
    executiveFsManager.update(WS_PATH, updater);

    verify(fsOperations).move(TMP_FS_PATH, FS_PATH);
  }

  @Test
  public void shouldTransformWsPathToFsPathOnDelete() throws Exception {
    executiveFsManager.delete(WS_PATH, false);

    verify(pathTransformer).transform(WS_PATH);
  }

  @Test
  public void shouldFsOperationDeleteIfExistsOnDeleteWithQuietly() throws Exception {
    executiveFsManager.delete(WS_PATH, true);

    verify(fsOperations).deleteIfExists(FS_PATH);
  }

  @Test
  public void shouldNotFsOperationDeleteOnDeleteWithQuietly() throws Exception {
    executiveFsManager.delete(WS_PATH, true);

    verify(fsOperations, never()).delete(FS_PATH);
  }

  @Test
  public void shouldFsOperationDeleteOnDeleteWithoutQuietly() throws Exception {
    executiveFsManager.delete(WS_PATH, false);

    verify(fsOperations).delete(FS_PATH);
  }

  @Test
  public void shouldNotFsOperationDeleteIfExistsOnDeleteWithoutQuietly() throws Exception {
    executiveFsManager.delete(WS_PATH, false);

    verify(fsOperations, never()).deleteIfExists(FS_PATH);
  }

  @Test
  public void shouldTransformWsPathToFsPathOnCopy() throws Exception {
    executiveFsManager.copy(SRC_WS_PATH, DST_WS_PATH, false, false);

    verify(pathTransformer).transform(SRC_WS_PATH);

    verify(pathTransformer).transform(DST_WS_PATH);
  }

  @Test
  public void shouldDeleteIfExistsOnCopyWhenOverwriteIsEnabled() throws Exception {
    executiveFsManager.copy(SRC_WS_PATH, DST_WS_PATH, true, false);

    verify(fsOperations).deleteIfExists(DST_FS_PATH);
  }

  @Test
  public void shouldNotDeleteFileIfExistsOnCopyWhenOverwriteIsDisabled() throws Exception {
    executiveFsManager.copy(SRC_WS_PATH, DST_WS_PATH, false, false);

    verify(fsOperations, never()).deleteIfExists(DST_FS_PATH);
  }

  @Test
  public void shouldCopyWithParentsOnCopyWhenWithParentsIsEnabled() throws Exception {
    executiveFsManager.copy(SRC_WS_PATH, DST_WS_PATH, false, true);

    verify(fsOperations).copyWithParents(SRC_FS_PATH, DST_FS_PATH);
  }

  @Test
  public void shouldNotCopyWithParentsOnCopyWhenWithParentsIsDisabled() throws Exception {
    executiveFsManager.copy(SRC_WS_PATH, DST_WS_PATH, false, false);

    verify(fsOperations, never()).copyWithParents(SRC_FS_PATH, DST_FS_PATH);
  }

  @Test
  public void shouldNotCopyOnCopyWhenWithParentsIsEnabled() throws Exception {
    executiveFsManager.copy(SRC_WS_PATH, DST_WS_PATH, false, true);

    verify(fsOperations, never()).copy(SRC_FS_PATH, DST_FS_PATH);
  }

  @Test
  public void shouldCopyOnCopyWhenWithParentsIsDisabled() throws Exception {
    executiveFsManager.copy(SRC_WS_PATH, DST_WS_PATH, false, false);

    verify(fsOperations).copy(SRC_FS_PATH, DST_FS_PATH);
  }

  @Test
  public void shouldTransformWsPathToFsPathOnMove() throws Exception {
    executiveFsManager.move(SRC_WS_PATH, DST_WS_PATH, false, false);

    verify(pathTransformer).transform(SRC_WS_PATH);

    verify(pathTransformer).transform(DST_WS_PATH);
  }

  @Test
  public void shouldDeleteIfExistsOnMoveWhenOverwriteIsEnabled() throws Exception {
    executiveFsManager.move(SRC_WS_PATH, DST_WS_PATH, true, false);

    verify(fsOperations).deleteIfExists(DST_FS_PATH);
  }

  @Test
  public void shouldNotDeleteFileIfExistsOnMoveWhenOverwriteIsDisabled() throws Exception {
    executiveFsManager.move(SRC_WS_PATH, DST_WS_PATH, false, false);

    verify(fsOperations, never()).deleteIfExists(DST_FS_PATH);
  }

  @Test
  public void shouldMoveWithParentsOnMoveWhenWithParentsIsEnabled() throws Exception {
    executiveFsManager.move(SRC_WS_PATH, DST_WS_PATH, false, true);

    verify(fsOperations).moveWithParents(SRC_FS_PATH, DST_FS_PATH);
  }

  @Test
  public void shouldNotMoveWithParentsOnMoveWhenWithParentsIsDisabled() throws Exception {
    executiveFsManager.move(SRC_WS_PATH, DST_WS_PATH, false, false);

    verify(fsOperations, never()).moveWithParents(SRC_FS_PATH, DST_FS_PATH);
  }

  @Test
  public void shouldNotMoveOnMoveWhenWithParentsIsEnabled() throws Exception {
    executiveFsManager.move(SRC_WS_PATH, DST_WS_PATH, false, true);

    verify(fsOperations, never()).move(SRC_FS_PATH, DST_FS_PATH);
  }

  @Test
  public void shouldMoveOnMoveWhenWithParentsIsDisabled() throws Exception {
    executiveFsManager.move(SRC_WS_PATH, DST_WS_PATH, false, false);

    verify(fsOperations).move(SRC_FS_PATH, DST_FS_PATH);
  }

  @Test
  public void shouldTransformWsPathToFsPathOnIsFile() throws Exception {
    executiveFsManager.isFile(WS_PATH);

    verify(pathTransformer).transform(WS_PATH);
  }

  @Test
  public void shouldFsOperationsIsFileOnIsFile() throws Exception {
    executiveFsManager.isFile(WS_PATH);

    verify(fsOperations).isFile(FS_PATH);
  }

  @Test
  public void shouldTransformWsPathToFsPathOnIsDir() throws Exception {
    executiveFsManager.isDir(WS_PATH);

    verify(pathTransformer).transform(WS_PATH);
  }

  @Test
  public void shouldFsOperationsIsDirOnIsDir() throws Exception {
    executiveFsManager.isDir(WS_PATH);

    verify(fsOperations).isDir(FS_PATH);
  }

  @Test
  public void shouldTransformWsPathToFsPathOnExists() throws Exception {
    executiveFsManager.exists(WS_PATH);

    verify(pathTransformer).transform(WS_PATH);
  }

  @Test
  public void shouldFsOperationsExistsOnExists() throws Exception {
    executiveFsManager.exists(WS_PATH);

    verify(fsOperations).exists(FS_PATH);
  }

  @Test
  public void shouldTransformWsPathToFsPathOnLength() throws Exception {
    executiveFsManager.length(WS_PATH);

    verify(pathTransformer).transform(WS_PATH);
  }

  @Test
  public void shouldFsOperationsLengthOnLength() throws Exception {
    executiveFsManager.length(WS_PATH);

    verify(fsOperations).length(FS_PATH);
  }

  @Test
  public void shouldTransformWsPathToFsPathOnLastModified() throws Exception {
    executiveFsManager.lastModified(WS_PATH);

    verify(pathTransformer).transform(WS_PATH);
  }

  @Test
  public void shouldFsOperationsLastModifiedOnLastModified() throws Exception {
    executiveFsManager.lastModified(WS_PATH);

    verify(fsOperations).lastModified(FS_PATH);
  }

  @Test
  public void shouldTransformWsPathToFsPathOnGetFileNames() throws Exception {
    executiveFsManager.getFileNames(WS_PATH);

    verify(pathTransformer).transform(WS_PATH);
  }

  @Test
  public void shouldFsOperationsGetFileNamesOnGetFileNames() throws Exception {
    executiveFsManager.getFileNames(WS_PATH);

    verify(fsOperations).getFileNames(FS_PATH);
  }

  @Test
  public void shouldTransformWsPathToFsPathOnGetFileWsPaths() throws Exception {
    executiveFsManager.getFileWsPaths(WS_PATH);

    verify(pathTransformer).transform(WS_PATH);
  }

  @Test
  public void shouldFsOperationsGetFilePathsOnGetFileWsPaths() throws Exception {
    executiveFsManager.getFileWsPaths(WS_PATH);

    verify(fsOperations).getFilePaths(FS_PATH);
  }

  @Test
  public void shouldTransformWsPathToFsPathOnGetDirNames() throws Exception {
    executiveFsManager.getDirNames(WS_PATH);

    verify(pathTransformer).transform(WS_PATH);
  }

  @Test
  public void shouldFsOperationsGetDirNamesOnGetDirNames() throws Exception {
    executiveFsManager.getDirNames(WS_PATH);

    verify(fsOperations).getDirNames(FS_PATH);
  }

  @Test
  public void shouldTransformWsPathToFsPathOnGetDirWsPaths() throws Exception {
    executiveFsManager.getDirWsPaths(WS_PATH);

    verify(pathTransformer).transform(WS_PATH);
  }

  @Test
  public void shouldFsOperationsGetDirPathsOnGetDirWsPaths() throws Exception {
    executiveFsManager.getDirWsPaths(WS_PATH);

    verify(fsOperations).getDirPaths(FS_PATH);
  }

  @Test
  public void shouldTransformWsPathToFsPathOnToIoFile() throws Exception {
    executiveFsManager.toIoFile(WS_PATH);

    verify(pathTransformer).transform(WS_PATH);
  }

  @Test
  public void shouldFsOperationsToIoFileOnToIoFile() throws Exception {
    executiveFsManager.toIoFile(WS_PATH);

    verify(fsOperations).toIoFile(FS_PATH);
  }
}
