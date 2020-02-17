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

import static org.eclipse.che.api.core.model.workspace.config.MachineConfig.CPU_LIMIT_ATTRIBUTE;
import static org.eclipse.che.api.core.model.workspace.config.MachineConfig.CPU_REQUEST_ATTRIBUTE;
import static org.eclipse.che.api.core.model.workspace.config.MachineConfig.MEMORY_LIMIT_ATTRIBUTE;
import static org.eclipse.che.api.core.model.workspace.config.MachineConfig.MEMORY_REQUEST_ATTRIBUTE;
import static org.eclipse.che.workspace.infrastructure.kubernetes.Names.machineName;

import io.fabric8.kubernetes.api.model.Container;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.eclipse.che.api.workspace.server.spi.environment.ResourceLimitAttributesProvisioner;
import org.eclipse.che.commons.annotation.Traced;
import org.eclipse.che.commons.tracing.TracingTags;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment.PodData;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.ConfigurationProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.Containers;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.KubernetesSize;

/**
 * Sets or overrides Kubernetes container RAM and CPU limit and request if corresponding attributes
 * are present in machine config corresponding to the container.
 *
 * <p>There are two memory-related properties:
 *
 * <ul>
 *   <li>che.workspace.default_memory_limit_mb - defines default machine memory limit
 *   <li>che.workspace.default_memory_request_mb - defines default requested machine memory
 *       allocation.
 * </ul>
 *
 * <p>Similarly, there are two CPU-related properties:
 *
 * <ul>
 *   <li>che.workspace.default_cpu_limit_cores - defines default machine CPU limit
 *   <li>che.workspace.default_cpu_request_cores - defines default machine CPU request
 * </ul>
 *
 * @author Anton Korneta
 * @auhtor Max Shaposhnyk
 */
@Singleton
public class ContainerResourceProvisioner implements ConfigurationProvisioner {

  private final ResourceLimitAttributesProvisioner resourceLimitAttributesProvisioner;

  private final long defaultMachineMaxMemorySizeAttribute;
  private final long defaultMachineRequestMemorySizeAttribute;
  private final float defaultMachineCpuLimitAttribute;
  private final float defaultMachineCpuRequestAttribute;

  @Inject
  public ContainerResourceProvisioner(
      @Named("che.workspace.default_memory_limit_mb") long defaultMachineMaxMemorySizeAttribute,
      @Named("che.workspace.default_memory_request_mb")
          long defaultMachineRequestMemorySizeAttribute,
      @Named("che.workspace.default_cpu_limit_cores") String defaultMachineCpuLimitAttribute,
      @Named("che.workspace.default_cpu_request_cores") String defaultMachineCpuRequestAttribute,
      ResourceLimitAttributesProvisioner resourceLimitAttributesProvisioner) {
    this.resourceLimitAttributesProvisioner = resourceLimitAttributesProvisioner;
    this.defaultMachineMaxMemorySizeAttribute = defaultMachineMaxMemorySizeAttribute * 1024 * 1024;
    this.defaultMachineRequestMemorySizeAttribute =
        defaultMachineRequestMemorySizeAttribute * 1024 * 1024;
    this.defaultMachineCpuLimitAttribute = KubernetesSize.toCores(defaultMachineCpuLimitAttribute);
    this.defaultMachineCpuRequestAttribute =
        KubernetesSize.toCores(defaultMachineCpuRequestAttribute);
  }

  @Override
  @Traced
  public void provision(KubernetesEnvironment k8sEnv, RuntimeIdentity identity)
      throws InfrastructureException {

    TracingTags.WORKSPACE_ID.set(identity::getWorkspaceId);

    final Map<String, InternalMachineConfig> machines = k8sEnv.getMachines();
    for (PodData pod : k8sEnv.getPodsData().values()) {
      for (Container container : pod.getSpec().getContainers()) {

        // make sure that machine configs have settings for RAM limit and request
        InternalMachineConfig machineConfig = machines.get(machineName(pod, container));
        resourceLimitAttributesProvisioner.provisionMemory(
            machineConfig,
            Containers.getRamLimit(container),
            Containers.getRamRequest(container),
            defaultMachineMaxMemorySizeAttribute,
            defaultMachineRequestMemorySizeAttribute);

        // make sure that machine configs have settings for CPU limit and request
        resourceLimitAttributesProvisioner.provisionCPU(
            machineConfig,
            Containers.getCpuLimit(container),
            Containers.getCpuRequest(container),
            defaultMachineCpuLimitAttribute,
            defaultMachineCpuRequestAttribute);

        // reapply memory  and CPU settings to k8s container to make sure that provisioned
        // values above are set
        final Map<String, String> attributes = machineConfig.getAttributes();
        Containers.addRamLimit(container, Long.parseLong(attributes.get(MEMORY_LIMIT_ATTRIBUTE)));
        Containers.addRamRequest(
            container, Long.parseLong(attributes.get(MEMORY_REQUEST_ATTRIBUTE)));
        Containers.addCpuLimit(container, Float.parseFloat(attributes.get(CPU_LIMIT_ATTRIBUTE)));
        Containers.addCpuRequest(
            container, Float.parseFloat(attributes.get(CPU_REQUEST_ATTRIBUTE)));
      }
    }
  }
}
