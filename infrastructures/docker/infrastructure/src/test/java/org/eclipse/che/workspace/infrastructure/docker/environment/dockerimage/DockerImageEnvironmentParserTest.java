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
package org.eclipse.che.workspace.infrastructure.docker.environment.dockerimage;

import static java.util.Collections.singletonMap;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import com.google.common.collect.ImmutableMap;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.workspace.server.spi.environment.InternalEnvironment;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.eclipse.che.api.workspace.server.spi.environment.InternalRecipe;
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
public class DockerImageEnvironmentParserTest {
  private static final String DEFAULT_MACHINE_NAME = "dev-machine";
  private static final String DEFAULT_DOCKER_IMAGE = "codenvy/ubuntu_jdk8";

  @Mock InternalEnvironment environment;
  @Mock InternalRecipe recipe;
  @Mock InternalMachineConfig machineConfig;

  public DockerImageEnvironmentParser parser = new DockerImageEnvironmentParser();

  @BeforeMethod
  public void setUp() throws Exception {
    when(environment.getRecipe()).thenReturn(recipe);
    when(environment.getMachines()).thenReturn(singletonMap(DEFAULT_MACHINE_NAME, machineConfig));
    when(recipe.getType()).thenReturn(DockerImageEnvironmentParser.TYPE);
    when(recipe.getContent()).thenReturn(DEFAULT_DOCKER_IMAGE);
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
    parser.parse(environment);
  }

  @Test
  public void shouldBeAbleToParseDockerImageEnvironment() throws Exception {
    // given
    DockerContainerConfig container = new DockerContainerConfig().setImage(DEFAULT_DOCKER_IMAGE);
    DockerEnvironment expected =
        new DockerEnvironment().setContainers(singletonMap(DEFAULT_MACHINE_NAME, container));

    // when
    DockerEnvironment actual = parser.parse(environment);

    // then
    assertEquals(actual, expected);
  }

  @Test(
    expectedExceptions = ValidationException.class,
    expectedExceptionsMessageRegExp =
        "Docker image environment parser doesn't support recipe type 'dockerfile'"
  )
  public void shouldThrowExceptionInCaseEnvironmentContainsNotSupportedRecipeType()
      throws Exception {
    // given
    when(recipe.getType()).thenReturn("dockerfile");

    // when
    parser.parse(environment);
  }
}
