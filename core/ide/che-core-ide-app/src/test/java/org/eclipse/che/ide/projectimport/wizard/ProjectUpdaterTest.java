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

import org.eclipse.che.api.project.gwt.client.ProjectServiceClient;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.event.project.CreateProjectEvent;
import org.eclipse.che.ide.api.project.wizard.ProjectNotificationSubscriber;
import org.eclipse.che.ide.api.wizard.Wizard;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.rest.Unmarshallable;
import org.eclipse.che.test.GwtReflectionUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.eq;
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

    //additional mocks
    @Mock
    private ProjectConfigDto                 projectConfig;
    @Mock
    private Unmarshallable<ProjectConfigDto> projectUnmarshallable;
    @Mock
    private Wizard.CompleteCallback          completeCallback;

    @Captor
    private ArgumentCaptor<AsyncRequestCallback<ProjectConfigDto>> asyncRequestCallback;

    private ProjectUpdater updater;

    @Before
    public void setUp() {
        when(appContext.getWorkspaceId()).thenReturn(WORKSPACE_ID);
        when(projectConfig.getName()).thenReturn(PROJECT_NAME);
        when(dtoUnmarshallerFactory.newUnmarshaller(ProjectConfigDto.class)).thenReturn(projectUnmarshallable);

        updater = new ProjectUpdater(dtoUnmarshallerFactory,
                                     projectServiceClient,
                                     projectNotificationSubscriber,
                                     eventBus,
                                     appContext);
    }

    @Test
    public void projectShouldBeUpdated() {
        updater.updateProject(completeCallback, projectConfig, true);

        verify(projectConfig).getName();
        verify(dtoUnmarshallerFactory).newUnmarshaller(ProjectConfigDto.class);

        verify(projectServiceClient).updateProject(eq(WORKSPACE_ID),
                                                   eq('/' + PROJECT_NAME),
                                                   eq(projectConfig),
                                                   asyncRequestCallback.capture());
        GwtReflectionUtils.callOnSuccess(asyncRequestCallback.getValue(), projectConfig);

        verify(eventBus, times(2)).fireEvent(Matchers.<CreateProjectEvent>anyObject());
        verify(projectNotificationSubscriber).onSuccess();
        verify(completeCallback).onCompleted();
    }
}