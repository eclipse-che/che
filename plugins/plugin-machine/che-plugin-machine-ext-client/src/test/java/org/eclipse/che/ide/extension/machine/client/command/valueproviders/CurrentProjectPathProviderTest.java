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
package org.eclipse.che.ide.extension.machine.client.command.valueproviders;

import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.machine.gwt.client.MachineServiceClient;
import org.eclipse.che.api.machine.shared.dto.MachineConfigDto;
import org.eclipse.che.api.machine.shared.dto.MachineDto;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentProject;
import org.eclipse.che.ide.api.event.project.CloseCurrentProjectEvent;
import org.eclipse.che.ide.api.event.project.ProjectReadyEvent;
import org.eclipse.che.ide.extension.machine.client.machine.MachineStateEvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** @author Artem Zatsarynnyi */
@RunWith(MockitoJUnitRunner.class)
public class CurrentProjectPathProviderTest {

    private static final String PROJECTS_ROOT = "/projects";
    private static final String PROJECT_PATH  = "/my_project";

    @Mock
    private EventBus             eventBus;
    @Mock
    private AppContext           appContext;
    @Mock
    private MachineServiceClient machineServiceClient;

    @InjectMocks
    private CurrentProjectPathProvider currentProjectPathProvider;

    @Mock
    private Promise<MachineDto>                   machinePromise;
    @Captor
    private ArgumentCaptor<Operation<MachineDto>> machineCaptor;

    @Before
    public void setUp() {
        CurrentProject currentProjectMock = mock(CurrentProject.class);
        when(appContext.getCurrentProject()).thenReturn(currentProjectMock);

        ProjectConfigDto projectConfig = mock(ProjectConfigDto.class);
        when(projectConfig.getPath()).thenReturn(PROJECT_PATH);
        when(currentProjectMock.getProjectConfig()).thenReturn(projectConfig);
    }

//    @Test
    //TODO: temporary skip
    public void shouldReturnPathAfterRunningMachine() throws Exception {
        MachineDto machineMock = mock(MachineDto.class);
        MachineStateEvent machineStateEvent = mock(MachineStateEvent.class);
        CurrentProject currentProject = mock(CurrentProject.class);
        ProjectConfigDto projectConfig = mock(ProjectConfigDto.class);

        when(appContext.getCurrentProject()).thenReturn(currentProject);
        when(currentProject.getProjectConfig()).thenReturn(projectConfig);

        final MachineConfigDto machineConfigMock = mock(MachineConfigDto.class);
        when(machineConfigMock.isDev()).thenReturn(Boolean.TRUE);
        when(machineMock.getConfig()).thenReturn(machineConfigMock);
        when(machineStateEvent.getMachine()).thenReturn(machineMock);


        verify(appContext, times(2)).getCurrentProject();
        verify(currentProject).getProjectConfig();
        verify(projectConfig).getPath();
    }

    @Test
    public void shouldReturnEmptyValueAfterDestroyingMachine() throws Exception {
        final MachineDto machineMock = mock(MachineDto.class);
        final MachineConfigDto machineConfigMock = mock(MachineConfigDto.class);
        when(machineConfigMock.isDev()).thenReturn(Boolean.FALSE);
        when(machineMock.getConfig()).thenReturn(machineConfigMock);
        final MachineStateEvent machineStateEvent = mock(MachineStateEvent.class);
        when(machineStateEvent.getMachine()).thenReturn(machineMock);


        assertTrue(currentProjectPathProvider.getValue().isEmpty());
    }

}
