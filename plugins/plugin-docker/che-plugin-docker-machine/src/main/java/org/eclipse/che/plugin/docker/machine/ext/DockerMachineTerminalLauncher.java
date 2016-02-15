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
package org.eclipse.che.plugin.docker.machine.ext;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.machine.server.MachineManager;
import org.eclipse.che.api.machine.server.exception.MachineException;
import org.eclipse.che.api.machine.server.spi.Instance;
import org.eclipse.che.api.machine.shared.dto.event.MachineStatusEvent;
import org.eclipse.che.plugin.docker.client.DockerConnector;
import org.eclipse.che.plugin.docker.client.Exec;
import org.eclipse.che.plugin.docker.client.LogMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;

/**
 * Starts websocket terminal in the machine after container start
 *
 * @author Alexander Garagatyi
 */
@Singleton // must be eager
public class DockerMachineTerminalLauncher {
    public static final String START_TERMINAL_COMMAND    = "machine.server.terminal.run_command";

    private static final Logger LOG = LoggerFactory.getLogger(DockerMachineTerminalLauncher.class);

    private final EventService    eventService;
    private final DockerConnector docker;
    private final MachineManager  machineManager;
    private final String          terminalStartCommand;

    @Inject
    public DockerMachineTerminalLauncher(EventService eventService,
                                         DockerConnector docker,
                                         MachineManager machineManager,
                                         @Named(START_TERMINAL_COMMAND) String terminalStartCommand) {
        this.eventService = eventService;
        this.docker = docker;
        this.machineManager = machineManager;
        this.terminalStartCommand = terminalStartCommand;
    }

    @PostConstruct
    public void start() {
        eventService.subscribe(new EventSubscriber<MachineStatusEvent>() {
            @Override
            public void onEvent(MachineStatusEvent event) {
                if (event.getEventType() == MachineStatusEvent.EventType.RUNNING) {
                    try {
                        final Instance machine = machineManager.getInstance(event.getMachineId());
                        final String containerId = machine.getRuntime().getProperties().get("id");

                        final Exec exec = docker.createExec(containerId, true, "/bin/bash", "-c", terminalStartCommand);
                        docker.startExec(exec.getId(), logMessage -> {
                            if (logMessage.getType() == LogMessage.Type.STDERR) {
                                try {
                                    machine.getLogger().writeLine("Terminal error. %s" + logMessage.getContent());
                                } catch (IOException ignore) {
                                }
                            }
                        });
                    } catch (IOException | MachineException | NotFoundException e) {
                        LOG.error(e.getLocalizedMessage(), e);
                        // TODO send event that terminal is unavailable
                    }
                }
            }
        });
    }
}
