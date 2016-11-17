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
import org.eclipse.che.api.core.model.user.User;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.environment.server.CheEnvironmentEngine;
import org.eclipse.che.api.machine.server.spi.Instance;
import org.eclipse.che.api.machine.shared.dto.event.MachineStatusEvent;
import org.eclipse.che.api.ssh.server.SshManager;
import org.eclipse.che.api.ssh.server.model.impl.SshPairImpl;
import org.eclipse.che.api.user.server.UserManager;
import org.eclipse.che.plugin.docker.client.DockerConnector;
import org.eclipse.che.plugin.docker.client.Exec;
import org.eclipse.che.plugin.docker.client.LogMessage;
import org.eclipse.che.plugin.docker.client.params.CreateExecParams;
import org.eclipse.che.plugin.docker.client.params.StartExecParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.ArrayList;
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

    private final EventService         eventService;
    private final DockerConnector      docker;
    private final SshManager           sshManager;
    private final UserManager          userManager;
    // TODO replace with WorkspaceManager
    private final CheEnvironmentEngine environmentEngine;

    @Inject
    public KeysInjector(EventService eventService,
                        DockerConnector docker,
                        SshManager sshManager,
                        CheEnvironmentEngine environmentEngine,
                        UserManager userManager) {
        this.eventService = eventService;
        this.docker = docker;
        this.sshManager = sshManager;
        this.environmentEngine = environmentEngine;
        this.userManager = userManager;
    }

    @PostConstruct
    public void start() {
        eventService.subscribe(new EventSubscriber<MachineStatusEvent>() {
            @Override
            public void onEvent(MachineStatusEvent event) {
                if (event.getEventType() == MachineStatusEvent.EventType.RUNNING) {
                    final Instance machine;
                    try {
                        machine = environmentEngine.getMachine(event.getWorkspaceId(),
                                                               event.getMachineId());
                    } catch (NotFoundException e) {
                        LOG.error("Unable to find machine: " + e.getLocalizedMessage(), e);
                        return;
                    }

                    try {
                        // get userid
                        final String userId;
                        try {
                            final User user = userManager.getByName(machine.getOwner());
                            userId = user.getId();
                        } catch (NotFoundException e) {
                            LOG.error("User with name {} associated to machine not found", machine.getOwner());
                            return;
                        }


                        // get machine keypairs
                        List<SshPairImpl> sshPairs = sshManager.getPairs(userId, "machine");
                        final List<String> publicMachineKeys = sshPairs.stream()
                                                             .filter(sshPair -> sshPair.getPublicKey() != null)
                                                             .map(SshPairImpl::getPublicKey)
                                                             .collect(Collectors.toList());

                        // get workspace keypair (if any)
                        SshPairImpl sshWorkspacePair = null;
                        try {
                            sshWorkspacePair = sshManager.getPair(userId, "workspace", event.getWorkspaceId());
                        } catch (NotFoundException e) {
                            LOG.debug("No ssh key associated to the workspace", e);
                        }

                        // build list of all pairs.
                        final List<String> publicKeys;
                        if (sshWorkspacePair != null && sshWorkspacePair.getPublicKey() != null) {
                            publicKeys = new ArrayList<>(publicMachineKeys.size() + 1);
                            publicKeys.add(sshWorkspacePair.getPublicKey());
                            publicKeys.addAll(publicMachineKeys);
                        } else {
                            publicKeys = publicMachineKeys;
                        }

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

                        final Exec exec = docker.createExec(CreateExecParams.create(containerId,
                                                                                    new String[] {"/bin/bash",
                                                                                                  "-c",
                                                                                                  command.toString()})
                                                                            .withDetach(true));
                        docker.startExec(StartExecParams.create(exec.getId()), logMessage -> {
                            if (logMessage.getType() == LogMessage.Type.STDERR) {
                                try {
                                    machine.getLogger().writeLine("Error of injection public ssh keys. " + logMessage.getContent());
                                } catch (IOException ignore) {
                                }
                            }
                        });
                    } catch (IOException | ServerException e) {
                        LOG.error(e.getLocalizedMessage(), e);
                    }
                }
            }
        });
    }
}
