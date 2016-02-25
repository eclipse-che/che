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
package org.eclipse.che.ide.project.node;

import com.google.common.collect.Sets;
import com.google.web.bindery.event.shared.Event;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.project.gwt.client.ProjectServiceClient;
import org.eclipse.che.api.project.shared.dto.ItemReference;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.api.workspace.shared.dto.UsersWorkspaceDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.project.node.Node;
import org.eclipse.che.ide.api.project.node.settings.NodeSettings;
import org.eclipse.che.ide.api.project.node.settings.SettingsProvider;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.project.node.factory.NodeFactory;
import org.eclipse.che.ide.project.node.icon.NodeIconProvider;
import org.eclipse.che.ide.project.shared.NodesResources;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for the {@link NodeManager}.
 *
 * @author Vlad Zhukovskyi
 */
@RunWith(MockitoJUnitRunner.class)
public class NodeManagerTest {

    @Mock
    NodeFactory nodeFactoryMock;

    @Mock
    ProjectServiceClient projectServiceClientMock;

    @Mock
    DtoUnmarshallerFactory dtoUnmarshallerFactoryMock;

    @Mock
    NodesResources nodesResourcesMock;

    @Mock
    SettingsProvider settingsProviderMock;

    @Mock
    DtoFactory dtoFactoryMock;

    @Mock
    NodeIconProvider nodeIconProviderMock;

    @Mock
    AppContext appContextMock;

    @Mock
    EventBus eventBusMock;

    @Mock
    UsersWorkspaceDto usersWorkspaceDtoMock;

    @Mock
    Promise<List<ProjectConfigDto>> projectConfigsMock;

    @Captor
    ArgumentCaptor<Function<List<ProjectConfigDto>, List<Node>>> getProjectsCaptor;

    private NodeManager nodeManager;

    @Before
    public void setUp() throws Exception {
        when(appContextMock.getWorkspace()).thenReturn(usersWorkspaceDtoMock);
        when(usersWorkspaceDtoMock.getId()).thenReturn("dummy");

        nodeManager = new NodeManager(nodeFactoryMock,
                                      projectServiceClientMock,
                                      dtoUnmarshallerFactoryMock,
                                      nodesResourcesMock,
                                      settingsProviderMock,
                                      dtoFactoryMock,
                                      Sets.newHashSet(nodeIconProviderMock),
                                      appContextMock,
                                      eventBusMock);
    }

    @After
    public void tearDown() throws Exception {
        nodeManager = null;
    }

    @Test
    public void testShouldCheckEventSubscription() throws Exception {
        verify(eventBusMock, times(3)).addHandler(any(Event.Type.class), any());
    }

    @Test
    public void testShouldReturnProjectNode() throws Exception {
        final ItemReference itemReferenceMock = mock(ItemReference.class);
        final NodeSettings nodeSettingsMock = mock(NodeSettings.class);
        final ProjectConfigDto projectConfigMock = mock(ProjectConfigDto.class);
        final WorkspaceConfigDto workspaceConfigDtoMock = mock(WorkspaceConfigDto.class);
        final UsersWorkspaceDto usersWorkspaceDtoMock = mock(UsersWorkspaceDto.class);

        when(itemReferenceMock.getType()).thenReturn("project");
        when(appContextMock.getWorkspace()).thenReturn(usersWorkspaceDtoMock);
        when(usersWorkspaceDtoMock.getConfig()).thenReturn(workspaceConfigDtoMock);
        when(workspaceConfigDtoMock.getProjects()).thenReturn(Collections.singletonList(projectConfigMock));
        when(projectConfigMock.getPath()).thenReturn("/path");
        when(itemReferenceMock.getPath()).thenReturn("/path");

        nodeManager.createNodeByType(itemReferenceMock, null, nodeSettingsMock);

        verify(nodeFactoryMock).newProjectNode(eq(projectConfigMock), eq(nodeSettingsMock));
        verify(nodeFactoryMock, never()).newFolderReferenceNode(any(ItemReference.class), any(ProjectConfigDto.class), any(NodeSettings.class));
    }

