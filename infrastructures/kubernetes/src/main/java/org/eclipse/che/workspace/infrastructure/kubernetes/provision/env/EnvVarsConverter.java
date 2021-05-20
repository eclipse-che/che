/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
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

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.EnvVar;
import java.util.List;
import java.util.Map;
import javax.inject.Singleton;
import org.eclipse.che.api.core.model.workspace.config.MachineConfig;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.eclipse.che.commons.annotation.Traced;
import org.eclipse.che.commons.lang.TopologicalSort;
import org.eclipse.che.commons.tracing.TracingTags;
import org.eclipse.che.workspace.infrastructure.kubernetes.Names;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment.PodData;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.ConfigurationProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.EnvVars;

/**
 * Converts environment variables in {@link MachineConfig} to Kubernetes environment variables.
 *
 * @author Alexander Garagatyi
 */
@Singleton
public class EnvVarsConverter implements ConfigurationProvisioner {

  private final TopologicalSort<EnvVar, String> topoSort =
      new TopologicalSort<>(EnvVar::getName, EnvVars::extractReferencedVariables);

  @Override
  @Traced
  public void provision(KubernetesEnvironment k8sEnv, RuntimeIdentity identity)
      throws InfrastructureException {

    TracingTags.WORKSPACE_ID.set(identity::getWorkspaceId);

    for (PodData pod : k8sEnv.getPodsData().values()) {
      for (Container container : pod.getSpec().getContainers()) {
        String machineName = Names.machineName(pod, container);
        InternalMachineConfig machineConf = k8sEnv.getMachines().get(machineName);

        // we need to combine the env vars from the machine config with the variables already
        // present in the container. Let's key the variables by name and use the map for merging
        Map<String, EnvVar> envVars =
            machineConf
                .getEnv()
                .entrySet()
                .stream()
                .map(e -> new EnvVar(e.getKey(), e.getValue(), null))
                .collect(toMap(EnvVar::getName, identity()));

        // the env vars defined in our machine config take precedence over the ones already defined
        // in the container, if any
        container.getEnv().forEach(v -> envVars.putIfAbsent(v.getName(), v));

        // The environment variable expansion only works if a variable that is referenced
        // is already defined earlier in the list of environment variables.
        // We need to produce a list where variables that reference others always appear later
        // in the list.

        List<EnvVar> sorted = topoSort.sort(envVars.values());

        container.setEnv(sorted);
      }
    }
  }
}
