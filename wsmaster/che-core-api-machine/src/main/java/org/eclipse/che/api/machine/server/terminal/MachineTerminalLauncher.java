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
package org.eclipse.che.api.machine.server.terminal;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.machine.server.MachineManager;
import org.eclipse.che.api.machine.server.exception.MachineException;
import org.eclipse.che.api.machine.server.spi.Instance;
import org.eclipse.che.api.machine.shared.dto.event.MachineStatusEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Starts websocket terminal in the machine after its start
 *
 * @author Alexander Garagatyi
 */
@Singleton
public class MachineTerminalLauncher {
    private static final Logger LOG = LoggerFactory.getLogger(MachineTerminalLauncher.class);

    private final EventService                                     eventService;
    private final MachineManager                                   machineManager;
    private final Map<String, MachineImplSpecificTerminalLauncher> terminalLaunchers;

    @Inject
    public MachineTerminalLauncher(EventService eventService,
                                   MachineManager machineManager,
                                   Set<MachineImplSpecificTerminalLauncher> machineImplSpecificTerminalLaunchers) {
        this.eventService = eventService;
        this.machineManager = machineManager;
        this.terminalLaunchers = machineImplSpecificTerminalLaunchers.stream()
                                                                     .collect(Collectors.toMap(MachineImplSpecificTerminalLauncher::getMachineType,
                                                                                               Function.identity()));
    }

    @PostConstruct
    public void start() {
        eventService.subscribe(new EventSubscriber<MachineStatusEvent>() {
            @Override
            public void onEvent(MachineStatusEvent event) {
                if (event.getEventType() == MachineStatusEvent.EventType.RUNNING) {
                    try {
                        final Instance machine = machineManager.getInstance(event.getMachineId());

                        MachineImplSpecificTerminalLauncher machineImplSpecificTerminalLauncher = terminalLaunchers.get(machine.getConfig().getType());
                        if (machineImplSpecificTerminalLauncher == null) {
                            LOG.warn("Terminal launcher implementation was not found for machine {} with type {}.",
                                     machine.getId(),
                                     machine.getConfig().getType());
                        } else {
                            machineImplSpecificTerminalLauncher.launchTerminal(machine);
                        }
                    } catch (MachineException | NotFoundException e) {
                        LOG.error(e.getLocalizedMessage(), e);
                        // TODO send event that terminal is unavailable
                    }
                }
            }
        });
    }
}
