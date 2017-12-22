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
package org.eclipse.che.workspace.infrastructure.docker.local.projects;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.api.workspace.server.spi.WorkspaceDao;
import org.eclipse.che.commons.lang.IoUtil;
import org.eclipse.che.workspace.infrastructure.docker.WindowsHostUtils;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(value = {MockitoTestNGListener.class})
public class LocalProjectsFolderPathProviderTest {

  private static final String WS_ID = "testWsId";
  private static final String WS_NAME = "testWsName";
  private static final String WS_NAMESPACE = "che";

  @Mock private WorkspaceDao workspaceDao;
  @Mock private LocalProjectsMigrator localProjectsMigrator;

  private String singleFolderForAllWorkspaces;
  private String oldWorkspacesRoot;
  private File workspacesRootFile;
  private String workspacesRoot;

  @BeforeMethod
  public void setUp() throws Exception {
    WorkspaceImpl workspace = mock(WorkspaceImpl.class);
    WorkspaceConfigImpl workspaceConfig = mock(WorkspaceConfigImpl.class);
    when(workspaceDao.get(WS_ID)).thenReturn(workspace);
    when(workspaceDao.get(WS_NAME, WS_NAMESPACE)).thenReturn(workspace);
    when(workspaceDao.getWorkspaces(eq(false), anyInt(), anyLong()))
        .thenReturn(new Page<>(Collections.singletonList(workspace), 0, 1, 1));
    when(workspace.getConfig()).thenReturn(workspaceConfig);
    when(workspaceConfig.getName()).thenReturn(WS_NAME);
    when(workspace.getNamespace()).thenReturn(WS_NAMESPACE);
    when(workspace.getId()).thenReturn(WS_ID);

    Path tempDirectory = Files.createTempDirectory(getClass().getSimpleName());
    workspacesRoot = tempDirectory.toString();
    workspacesRootFile = tempDirectory.toFile();
    singleFolderForAllWorkspaces =
        Paths.get(workspacesRoot, "singleFolderForAllWorkspaces").toString();
    oldWorkspacesRoot = Paths.get(workspacesRoot, "oldWorkspacesRoot").toString();
  }

  @AfterMethod
  public void tearDown() throws Exception {
    IoUtil.deleteRecursive(workspacesRootFile);
  }

  @Test
  public void createsFoldersByDefault() throws Exception {
    assertTrue(workspacesRootFile.delete());

    LocalProjectsFolderPathProvider provider =
        new LocalProjectsFolderPathProvider(
            workspacesRoot, false, workspaceDao, localProjectsMigrator);

    provider.init();

    assertTrue(workspacesRootFile.exists());
    assertTrue(workspacesRootFile.isDirectory());

    String providerPath = provider.getPath(WS_ID);

    assertTrue(new File(providerPath).exists());
    assertTrue(new File(providerPath).isDirectory());
  }

  @Test
  public void createsFoldersIfConfigured() throws Exception {
    assertTrue(workspacesRootFile.delete());
    LocalProjectsFolderPathProvider provider =
        new LocalProjectsFolderPathProvider(
            workspacesRoot,
            null,
            false,
            null,
            true, // <- Create folder if it doesn't exist
            workspaceDao,
            localProjectsMigrator,
            false);

    provider.init();

    assertTrue(workspacesRootFile.exists());
    assertTrue(workspacesRootFile.isDirectory());

    String providerPath = provider.getPath(WS_ID);

    assertTrue(new File(providerPath).exists());
    assertTrue(new File(providerPath).isDirectory());
  }

  @Test
  public void worksIfWorkspaceFolderExists() throws Exception {
    assertTrue(Paths.get(workspacesRoot, WS_ID).toFile().mkdir());
    LocalProjectsFolderPathProvider provider =
        new LocalProjectsFolderPathProvider(
            workspacesRoot,
            null,
            false,
            null,
            false, // <- Do not create folders
            workspaceDao,
            localProjectsMigrator,
            false);

    provider.init();

    assertTrue(workspacesRootFile.exists());
    assertTrue(workspacesRootFile.isDirectory());

    String providerPath = provider.getPathByName(WS_NAME, WS_NAMESPACE);

    assertTrue(new File(providerPath).exists());
    assertTrue(new File(providerPath).isDirectory());
  }

