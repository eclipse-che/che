/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.workspace.server.event;

import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;

/**
 * @author Dmitry Shnurenko
 */
@Listeners(MockitoTestNGListener.class)
public class MachineStateListenerTest {

//    @Mock
//    private WorkspaceManager workspaceManager;
//    @Mock
//    private EventService     eventService;
//
//    @Mock
//    private MachineStatusEvent event;
//    @Mock
//    private WorkspaceImpl      workspace;
//
//    @InjectMocks
//    private MachineStateListener listener;
//
//    @Test
//    public void workspaceShouldNotBeStoppedWhenStoppedMachineIsNotDev() throws Exception {
//        when(event.isDev()).thenReturn(false);
//
//        listener.onEvent(event);
//
//        verify(workspaceManager, never()).stopWorkspace(anyString());
//    }
//
//    @Test
//    public void workspaceShouldNotBeStoppedWhenStoppedMachineIsNotDestroyed() throws Exception {
//        when(event.getEventType()).thenReturn(RUNNING);
//
//        listener.onEvent(event);
//
//        verify(workspaceManager, never()).stopWorkspace(anyString());
//    }
//
//    @Test
//    public void workspaceShouldNotBeStoppedWhenItIsNotRunning() throws Exception {
//        when(event.isDev()).thenReturn(true);
//        when(event.getEventType()).thenReturn(DESTROYED);
//        when(workspaceManager.getWorkspace(anyString())).thenReturn(workspace);
//        when(workspace.getStatus()).thenReturn(WorkspaceStatus.STOPPED);
//
//        listener.onEvent(event);
//
//        verify(workspaceManager, never()).stopWorkspace(anyString());
//    }
//
//    @Test
//    public void workspaceShouldBeStopped() throws Exception {
//        when(event.isDev()).thenReturn(true);
//        when(event.getEventType()).thenReturn(DESTROYED);
//        when(workspaceManager.getWorkspace(anyString())).thenReturn(workspace);
//        when(workspace.getStatus()).thenReturn(WorkspaceStatus.RUNNING);
//
//        listener.onEvent(event);
//
//        verify(workspaceManager).stopWorkspace(anyString());
//    }
}