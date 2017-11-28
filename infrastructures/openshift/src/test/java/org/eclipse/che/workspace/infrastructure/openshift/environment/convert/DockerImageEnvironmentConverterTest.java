/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.openshift.environment.convert;

import static java.lang.String.format;
import static java.util.Collections.emptyMap;
import static org.eclipse.che.workspace.infrastructure.openshift.Constants.MACHINE_NAME_ANNOTATION_FMT;
import static org.eclipse.che.workspace.infrastructure.openshift.environment.convert.DockerImageEnvironmentConverter.CONTAINER_NAME;
import static org.eclipse.che.workspace.infrastructure.openshift.environment.convert.DockerImageEnvironmentConverter.POD_NAME;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;

import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.eclipse.che.api.workspace.server.spi.environment.InternalRecipe;
import org.eclipse.che.workspace.infrastructure.docker.environment.dockerimage.DockerImageEnvironment;
import org.eclipse.che.workspace.infrastructure.openshift.environment.OpenShiftEnvironment;
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

  private Pod pod;
  private Map<String, InternalMachineConfig> machines;
  private DockerImageEnvironmentConverter converter;

  @BeforeMethod
  public void setup() throws Exception {
    converter = new DockerImageEnvironmentConverter();
    when(recipe.getContent()).thenReturn(RECIPE_CONTENT);
    when(recipe.getType()).thenReturn(RECIPE_TYPE);
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
                new ContainerBuilder().withImage(RECIPE_CONTENT).withName(CONTAINER_NAME).build())
            .endSpec()
            .build();
  }

  @Test
  public void testConvertsDockerImageEnvironment2OpenShiftEnvironment() throws Exception {
    when(dockerEnv.getMachines()).thenReturn(machines);
    when(dockerEnv.getRecipe()).thenReturn(recipe);

    final OpenShiftEnvironment actual = converter.convert(dockerEnv);

    assertEquals(pod, actual.getPods().values().iterator().next());
    assertEquals(recipe, actual.getRecipe());
    assertEquals(machines, actual.getMachines());
  }

  @Test(
    expectedExceptions = InfrastructureException.class,
    expectedExceptionsMessageRegExp =
        "DockerImage environment must contain at least one machine configuration"
  )
  public void throwsValidationExceptionWhenNoMachineConfigProvided() throws Exception {
    when(dockerEnv.getMachines()).thenReturn(emptyMap());

    converter.convert(dockerEnv);
  }
}
