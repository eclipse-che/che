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

import java.util.ArrayList;
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
import org.eclipse.che.api.core.model.workspace.Runtime;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.core.model.workspace.runtime.Machine;
import org.eclipse.che.api.core.model.workspace.runtime.Server;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.ssh.server.SshManager;
import org.eclipse.che.api.ssh.server.model.impl.SshPairImpl;
import org.eclipse.che.api.workspace.server.WorkspaceManager;
import org.eclipse.che.api.workspace.shared.dto.event.WorkspaceStatusEvent;
import org.eclipse.che.commons.env.EnvironmentContext;
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
  private final WorkspaceManager workspaceManager;
  private final ExecAgentClientFactory execAgentClientFactory;

  @Inject
  public KeysInjector(
      EventService eventService,
      SshManager sshManager,
      WorkspaceManager workspaceManager,
      ExecAgentClientFactory execAgentClientFactory) {
    this.eventService = eventService;
    this.sshManager = sshManager;
    this.workspaceManager = workspaceManager;
    this.execAgentClientFactory = execAgentClientFactory;
  }

  @PostConstruct
  public void start() {
    eventService.subscribe(
        new EventSubscriber<WorkspaceStatusEvent>() {
          @Override
          public void onEvent(WorkspaceStatusEvent event) {
            if (event.getStatus() != WorkspaceStatus.RUNNING) {
              return;
            }

            final String workspaceId = event.getWorkspaceId();
            final List<String> execServerUrls = getExecServerUrls(workspaceId);
            if (execServerUrls.isEmpty()) {
              return; // no exec servers installed, so not possible to execute commands
            }

            List<String> publicKeys = getPublicKeys(workspaceId);
            if (publicKeys.isEmpty()) {
              return; // no keys found, exiting
            }

            StringBuilder commandLine = new StringBuilder("mkdir ~/.ssh/ -p");
            for (String publicKey : publicKeys) {
              commandLine
                  .append("&& echo '")
                  .append(publicKey)
                  .append("' >> ~/.ssh/authorized_keys");
            }
            for (String serverUrl : execServerUrls) {
              doInjectPublicKeys(serverUrl, workspaceId, commandLine.toString());
            }
          }
        });
  }

  private List<String> getExecServerUrls(String workspaceId) {
    List<String> execServerUrls = new ArrayList<>();
    try {
      Runtime runtime = workspaceManager.getWorkspace(workspaceId).getRuntime();
      if (runtime != null) {
        for (Machine machine : runtime.getMachines().values()) {
          Server execServer = machine.getServers().get(SERVER_EXEC_AGENT_HTTP_REFERENCE);
          if (execServer != null) {
            execServerUrls.add(execServer.getUrl());
          }
        }
      }
    } catch (NotFoundException | ServerException e) {
      LOG.warn("Unable to get workspace {}. Error: {}", workspaceId, e.getMessage());
    }
    return execServerUrls;
  }

  private List<String> getPublicKeys(String workspaceId) {
    final String userId = EnvironmentContext.getCurrent().getSubject().getUserId();
    List<String> publicKeys = new ArrayList<>();
    try {
      // get machine keypairs
      List<SshPairImpl> sshPairs = sshManager.getPairs(userId, "machine");
      publicKeys.addAll(
          sshPairs
              .stream()
              .filter(sshPair -> sshPair.getPublicKey() != null)
              .map(SshPairImpl::getPublicKey)
              .collect(Collectors.toList()));
      // get workspace keypair (if any)
      SshPairImpl sshWorkspacePair = null;
      try {
        sshWorkspacePair = sshManager.getPair(userId, "workspace", workspaceId);
      } catch (NotFoundException e) {
        LOG.debug("No ssh key associated to the workspace " + workspaceId, e);
      }

      if (sshWorkspacePair != null && sshWorkspacePair.getPublicKey() != null) {
        publicKeys.add(sshWorkspacePair.getPublicKey());
      }
    } catch (ServerException e) {
      LOG.warn("Unable to get workspace {} public keys. Error: {}", workspaceId, e.getMessage());
    }
    return publicKeys;
  }

  private void doInjectPublicKeys(String execServerUrl, String workspaceId, String command) {
    ExecAgentClient client = execAgentClientFactory.create(execServerUrl);
    try {
      ProcessStartResponseDto startProcess =
          client.startProcess(workspaceId, command, "sshUpload", "custom");
      GetProcessResponseDto process = client.getProcess(workspaceId, startProcess.getPid());
      int tryNumber = 1;
      while (process.isAlive()) {
        Thread.sleep(100);
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
    } catch (ServerException | InterruptedException e) {
      LOG.warn("Unable to inject workspace {} public keys. Error: {}", workspaceId, e.getMessage());
    }
  }
}
