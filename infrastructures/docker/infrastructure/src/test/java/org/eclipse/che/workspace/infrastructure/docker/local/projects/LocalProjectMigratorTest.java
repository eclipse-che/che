/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.docker.local.projects;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.api.workspace.server.spi.WorkspaceDao;
import org.eclipse.che.commons.lang.IoUtil;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/** @author Mihail Kuznyetsov */
@Listeners(value = {MockitoTestNGListener.class})
public class LocalProjectMigratorTest {
  private static final String WS_ID_PREFIX = "ws_id_";
  private static final String WS_NAME_PREFIX = "ws_name_";

  private LocalProjectsMigrator localProjectsMigrator;

  @Mock WorkspaceDao workspaceDao;

  private String workspacesRoot;
  private File workspacesRootFile;

  @BeforeMethod
  public void setUp() throws IOException {
    Path tempDirectory = Files.createTempDirectory(getClass().getSimpleName());
    workspacesRoot = tempDirectory.toString();
    workspacesRootFile = tempDirectory.toFile();

    localProjectsMigrator = new LocalProjectsMigrator(workspaceDao);
  }

  @AfterMethod
  public void tearDown() throws IOException {
    IoUtil.deleteRecursive(workspacesRootFile);
  }

  @Test
  public void shouldPerformMigrationOnSingleWorkspace() throws Exception {
    List<WorkspaceImpl> workspaces = createWorkspaces(1, "oldWorkspace", true);
    when(workspaceDao.getWorkspaces(anyBoolean(), anyInt(), anyLong()))
        .thenReturn(new Page<>(workspaces, 0, 1, 1));

    localProjectsMigrator.performMigration(workspacesRoot);

    Path oldWorkspacePath =
        Paths.get(workspacesRoot).resolve(WS_NAME_PREFIX + "ws_name_oldWorkspace1");
    Path newWorkspacePath = Paths.get(workspacesRoot).resolve(WS_ID_PREFIX + "oldWorkspace1");

    assertFalse(Files.exists(oldWorkspacePath));
    assertTrue(Files.exists(newWorkspacePath));
  }

  private List<WorkspaceImpl> createWorkspaces(
      int amount, String namePrefix, boolean oldProjectsLocation) throws IOException {
    List<WorkspaceImpl> result = new ArrayList<>();
    for (int i = 1; i <= amount; i++) {
      String workspaceName = "ws_name_" + namePrefix + i;
      String workspaceId = "ws_id_" + namePrefix + i;

      WorkspaceImpl workspace = mock(WorkspaceImpl.class);
      WorkspaceConfigImpl workspaceConfig = mock(WorkspaceConfigImpl.class);
      when(workspace.getId()).thenReturn(workspaceId);
      when(workspace.getConfig()).thenReturn(workspaceConfig);
      when(workspace.getConfig().getName()).thenReturn(workspaceName);

      result.add(workspace);

      String workspaceFolder = oldProjectsLocation ? workspaceName : workspaceId;
      Files.createDirectories(Paths.get(workspacesRoot).resolve(workspaceFolder));
      Files.createFile(Paths.get(workspacesRoot).resolve(workspaceFolder).resolve("pom.xml"));
    }

    return result;
  }
}