  @Test
  public void neitherCheckNorCreateFoldersIfCreationIsDisabled() throws Exception {
    assertTrue(workspacesRootFile.delete());
    LocalProjectsFolderPathProvider provider =
        new LocalProjectsFolderPathProvider(
            workspacesRoot,
            null,
            false,
            null,
            false, // <- Do not create folders
            workspaceDao,
            localProjectsMigrator,
            false);

    provider.init();
    String providerPath = provider.getPath(WS_ID);

    assertFalse(workspacesRootFile.exists());
    assertFalse(new File(providerPath).exists());
  }

  @Test
  public void returnSpecificFolderOnOnWindows() throws Exception {
    LocalProjectsFolderPathProvider provider =
        new LocalProjectsFolderPathProvider(
            workspacesRoot,
            oldWorkspacesRoot,
            false,
            singleFolderForAllWorkspaces,
            false,
            workspaceDao,
            localProjectsMigrator,
            true);

    provider.init();
    String providerPath = provider.getPath(WS_ID);

    assertEquals(
        providerPath, WindowsHostUtils.getCheHome().resolve("vfs").resolve(WS_ID).toString());
  }

  @Test
  public void returnsSingleFolderForAllWorkspacesIfConfigured() throws Exception {
    LocalProjectsFolderPathProvider provider =
        new LocalProjectsFolderPathProvider(
            workspacesRoot,
            oldWorkspacesRoot,
            false,
            singleFolderForAllWorkspaces,
            false,
            workspaceDao,
            localProjectsMigrator,
            false);

    provider.init();
    String providerPath = provider.getPath(WS_ID);

    assertEquals(providerPath, singleFolderForAllWorkspaces);
  }

  @Test(
    expectedExceptions = IOException.class,
    expectedExceptionsMessageRegExp = "Workspace folder '.*' is not directory"
  )
  public void throwsExceptionIfFileIsFoundByWorkspacesPath() throws Exception {
    assertTrue(Paths.get(workspacesRoot, WS_ID).toFile().createNewFile());
    LocalProjectsFolderPathProvider provider =
        new LocalProjectsFolderPathProvider(
            workspacesRoot, null, false, null, true, workspaceDao, localProjectsMigrator, false);

    provider.init();
    provider.getPath(WS_ID);
  }

  @Test(
    expectedExceptions = IOException.class,
    expectedExceptionsMessageRegExp =
        "Workspace folder '.*' is not directory. Check .* configuration property"
  )
  public void throwsExceptionIfFileIsFoundByWorkspacesRootPath() throws Exception {
    Path tempFile = Files.createTempFile(getClass().getSimpleName(), null);
    LocalProjectsFolderPathProvider provider =
        new LocalProjectsFolderPathProvider(
            tempFile.toString(),
            null,
            false,
            null,
            true,
            workspaceDao,
            localProjectsMigrator,
            false);

    provider.init();
  }

  @Test(
    expectedExceptions = IOException.class,
    expectedExceptionsMessageRegExp =
        "Workspace folder '.*' is not directory. Check .* configuration property"
  )
  public void throwsExceptionIfFileIsFoundBySingleWorkspacePath() throws Exception {
    Path tempFile = Files.createTempFile(getClass().getSimpleName(), null);
    LocalProjectsFolderPathProvider provider =
        new LocalProjectsFolderPathProvider(
            workspacesRoot,
            oldWorkspacesRoot,
            false,
            tempFile.toString(),
            true,
            workspaceDao,
            localProjectsMigrator,
            false);

    provider.init();
    provider.getPath(WS_ID);
  }

  @Test(
    expectedExceptions = IOException.class,
    expectedExceptionsMessageRegExp = "expected test exception"
  )
  public void throwsIOExceptionIfWorkspaceRetrievalFails() throws Exception {
    when(workspaceDao.get(WS_NAME, WS_NAMESPACE))
        .thenThrow(new ServerException("expected test exception"));
    LocalProjectsFolderPathProvider provider =
        new LocalProjectsFolderPathProvider(
            workspacesRoot, null, false, null, false, workspaceDao, localProjectsMigrator, true);

    provider.init();
    provider.getPathByName(WS_NAME, WS_NAMESPACE);
  }

  @Test
  public void shouldPerformWorkspaceMigration() throws Exception {
    Files.createDirectories(Paths.get(workspacesRoot).resolve(WS_NAME));
    Files.createFile(Paths.get(workspacesRoot).resolve(WS_NAME).resolve("pom.xml"));

    LocalProjectsFolderPathProvider provider =
        new LocalProjectsFolderPathProvider(
            workspacesRoot, null, true, null, false, workspaceDao, localProjectsMigrator, false);

    provider.init();

    verify(localProjectsMigrator).performMigration(eq(workspacesRoot));
  }
}
