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

import static org.eclipse.che.api.fs.server.WsPathUtils.parentOf;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.model.workspace.config.ProjectConfig;
import org.eclipse.che.api.core.model.workspace.config.SourceStorage;
import org.eclipse.che.api.fs.server.FsManager;
import org.eclipse.che.api.project.shared.NewProjectConfig;
import org.eclipse.che.api.project.shared.RegisteredProject;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/** Test cases for {@link ValidatingProjectManager} */
@Listeners(MockitoTestNGListener.class)
public class ValidatingProjectManagerTest {

  private static final String WS_PATH = "/ws/path";
  private static final String PROJECT_TYPE = "project-type";
  private static final String SRC_WS_PATH = "/src/ws/path";
  private static final String DST_WS_PATH = "/dst/ws/path";

  @Mock private ExecutiveProjectManager executiveProjectManager;
  @Mock private FsManager fsManager;
  @InjectMocks private ValidatingProjectManager validatingProjectManager;

  @Mock private Map<ProjectConfig, Map<String, String>> projectConfigs;
  @Mock private ProjectConfig projectConfig;
  @Mock private RegisteredProject registeredProject;
  @Mock private NewProjectConfig newProjectConfig;
  @Mock private Map<String, String> projectOptions;
  @Mock private BiConsumer<String, String> consumer;
  @Mock private SourceStorage sourceStorage;

  @Test
  public void shouldCallIsRegistered() throws Exception {
    validatingProjectManager.isRegistered(WS_PATH);

    verify(executiveProjectManager).isRegistered(WS_PATH);
  }

  @Test
  public void shouldCallGet() throws Exception {
    validatingProjectManager.get(WS_PATH);

    verify(executiveProjectManager).get(WS_PATH);
  }

  @Test
  public void shouldCallGetClosest() throws Exception {
    validatingProjectManager.getClosest(WS_PATH);

    verify(executiveProjectManager).getClosest(WS_PATH);
  }

  @Test
  public void shouldCallGetOrNull() throws Exception {
    validatingProjectManager.getOrNull(WS_PATH);

    verify(executiveProjectManager).getOrNull(WS_PATH);
  }

  @Test
  public void shouldCallGetClosestOrNull() throws Exception {
    validatingProjectManager.getClosestOrNull(WS_PATH);

    verify(executiveProjectManager).getClosestOrNull(WS_PATH);
  }

  @Test
  public void shouldCallGetAll() throws Exception {
    validatingProjectManager.getAll();

    verify(executiveProjectManager).getAll();
  }

  @Test
  public void shouldCallGetAllWithPath() throws Exception {
    validatingProjectManager.getAll(WS_PATH);

    verify(executiveProjectManager).getAll(WS_PATH);
  }

  @Test
  public void shouldCallCreate() throws Exception {
    when(projectConfig.getPath()).thenReturn(WS_PATH);
    when(fsManager.existsAsDir(parentOf(WS_PATH))).thenReturn(true);
    when(projectConfig.getType()).thenReturn(PROJECT_TYPE);
    when(executiveProjectManager.get(WS_PATH)).thenReturn(Optional.empty());

    validatingProjectManager.create(projectConfig, projectOptions);

    verify(executiveProjectManager).create(projectConfig, projectOptions);
  }

  @Test(expectedExceptions = BadRequestException.class)
  public void shouldThrowBadRequestExceptionForCreateWithProjectConfigWithoutPath()
      throws Exception {
    when(projectConfig.getPath()).thenReturn(null);

    validatingProjectManager.create(projectConfig, projectOptions);
  }

  @Test(expectedExceptions = NotFoundException.class)
  public void shouldThrowNotFoundExceptionForCreateWithNotExistingParent() throws Exception {
    when(projectConfig.getPath()).thenReturn(WS_PATH);
    when(fsManager.existsAsDir(parentOf(WS_PATH))).thenReturn(false);

    validatingProjectManager.create(projectConfig, projectOptions);
  }

  @Test(expectedExceptions = ConflictException.class)
  public void shouldThrowConflictExceptionForCreateWithNotExistingType() throws Exception {
    when(projectConfig.getPath()).thenReturn(WS_PATH);
    when(fsManager.existsAsDir(parentOf(WS_PATH))).thenReturn(true);
    when(projectConfig.getType()).thenReturn(null);

    validatingProjectManager.create(projectConfig, projectOptions);
  }

