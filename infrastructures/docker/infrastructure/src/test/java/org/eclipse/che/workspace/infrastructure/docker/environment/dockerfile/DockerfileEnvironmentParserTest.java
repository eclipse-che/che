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
package org.eclipse.che.workspace.infrastructure.docker.environment.dockerfile;

import static java.util.Collections.singletonMap;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import com.google.common.collect.ImmutableMap;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.workspace.server.spi.environment.InternalEnvironment;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.eclipse.che.api.workspace.server.spi.environment.InternalRecipe;
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
public class DockerfileEnvironmentParserTest {
  private static final String DEFAULT_MACHINE_NAME = "dev-machine";
  private static final String DEFAULT_DOCKERFILE = "FROM codenvy/ubuntu_jdk8\n";

  @Mock InternalEnvironment environment;
  @Mock InternalRecipe recipe;
  @Mock InternalMachineConfig machineConfig;

  DockerfileEnvironmentParser parser = new DockerfileEnvironmentParser();

  @BeforeMethod
  public void setUp() throws Exception {
    when(environment.getRecipe()).thenReturn(recipe);
    when(environment.getMachines()).thenReturn(singletonMap(DEFAULT_MACHINE_NAME, machineConfig));
    when(recipe.getType()).thenReturn(DockerfileEnvironmentParser.TYPE);
    when(recipe.getContent()).thenReturn(DEFAULT_DOCKERFILE);
    when(recipe.getContentType()).thenReturn(DockerfileEnvironmentParser.CONTENT_TYPE);
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
  public void shouldBeAbleToParseDockerfileEnvironment() throws Exception {
    // given
    DockerContainerConfig container =
        new DockerContainerConfig()
            .setBuild(new DockerBuildContext().setDockerfileContent(DEFAULT_DOCKERFILE));
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
        "Dockerfile environment parser doesn't support recipe type 'dockerimage'"
  )
  public void shouldThrowExceptionInCaseEnvironmentContainsNotSupportedRecipeType()
      throws Exception {
    // given
    when(recipe.getType()).thenReturn("dockerimage");

    // when
    parser.parse(environment);
  }

  @Test(
    expectedExceptions = ValidationException.class,
    expectedExceptionsMessageRegExp =
        "Content type '.*' of recipe of environment is unsupported."
            + " Supported values are: text/x-dockerfile"
  )
  public void shouldThrowExceptionOnParseOfDockerfileEnvWithNotSupportedContentType()
      throws Exception {
    // given
    when(recipe.getContentType()).thenReturn("dockerfile");

    // when
    parser.parse(environment);
  }
}
