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
package org.eclipse.che.workspace.infrastructure.kubernetes.provision.limits.ram;

import static org.eclipse.che.api.core.model.workspace.config.MachineConfig.MEMORY_LIMIT_ATTRIBUTE;
import static org.eclipse.che.workspace.infrastructure.kubernetes.Names.machineName;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.Pod;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.ConfigurationProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.Containers;

/**
 * Sets Ram limit to Kubernetes machine.
 *
 * @author Anton Korneta
 */
public class RamLimitProvisioner implements ConfigurationProvisioner {

  @Override
  public void provision(KubernetesEnvironment k8sEnv, RuntimeIdentity identity)
      throws InfrastructureException {
    final Map<String, InternalMachineConfig> machines = k8sEnv.getMachines();
    for (Pod pod : k8sEnv.getPods().values()) {
      for (Container container : pod.getSpec().getContainers()) {
        final Map<String, String> attributes =
            machines.get(machineName(pod, container)).getAttributes();
        String memoryLimitAttribute = attributes.get(MEMORY_LIMIT_ATTRIBUTE);
        if (memoryLimitAttribute != null) {
          Containers.addRamLimit(container, Long.parseLong(memoryLimitAttribute));
        }
      }
    }
  }
}
