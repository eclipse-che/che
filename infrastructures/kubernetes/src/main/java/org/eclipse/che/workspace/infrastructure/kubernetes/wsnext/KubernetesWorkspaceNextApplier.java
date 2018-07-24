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

import static java.util.stream.Collectors.toMap;
import static org.eclipse.che.api.core.model.workspace.config.MachineConfig.MEMORY_LIMIT_ATTRIBUTE;

import com.google.common.annotations.Beta;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.Pod;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.workspace.server.model.impl.ServerConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.VolumeImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.environment.InternalEnvironment;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.eclipse.che.api.workspace.server.wsnext.WorkspaceNextApplier;
import org.eclipse.che.api.workspace.server.wsnext.model.CheContainer;
import org.eclipse.che.api.workspace.server.wsnext.model.CheContainerPort;
import org.eclipse.che.api.workspace.server.wsnext.model.ChePlugin;
import org.eclipse.che.api.workspace.server.wsnext.model.ChePluginEndpoint;
import org.eclipse.che.api.workspace.server.wsnext.model.EnvVar;
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
  public void apply(InternalEnvironment internalEnvironment, Collection<ChePlugin> chePlugins)
      throws InfrastructureException {
    if (chePlugins.isEmpty()) {
      return;
    }

    KubernetesEnvironment kubernetesEnvironment = (KubernetesEnvironment) internalEnvironment;

    Map<String, Pod> pods = kubernetesEnvironment.getPods();
    if (pods.size() != 1) {
      throw new InfrastructureException(
          "Workspace.Next configuration can be applied to a workspace with one pod only");
    }
    Pod pod = pods.values().iterator().next();

    for (ChePlugin chePlugin : chePlugins) {
      for (CheContainer container : chePlugin.getContainers()) {
        addMachine(pod, container, chePlugin, kubernetesEnvironment);
      }
    }
  }

  private void addMachine(
      Pod pod,
      CheContainer container,
      ChePlugin chePlugin,
      KubernetesEnvironment kubernetesEnvironment) {

    List<ChePluginEndpoint> containerEndpoints =
        getContainerEndpoints(container.getPorts(), chePlugin.getEndpoints());
    io.fabric8.kubernetes.api.model.Container k8sContainer =
        addContainer(pod, container.getImage(), container.getEnv());

    String machineName = Names.machineName(pod, k8sContainer);

    InternalMachineConfig machineConfig =
        addMachineConfig(
            kubernetesEnvironment, machineName, containerEndpoints, container.getVolumes());

    normalizeMemory(k8sContainer, machineConfig);
  }

  private io.fabric8.kubernetes.api.model.Container addContainer(
      Pod toolingPod, String image, List<EnvVar> env) {
    io.fabric8.kubernetes.api.model.Container container =
        new ContainerBuilder()
            .withImage(image)
            .withName(Names.generateName("tooling"))
            .withEnv(toK8sEnv(env))
            .build();
    toolingPod.getSpec().getContainers().add(container);
    return container;
  }

  private InternalMachineConfig addMachineConfig(
      KubernetesEnvironment kubernetesEnvironment,
      String machineName,
      List<ChePluginEndpoint> endpoints,
      List<Volume> volumes) {

    InternalMachineConfig machineConfig =
        new InternalMachineConfig(
            null, toWorkspaceServers(endpoints), null, null, toWorkspaceVolumes(volumes));
    kubernetesEnvironment.getMachines().put(machineName, machineConfig);

    return machineConfig;
  }

  private List<ChePluginEndpoint> getContainerEndpoints(
      List<CheContainerPort> ports, List<ChePluginEndpoint> endpoints) {

    if (ports != null) {
      return ports
          .stream()
          .flatMap(
              cheContainerPort ->
                  endpoints
                      .stream()
                      .filter(
                          chePluginEndpoint ->
                              chePluginEndpoint.getTargetPort()
                                  == cheContainerPort.getExposedPort()))
          .collect(Collectors.toList());
    }
    return Collections.emptyList();
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

  private Map<String, ? extends ServerConfig> toWorkspaceServers(
      List<ChePluginEndpoint> endpoints) {
    return endpoints
        .stream()
        .collect(
            toMap(ChePluginEndpoint::getName, endpoint -> normalizeServer(toServer(endpoint))));
  }

  private ServerConfigImpl toServer(ChePluginEndpoint endpoint) {
    Map<String, String> attributes = new HashMap<>();
    attributes.put("internal", Boolean.toString(!endpoint.isPublic()));
    endpoint
        .getAttributes()
        .forEach(
            (k, v) -> {
              if (!"protocol".equals(k) && !"path".equals(k)) {
                attributes.put(k, v);
              }
            });
    return new ServerConfigImpl(
        Integer.toString(endpoint.getTargetPort()),
        endpoint.getAttributes().get("protocol"),
        endpoint.getAttributes().get("path"),
        attributes);
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
