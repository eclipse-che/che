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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.api.workspace.server.spi.WorkspaceDao;
import org.eclipse.che.workspace.infrastructure.docker.WindowsHostUtils;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(value = {MockitoTestNGListener.class})
public class LocalProjectsFolderPathProviderTest {

  private static final String WS_ID = "testWsId";
  private static final String WS_NAME = "testWsName";

  @Mock private WorkspaceDao workspaceDao;

  private String singleFolderForAllWorkspaces;
  private String oldWorkspacesRoot;
  private File workspacesRootFile;
  private String workspacesRoot;

  @BeforeMethod
  public void setUp() throws Exception {
    WorkspaceImpl workspace = mock(WorkspaceImpl.class);
    WorkspaceConfigImpl workspaceConfig = mock(WorkspaceConfigImpl.class);
    when(workspaceDao.get(WS_ID)).thenReturn(workspace);
    when(workspace.getConfig()).thenReturn(workspaceConfig);
    when(workspaceConfig.getName()).thenReturn(WS_NAME);

    Path tempDirectory = Files.createTempDirectory(getClass().getSimpleName());
    workspacesRoot = tempDirectory.toString();
    workspacesRootFile = tempDirectory.toFile();
    singleFolderForAllWorkspaces =
        Paths.get(workspacesRoot, "singleFolderForAllWorkspaces").toString();
    oldWorkspacesRoot = Paths.get(workspacesRoot, "oldWorkspacesRoot").toString();
  }

  @Test
  public void createsFoldersByDefault() throws Exception {
    assertTrue(workspacesRootFile.delete());

    LocalProjectsFolderPathProvider provider =
        new LocalProjectsFolderPathProvider(workspacesRoot, workspaceDao);

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
            null,
            true, // <- Create folder if it doesn't exist
            workspaceDao,
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
    assertTrue(Paths.get(workspacesRoot, WS_NAME).toFile().mkdir());
    LocalProjectsFolderPathProvider provider =
        new LocalProjectsFolderPathProvider(
            workspacesRoot,
            null,
            null,
            false, // <- Do not create folders
            workspaceDao,
            false);

    provider.init();

    assertTrue(workspacesRootFile.exists());
    assertTrue(workspacesRootFile.isDirectory());

    String providerPath = provider.getPath(WS_ID);

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
            null,
            false, // <- Do not create folders
            workspaceDao,
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
            singleFolderForAllWorkspaces,
            false,
            workspaceDao,
            true);

    provider.init();
    String providerPath = provider.getPath(WS_ID);

    assertEquals(
        providerPath, WindowsHostUtils.getCheHome().resolve("vfs").resolve(WS_NAME).toString());
  }

  @Test
  public void returnsSingleFolderForAllWorkspacesIfConfigured() throws Exception {
    LocalProjectsFolderPathProvider provider =
        new LocalProjectsFolderPathProvider(
            workspacesRoot,
            oldWorkspacesRoot,
            singleFolderForAllWorkspaces,
            false,
            workspaceDao,
            false);

    provider.init();
    String providerPath = provider.getPath(WS_ID);

    assertEquals(providerPath, singleFolderForAllWorkspaces);
  }

  @Test
  public void useOlderFolderIfConfigured() throws Exception {
    LocalProjectsFolderPathProvider provider =
        new LocalProjectsFolderPathProvider(
            workspacesRoot, oldWorkspacesRoot, null, false, workspaceDao, false);

    provider.init();
    String providerPath = provider.getPath(WS_ID);

    assertEquals(providerPath, Paths.get(oldWorkspacesRoot, WS_NAME).toString());
  }

  @Test(
    expectedExceptions = IOException.class,
    expectedExceptionsMessageRegExp = "Workspace folder '.*' is not directory"
  )
  public void throwsExceptionIfFileIsFoundByWorkspacesPath() throws Exception {
    assertTrue(Paths.get(workspacesRoot, WS_NAME).toFile().createNewFile());
    LocalProjectsFolderPathProvider provider =
        new LocalProjectsFolderPathProvider(workspacesRoot, null, null, true, workspaceDao, false);

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
            tempFile.toString(), null, null, true, workspaceDao, false);

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
            workspacesRoot, oldWorkspacesRoot, tempFile.toString(), true, workspaceDao, false);

    provider.init();
    provider.getPath(WS_ID);
  }

  @Test(
    expectedExceptions = IOException.class,
    expectedExceptionsMessageRegExp = "expected test exception"
  )
  public void throwsIOExceptionIfWorkspaceRetrievalFails() throws Exception {
    when(workspaceDao.get(WS_ID)).thenThrow(new ServerException("expected test exception"));
    LocalProjectsFolderPathProvider provider =
        new LocalProjectsFolderPathProvider(workspacesRoot, null, null, false, workspaceDao, true);

    provider.init();
    provider.getPath(WS_ID);
  }
}
