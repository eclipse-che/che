/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.project.server.impl;

import static java.util.Collections.singleton;
import static java.util.Collections.singletonMap;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.Map;
import java.util.function.BiConsumer;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.config.ProjectConfig;
import org.eclipse.che.api.core.model.workspace.config.SourceStorage;
import org.eclipse.che.api.project.shared.NewProjectConfig;
import org.eclipse.che.api.project.shared.RegisteredProject;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/** Test cases for {@link SynchronizingProjectManager} */
@Listeners(MockitoTestNGListener.class)
public class SynchronizingProjectManagerTest {

  private static final String WS_PATH = "/ws/path";
  private static final String PROJECT_TYPE = "project-type";
  private static final String SRC_WS_PATH = "/src/ws/path";
  private static final String DST_WS_PATH = "/dst/ws/path";

  @Mock private ExecutiveProjectManager executiveProjectManager;
  @Mock private WorkspaceProjectSynchronizer projectSynchronizer;
  @InjectMocks private SynchronizingProjectManager synchronizingProjectManager;

  @Mock private Map<ProjectConfig, Map<String, String>> projectConfigs;
  @Mock private ProjectConfig projectConfig;
  @Mock private RegisteredProject registeredProject;
  @Mock private NewProjectConfig newProjectConfig;
  @Mock private Map<String, String> projectOptions;
  @Mock private BiConsumer<String, String> consumer;
  @Mock private SourceStorage sourceStorage;

  @Test
  public void shouldCallIsRegistered() throws Exception {
    synchronizingProjectManager.isRegistered(WS_PATH);

    verify(executiveProjectManager).isRegistered(WS_PATH);
  }

  @Test
  public void shouldCallGet() throws Exception {
    synchronizingProjectManager.get(WS_PATH);

    verify(executiveProjectManager).get(WS_PATH);
  }

  @Test
  public void shouldCallGetClosest() throws Exception {
    synchronizingProjectManager.getClosest(WS_PATH);

    verify(executiveProjectManager).getClosest(WS_PATH);
  }

  @Test
  public void shouldCallGetOrNull() throws Exception {
    synchronizingProjectManager.getOrNull(WS_PATH);

    verify(executiveProjectManager).getOrNull(WS_PATH);
  }

  @Test
  public void shouldCallGetClosestOrNull() throws Exception {
    synchronizingProjectManager.getClosestOrNull(WS_PATH);

    verify(executiveProjectManager).getClosestOrNull(WS_PATH);
  }

  @Test
  public void shouldCallGetAll() throws Exception {
    synchronizingProjectManager.getAll();

    verify(executiveProjectManager).getAll();
  }

  @Test
  public void shouldCallGetAllWithPath() throws Exception {
    synchronizingProjectManager.getAll(WS_PATH);

    verify(executiveProjectManager).getAll(WS_PATH);
  }

  @Test
  public void shouldCallCreateAll() throws Exception {
    synchronizingProjectManager.createAll(projectConfigs);

    verify(executiveProjectManager).createAll(projectConfigs);
  }

  @Test(expectedExceptions = ServerException.class)
  public void shouldCallSynchronizeAfterCreateAll() throws Exception {
    doThrow(new ServerException("")).when(executiveProjectManager).createAll(projectConfigs);

    synchronizingProjectManager.createAll(projectConfigs);

    verify(projectSynchronizer).synchronize();
  }

  @Test
  public void shouldCallCreate() throws Exception {
    synchronizingProjectManager.create(projectConfig, projectOptions);

    verify(executiveProjectManager).create(projectConfig, projectOptions);
  }

  @Test(expectedExceptions = ServerException.class)
  public void shouldCallSynchronizeAfterCreate() throws Exception {
    doThrow(new ServerException(""))
        .when(executiveProjectManager)
        .create(projectConfig, projectOptions);

    synchronizingProjectManager.create(projectConfig, projectOptions);

    verify(projectSynchronizer).synchronize();
  }

  @Test
  public void shouldCallUpdateAll() throws Exception {
    synchronizingProjectManager.updateAll(Collections.singleton(projectConfig));

    verify(executiveProjectManager).updateAll(Collections.singleton(projectConfig));
  }

  @Test(expectedExceptions = ServerException.class)
  public void shouldCallSynchronizeAfterUpdateAll() throws Exception {
    doThrow(new ServerException(""))
        .when(executiveProjectManager)
        .updateAll(singleton(projectConfig));

    synchronizingProjectManager.updateAll(singleton(projectConfig));

    verify(projectSynchronizer).synchronize();
  }

  @Test
  public void shouldCallUpdate() throws Exception {
    synchronizingProjectManager.update(projectConfig);

    verify(executiveProjectManager).update(projectConfig);
  }

  @Test(expectedExceptions = ServerException.class)
  public void shouldCallSynchronizeAfterUpdate() throws Exception {
    doThrow(new ServerException("")).when(executiveProjectManager).update(projectConfig);

    synchronizingProjectManager.update(projectConfig);

    verify(projectSynchronizer).synchronize();
  }

  @Test
  public void shouldCallDeleteAllWithPath() throws Exception {
    synchronizingProjectManager.deleteAll(singleton(WS_PATH));

    verify(executiveProjectManager).deleteAll(singleton(WS_PATH));
  }

