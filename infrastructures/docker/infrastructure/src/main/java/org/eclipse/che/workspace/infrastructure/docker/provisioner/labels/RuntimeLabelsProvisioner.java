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
package org.eclipse.che.workspace.infrastructure.docker.provisioner.labels;

import java.util.Map;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.eclipse.che.workspace.infrastructure.docker.Labels;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerContainerConfig;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerEnvironment;
import org.eclipse.che.workspace.infrastructure.docker.provisioner.ConfigurationProvisioner;

/**
 * Provision labels related to workspace runtime to docker environment.
 *
 * @author Alexander Garagatyi
 */
public class RuntimeLabelsProvisioner implements ConfigurationProvisioner {
  @Override
  public void provision(DockerEnvironment internalEnv, RuntimeIdentity identity)
      throws InfrastructureException {

    for (Map.Entry<String, InternalMachineConfig> entry : internalEnv.getMachines().entrySet()) {
      String name = entry.getKey();
      DockerContainerConfig container = internalEnv.getContainers().get(name);
      container
          .getLabels()
          .putAll(Labels.newSerializer().machineName(name).runtimeId(identity).labels());
    }
  }
}
