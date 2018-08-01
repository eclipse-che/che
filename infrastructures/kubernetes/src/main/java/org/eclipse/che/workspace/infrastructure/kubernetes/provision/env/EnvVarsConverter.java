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
package org.eclipse.che.workspace.infrastructure.kubernetes.provision.env;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.Pod;
import javax.inject.Singleton;
import org.eclipse.che.api.core.model.workspace.config.MachineConfig;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.eclipse.che.workspace.infrastructure.kubernetes.Names;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.ConfigurationProvisioner;

/**
 * Converts environment variables in {@link MachineConfig} to Kubernetes environment variables.
 *
 * @author Alexander Garagatyi
 */
@Singleton
public class EnvVarsConverter implements ConfigurationProvisioner {
  @Override
  public void provision(KubernetesEnvironment k8sEnv, RuntimeIdentity identity)
      throws InfrastructureException {

    for (Pod pod : k8sEnv.getPods().values()) {
      for (Container container : pod.getSpec().getContainers()) {
        String machineName = Names.machineName(pod, container);
        InternalMachineConfig machineConf = k8sEnv.getMachines().get(machineName);
        machineConf
            .getEnv()
            .forEach(
                (key, value) -> {
                  container.getEnv().removeIf(env -> key.equals(env.getName()));
                  container.getEnv().add(new EnvVar(key, value, null));
                });
      }
    }
  }
}