  @Test(expectedExceptions = ConflictException.class)
  public void shouldThrowConflictExceptionForCreateWithAlreadyExistingProject() throws Exception {
    when(projectConfig.getPath()).thenReturn(WS_PATH);
    when(fsManager.existsAsDir(parentOf(WS_PATH))).thenReturn(true);
    when(projectConfig.getType()).thenReturn(PROJECT_TYPE);
    when(executiveProjectManager.get(WS_PATH)).thenReturn(Optional.of(registeredProject));

    validatingProjectManager.create(projectConfig, projectOptions);
  }

  @Test
  public void shouldCallUpdate() throws Exception {
    when(projectConfig.getPath()).thenReturn(WS_PATH);
    when(fsManager.existsAsDir(WS_PATH)).thenReturn(true);

    validatingProjectManager.update(projectConfig);

    verify(executiveProjectManager).update(projectConfig);
  }

  @Test(expectedExceptions = BadRequestException.class)
  public void shouldThrowBadRequestExceptionUpdateWithUndefinedProjectPath() throws Exception {
    when(projectConfig.getPath()).thenReturn(null);

    validatingProjectManager.update(projectConfig);
  }

  @Test(expectedExceptions = NotFoundException.class)
  public void shouldThrowNotFoundExceptionUpdateWithNotExistingParent() throws Exception {
    when(projectConfig.getPath()).thenReturn(WS_PATH);
    when(fsManager.existsAsDir(parentOf(WS_PATH))).thenReturn(false);

    validatingProjectManager.update(projectConfig);
  }

  @Test
  public void shouldCallDelete() throws Exception {
    validatingProjectManager.delete(WS_PATH);

    verify(executiveProjectManager).delete(WS_PATH);
  }

  @Test
  public void shouldCallDeleteAll() throws Exception {
    validatingProjectManager.deleteAll();

    verify(executiveProjectManager).deleteAll();
  }

  @Test
  public void shouldCallCopy() throws Exception {
    when(fsManager.existsAsDir(SRC_WS_PATH)).thenReturn(true);
    when(fsManager.existsAsDir(parentOf(DST_WS_PATH))).thenReturn(true);
    when(fsManager.exists(DST_WS_PATH)).thenReturn(false);
    when(executiveProjectManager.isRegistered(SRC_WS_PATH)).thenReturn(true);

    validatingProjectManager.copy(SRC_WS_PATH, DST_WS_PATH, false);

    verify(executiveProjectManager).copy(SRC_WS_PATH, DST_WS_PATH, false);
  }

  @Test(expectedExceptions = NotFoundException.class)
  public void shouldThrowNotFoundExceptionForCopyWhenSrcNotExists() throws Exception {
    when(fsManager.existsAsDir(SRC_WS_PATH)).thenReturn(false);

    validatingProjectManager.copy(SRC_WS_PATH, DST_WS_PATH, false);
  }

  @Test(expectedExceptions = NotFoundException.class)
  public void shouldThrowNotFoundExceptionForCopyWhenDstParentNotExists() throws Exception {
    when(fsManager.existsAsDir(SRC_WS_PATH)).thenReturn(true);
    when(fsManager.existsAsDir(parentOf(DST_WS_PATH))).thenReturn(false);

    validatingProjectManager.copy(SRC_WS_PATH, DST_WS_PATH, false);
  }

  @Test(expectedExceptions = ConflictException.class)
  public void shouldThrowConflictExceptionForCopyWhenDestinationItemExistsAndOverwriteIsFalse()
      throws Exception {
    when(fsManager.existsAsDir(SRC_WS_PATH)).thenReturn(true);
    when(fsManager.existsAsDir(parentOf(DST_WS_PATH))).thenReturn(true);
    when(fsManager.exists(DST_WS_PATH)).thenReturn(true);

    validatingProjectManager.copy(SRC_WS_PATH, DST_WS_PATH, false);
  }

  @Test(expectedExceptions = NotFoundException.class)
  public void shouldThrowNotFoundExceptionForCopyWhenProjectIsNotRegistered() throws Exception {
    when(fsManager.existsAsDir(SRC_WS_PATH)).thenReturn(true);
    when(fsManager.existsAsDir(parentOf(DST_WS_PATH))).thenReturn(true);
    when(fsManager.exists(DST_WS_PATH)).thenReturn(false);
    when(executiveProjectManager.isRegistered(SRC_WS_PATH)).thenReturn(false);

    validatingProjectManager.copy(SRC_WS_PATH, DST_WS_PATH, false);
  }

