/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.docker.environment.convert;

import static com.google.common.collect.Maps.newLinkedHashMap;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.collections.Maps.newLinkedHashMap;

import com.google.common.collect.ImmutableMap;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.workspace.server.spi.environment.InternalEnvironment;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.eclipse.che.api.workspace.server.spi.environment.InternalRecipe;
import org.eclipse.che.workspace.infrastructure.docker.environment.dockerimage.DockerImageEnvironment;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerContainerConfig;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerEnvironment;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * @author Alexander Garagatyi
 * @author Alexander Andrienko
 */
@Listeners(MockitoTestNGListener.class)
public class DockerImageEnvironmentConverterTest {
  private static final String DEFAULT_MACHINE_NAME = "dev-machine";
  private static final String DEFAULT_DOCKER_IMAGE = "codenvy/ubuntu_jdk8";

  @Mock private DockerImageEnvironment environment;
  @Mock private InternalRecipe recipe;
  @Mock private InternalMachineConfig machineConfig;

  public DockerImageEnvironmentConverter converter = new DockerImageEnvironmentConverter();

  @BeforeMethod
  public void setUp() throws Exception {
    when(recipe.getType()).thenReturn(DockerImageEnvironment.TYPE);

    when(environment.getDockerImage()).thenReturn(DEFAULT_DOCKER_IMAGE);
    when(environment.getMachines()).thenReturn(singletonMap(DEFAULT_MACHINE_NAME, machineConfig));
    when(environment.getRecipe()).thenReturn(recipe);
  }

  @Test(
    expectedExceptions = ValidationException.class,
    expectedExceptionsMessageRegExp =
        "Environment of type '.*' doesn't support multiple machines, but contains machines: .*"
  )
  public void shouldFailIfSeveralMachineConfigsArePresent() throws Exception {
    // given
    when(environment.getMachines())
        .thenReturn(
            ImmutableMap.of(DEFAULT_MACHINE_NAME, machineConfig, "anotherMachine", machineConfig));

    // when
    converter.convert(environment);
  }

  @Test
  public void shouldBeAbleToConvertToDockerEnvironment() throws Exception {
    // given
    DockerContainerConfig expectedContainer =
        new DockerContainerConfig().setImage(DEFAULT_DOCKER_IMAGE);
    DockerEnvironment expectedEnv =
        new DockerEnvironment(
                recipe, singletonMap(DEFAULT_MACHINE_NAME, machineConfig), emptyList())
            .setContainers(newLinkedHashMap(singletonMap(DEFAULT_MACHINE_NAME, expectedContainer)));

    // when
    DockerEnvironment actual = converter.convert(environment);

    // then
    assertEquals(actual, expectedEnv);
  }

  @Test(
    expectedExceptions = ValidationException.class,
    expectedExceptionsMessageRegExp = "The specified environment is not docker image environment"
  )
  public void shouldThrowExceptionInCaseEnvironmentHasWrongType() throws Exception {
    // when
    converter.convert(mock(InternalEnvironment.class));
  }
}
