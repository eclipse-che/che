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
package org.eclipse.che.api.project.server;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import javax.ws.rs.core.UriInfo;
import org.apache.commons.fileupload.FileItem;
import org.eclipse.che.api.project.server.impl.ProjectServiceApi;
import org.eclipse.che.api.project.server.impl.ProjectServiceApiFactory;
import org.eclipse.che.api.project.shared.dto.CopyOptions;
import org.eclipse.che.api.project.shared.dto.MoveOptions;
import org.eclipse.che.api.project.shared.dto.NewProjectConfigDto;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.api.workspace.shared.dto.SourceStorageDto;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/** Tests for {@link ProjectService} */
@Listeners(MockitoTestNGListener.class)
public class ProjectServiceTest {

  private static final String FILE_NAME = "file.name";
  private static final String PARENT_WS_PATH = "parent/ws/path";
  private static final String NEW_PARENT_WS_PATH = "new-parent-ws-path";
  private static final String WS_PATH = "ws/path";
  private static final String CLIENT_ID = "client-id";
  private static final String PROJECT_TYPE = "project-type";
  private static final String NAME = "name";
  private static final String TEXT = "text";

  private static final int MAX_ITEMS = 0;
  private static final int SKIP_COUNT = 0;
  private static final int DEPTH = 0;

  private static final boolean INCLUDE_FILES = false;
  private static final boolean SKIP_FIRST_LEVEL = false;
  private static final boolean FORCE = false;
  private static final boolean REWRITE = false;

  @Mock private ProjectServiceApiFactory projectServiceApiFactory;
  @InjectMocks private ProjectService projectService;

  @Mock private ProjectServiceApi projectServiceApi;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private UriInfo uriInfo;

  @Mock private ProjectConfigDto projectConfigDto;
  @Mock private List<NewProjectConfigDto> newProjectConfigDtos;
  @Mock private SourceStorageDto sourceStorageDto;
  @Mock private InputStream inputStream;
  @Mock private Iterator<FileItem> fileItemIterator;
  @Mock private CopyOptions copyOptions;
  @Mock private MoveOptions moveOptions;

  @BeforeMethod
  public void setUp() throws Exception {
    when(projectServiceApiFactory.create(any())).thenReturn(projectServiceApi);

    projectService.uriInfo = uriInfo;
  }

  @Test
  public void shouldGetProjects() throws Exception {
    projectService.getProjects();

    verify(projectServiceApi).getProjects();
  }

  @Test
  public void shouldCallGetProject() throws Exception {
    projectService.getProject(WS_PATH);

    verify(projectServiceApi).getProject(WS_PATH);
  }

  @Test
  public void shouldCallCreateProject() throws Exception {
    projectService.createProject(uriInfo, projectConfigDto);

    verify(projectServiceApi).createProject(uriInfo, projectConfigDto);
  }

  @Test
  public void shouldCallCreateBatchProjects() throws Exception {
    projectService.createBatchProjects(newProjectConfigDtos, REWRITE, CLIENT_ID);

    verify(projectServiceApi).createBatchProjects(newProjectConfigDtos, REWRITE, CLIENT_ID);
  }

  @Test
  public void shouldCallUpdatePoject() throws Exception {
    projectService.updateProject(WS_PATH, projectConfigDto);

    verify(projectServiceApi).updateProject(WS_PATH, projectConfigDto);
  }

  @Test
  public void shouldCallDelete() throws Exception {
    projectService.delete(WS_PATH);

    verify(projectServiceApi).delete(WS_PATH);
  }

  @Test
  public void shouldCallEstimateProject() throws Exception {
    projectService.estimateProject(WS_PATH, PROJECT_TYPE);

    verify(projectServiceApi).estimateProject(WS_PATH, PROJECT_TYPE);
  }

  @Test
  public void shouldCallResolveSources() throws Exception {
    projectService.resolveSources(WS_PATH);

    verify(projectServiceApi).resolveSources(WS_PATH);
  }