  @Test
  public void shouldNotThrowConflictExceptionForCopyWhenDestinationItemExistsAndOverwriteIsTrue()
      throws Exception {
    when(fsManager.existsAsDir(SRC_WS_PATH)).thenReturn(true);
    when(fsManager.existsAsDir(parentOf(DST_WS_PATH))).thenReturn(true);
    when(fsManager.exists(DST_WS_PATH)).thenReturn(true);
    when(executiveProjectManager.isRegistered(SRC_WS_PATH)).thenReturn(true);

    validatingProjectManager.copy(SRC_WS_PATH, DST_WS_PATH, true);
  }

  @Test
  public void shouldCallMove() throws Exception {
    when(fsManager.existsAsDir(SRC_WS_PATH)).thenReturn(true);
    when(fsManager.existsAsDir(parentOf(DST_WS_PATH))).thenReturn(true);
    when(fsManager.exists(DST_WS_PATH)).thenReturn(false);
    when(executiveProjectManager.isRegistered(SRC_WS_PATH)).thenReturn(true);

    validatingProjectManager.move(SRC_WS_PATH, DST_WS_PATH, false);

    verify(executiveProjectManager).move(SRC_WS_PATH, DST_WS_PATH, false);
  }

  @Test(expectedExceptions = NotFoundException.class)
  public void shouldThrowNotFoundExceptionForMoveWhenSrcNotExists() throws Exception {
    when(fsManager.existsAsDir(SRC_WS_PATH)).thenReturn(false);

    validatingProjectManager.move(SRC_WS_PATH, DST_WS_PATH, false);
  }

  @Test(expectedExceptions = NotFoundException.class)
  public void shouldThrowNotFoundExceptionForMoveWhenDstParentNotExists() throws Exception {
    when(fsManager.existsAsDir(SRC_WS_PATH)).thenReturn(true);
    when(fsManager.existsAsDir(parentOf(DST_WS_PATH))).thenReturn(false);

    validatingProjectManager.move(SRC_WS_PATH, DST_WS_PATH, false);
  }

  @Test(expectedExceptions = ConflictException.class)
  public void shouldThrowConflictExceptionForMoveWhenDestinationItemExistsAndOverwriteIsFalse()
      throws Exception {
    when(executiveProjectManager.isRegistered(SRC_WS_PATH)).thenReturn(true);
    when(fsManager.existsAsDir(SRC_WS_PATH)).thenReturn(true);
    when(fsManager.existsAsDir(parentOf(DST_WS_PATH))).thenReturn(true);
    when(fsManager.existsAsDir(DST_WS_PATH)).thenReturn(true);

    validatingProjectManager.move(SRC_WS_PATH, DST_WS_PATH, false);
  }

  @Test(expectedExceptions = NotFoundException.class)
  public void shouldThrowNotFoundExceptionForMoveWhenProjectIsNotRegistered() throws Exception {
    when(executiveProjectManager.isRegistered(SRC_WS_PATH)).thenReturn(false);

    validatingProjectManager.move(SRC_WS_PATH, DST_WS_PATH, false);
  }

  @Test
  public void shouldNotThrowConflictExceptionForMoveWhenDestinationItemExistsAndOverwriteIsTrue()
      throws Exception {
    when(executiveProjectManager.isRegistered(SRC_WS_PATH)).thenReturn(true);
    when(fsManager.existsAsDir(SRC_WS_PATH)).thenReturn(true);
    when(fsManager.existsAsDir(parentOf(DST_WS_PATH))).thenReturn(true);
    when(fsManager.exists(DST_WS_PATH)).thenReturn(true);

    validatingProjectManager.move(SRC_WS_PATH, DST_WS_PATH, true);
  }

  @Test
  public void shouldCallSetType() throws Exception {
    validatingProjectManager.setType(WS_PATH, PROJECT_TYPE, true);

    verify(executiveProjectManager).setType(WS_PATH, PROJECT_TYPE, true);
  }

  @Test
  public void shouldCallRemove() throws Exception {
    validatingProjectManager.removeType(WS_PATH, PROJECT_TYPE);

    verify(executiveProjectManager).removeType(WS_PATH, PROJECT_TYPE);
  }

  @Test
  public void shouldCallQualifyForProjectType() throws Exception {
    validatingProjectManager.verify(WS_PATH, PROJECT_TYPE);

    verify(executiveProjectManager).verify(WS_PATH, PROJECT_TYPE);
  }

  @Test
  public void shouldCallQualify() throws Exception {
    validatingProjectManager.recognize(WS_PATH);

    verify(executiveProjectManager).recognize(WS_PATH);
  }
}