    @Test
    public void testShouldReturnFolderNodeIfProjectWasNotFound() throws Exception {
        final ItemReference itemReferenceMock = mock(ItemReference.class);
        final NodeSettings nodeSettingsMock = mock(NodeSettings.class);
        final ProjectConfigDto projectConfigMock = mock(ProjectConfigDto.class);
        final WorkspaceConfigDto workspaceConfigDtoMock = mock(WorkspaceConfigDto.class);
        final UsersWorkspaceDto usersWorkspaceDtoMock = mock(UsersWorkspaceDto.class);

        when(itemReferenceMock.getType()).thenReturn("project");
        when(appContextMock.getWorkspace()).thenReturn(usersWorkspaceDtoMock);
        when(usersWorkspaceDtoMock.getConfig()).thenReturn(workspaceConfigDtoMock);
        when(workspaceConfigDtoMock.getProjects()).thenReturn(Collections.singletonList(projectConfigMock));
        when(projectConfigMock.getPath()).thenReturn("/path1");
        when(itemReferenceMock.getPath()).thenReturn("/path2");

        nodeManager.createNodeByType(itemReferenceMock, null, nodeSettingsMock);

        verify(nodeFactoryMock, never()).newProjectNode(any(ProjectConfigDto.class), any(NodeSettings.class));
        verify(nodeFactoryMock).newFolderReferenceNode(eq(itemReferenceMock), isNull(ProjectConfigDto.class), eq(nodeSettingsMock));
    }

    @Test
    public void testShouldReturnFolderNode() throws Exception {
        final ItemReference itemReferenceMock = mock(ItemReference.class);
        final NodeSettings nodeSettingsMock = mock(NodeSettings.class);
        final WorkspaceConfigDto workspaceConfigMock = mock(WorkspaceConfigDto.class);
        final UsersWorkspaceDto usersWorkspaceDtoMock = mock(UsersWorkspaceDto.class);

        when(itemReferenceMock.getType()).thenReturn("folder");
        when(appContextMock.getWorkspace()).thenReturn(usersWorkspaceDtoMock);
        when(usersWorkspaceDtoMock.getConfig()).thenReturn(workspaceConfigMock);
        when(workspaceConfigMock.getProjects()).thenReturn(Collections.<ProjectConfigDto>emptyList());

        nodeManager.createNodeByType(itemReferenceMock, null, nodeSettingsMock);

        verify(nodeFactoryMock, never()).newProjectNode(any(ProjectConfigDto.class), any(NodeSettings.class));
        verify(nodeFactoryMock).newFolderReferenceNode(eq(itemReferenceMock), isNull(ProjectConfigDto.class), eq(nodeSettingsMock));
    }

    @Test
    public void testShouldReturnFileNode() throws Exception {
        final ItemReference itemReferenceMock = mock(ItemReference.class);
        final NodeSettings nodeSettingsMock = mock(NodeSettings.class);

        when(itemReferenceMock.getType()).thenReturn("file");

        nodeManager.createNodeByType(itemReferenceMock, null, nodeSettingsMock);

        verify(nodeFactoryMock).newFileReferenceNode(eq(itemReferenceMock), isNull(ProjectConfigDto.class), eq(nodeSettingsMock));
    }

    @Test
    public void testShouldReturnRootProjectNodesOnly() throws Exception {
        final ProjectConfigDto project_1 = mock(ProjectConfigDto.class);
        final ProjectConfigDto project_2 = mock(ProjectConfigDto.class);
        final ProjectConfigDto project_3 = mock(ProjectConfigDto.class);
        final WorkspaceConfigDto workspaceConfigDtoMock = mock(WorkspaceConfigDto.class);
        final UsersWorkspaceDto usersWorkspaceDtoMock = mock(UsersWorkspaceDto.class);
        final NodeSettings nodeSettingsMock = mock(NodeSettings.class);

        when(projectServiceClientMock.getProjects(anyString(), anyBoolean())).thenReturn(projectConfigsMock);
        when(appContextMock.getWorkspace()).thenReturn(usersWorkspaceDtoMock);
        when(usersWorkspaceDtoMock.getConfig()).thenReturn(workspaceConfigDtoMock);
        when(project_1.getPath()).thenReturn("/project_1/");
        when(project_2.getPath()).thenReturn("/project_1/some_path/");
        when(project_3.getPath()).thenReturn("/project_2/");
        when(settingsProviderMock.getSettings()).thenReturn(nodeSettingsMock);

        nodeManager.getProjectNodes();

        verify(projectConfigsMock).then(getProjectsCaptor.capture());
        getProjectsCaptor.getValue().apply(newArrayList(project_1, project_2, project_3));
        verify(nodeFactoryMock, times(2)).newProjectNode(any(ProjectConfigDto.class), eq(nodeSettingsMock));
        verify(workspaceConfigDtoMock).withProjects(eq(newArrayList(project_1, project_2, project_3)));
    }
}