  @Test
  public void shouldCallImportProject() throws Exception {
    projectService.importProject(WS_PATH, FORCE, CLIENT_ID, sourceStorageDto);

    verify(projectServiceApi).importProject(WS_PATH, FORCE, CLIENT_ID, sourceStorageDto);
  }

  @Test
  public void shouldCallCreateFile() throws Exception {
    projectService.createFile(PARENT_WS_PATH, FILE_NAME, inputStream);

    verify(projectServiceApi).createFile(PARENT_WS_PATH, FILE_NAME, inputStream);
  }

  @Test
  public void shouldCallCreateFolder() throws Exception {
    projectService.createFolder(WS_PATH);

    verify(projectServiceApi).createFolder(WS_PATH);
  }

  @Test
  public void shouldCallUploadFile() throws Exception {
    projectService.uploadFile(PARENT_WS_PATH, fileItemIterator);

    verify(projectServiceApi).uploadFile(PARENT_WS_PATH, fileItemIterator);
  }

  @Test
  public void shouldCallUploadFolderFromZip() throws Exception {
    projectService.uploadFolderFromZip(PARENT_WS_PATH, fileItemIterator);

    verify(projectServiceApi).uploadFolderFromZip(PARENT_WS_PATH, fileItemIterator);
  }

  @Test
  public void shouldCallGetFile() throws Exception {
    projectService.getFile(WS_PATH);

    verify(projectServiceApi).getFile(WS_PATH);
  }

  @Test
  public void shouldCallUpdateFile() throws Exception {
    projectService.updateFile(WS_PATH, inputStream);

    verify(projectServiceApi).updateFile(WS_PATH, inputStream);
  }

  @Test
  public void shouldCallCopyFile() throws Exception {
    projectService.copy(WS_PATH, NEW_PARENT_WS_PATH, copyOptions);

    verify(projectServiceApi).copy(WS_PATH, NEW_PARENT_WS_PATH, copyOptions);
  }

  @Test
  public void shouldCallMoveFile() throws Exception {
    projectService.move(WS_PATH, NEW_PARENT_WS_PATH, moveOptions);

    verify(projectServiceApi).move(WS_PATH, NEW_PARENT_WS_PATH, moveOptions);
  }

  @Test
  public void shouldCallUploadProjectFromZip() throws Exception {
    projectService.uploadProjectFromZip(WS_PATH, FORCE, fileItemIterator);

    verify(projectServiceApi).uploadProjectFromZip(WS_PATH, FORCE, fileItemIterator);
  }

  @Test
  public void shouldCallImportZip() throws Exception {
    projectService.importZip(WS_PATH, inputStream, SKIP_FIRST_LEVEL);

    verify(projectServiceApi).importZip(WS_PATH, inputStream, SKIP_FIRST_LEVEL);
  }

  @Test
  public void shouldCallExportZip() throws Exception {
    projectService.exportZip(WS_PATH);

    verify(projectServiceApi).exportZip(WS_PATH);
  }

  @Test
  public void shouldCallExportFile() throws Exception {
    projectService.exportFile(WS_PATH);

    verify(projectServiceApi).exportFile(WS_PATH);
  }

  @Test
  public void shouldCallGetChildren() throws Exception {
    projectService.getChildren(WS_PATH);

    verify(projectServiceApi).getChildren(WS_PATH);
  }

  @Test
  public void shouldCallGetTree() throws Exception {
    projectService.getTree(WS_PATH, DEPTH, INCLUDE_FILES);

    verify(projectServiceApi).getTree(WS_PATH, DEPTH, INCLUDE_FILES);
  }

  @Test
  public void shouldCallGetItem() throws Exception {
    projectService.getItem(WS_PATH);

    verify(projectServiceApi).getItem(WS_PATH);
  }

  @Test
  public void shouldCallSearch() throws Exception {
    projectService.search(WS_PATH, NAME, TEXT, MAX_ITEMS, SKIP_COUNT);

    verify(projectServiceApi).search(WS_PATH, NAME, TEXT, MAX_ITEMS, SKIP_COUNT);
  }
}
