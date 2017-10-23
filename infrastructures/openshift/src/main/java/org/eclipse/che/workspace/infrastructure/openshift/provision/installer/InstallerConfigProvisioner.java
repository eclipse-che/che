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
package org.eclipse.che.workspace.infrastructure.openshift.provision.installer;

import java.util.Map.Entry;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.core.model.workspace.config.MachineConfig;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.WsAgentMachineFinderUtil;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalEnvironment;
import org.eclipse.che.api.workspace.server.spi.InternalMachineConfig;
import org.eclipse.che.api.workspace.server.token.MachineTokenProvider;
import org.eclipse.che.workspace.infrastructure.openshift.environment.OpenShiftEnvironment;
import org.eclipse.che.workspace.infrastructure.openshift.provision.ConfigurationProvisioner;

/**
 * Adds environment variable to {@link MachineConfig} that are required by installers or agents.
 *
 * @author Sergii Leshchenko
 * @author Alexander Garagatyi
 */
public class InstallerConfigProvisioner implements ConfigurationProvisioner {

  private final MachineTokenProvider machineTokenProvider;
  private final String cheServerEndpoint;

  @Inject
  public InstallerConfigProvisioner(
      MachineTokenProvider machineTokenProvider, @Named("che.api") String cheServerEndpoint) {
    this.machineTokenProvider = machineTokenProvider;
    this.cheServerEndpoint = cheServerEndpoint;
  }

  @Override
  public void provision(
      InternalEnvironment environment, OpenShiftEnvironment osEnv, RuntimeIdentity identity)
      throws InfrastructureException {

    String devMachineName =
        WsAgentMachineFinderUtil.getWsAgentServerMachine(environment)
            .orElseThrow(() -> new InfrastructureException("Machine with wsagent not found"));

    for (Entry<String, InternalMachineConfig> machineEntry : environment.getMachines().entrySet()) {
      InternalMachineConfig config = machineEntry.getValue();

      // CHE_API is used by installers for agent binary downloading
      config.getEnv().put("CHE_API", cheServerEndpoint);

      config.getEnv().put("USER_TOKEN", machineTokenProvider.getToken(identity.getWorkspaceId()));

      // TODO incorrect place for env variable addition. workspace ID is needed for wsagent
      // server, not installer
      // WORKSPACE_ID is required only by workspace agent
      if (devMachineName.equals(machineEntry.getKey())) {
        config.getEnv().put("CHE_WORKSPACE_ID", identity.getWorkspaceId());
      }
    }
  }
}
