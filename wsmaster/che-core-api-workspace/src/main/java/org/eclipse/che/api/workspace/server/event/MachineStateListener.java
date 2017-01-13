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

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.machine.shared.dto.event.MachineStatusEvent;
import org.eclipse.che.api.workspace.server.WorkspaceManager;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;

import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.RUNNING;
import static org.eclipse.che.api.machine.shared.dto.event.MachineStatusEvent.EventType.DESTROYED;

/**
 * The class listens changing of machine status and perform some actions when status is changed.
 *
 * @author Dmitry Shnurenko
 */
@Singleton
public class MachineStateListener implements EventSubscriber<MachineStatusEvent> {
    private static final Logger LOG = LoggerFactory.getLogger(MachineStateListener.class);

    private final WorkspaceManager workspaceManager;
    private final EventService     eventService;

    @Inject
    public MachineStateListener(WorkspaceManager workspaceManager, EventService eventService) {
        this.workspaceManager = workspaceManager;
        this.eventService = eventService;
    }

    @Override
    public void onEvent(MachineStatusEvent event) {
        String workspaceId = event.getWorkspaceId();

        if (event.isDev() && DESTROYED.equals(event.getEventType())) {
            try {
                WorkspaceImpl currentWorkspace = workspaceManager.getWorkspace(workspaceId);

                if (RUNNING.equals(currentWorkspace.getStatus())) {
                    workspaceManager.stopWorkspace(workspaceId);
                }

            } catch (ServerException | ConflictException exception) {
                LOG.error(exception.getLocalizedMessage(), exception);
            } catch (NotFoundException ignored) {
            }
        }
    }

    @PostConstruct
    private void subscribe() {
        eventService.subscribe(this);
    }

    @PreDestroy
    private void unsubscribe() {
        eventService.unsubscribe(this);
    }
}
