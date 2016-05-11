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
package org.eclipse.che.api.workspace.server.event;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.machine.server.MachineManager;
import org.eclipse.che.api.machine.server.exception.MachineException;
import org.eclipse.che.api.machine.server.model.impl.MachineImpl;
import org.eclipse.che.api.machine.shared.dto.event.MachineStatusEvent;
import org.eclipse.che.api.workspace.server.WorkspaceManager;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static org.eclipse.che.api.machine.shared.dto.event.MachineStatusEvent.EventType.CREATING;
import static org.eclipse.che.api.machine.shared.dto.event.MachineStatusEvent.EventType.RUNNING;
import static org.eclipse.che.api.machine.shared.dto.event.MachineStatusEvent.EventType.DESTROYING;
import static org.eclipse.che.api.machine.shared.dto.event.MachineStatusEvent.EventType.DESTROYED;
import static org.eclipse.che.api.machine.shared.dto.event.MachineStatusEvent.EventType.ERROR;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Mykola Morhun
 */
@Listeners(MockitoTestNGListener.class)
public class AddMachineIntoWorkspaceRuntimeTest {

    private static final String MACHINE_ID   = "machineId";

    @Mock
    private MachineImpl machine;

    @Mock
    private MachineStatusEvent event;
    @Mock
    private WorkspaceImpl      workspace;
    @Mock
    private MachineManager     machineManager;
    @Mock
    private WorkspaceManager   workspaceManager;

    @InjectMocks
    private AddMachineIntoWorkspaceRuntime listener;

    @BeforeMethod
    public void setup() throws NotFoundException, MachineException {
        when(event.getEventType()).thenReturn(RUNNING);
        when(event.getMachineId()).thenReturn(MACHINE_ID);
        when(machineManager.getMachine(MACHINE_ID)).thenReturn(machine);
        when(event.isDev()).thenReturn(false);
    }

    @Test
    public void shouldAddNewMachineIntoWorkspaceRuntime() throws ConflictException, NotFoundException, ServerException {
        listener.onEvent(event);

        verify(workspaceManager).addMachine(eq(machine));
    }

    @Test
    public void shouldNotAddDevMachineIntoWorkspaceRuntimeList() throws ConflictException, NotFoundException, ServerException {
        when(event.isDev()).thenReturn(true);

        verify(workspaceManager, never()).addMachine(anyObject());
    }

    @Test
    public void shouldNotAddMachineIfItsStatusIsCreating() throws ConflictException, NotFoundException, ServerException {
        when(event.getEventType()).thenReturn(CREATING);

        listener.onEvent(event);

        verify(workspaceManager, never()).addMachine(anyObject());
    }

    @Test
    public void shouldNotAddMachineIfItsStatusIsDestroying() throws ConflictException, NotFoundException, ServerException {
        when(event.getEventType()).thenReturn(DESTROYING);

        listener.onEvent(event);

        verify(workspaceManager, never()).addMachine(anyObject());
    }

    @Test
    public void shouldNotAddMachineIfItsStatusIsDESTROYED() throws ConflictException, NotFoundException, ServerException {
        when(event.getEventType()).thenReturn(DESTROYED);

        listener.onEvent(event);

        verify(workspaceManager, never()).addMachine(anyObject());
    }

    @Test
    public void shouldNotAddMachineIfItsStatusIsERROR() throws ConflictException, NotFoundException, ServerException {
        when(event.getEventType()).thenReturn(ERROR);

        listener.onEvent(event);

        verify(workspaceManager, never()).addMachine(anyObject());
    }

    @Test
    public void shouldDestroyMachineIfErrorOccurred() throws ConflictException, NotFoundException, ServerException {
        doThrow(ServerException.class).when(workspaceManager).addMachine(machine);

        listener.onEvent(event);

        verify(machineManager).destroy(eq(MACHINE_ID), eq(true));
    }

}
