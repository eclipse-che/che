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

import com.google.common.util.concurrent.ThreadFactoryBuilder;

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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

/**
 * Starts websocket terminal in the machine after its start.
 *
 * @author Alexander Garagatyi
 */
@Singleton
public class MachineTerminalLauncher {
    private static final Logger LOG = LoggerFactory.getLogger(MachineTerminalLauncher.class);

    private final EventService                                     eventService;
    private final MachineManager                                   machineManager;
    private final Map<String, MachineImplSpecificTerminalLauncher> terminalLaunchers;
    private final ExecutorService                                  executor;

    @Inject
    public MachineTerminalLauncher(EventService eventService,
                                   MachineManager machineManager,
                                   Set<MachineImplSpecificTerminalLauncher> machineImplLaunchers) {
        this.eventService = eventService;
        this.machineManager = machineManager;
        this.terminalLaunchers = machineImplLaunchers.stream()
                                                     .collect(toMap(MachineImplSpecificTerminalLauncher::getMachineType,
                                                                    Function.identity()));
        this.executor = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat("MachineTerminalLauncher-%d")
                                                                                .setDaemon(true)
                                                                                .build());

    }

    @PostConstruct
    public void start() {
        eventService.subscribe(new EventSubscriber<MachineStatusEvent>() {
            @Override
            public void onEvent(MachineStatusEvent event) {
                if (event.getEventType() == MachineStatusEvent.EventType.RUNNING) {
                    executor.execute(() -> {
                        try {
                            final Instance machine = machineManager.getInstance(event.getMachineId());

                            MachineImplSpecificTerminalLauncher terminalLauncher = terminalLaunchers.get(machine.getConfig()
                                                                                                                .getType());
                            if (terminalLauncher == null) {
                                LOG.warn("Terminal launcher implementation was not found for machine {} with type {}.",
                                         machine.getId(),
                                         machine.getConfig().getType());
                            } else {
                                terminalLauncher.launchTerminal(machine);
                            }
                        } catch (MachineException | NotFoundException e) {
                            LOG.error(e.getLocalizedMessage(), e);
                            // TODO send event that terminal is unavailable
                        }
                    });
                }
            }
        });
    }
}