  @Test(expectedExceptions = ServerException.class)
  public void shouldCallSynchronizeAfterDeleteAllWithPath() throws Exception {
    doThrow(new ServerException("")).when(executiveProjectManager).deleteAll(singleton(WS_PATH));

    synchronizingProjectManager.deleteAll(singleton(WS_PATH));

    verify(projectSynchronizer).synchronize();
  }

  @Test
  public void shouldCallDelete() throws Exception {
    synchronizingProjectManager.delete(WS_PATH);

    verify(executiveProjectManager).delete(WS_PATH);
  }

  @Test(expectedExceptions = ServerException.class)
  public void shouldCallSynchronizeAfterDelete() throws Exception {
    doThrow(new ServerException("")).when(executiveProjectManager).delete(WS_PATH);

    synchronizingProjectManager.delete(WS_PATH);

    verify(projectSynchronizer).synchronize();
  }

  @Test
  public void shouldCallDeleteAll() throws Exception {
    synchronizingProjectManager.deleteAll();

    verify(executiveProjectManager).deleteAll();
  }

  @Test(expectedExceptions = ServerException.class)
  public void shouldCallSynchronizeAfterDeleteAll() throws Exception {
    doThrow(new ServerException("")).when(executiveProjectManager).deleteAll();

    synchronizingProjectManager.deleteAll();

    verify(projectSynchronizer).synchronize();
  }

  @Test
  public void shouldCallCopy() throws Exception {
    synchronizingProjectManager.copy(SRC_WS_PATH, DST_WS_PATH, false);

    verify(executiveProjectManager).copy(SRC_WS_PATH, DST_WS_PATH, false);
  }

  @Test(expectedExceptions = ServerException.class)
  public void shouldCallSynchronizeAfterCopy() throws Exception {
    doThrow(new ServerException(""))
        .when(executiveProjectManager)
        .copy(SRC_WS_PATH, DST_WS_PATH, false);

    synchronizingProjectManager.copy(SRC_WS_PATH, DST_WS_PATH, false);

    verify(projectSynchronizer).synchronize();
  }

  @Test
  public void shouldCallMove() throws Exception {
    synchronizingProjectManager.move(SRC_WS_PATH, DST_WS_PATH, false);

    verify(executiveProjectManager).move(SRC_WS_PATH, DST_WS_PATH, false);
  }

  @Test(expectedExceptions = ServerException.class)
  public void shouldCallSynchronizeAfterMove() throws Exception {
    doThrow(new ServerException(""))
        .when(executiveProjectManager)
        .move(SRC_WS_PATH, DST_WS_PATH, false);

    synchronizingProjectManager.move(SRC_WS_PATH, DST_WS_PATH, false);

    verify(projectSynchronizer).synchronize();
  }

  @Test
  public void shouldCallSetType() throws Exception {
    synchronizingProjectManager.setType(WS_PATH, PROJECT_TYPE, true);

    verify(executiveProjectManager).setType(WS_PATH, PROJECT_TYPE, true);
  }

  @Test(expectedExceptions = ServerException.class)
  public void shouldCallSynchronizeAfterSetType() throws Exception {
    doThrow(new ServerException(""))
        .when(executiveProjectManager)
        .setType(WS_PATH, PROJECT_TYPE, true);

    synchronizingProjectManager.setType(WS_PATH, PROJECT_TYPE, true);

    verify(projectSynchronizer).synchronize();
  }

  @Test
  public void shouldCallRemove() throws Exception {
    synchronizingProjectManager.removeType(WS_PATH, PROJECT_TYPE);

    verify(executiveProjectManager).removeType(WS_PATH, PROJECT_TYPE);
  }

  @Test(expectedExceptions = ServerException.class)
  public void shouldCallSynchronizeAfterRemoveType() throws Exception {
    doThrow(new ServerException(""))
        .when(executiveProjectManager)
        .removeType(WS_PATH, PROJECT_TYPE);

    synchronizingProjectManager.removeType(WS_PATH, PROJECT_TYPE);

    verify(projectSynchronizer).synchronize();
  }

  @Test
  public void shouldCallDoImportForProjectConfig() throws Exception {
    synchronizingProjectManager.doImport(newProjectConfig, false, consumer);

    verify(executiveProjectManager).doImport(newProjectConfig, false, consumer);
  }

  @Test
  public void shouldCallDoImportForProjectConfigs() throws Exception {
    synchronizingProjectManager.doImport(singleton(newProjectConfig), false, consumer);

    verify(executiveProjectManager).doImport(singleton(newProjectConfig), false, consumer);
  }

  @Test
  public void shouldCallDoImportForWsPathAndSourceStorage() throws Exception {
    synchronizingProjectManager.doImport(WS_PATH, sourceStorage, false, consumer);

    verify(executiveProjectManager).doImport(WS_PATH, sourceStorage, false, consumer);
  }

  @Test
  public void shouldCallDoImportForProjectLocations() throws Exception {
    Map<String, SourceStorage> projectLocations = singletonMap(WS_PATH, sourceStorage);

    synchronizingProjectManager.doImport(projectLocations, false, consumer);

    verify(executiveProjectManager).doImport(projectLocations, false, consumer);
  }

  @Test
  public void shouldCallQualifyForProjectType() throws Exception {
    synchronizingProjectManager.verify(WS_PATH, PROJECT_TYPE);

    verify(executiveProjectManager).verify(WS_PATH, PROJECT_TYPE);
  }

  @Test
  public void shouldCallQualify() throws Exception {
    synchronizingProjectManager.recognize(WS_PATH);

    verify(executiveProjectManager).recognize(WS_PATH);
  }
}
