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
package org.eclipse.che.ide.ext.machine.server.ssh;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.machine.server.MachineManager;
import org.eclipse.che.api.machine.server.spi.Instance;
import org.eclipse.che.api.machine.shared.dto.event.MachineStatusEvent;
import org.eclipse.che.api.ssh.server.SshManager;
import org.eclipse.che.api.ssh.server.model.impl.SshPairImpl;
import org.eclipse.che.plugin.docker.client.DockerConnector;
import org.eclipse.che.plugin.docker.client.Exec;
import org.eclipse.che.plugin.docker.client.LogMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Injects public parts of ssh keys in the machine after container start
 *
 * @author Sergii Leschenko
 */
@Singleton // must be eager
public class KeysInjector {
    private static final Logger LOG = LoggerFactory.getLogger(KeysInjector.class);

    private final EventService    eventService;
    private final DockerConnector docker;
    private final MachineManager  machineManager;
    private final SshManager      sshManager;

    @Inject
    public KeysInjector(EventService eventService,
                        DockerConnector docker,
                        MachineManager machineManager,
                        SshManager sshManager) {
        this.eventService = eventService;
        this.docker = docker;
        this.machineManager = machineManager;
        this.sshManager = sshManager;
    }

    @PostConstruct
    public void start() {
        eventService.subscribe(new EventSubscriber<MachineStatusEvent>() {
            @Override
            public void onEvent(MachineStatusEvent event) {
                if (event.getEventType() == MachineStatusEvent.EventType.RUNNING) {
                    try {
                        final Instance machine = machineManager.getInstance(event.getMachineId());
                        List<SshPairImpl> sshPairs = sshManager.getPairs(machine.getOwner(), "machine");
                        final List<String> publicKeys = sshPairs.stream()
                                                             .filter(sshPair -> sshPair.getPublicKey() != null)
                                                             .map(SshPairImpl::getPublicKey)
                                                             .collect(Collectors.toList());

                        if (publicKeys.isEmpty()) {
                            return;
                        }

                        final String containerId = machine.getRuntime().getProperties().get("id");
                        StringBuilder command = new StringBuilder("mkdir ~/.ssh/ -p");
                        for (String publicKey : publicKeys) {
                            command.append("&& echo '")
                                   .append(publicKey)
                                   .append("' >> ~/.ssh/authorized_keys");
                        }

                        final Exec exec = docker.createExec(containerId, true, "/bin/bash", "-c", command.toString());
                        docker.startExec(exec.getId(), logMessage -> {
                            if (logMessage.getType() == LogMessage.Type.STDERR) {
                                try {
                                    machine.getLogger().writeLine("Error of injection public ssh keys. " + logMessage.getContent());
                                } catch (IOException ignore) {
                                }
                            }
                        });
                    } catch (IOException | ServerException | NotFoundException e) {
                        LOG.error(e.getLocalizedMessage(), e);
                    }
                }
            }
        });
    }
}
