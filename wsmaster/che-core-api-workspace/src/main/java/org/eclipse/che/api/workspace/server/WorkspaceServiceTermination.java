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
package org.eclipse.che.api.workspace.server;

import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.system.server.ServiceTermination;
import org.eclipse.che.api.system.shared.event.service.SystemServiceItemStoppedEvent;
import org.eclipse.che.api.system.shared.event.service.SystemServiceStoppedEvent;
import org.eclipse.che.api.workspace.shared.dto.event.WorkspaceStatusEvent;

import javax.inject.Inject;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Terminates workspace service.
 *
 * @author Yevhenii Voevodin
 */
public class WorkspaceServiceTermination implements ServiceTermination {

    @Inject
    private WorkspaceManager workspaceManager;

    @Inject
    private EventService eventService;

    @Override
    public void terminate() throws InterruptedException {
        EventSubscriber propagator = new WorkspaceStoppedEventsPropagator();
        eventService.subscribe(propagator);
        try {
            workspaceManager.shutdown();
        } finally {
            eventService.unsubscribe(propagator);
        }
    }

    @Override
    public String getServiceName() {
        return "workspace";
    }

    /**
     * Propagates workspace stopped events as {@link SystemServiceStoppedEvent} events.
     */
    private class WorkspaceStoppedEventsPropagator implements EventSubscriber<WorkspaceStatusEvent> {

        private final int           totalRunning;
        private final AtomicInteger currentlyStopped;

        private WorkspaceStoppedEventsPropagator() {
            this.totalRunning = workspaceManager.getRunningWorkspacesIds().size();
            this.currentlyStopped = new AtomicInteger(0);
        }

        @Override
        public void onEvent(WorkspaceStatusEvent event) {
            if (event.getStatus() == WorkspaceStatus.STOPPED) {
                eventService.publish(new SystemServiceItemStoppedEvent(getServiceName(),
                                                                       event.getWorkspaceId(),
                                                                       currentlyStopped.incrementAndGet(),
                                                                       totalRunning));
            }
        }
    }
}
