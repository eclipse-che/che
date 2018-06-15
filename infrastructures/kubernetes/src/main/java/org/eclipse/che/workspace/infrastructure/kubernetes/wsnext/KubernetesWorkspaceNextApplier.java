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
package org.eclipse.che.workspace.infrastructure.kubernetes.wsnext;

import static java.util.Collections.singletonMap;
import static org.eclipse.che.api.core.model.workspace.config.MachineConfig.MEMORY_LIMIT_ATTRIBUTE;

import com.google.common.annotations.Beta;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Quantity;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.workspace.server.model.impl.ServerConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.VolumeImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.environment.InternalEnvironment;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.eclipse.che.api.workspace.server.wsnext.WorkspaceNextApplier;
import org.eclipse.che.api.workspace.server.wsnext.model.CheService;
import org.eclipse.che.api.workspace.server.wsnext.model.Container;
import org.eclipse.che.api.workspace.server.wsnext.model.EnvVar;
import org.eclipse.che.api.workspace.server.wsnext.model.ResourceRequirements;
import org.eclipse.che.api.workspace.server.wsnext.model.Server;
import org.eclipse.che.api.workspace.server.wsnext.model.Volume;
import org.eclipse.che.workspace.infrastructure.kubernetes.Names;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.Containers;

/**
 * Applies Workspace.Next configuration to a kubernetes internal runtime object.
 *
 * @author Oleksander Garagatyi
 */
@Beta
public class KubernetesWorkspaceNextApplier implements WorkspaceNextApplier {

  private final String defaultMachineMemorySizeAttribute;

  @Inject
  public KubernetesWorkspaceNextApplier(
      @Named("che.workspace.default_memory_mb") long defaultMachineMemorySizeMB) {
    this.defaultMachineMemorySizeAttribute =
        String.valueOf(defaultMachineMemorySizeMB * 1024 * 1024);
  }

  @Override
  public void apply(InternalEnvironment internalEnvironment, Collection<CheService> cheServices)
      throws InfrastructureException {
    if (cheServices.isEmpty()) {
      return;
    }
    KubernetesEnvironment kubernetesEnvironment = (KubernetesEnvironment) internalEnvironment;
    Map<String, Pod> pods = kubernetesEnvironment.getPods();
    if (pods.size() != 1) {
      throw new InfrastructureException(
          "Workspace.Next configuration can be applied to a workspace with one pod only");
    }
    Pod pod = pods.values().iterator().next();
    for (CheService cheService : cheServices) {
      for (Container container : cheService.getSpec().getContainers()) {
        io.fabric8.kubernetes.api.model.Container k8sContainer =
            addContainer(pod, container.getImage(), container.getEnv(), container.getResources());

        String machineName = Names.machineName(pod, k8sContainer);

        InternalMachineConfig machineConfig =
            addMachine(
                kubernetesEnvironment, machineName, container.getServers(), container.getVolumes());

        normalizeMemory(k8sContainer, machineConfig);
      }
    }
  }

  private io.fabric8.kubernetes.api.model.Container addContainer(
      Pod toolingPod, String image, List<EnvVar> env, ResourceRequirements resources) {
    io.fabric8.kubernetes.api.model.Container container =
        new ContainerBuilder()
            .withImage(image)
            .withName(Names.generateName("tooling"))
            .withEnv(toK8sEnv(env))
            .withResources(toK8sResources(resources))
            .build();
    toolingPod.getSpec().getContainers().add(container);
    return container;
  }

  private io.fabric8.kubernetes.api.model.ResourceRequirements toK8sResources(
      ResourceRequirements resources) {
    io.fabric8.kubernetes.api.model.ResourceRequirements result =
        new io.fabric8.kubernetes.api.model.ResourceRequirements();
    String memory = resources.getRequests().get("memory");
    if (memory != null) {
      result.setRequests(singletonMap("memory", new Quantity(memory)));
    }
    return result;
  }

  private InternalMachineConfig addMachine(
      KubernetesEnvironment kubernetesEnvironment,
      String machineName,
      List<Server> servers,
      List<Volume> volumes) {

    InternalMachineConfig machineConfig =
        new InternalMachineConfig(
            null, toWorkspaceServers(servers), null, null, toWorkspaceVolumes(volumes));
    kubernetesEnvironment.getMachines().put(machineName, machineConfig);

    return machineConfig;
  }

  private void normalizeMemory(
      io.fabric8.kubernetes.api.model.Container container, InternalMachineConfig machineConfig) {
    long ramLimit = Containers.getRamLimit(container);
    Map<String, String> attributes = machineConfig.getAttributes();
    if (ramLimit > 0) {
      attributes.put(MEMORY_LIMIT_ATTRIBUTE, String.valueOf(ramLimit));
    } else {
      attributes.put(MEMORY_LIMIT_ATTRIBUTE, defaultMachineMemorySizeAttribute);
    }
  }

  private Map<String, ? extends org.eclipse.che.api.core.model.workspace.config.Volume>
      toWorkspaceVolumes(List<Volume> volumes) {
    Map<String, VolumeImpl> result = new HashMap<>();

    for (Volume volume : volumes) {
      result.put(volume.getName(), new VolumeImpl().withPath(volume.getMountPath()));
    }
    return result;
  }

  private Map<String, ? extends ServerConfig> toWorkspaceServers(List<Server> servers) {
    HashMap<String, ServerConfigImpl> result = new HashMap<>();
    for (Server server : servers) {
      result.put(
          server.getName(),
          normalizeServer(
              new ServerConfigImpl(
                  server.getPort().toString(),
                  server.getProtocol(),
                  null,
                  server.getAttributes())));
    }
    return result;
  }

  private List<io.fabric8.kubernetes.api.model.EnvVar> toK8sEnv(List<EnvVar> env) {
    List<io.fabric8.kubernetes.api.model.EnvVar> result = new ArrayList<>();

    for (EnvVar envVar : env) {
      result.add(
          new io.fabric8.kubernetes.api.model.EnvVar(envVar.getName(), envVar.getValue(), null));
    }

    return result;
  }

  private ServerConfigImpl normalizeServer(ServerConfigImpl serverConfig) {
    String port = serverConfig.getPort();
    if (port != null && !port.contains("/")) {
      serverConfig.setPort(port + "/tcp");
    }
    return serverConfig;
  }
}
