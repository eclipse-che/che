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
package org.eclipse.che.api.system.server;

import com.google.common.collect.ImmutableSet;

import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.system.shared.event.service.SystemServiceItemStoppedEvent;
import org.eclipse.che.api.workspace.server.WorkspaceManager;
import org.eclipse.che.api.workspace.shared.dto.event.WorkspaceStatusEvent;
import org.eclipse.che.dto.server.DtoFactory;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests {@link WorkspaceServiceTermination}.
 *
 * @author Yevhenii Voevodin
 */
@Listeners(MockitoTestNGListener.class)
public class WorkspaceServiceTerminationTest {

    @Mock
    private EventService eventService;

    @Mock
    private WorkspaceManager workspaceManager;

    @InjectMocks
    private WorkspaceServiceTermination termination;

    @Test
    public void shutsDownWorkspaceService() throws Exception {
        termination.terminate();

        verify(workspaceManager).shutdown();
    }

    @Test
    public void publishesStoppedWorkspaceStoppedEventsAsServiceItemStoppedEvents() throws Exception {
        when(workspaceManager.getRunningWorkspacesIds()).thenReturn(ImmutableSet.of("id1", "id2", "id3"));
        doAnswer(inv -> {
            @SuppressWarnings("unchecked")
            EventSubscriber<WorkspaceStatusEvent> subscriber = (EventSubscriber<WorkspaceStatusEvent>)inv.getArguments()[0];

            // id1
            subscriber.onEvent(newWorkspaceStatusEvent(WorkspaceStatus.STARTING, "id1"));
            subscriber.onEvent(newWorkspaceStatusEvent(WorkspaceStatus.RUNNING, "id1"));
            subscriber.onEvent(newWorkspaceStatusEvent(WorkspaceStatus.STOPPING, "id1"));
            subscriber.onEvent(newWorkspaceStatusEvent(WorkspaceStatus.STOPPED, "id1"));

            // id2
            subscriber.onEvent(newWorkspaceStatusEvent(WorkspaceStatus.RUNNING, "id2"));
            subscriber.onEvent(newWorkspaceStatusEvent(WorkspaceStatus.STOPPING, "id2"));
            subscriber.onEvent(newWorkspaceStatusEvent(WorkspaceStatus.STOPPED, "id2"));

            // id3
            subscriber.onEvent(newWorkspaceStatusEvent(WorkspaceStatus.STOPPED, "id3"));

            return null;
        }).when(eventService).subscribe(any());

        termination.terminate();

        verify(eventService).publish(new SystemServiceItemStoppedEvent("workspace", "id1", 1, 3));
        verify(eventService).publish(new SystemServiceItemStoppedEvent("workspace", "id2", 2, 3));
        verify(eventService).publish(new SystemServiceItemStoppedEvent("workspace", "id3", 3, 3));
    }

    private static WorkspaceStatusEvent newWorkspaceStatusEvent(WorkspaceStatus status, String workspaceId) {
        return DtoFactory.newDto(WorkspaceStatusEvent.class)
                         .withStatus(status)
                         .withWorkspaceId(workspaceId);
    }
}
