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
package org.eclipse.che.workspace.infrastructure.docker.environment;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.workspace.server.model.impl.ServerConfigImpl;
import org.eclipse.che.api.workspace.server.spi.environment.InternalEnvironment;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.eclipse.che.api.workspace.server.spi.environment.InternalRecipe;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerContainerConfig;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerEnvironment;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/** @author Alexander Garagatyi */
@Listeners(MockitoTestNGListener.class)
public class EnvironmentParserTest {

  private static final String TEXT = "to be or not to be";
  private static final String DEFAULT_MACHINE_NAME = "dev-machine";

  @Mock InternalEnvironment environment;
  @Mock InternalRecipe recipe;
  @Mock InternalMachineConfig machine;
  @Mock DockerConfigSourceSpecificEnvironmentParser envParser;
  @Mock DockerEnvironment dockerEnv;
  @Mock DockerContainerConfig containerConfig;

  private EnvironmentParser parser;

  @BeforeMethod
  public void setUp() throws Exception {
    when(environment.getRecipe()).thenReturn(recipe);
    when(environment.getMachines()).thenReturn(singletonMap(DEFAULT_MACHINE_NAME, machine));
    when(recipe.getType()).thenReturn("type1");
    when(recipe.getContent()).thenReturn(TEXT);
    when(envParser.parse(environment)).thenReturn(dockerEnv);
    when(dockerEnv.getContainers()).thenReturn(singletonMap(DEFAULT_MACHINE_NAME, containerConfig));

    parser =
        new EnvironmentParser(
            ImmutableMap.of(
                "type1", envParser,
                "type2", envParser,
                "type3", envParser));
  }

  @Test(
    expectedExceptions = ValidationException.class,
    expectedExceptionsMessageRegExp =
        "Environment type '.*' is not supported. " + "Supported environment types: .*"
  )
  public void shouldThrowExceptionOnParsingUnknownEnvironmentType() throws Exception {
    when(recipe.getType()).thenReturn("unknownType");

    parser.parse(environment);
  }

  @Test(
    expectedExceptions = ValidationException.class,
    expectedExceptionsMessageRegExp = "Environment should not be null"
  )
  public void environmentShouldNotBeNull() throws Exception {
    parser.parse(null);
  }

  @Test(
    expectedExceptions = ValidationException.class,
    expectedExceptionsMessageRegExp = "Environment recipe should not be null"
  )
  public void environmentRecipeShouldNotBeNull() throws Exception {
    when(environment.getRecipe()).thenReturn(null);

    parser.parse(environment);
  }

  @Test(
    expectedExceptions = ValidationException.class,
    expectedExceptionsMessageRegExp = "Environment recipe type should not be null"
  )
  public void recipeTypeShouldNotBeNull() throws Exception {
    when(recipe.getType()).thenReturn(null);

    parser.parse(environment);
  }

  @Test(
    expectedExceptions = ValidationException.class,
    expectedExceptionsMessageRegExp = "Recipe of environment must contain content"
  )
  public void recipeShouldContainsContent() throws Exception {
    when(recipe.getContent()).thenReturn(null);

    parser.parse(environment);
  }

  @Test
  public void shouldBeAbleToParseEnvironment() throws Exception {
    // when
    DockerEnvironment actual = parser.parse(environment);

    // then
    assertEquals(actual, dockerEnv);
    verify(envParser).parse(environment);
  }

  @DataProvider(name = "environmentWithServersProvider")
  public static Object[][] environmentWithServersProvider() {
    // Format of result array:
    // [ [InternalMachineConfig object, expected exposes list, parsed representation of
    // environment], ... ]
    List<List<Object>> data = new ArrayList<>();

    // no exposes, servers -> no exposes
    data.add(asList(mockMachine(), createContainer(), emptyList()));

    // server port normalization
    data.add(
        asList(
            mockMachineWithServers(singletonMap("ref1", new ServerConfigImpl("8080", null, null))),
            createContainer(emptyList()),
            singletonList("8080/tcp")));

    // when expose match server port single value is used
    data.add(
        asList(
            mockMachineWithServers(
                singletonMap("ref1", new ServerConfigImpl("8080/tcp", null, null))),
            createContainer(singletonList("8080/tcp")),
            singletonList("8080/tcp")));

    // when expose match server port without protocol suffix single value is used
    data.add(
        asList(
            mockMachineWithServers(singletonMap("ref1", new ServerConfigImpl("8080", null, null))),
            createContainer(singletonList("8080/tcp")),
            singletonList("8080/tcp")));

    // when expose without protocol suffix match server port single value is used
    data.add(
        asList(
            mockMachineWithServers(
                singletonMap("ref1", new ServerConfigImpl("8080/tcp", null, null))),
            createContainer(singletonList("8080")),
            singletonList("8080/tcp")));

    // normalization of ports
    data.add(
        asList(
            mockMachineWithServers(singletonMap("ref1", new ServerConfigImpl("8080", null, null))),
            createContainer(singletonList("8080")),
            singletonList("8080/tcp")));

    // normalization of several servers
    data.add(
        asList(
            mockMachineWithServers(
                ImmutableMap.of(
                    "ref1",
                    new ServerConfigImpl("8080", null, null),
                    "ref2",
                    new ServerConfigImpl("9090", null, null))),
            createContainer(emptyList()),
            asList("8080/tcp", "9090/tcp")));

    // it's OK to have expose of single port with different protocols
    data.add(
        asList(
            mockMachineWithServers(
                ImmutableMap.of(
                    "ref1",
                    new ServerConfigImpl("8080/tcp", null, null),
                    "ref2",
                    new ServerConfigImpl("8080/udp", null, null))),
            createContainer(emptyList()),
            asList("8080/tcp", "8080/udp")));

    // it's OK to have expose of single port with different protocols
    data.add(
        asList(
            mockMachineWithServers(
                ImmutableMap.of(
                    "ref1",
                    new ServerConfigImpl("8080", null, null),
                    "ref2",
                    new ServerConfigImpl("8080/udp", null, null))),
            createContainer(emptyList()),
            asList("8080/tcp", "8080/udp")));

    // merging expose of container and servers ports
    data.add(
        asList(
            mockMachineWithServers(
                ImmutableMap.of(
                    "ref1",
                    new ServerConfigImpl("8080", null, null),
                    "ref2",
                    new ServerConfigImpl("9090/udp", null, null))),
            createContainer(asList("7070", "6060/udp")),
            asList("8080/tcp", "9090/udp", "7070/tcp", "6060/udp")));

    return data.stream()
        .map(list -> list.toArray(new Object[list.size()]))
        .toArray(value -> new Object[data.size()][]);
  }

  private static DockerContainerConfig createContainer() {
    return new DockerContainerConfig();
  }

  private static DockerContainerConfig createContainer(List<String> expose) {
    DockerContainerConfig container = createContainer();
    container.setExpose(expose);
    return container;
  }

  private static InternalMachineConfig mockMachine() {
    return mock(InternalMachineConfig.class);
  }

  private static InternalMachineConfig mockMachineWithServers(Map<String, ServerConfig> servers) {
    InternalMachineConfig mock = mock(InternalMachineConfig.class);
    when(mock.getServers()).thenReturn(new HashMap<>(servers));
    return mock;
  }

  private InternalMachineConfig mockMachine(Map<String, String> attributes) {
    InternalMachineConfig mock = mockMachine();
    when(mock.getAttributes()).thenReturn(new HashMap<>(attributes));
    return mock;
  }
}
