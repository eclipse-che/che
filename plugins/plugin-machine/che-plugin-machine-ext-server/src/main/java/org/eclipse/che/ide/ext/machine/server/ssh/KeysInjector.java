/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.ext.machine.server.ssh;

import static org.eclipse.che.api.workspace.shared.Constants.SERVER_EXEC_AGENT_HTTP_REFERENCE;

import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.agent.exec.client.ExecAgentClient;
import org.eclipse.che.agent.exec.client.ExecAgentClientFactory;
import org.eclipse.che.agent.exec.shared.dto.GetProcessResponseDto;
import org.eclipse.che.agent.exec.shared.dto.ProcessStartResponseDto;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.user.User;
import org.eclipse.che.api.core.model.workspace.Runtime;
import org.eclipse.che.api.core.model.workspace.runtime.Machine;
import org.eclipse.che.api.core.model.workspace.runtime.MachineStatus;
import org.eclipse.che.api.core.model.workspace.runtime.Server;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.ssh.server.SshManager;
import org.eclipse.che.api.ssh.server.model.impl.SshPairImpl;
import org.eclipse.che.api.user.server.UserManager;
import org.eclipse.che.api.workspace.server.WorkspaceManager;
import org.eclipse.che.api.workspace.shared.dto.RuntimeIdentityDto;
import org.eclipse.che.api.workspace.shared.dto.event.MachineStatusEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Injects public parts of ssh keys in the machine after container start
 *
 * @author Sergii Leschenko
 */
@Singleton // must be eager
public class KeysInjector {

  private static final Logger LOG = LoggerFactory.getLogger(KeysInjector.class);

  private final EventService eventService;
  private final SshManager sshManager;
  private final UserManager userManager;
  private final WorkspaceManager workspaceManager;
  private final ExecAgentClientFactory execAgentClientFactory;

  @Inject
  public KeysInjector(
      EventService eventService,
      SshManager sshManager,
      UserManager userManager,
      WorkspaceManager workspaceManager,
      ExecAgentClientFactory execAgentClientFactory) {
    this.eventService = eventService;
    this.sshManager = sshManager;
    this.userManager = userManager;
    this.workspaceManager = workspaceManager;
    this.execAgentClientFactory = execAgentClientFactory;
  }

  @PostConstruct
  public void start() {
    eventService.subscribe(
        new EventSubscriber<MachineStatusEvent>() {
          @Override
          public void onEvent(MachineStatusEvent event) {
            if (event.getEventType() != MachineStatus.RUNNING) {
              return;
            }
            final RuntimeIdentityDto identity = event.getIdentity();
            final String workspaceId = identity.getWorkspaceId();
            User user = getOwner(workspaceId, identity.getOwner());
            if (user == null) {
              return;
            }
            Server execServer = getExecServer(workspaceId, event.getMachineName());
            if (execServer == null) {
              return; // no exec server installed, so not possible to execute command
            }
            // get machine keypairs
            try {
              List<SshPairImpl> sshPairs = sshManager.getPairs(user.getId(), "machine");
              List<String> publicKeys =
                  sshPairs
                      .stream()
                      .filter(sshPair -> sshPair.getPublicKey() != null)
                      .map(SshPairImpl::getPublicKey)
                      .collect(Collectors.toList());
              // get workspace keypair (if any)
              SshPairImpl sshWorkspacePair = null;
              try {
                sshWorkspacePair = sshManager.getPair(user.getId(), "workspace", workspaceId);
              } catch (NotFoundException e) {
                LOG.debug("No ssh key associated to the workspace", e);
              }

              if (sshWorkspacePair != null && sshWorkspacePair.getPublicKey() != null) {
                publicKeys.add(sshWorkspacePair.getPublicKey());
              }

              if (publicKeys.isEmpty()) {
                return;
              }

              StringBuilder commandLine = new StringBuilder("mkdir ~/.ssh/ -p");
              for (String publicKey : publicKeys) {
                commandLine
                    .append("&& echo '")
                    .append(publicKey)
                    .append("' >> ~/.ssh/authorized_keys");
              }

              ExecAgentClient client = execAgentClientFactory.create(execServer.getUrl());
              ProcessStartResponseDto startProcess =
                  client.startProcess(workspaceId, commandLine.toString(), "sshUpload", "custom");

              GetProcessResponseDto process = client.getProcess(workspaceId, startProcess.getPid());
              int tryNumber = 1;
              while (process.isAlive()) {
                process = client.getProcess(workspaceId, startProcess.getPid());
                tryNumber++;
                if (tryNumber > 10) {
                  LOG.warn("Uploading key command executed too long, exiting.");
                  client.killProcess(workspaceId, startProcess.getPid());
                  return;
                }
              }
              if (process.getExitCode() != 0) {
                LOG.warn("Uploading key failed with exit code " + process.getExitCode());
              }

            } catch (ServerException e) {
              LOG.error(e.getLocalizedMessage(), e);
            }
          }
        });
  }

  private Server getExecServer(String workspaceId, String machineName) {
    try {
      Runtime runtime =  workspaceManager.getWorkspace(workspaceId).getRuntime();
      if (runtime != null) {
        Machine machine = runtime.getMachines().get(machineName);
        if (machine != null) {
          return machine.getServers().get(SERVER_EXEC_AGENT_HTTP_REFERENCE);
        }
      }
    } catch (NotFoundException | ServerException e) {
      LOG.warn("Unable to get workspace {}. Error: {}", workspaceId, e.getMessage());
    }
    LOG.warn("Unable to get exec server of workspace {} machine {}.", workspaceId, machineName);
    return null;
  }

  private User getOwner(String workspaceId, String owner) {
    try {
      return userManager.getByName(owner);
    } catch (NotFoundException | ServerException e) {
      LOG.warn("Unable to get owner of the workspace {} with namespace {}", workspaceId, owner);
      return null;
    }
  }
}
