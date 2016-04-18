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
package org.eclipse.che.ide.projectimport.wizard;

import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.machine.gwt.client.DevMachine;
import org.eclipse.che.api.project.gwt.client.ProjectServiceClient;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.event.project.CreateProjectEvent;
import org.eclipse.che.ide.api.event.project.ProjectUpdatedEvent;
import org.eclipse.che.ide.api.project.wizard.ProjectNotificationSubscriber;
import org.eclipse.che.ide.api.wizard.Wizard;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.singletonList;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.RETURNS_DEFAULTS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Dmitry Shnurenko
 */
@RunWith(MockitoJUnitRunner.class)
public class ProjectUpdaterTest {
    private static final String WORKSPACE_ID = "id";
    private static final String PROJECT_NAME = "name";

    //constructor mocks
    @Mock
    private DtoUnmarshallerFactory        dtoUnmarshallerFactory;
    @Mock
    private ProjectServiceClient          projectServiceClient;
    @Mock
    private ProjectNotificationSubscriber projectNotificationSubscriber;
    @Mock
    private EventBus                      eventBus;
    @Mock
    private AppContext                    appContext;
    @Mock
    private DevMachine                    devMachine;

    //additional mocks
    @Mock
    private ProjectConfigDto        projectConfig;
    @Mock
    private Wizard.CompleteCallback completeCallback;
    @Mock
    private WorkspaceDto            usersWorkspaceDtoMock;
    @Mock
    private WorkspaceConfigDto      workspaceConfigDtoMock;

    private Promise<ProjectConfigDto> getUpdatedProjectMock;

    private Promise<List<ProjectConfigDto>> getProjectsMock;

    @Captor
    private ArgumentCaptor<Operation<ProjectConfigDto>> getUpdatedProjectCaptor;

    @Captor
    private ArgumentCaptor<Operation<List<ProjectConfigDto>>> getProjectsCaptor;

    @Captor
    private ArgumentCaptor<Operation<PromiseError>> errorOperationCaptor;

    @Captor
    private ArgumentCaptor<AsyncRequestCallback<ProjectConfigDto>> asyncRequestCallback;

    private ProjectUpdater updater;

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() {
        when(appContext.getWorkspaceId()).thenReturn(WORKSPACE_ID);
        when(appContext.getWorkspace()).thenReturn(usersWorkspaceDtoMock);
        when(appContext.getDevMachine()).thenReturn(devMachine);
        when(devMachine.getWsAgentBaseUrl()).thenReturn("/ext");
        when(usersWorkspaceDtoMock.getConfig()).thenReturn(workspaceConfigDtoMock);
        when(projectConfig.getName()).thenReturn(PROJECT_NAME);

        getUpdatedProjectMock = createPromiseMock();
        getProjectsMock = createPromiseMock();

        when(projectServiceClient.updateProject(eq(devMachine), anyString(), any(ProjectConfigDto.class))).thenReturn(getUpdatedProjectMock);
        when(projectServiceClient.getProjects(eq(devMachine))).thenReturn(getProjectsMock);

        updater = new ProjectUpdater(projectServiceClient,
                                     projectNotificationSubscriber,
                                     eventBus,
                                     appContext);
    }

    private Promise createPromiseMock() {
        return mock(Promise.class, new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                if (invocation.getMethod().getReturnType().isInstance(invocation.getMock())) {
                    return invocation.getMock();
                }
                return RETURNS_DEFAULTS.answer(invocation);
            }
        });
    }

    @Test
    public void projectShouldBeUpdatedWhenConfigurationIsRequired() throws Exception {
        updater.updateProject(completeCallback, projectConfig, true);

        verify(getUpdatedProjectMock).then(getUpdatedProjectCaptor.capture());
        getUpdatedProjectCaptor.getValue().apply(projectConfig);

        verify(projectConfig).getName();

        verify(projectServiceClient).updateProject(eq(devMachine),
                                                   eq('/' + PROJECT_NAME),
                                                   eq(projectConfig));

        verify(getProjectsMock).then(getProjectsCaptor.capture());
        getProjectsCaptor.getValue().apply(singletonList(projectConfig));

        verify(projectServiceClient).getProjects(eq(devMachine));
        verify(appContext).getWorkspace();
        verify(usersWorkspaceDtoMock).getConfig();
        verify(workspaceConfigDtoMock).withProjects(eq(newArrayList(projectConfig)));

        verify(eventBus, times(2)).fireEvent(Matchers.<CreateProjectEvent>anyObject());
        verify(projectNotificationSubscriber).onSuccess();
        verify(completeCallback).onCompleted();
    }

    @Test
    public void projectShouldBeUpdatedWhenConfigurationIsNotRequired() throws Exception {
        updater.updateProject(completeCallback, projectConfig, false);

        verify(getUpdatedProjectMock).then(getUpdatedProjectCaptor.capture());
        getUpdatedProjectCaptor.getValue().apply(projectConfig);

        verify(projectConfig).getName();

        verify(projectServiceClient).updateProject(eq(devMachine),
                                                   eq('/' + PROJECT_NAME),
                                                   eq(projectConfig));

        verify(getProjectsMock).then(getProjectsCaptor.capture());
        getProjectsCaptor.getValue().apply(singletonList(projectConfig));

        verify(eventBus).fireEvent(Matchers.<ProjectUpdatedEvent>anyObject());
        verify(projectNotificationSubscriber).onSuccess();
        verify(completeCallback).onCompleted();
    }
}
