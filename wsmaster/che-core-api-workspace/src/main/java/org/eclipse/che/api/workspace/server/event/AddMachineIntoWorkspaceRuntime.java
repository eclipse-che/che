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
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.machine.server.MachineManager;
import org.eclipse.che.api.machine.server.exception.MachineException;
import org.eclipse.che.api.machine.server.model.impl.MachineImpl;
import org.eclipse.che.api.machine.shared.dto.event.MachineStatusEvent;
import org.eclipse.che.api.workspace.server.WorkspaceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;

import static org.eclipse.che.api.machine.shared.dto.event.MachineStatusEvent.EventType.RUNNING;

/**
 * The class listens changing of machine status and add new running machine into workspace runtime.
 *
 * @author Mykola Morhun
 */
@Singleton
public class AddMachineIntoWorkspaceRuntime implements EventSubscriber<MachineStatusEvent> {
    private static final Logger LOG = LoggerFactory.getLogger(StopWorkspaceOnDestroyDevMachine.class);

    private final EventService     eventService;
    private final MachineManager   machineManager;
    private final WorkspaceManager workspaceManager;

    @Inject
    public AddMachineIntoWorkspaceRuntime(EventService eventService,
                                          MachineManager machineManager,
                                          WorkspaceManager workspaceManager) {
        this.eventService = eventService;
        this.machineManager = machineManager;
        this.workspaceManager = workspaceManager;
    }

    @Override
    public void onEvent(MachineStatusEvent event) {
        if (RUNNING.equals(event.getEventType())) {
            if (!event.isDev()) {
                MachineImpl machine;
                try {
                    machine = machineManager.getMachine(event.getMachineId());
                } catch (NotFoundException | MachineException exception) {
                    LOG.error(exception.getLocalizedMessage(), exception);
                    return;
                }
                try {
                    workspaceManager.addMachine(machine);
                } catch (NotFoundException exception) {
                    LOG.error(exception.getLocalizedMessage(), exception);
                } catch (ServerException | ConflictException exception) {
                    LOG.error(exception.getLocalizedMessage(), exception);
                    try {
                        machineManager.destroy(event.getMachineId(), true);
                    } catch (NotFoundException ignore) {
                    } catch (MachineException e) {
                        LOG.error("Cannot destroy machine {} from {} workspace",
                                  event.getMachineName(),
                                  event.getWorkspaceId());
                    }
                }
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
