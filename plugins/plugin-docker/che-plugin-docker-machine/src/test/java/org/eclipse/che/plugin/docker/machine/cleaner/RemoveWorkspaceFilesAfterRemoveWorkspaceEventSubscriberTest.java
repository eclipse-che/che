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
package org.eclipse.che.plugin.docker.machine.cleaner;

import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.workspace.server.WorkspaceFilesCleaner;
import org.eclipse.che.api.workspace.server.event.WorkspaceRemovedEvent;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test for {@link RemoveWorkspaceFilesAfterRemoveWorkspaceEventSubscriber}.
 *
 * @author Alexander Andrienko
 */
@Listeners(MockitoTestNGListener.class)
public class RemoveWorkspaceFilesAfterRemoveWorkspaceEventSubscriberTest {
    //mocks for constructor
    @Mock
    private EventService          eventService;
    @Mock
    private WorkspaceFilesCleaner workspaceFilesCleaner;

    @Mock
    private WorkspaceRemovedEvent event;
    @Mock
    private Workspace             workspace;

    private RemoveWorkspaceFilesAfterRemoveWorkspaceEventSubscriber removeWorkspaceFilesAfterRemoveWorkspaceEventSubscriber;

    @BeforeMethod
    public void setUp() {
        removeWorkspaceFilesAfterRemoveWorkspaceEventSubscriber =
                new RemoveWorkspaceFilesAfterRemoveWorkspaceEventSubscriber(eventService, workspaceFilesCleaner);
    }

    @Test
    public void shouldSubscribeListenerToEventService() {
        removeWorkspaceFilesAfterRemoveWorkspaceEventSubscriber.subscribe();

        verify(eventService).subscribe(removeWorkspaceFilesAfterRemoveWorkspaceEventSubscriber);
    }

    @Test
    public void workspaceShouldBeCleaned() throws Exception {
        when(event.getWorkspace()).thenReturn(workspace);

        removeWorkspaceFilesAfterRemoveWorkspaceEventSubscriber.onEvent(event);

        verify(event, timeout(2000)).getWorkspace();
        verify(workspaceFilesCleaner, timeout(2000)).clear(workspace);
    }
}
