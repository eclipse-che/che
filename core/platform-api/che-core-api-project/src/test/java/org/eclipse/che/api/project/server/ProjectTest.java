/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.project.server;

import org.eclipse.che.api.core.model.workspace.ProjectConfig;
import org.eclipse.che.api.project.server.handlers.ProjectHandlerRegistry;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;

/**
 * @author Artem Zatsarynnyi
 */
@Listeners(value = {MockitoTestNGListener.class})
public class ProjectTest {

    private static final String PROJECT_WS   = "ws1";
    private static final String PROJECT_NAME = "p1";
    private static final String PROJECT_PATH = "/p1";
    private static final long   DATE         = 1441371506187L;

    @Mock
    private ProjectMisc    projectMiscMock;
    @Mock
    private ProjectConfig  projectConfigMock;
    @Mock
    private FolderEntry    baseFolder;
    @Mock
    private ProjectManager projectManager;

    private Project project;

    @BeforeMethod
    public void setUp() throws Exception {
        when(baseFolder.getWorkspace()).thenReturn(PROJECT_WS);

        when(projectManager.getProjectMisc(any(Project.class))).thenReturn(projectMiscMock);
        when(projectManager.getProjectConfig(any(Project.class))).thenReturn(projectConfigMock);

        project = new Project(baseFolder, projectManager);
    }

    @Test
    public void shouldReturnWorkspace() throws Exception {
        final String projectWs = project.getWorkspace();

        verify(baseFolder).getWorkspace();
        assertEquals(projectWs, PROJECT_WS);
    }

    @Test
    public void shouldReturnName() throws Exception {
        when(baseFolder.getName()).thenReturn(PROJECT_NAME);

        final String projectName = project.getName();

        verify(baseFolder).getName();
        assertEquals(projectName, PROJECT_NAME);
    }

    @Test
    public void shouldReturnPath() throws Exception {
        when(baseFolder.getPath()).thenReturn(PROJECT_PATH);

        final String projectPath = project.getPath();

        verify(baseFolder).getPath();
        assertEquals(projectPath, PROJECT_PATH);
    }

    @Test
    public void shouldReturnBaseFolder() throws Exception {
        final FolderEntry projectBaseFolder = project.getBaseFolder();

        assertSame(projectBaseFolder, baseFolder);
    }

    @Test
    public void shouldReturnCreationDate() throws Exception {
        when(projectMiscMock.getCreationDate()).thenReturn(DATE);

        final long projectCreationDate = project.getCreationDate();

        verify(projectMiscMock).getCreationDate();
        assertEquals(projectCreationDate, DATE);
    }

    @Test
    public void shouldReturnModificationDate() throws Exception {
        when(projectMiscMock.getModificationDate()).thenReturn(DATE);

        final long projectModificationDate = project.getModificationDate();

        verify(projectMiscMock).getModificationDate();
        assertEquals(projectModificationDate, DATE);
    }

    @Test
    public void shouldReturnMisc() throws Exception {
        final ProjectMisc projectMisc = project.getMisc();

        verify(projectManager).getProjectMisc(project);
        assertSame(projectMisc, projectMiscMock);
    }

    @Test
    public void shouldSaveMisc() throws Exception {
        final ProjectMisc projectMisc = mock(ProjectMisc.class);
        project.saveMisc(projectMisc);

        verify(projectManager).saveProjectMisc(project, projectMisc);
    }

    @Test
    public void shouldReturnConfig() throws Exception {
        final ProjectConfig projectConfig = project.getConfig();

        verify(projectManager).getProjectConfig(project);
        assertSame(projectConfig, projectConfigMock);
    }

    @Test
    public void shouldBeAbleToUpdateConfig() throws Exception {
        final ProjectConfig projectConfig = mock(ProjectConfig.class);
        project.updateConfig(projectConfig);

        verify(projectManager).updateProjectConfig(project, projectConfig);
    }

    @Test
    public void shouldReturnVirtualFile() throws Exception {
        final FolderEntry rootFolder = mock(FolderEntry.class);
        final VirtualFileEntry projectEntry = mock(VirtualFileEntry.class);
        final ProjectHandlerRegistry handlers = mock(ProjectHandlerRegistry.class);
        when(rootFolder.getChild(PROJECT_PATH)).thenReturn(projectEntry);
        when(projectManager.getProjectsRoot(anyString())).thenReturn(rootFolder);
        when(projectManager.getHandlers()).thenReturn(handlers);

        final VirtualFileEntry item = project.getItem(PROJECT_PATH);

        assertSame(item, projectEntry);
    }
}
