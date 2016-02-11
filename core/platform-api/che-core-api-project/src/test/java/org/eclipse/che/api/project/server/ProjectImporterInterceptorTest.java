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

import org.aopalliance.intercept.MethodInvocation;
import org.eclipse.che.api.core.model.project.SourceStorage;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Dmitry Shnurenko
 */
@RunWith(MockitoJUnitRunner.class)
public class ProjectImporterInterceptorTest {

    private static final String WORKSPACE_ID    = "workspace_id";
    private static final String PATH_TO_PROJECT = "/projects/name";
    private static final String PROJECT_PATH    = "/name";

    @Mock
    private ProjectManager   projectManager;
    @Mock
    private MethodInvocation invocation;
    @Mock
    private FolderEntry      folderEntry;
    @Mock
    private SourceStorage    sourceStorage;

    @Captor
    private ArgumentCaptor<ProjectConfigDto> blankProjectCaptor;

    @InjectMocks
    private ProjectImporterInterceptor interceptor;

    @Before
    public void setUp() {
        Object[] methodParameters = new Object[]{folderEntry, sourceStorage};

        when(invocation.getArguments()).thenReturn(methodParameters);
        when(folderEntry.getWorkspace()).thenReturn(WORKSPACE_ID);
        when(folderEntry.getPath()).thenReturn(PATH_TO_PROJECT);
    }

    @Test
    public void projectInWorkspaceShouldBeFoundAndMethodReturnsControl() throws Throwable {
        ProjectConfigDto project = mock(ProjectConfigDto.class);
        when(projectManager.getProjectFromWorkspace(anyString(), anyString())).thenReturn(project);

        interceptor.invoke(invocation);

        verify(invocation).proceed();
        verify(projectManager).getProjectFromWorkspace(WORKSPACE_ID, PROJECT_PATH);
        verify(sourceStorage, never()).getLocation();
        verify(projectManager, never()).convertFolderToProject(eq(WORKSPACE_ID), eq(PROJECT_PATH), blankProjectCaptor.capture());
    }

    @Test
    public void projectInWorkspaceShouldNotBeFoundAndProjectShouldBeConfiguredAsBlank() throws Throwable {
        interceptor.invoke(invocation);

        verify(invocation).proceed();
        verify(projectManager).getProjectFromWorkspace(WORKSPACE_ID, PROJECT_PATH);

        verify(sourceStorage).getLocation();
        verify(sourceStorage).getType();
        verify(sourceStorage).getParameters();

        verify(projectManager).convertFolderToProject(eq(WORKSPACE_ID), eq(PROJECT_PATH), blankProjectCaptor.capture());
        ProjectConfigDto blankProject = blankProjectCaptor.getValue();

        assertThat(blankProject.getName(), is(equalTo("name")));
        assertThat(blankProject.getPath(), is(equalTo(PROJECT_PATH)));
        assertThat(blankProject.getType(), is(equalTo("blank")));
    }
}