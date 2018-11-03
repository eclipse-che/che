/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.workspace.server.spi.provision;

import static org.eclipse.che.api.workspace.shared.Constants.CHE_MACHINE_NAME_ENV_VAR;

import java.util.Map.Entry;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.environment.InternalEnvironment;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provisions environment variable with machine name into each machine.
 *
 * @author Sergii Leshchenko
 */
public class MachineNameProvisioner implements InternalEnvironmentProvisioner {

  private static final Logger LOG = LoggerFactory.getLogger(MachineNameProvisioner.class);

  @Override
  public void provision(RuntimeIdentity id, InternalEnvironment internalEnvironment)
      throws InfrastructureException {
    LOG.debug("Provisioning machine names for workspace '{}'", id.getWorkspaceId());
    for (Entry<String, InternalMachineConfig> machineEntry :
        internalEnvironment.getMachines().entrySet()) {
      machineEntry.getValue().getEnv().put(CHE_MACHINE_NAME_ENV_VAR, machineEntry.getKey());
    }
  }
}
