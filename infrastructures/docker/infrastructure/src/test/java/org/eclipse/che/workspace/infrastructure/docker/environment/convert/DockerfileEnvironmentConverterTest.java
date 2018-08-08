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
package org.eclipse.che.workspace.infrastructure.docker.environment.convert;

import static com.google.common.collect.Maps.newLinkedHashMap;
import static java.util.Collections.singletonMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import com.google.common.collect.ImmutableMap;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.workspace.server.spi.environment.InternalEnvironment;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.eclipse.che.api.workspace.server.spi.environment.InternalRecipe;
import org.eclipse.che.workspace.infrastructure.docker.environment.dockerfile.DockerfileEnvironment;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerBuildContext;
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
public class DockerfileEnvironmentConverterTest {

  private static final String DEFAULT_MACHINE_NAME = "dev-machine";
  private static final String DEFAULT_DOCKERFILE = "FROM codenvy/ubuntu_jdk8\n";

  @Mock private DockerfileEnvironment environment;
  @Mock private InternalRecipe recipe;
  @Mock private InternalMachineConfig machineConfig;

  private DockerfileEnvironmentConverter converter = new DockerfileEnvironmentConverter();

  @BeforeMethod
  public void setUp() throws Exception {
    when(environment.getRecipe()).thenReturn(recipe);
    when(environment.getMachines()).thenReturn(singletonMap(DEFAULT_MACHINE_NAME, machineConfig));
    when(recipe.getType()).thenReturn(DockerfileEnvironment.TYPE);
    when(environment.getDockerfileContent()).thenReturn(DEFAULT_DOCKERFILE);
  }

  @Test(
      expectedExceptions = ValidationException.class,
      expectedExceptionsMessageRegExp =
          "Environment of type '.*' doesn't support multiple machines, but contains machines: .*")
  public void shouldFailIfSeveralMachineConfigsArePresent() throws Exception {
    // given
    when(environment.getMachines())
        .thenReturn(
            ImmutableMap.of(DEFAULT_MACHINE_NAME, machineConfig, "anotherMachine", machineConfig));

    // when
    converter.convert(environment);
  }

  @Test
  public void shouldBeAbleToParseDockerfileEnvironment() throws Exception {
    // given
    DockerContainerConfig container =
        new DockerContainerConfig()
            .setBuild(new DockerBuildContext().setDockerfileContent(DEFAULT_DOCKERFILE));
    DockerEnvironment expected =
        new DockerEnvironment()
            .setContainers(newLinkedHashMap(singletonMap(DEFAULT_MACHINE_NAME, container)));
    expected.setMachines(singletonMap(DEFAULT_MACHINE_NAME, machineConfig));
    expected.setRecipe(recipe);

    // when
    DockerEnvironment actual = converter.convert(environment);

    // then
    assertEquals(actual, expected);
  }

  @Test(
      expectedExceptions = ValidationException.class,
      expectedExceptionsMessageRegExp = "The specified environment is not dockerfile environment")
  public void shouldThrowExceptionInCaseEnvironmentContainsNotSupportedRecipeType()
      throws Exception {
    // when
    converter.convert(mock(InternalEnvironment.class));
  }
}
