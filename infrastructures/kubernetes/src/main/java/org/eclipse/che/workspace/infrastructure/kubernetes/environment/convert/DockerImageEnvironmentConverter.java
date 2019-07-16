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
package org.eclipse.che.workspace.infrastructure.kubernetes.environment.convert;

import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.eclipse.che.workspace.infrastructure.docker.environment.dockerimage.DockerImageEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.Names;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.util.EntryPoint;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.util.EntryPointParser;

/**
 * Converts {@link DockerImageEnvironment} to {@link KubernetesEnvironment}.
 *
 * @author Sergii Leshchenko
 * @author Anton Korneta
 */
@Singleton
public class DockerImageEnvironmentConverter {

  static final String POD_NAME = "dockerimage";
  static final String CONTAINER_NAME = "container";

  private final EntryPointParser entryPointParser;

  @Inject
  public DockerImageEnvironmentConverter(EntryPointParser entryPointParser) {
    this.entryPointParser = entryPointParser;
  }

  public KubernetesEnvironment convert(DockerImageEnvironment environment)
      throws InfrastructureException {

    final String dockerImage = environment.getRecipe().getContent();

    Map.Entry<String, InternalMachineConfig> e =
        environment.getMachines().entrySet().iterator().next();

    InternalMachineConfig machine = e.getValue();
    String machineName = e.getKey();

    ContainerBuilder container =
        new ContainerBuilder()
            .withImage(dockerImage)
            .withName(CONTAINER_NAME)
            .withImagePullPolicy("Always");

    applyEntryPoint(machine, container);

    final Pod pod =
        new PodBuilder()
            .withNewMetadata()
            .withName(POD_NAME)
            .withAnnotations(Names.createMachineNameAnnotations(CONTAINER_NAME, machineName))
            .endMetadata()
            .withNewSpec()
            .withContainers(container.build())
            .endSpec()
            .build();

    return KubernetesEnvironment.builder(environment)
        .setMachines(environment.getMachines())
        .setInternalRecipe(environment.getRecipe())
        .setPods(ImmutableMap.of(machineName, pod))
        .build();
  }

  private void applyEntryPoint(InternalMachineConfig machineConfig, ContainerBuilder bld)
      throws InfrastructureException {
    EntryPoint ep = entryPointParser.parse(machineConfig.getAttributes());

    bld.withCommand(ep.getCommand());
    bld.withArgs(ep.getArguments());
  }
}
