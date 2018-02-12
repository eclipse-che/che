/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.openshift.provision;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodSpec;
import java.util.Map;
import javax.inject.Singleton;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.eclipse.che.workspace.infrastructure.kubernetes.Names;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.ConfigurationProvisioner;
import org.eclipse.che.workspace.infrastructure.openshift.environment.OpenShiftEnvironment;
import org.eclipse.che.workspace.infrastructure.openshift.server.OpenShiftServerExposer;

/**
 * Converts {@link ServerConfig} to OpenShift related objects to add a server into OpenShift
 * runtime.
 *
 * <p>Adds OpenShift objects by calling {@link OpenShiftServerExposer#expose(Map)} on each machine
 * with servers.
 *
 * @author Alexander Garagatyi
 */
@Singleton
public class OpenShiftServersConverter implements ConfigurationProvisioner<OpenShiftEnvironment> {

  @Override
  public void provision(OpenShiftEnvironment osEnv, RuntimeIdentity identity)
      throws InfrastructureException {

    for (Pod podConfig : osEnv.getPods().values()) {
      final PodSpec podSpec = podConfig.getSpec();
      for (Container containerConfig : podSpec.getContainers()) {
        String machineName = Names.machineName(podConfig, containerConfig);
        InternalMachineConfig machineConfig = osEnv.getMachines().get(machineName);
        if (!machineConfig.getServers().isEmpty()) {
          OpenShiftServerExposer openShiftServerExposer =
              new OpenShiftServerExposer(machineName, podConfig, containerConfig, osEnv);
          openShiftServerExposer.expose(machineConfig.getServers());
        }
      }
    }
  }
}
