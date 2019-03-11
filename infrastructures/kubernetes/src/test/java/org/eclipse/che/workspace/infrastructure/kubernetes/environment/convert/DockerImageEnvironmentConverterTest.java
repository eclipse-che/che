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
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.eclipse.che.workspace.infrastructure.kubernetes.Constants.MACHINE_NAME_ANNOTATION_FMT;
import static org.eclipse.che.workspace.infrastructure.kubernetes.environment.convert.DockerImageEnvironmentConverter.CONTAINER_NAME;
import static org.eclipse.che.workspace.infrastructure.kubernetes.environment.convert.DockerImageEnvironmentConverter.POD_NAME;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;

import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.eclipse.che.api.workspace.server.spi.environment.InternalRecipe;
import org.eclipse.che.workspace.infrastructure.docker.environment.dockerimage.DockerImageEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.util.EntryPoint;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.util.EntryPointParser;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/** @author Anton Korneta */
@Listeners(MockitoTestNGListener.class)
public class DockerImageEnvironmentConverterTest {

  private static final String MACHINE_NAME = "testMachine";
  private static final String RECIPE_CONTENT = "suse_jdk8";
  private static final String RECIPE_TYPE = "dockerimage";

  @Mock DockerImageEnvironment dockerEnv;
  @Mock InternalRecipe recipe;
  @Mock private EntryPointParser entryPointParser;

  private Pod pod;
  private Map<String, InternalMachineConfig> machines;
  private DockerImageEnvironmentConverter converter;

  @BeforeMethod
  public void setup() throws Exception {
    converter = new DockerImageEnvironmentConverter(entryPointParser);

    lenient()
        .when(entryPointParser.parse(any()))
        .thenReturn(new EntryPoint(emptyList(), emptyList()));

    lenient().when(recipe.getContent()).thenReturn(RECIPE_CONTENT);
    lenient().when(recipe.getType()).thenReturn(RECIPE_TYPE);
    machines = ImmutableMap.of(MACHINE_NAME, mock(InternalMachineConfig.class));
    final Map<String, String> annotations = new HashMap<>();
    annotations.put(format(MACHINE_NAME_ANNOTATION_FMT, CONTAINER_NAME), MACHINE_NAME);
    pod =
        new PodBuilder()
            .withNewMetadata()
            .withName(POD_NAME)
            .withAnnotations(annotations)
            .endMetadata()
            .withNewSpec()
            .withContainers(
                new ContainerBuilder()
                    .withImage(RECIPE_CONTENT)
                    .withName(CONTAINER_NAME)
                    .withImagePullPolicy("Always")
                    .build())
            .endSpec()
            .build();
  }

  @Test
  public void testConvertsDockerImageEnvironment2KubernetesEnvironment() throws Exception {
    when(dockerEnv.getMachines()).thenReturn(machines);
    when(dockerEnv.getRecipe()).thenReturn(recipe);

    final KubernetesEnvironment actual = converter.convert(dockerEnv);

    assertEquals(pod, actual.getPodsCopy().values().iterator().next());
    assertEquals(recipe, actual.getRecipe());
    assertEquals(machines, actual.getMachines());
  }

  @Test
  public void shouldUseMachineConfigIfProvided() throws Exception {
    // given
    doReturn(new EntryPoint(singletonList("/teh/script"), asList("teh", "argz")))
        .when(entryPointParser)
        .parse(any());

    InternalMachineConfig machineConfig = mock(InternalMachineConfig.class);

    Map<String, InternalMachineConfig> machines = new HashMap<>(1);
    machines.put(MACHINE_NAME, machineConfig);

    when(dockerEnv.getMachines()).thenReturn(machines);
    when(dockerEnv.getRecipe()).thenReturn(recipe);

    Container ctn = pod.getSpec().getContainers().get(0);
    ctn.setCommand(singletonList("/teh/script"));
    ctn.setArgs(asList("teh", "argz"));

    // when
    KubernetesEnvironment env = converter.convert(dockerEnv);

    // then
    assertEquals(pod, env.getPodsCopy().values().iterator().next());
    assertEquals(recipe, env.getRecipe());
    assertEquals(machines, env.getMachines());
  }
}
