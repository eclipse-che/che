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
package org.eclipse.che.ide.extension.machine.client.machine;

import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.core.model.machine.MachineConfig;
import org.eclipse.che.api.core.model.machine.MachineSource;
import org.eclipse.che.api.machine.shared.dto.MachineLimitsDto;
import org.eclipse.che.api.machine.shared.dto.MachineConfigDto;
import org.eclipse.che.api.machine.shared.dto.MachineSourceDto;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.machine.DevMachine;
import org.eclipse.che.ide.api.machine.MachineEntity;
import org.eclipse.che.ide.api.machine.MachineServiceClient;
import org.eclipse.che.ide.api.machine.events.MachineStateEvent;
import org.eclipse.che.ide.api.parts.PerspectiveManager;
import org.eclipse.che.ide.api.workspace.WorkspaceServiceClient;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.websocket.MessageBusProvider;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Check {@link MachineManagerImpl}
 *
 * @author Florent Benoit
 */
@RunWith(MockitoJUnitRunner.class)
public class MachineManagerImplTest {

    @Mock
    private DtoUnmarshallerFactory dtoUnmarshallerFactor;

    @Mock
    private MachineServiceClient machineServiceClient;

    @Mock
    private WorkspaceServiceClient workspaceServiceClient;

    @Mock
    private MachineStatusHandler machineStatusHandler;

    @Mock
    private MessageBusProvider messageBusProvider;

    @Mock
    private PerspectiveManager perspectiveManager;

    @Mock
    private EventBus eventBus;

    @Mock
    private AppContext appContext;

    @Mock
    private DtoFactory dtoFactory;

    @Captor
    private ArgumentCaptor<MachineStateEvent.Handler> startWorkspaceHandlerCaptor;

    @Captor
    private ArgumentCaptor<Operation<Void>> operationArgumentCaptor;

    @Captor
    private ArgumentCaptor<MachineConfigDto> machineConfigDtoArgumentCaptor;


    @InjectMocks
    private MachineManagerImpl machineManager;

    /**
     * Check a valid source object is used on machine destroyed with restart flag
     *
     * @throws OperationException
     *         if restart fails
     */
    @Test
    public void checkUseValidSource() throws OperationException {
        final String ID = "id";
        final String WORKSPACE_ID = "testWorkspaceId";
        final String DISPLAY_NAME = "my-display-name";
        final boolean IS_DEV = true;

        final String SOURCE_TYPE = "source-type";
        final String SOURCE_LOCATION = "source-location";
        final String SOURCE_CONTENT = "source-content";

        MachineEntity machineState = mock(MachineEntity.class);
        when(machineState.getId()).thenReturn(ID);
        when(machineState.getWorkspaceId()).thenReturn(WORKSPACE_ID);
        Promise<Void> promise = mock(Promise.class);
        Promise<Void> promiseThen = mock(Promise.class);
        when(machineServiceClient.destroyMachine(eq(WORKSPACE_ID), eq(ID))).thenReturn(promise);
        when(promise.then(Matchers.<Operation<Void>>anyObject())).thenReturn(promiseThen);

        MachineSource machineSource = mock(MachineSource.class);
        MachineConfig machineConfig = mock(MachineConfig.class);
        when(machineState.getConfig()).thenReturn(machineConfig);
        when(machineConfig.getSource()).thenReturn(machineSource);
        when(machineConfig.getName()).thenReturn(DISPLAY_NAME);
        when(machineConfig.isDev()).thenReturn(IS_DEV);
        when(machineSource.getType()).thenReturn(SOURCE_TYPE);
        when(machineSource.getLocation()).thenReturn(SOURCE_LOCATION);
        when(machineSource.getContent()).thenReturn(SOURCE_CONTENT);
        MachineSourceDto machineSourceDto = mock(MachineSourceDto.class);
        when(machineSourceDto.withType(eq(SOURCE_TYPE))).thenReturn(machineSourceDto);
        when(machineSourceDto.withLocation(eq(SOURCE_LOCATION))).thenReturn(machineSourceDto);
        when(machineSourceDto.withContent(eq(SOURCE_CONTENT))).thenReturn(machineSourceDto);
        when(dtoFactory.createDto(MachineSourceDto.class)).thenReturn(machineSourceDto);
        MachineLimitsDto limitsDto = mock(MachineLimitsDto.class);
        when(dtoFactory.createDto(MachineLimitsDto.class)).thenReturn(limitsDto);
        when(limitsDto.withRam(anyInt())).thenReturn(limitsDto);

        MachineConfigDto machineConfigDto = mock(MachineConfigDto.class);
        when(dtoFactory.createDto(MachineConfigDto.class)).thenReturn(machineConfigDto);
        when(machineConfigDto.withDev(anyBoolean())).thenReturn(machineConfigDto);
        when(machineConfigDto.withName(anyString())).thenReturn(machineConfigDto);
        when(machineConfigDto.withSource(machineSourceDto)).thenReturn(machineConfigDto);
        when(machineConfigDto.withLimits(limitsDto)).thenReturn(machineConfigDto);
        when(machineConfigDto.withType(anyString())).thenReturn(machineConfigDto);

        when(appContext.getWorkspaceId()).thenReturn(ID);
        DevMachine devMachine = mock(DevMachine.class);
        when(appContext.getDevMachine()).thenReturn(devMachine);
        when(devMachine.getId()).thenReturn(ID);

        Promise<Void> promiseEmpty = mock(Promise.class);
        when(workspaceServiceClient.createMachine(anyString(), any(MachineConfigDto.class))).thenReturn(promiseEmpty);

        machineManager.restartMachine(machineState);

        verify(promiseThen).then(operationArgumentCaptor.capture());
        operationArgumentCaptor.getValue().apply(null);

        verify(workspaceServiceClient).createMachine(eq(ID), machineConfigDtoArgumentCaptor.capture());
        verify(machineSourceDto).withType(eq(SOURCE_TYPE));
        verify(machineSourceDto).withLocation(eq(SOURCE_LOCATION));
        verify(machineSourceDto).withContent(eq(SOURCE_CONTENT));
    }

}
