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

import static java.lang.String.format;
import static org.eclipse.che.workspace.infrastructure.kubernetes.Constants.MACHINE_NAME_ANNOTATION_FMT;

import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Singleton;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.eclipse.che.workspace.infrastructure.docker.environment.dockerimage.DockerImageEnvironment;
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

  private EntryPointParser entryPointParser = new EntryPointParser();

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

    final Map<String, String> annotations = new HashMap<>();
    annotations.put(format(MACHINE_NAME_ANNOTATION_FMT, CONTAINER_NAME), machineName);

    final Pod pod =
        new PodBuilder()
            .withNewMetadata()
            .withName(POD_NAME)
            .withAnnotations(annotations)
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
