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
package org.eclipse.che.workspace.infrastructure.docker.environment.dockerimage;

import static java.util.Arrays.fill;
import static org.eclipse.che.api.core.model.workspace.config.MachineConfig.MEMORY_LIMIT_ATTRIBUTE;
import static org.eclipse.che.api.core.model.workspace.config.MachineConfig.MEMORY_REQUEST_ATTRIBUTE;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.api.installer.server.InstallerRegistry;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.eclipse.che.api.workspace.server.spi.environment.InternalRecipe;
import org.eclipse.che.api.workspace.server.spi.environment.MachineConfigsValidator;
import org.eclipse.che.api.workspace.server.spi.environment.RecipeRetriever;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/** @author Alexander Garagatyi */
@Listeners(MockitoTestNGListener.class)
public class DockerImageEnvironmentFactoryTest {
  private static final long DEFAULT_RAM_LIMIT_MB = 2048;
  private static final long DEFAULT_RAM_REQUEST_MB = 1024;
  private static final long BYTES_IN_MB = 1024 * 1024;
  private static final String MACHINE_NAME = "machine";

  @Mock private InternalRecipe recipe;
  @Mock private InstallerRegistry installerRegistry;
  @Mock private RecipeRetriever recipeRetriever;
  @Mock private MachineConfigsValidator machinesValidator;

  private DockerImageEnvironmentFactory factory;

  @BeforeMethod
  public void setUp() throws Exception {
    factory =
        new DockerImageEnvironmentFactory(
            installerRegistry,
            recipeRetriever,
            machinesValidator,
            DEFAULT_RAM_LIMIT_MB,
            DEFAULT_RAM_REQUEST_MB);

    when(recipe.getType()).thenReturn(DockerImageEnvironment.TYPE);
    when(recipe.getContent()).thenReturn("");
  }

  @Test
  public void testSetRamAttributesWhenTheyAreMissingInConfig() throws Exception {
    final Map<String, InternalMachineConfig> machines =
        ImmutableMap.of(MACHINE_NAME, mockInternalMachineConfig(new HashMap<>()));

    factory.doCreate(recipe, machines, Collections.emptyList());

    final long[] actualLimits = machinesRam(machines.values(), MEMORY_LIMIT_ATTRIBUTE);
    final long[] expectedLimits = new long[actualLimits.length];
    fill(expectedLimits, DEFAULT_RAM_LIMIT_MB * BYTES_IN_MB);
    assertTrue(Arrays.equals(actualLimits, expectedLimits));
    final long[] actualRequests = machinesRam(machines.values(), MEMORY_REQUEST_ATTRIBUTE);
    final long[] expectedRequests = new long[actualRequests.length];
    fill(expectedRequests, DEFAULT_RAM_REQUEST_MB * BYTES_IN_MB);
    assertTrue(Arrays.equals(actualRequests, expectedRequests));
  }

  @Test
  public void testRamDefaultMemoryRequestIsIgnoredIfGreaterThanDefaultRamLimit() throws Exception {
    factory =
        new DockerImageEnvironmentFactory(
            installerRegistry, recipeRetriever, machinesValidator, 1024, 2048);
    final Map<String, InternalMachineConfig> machines =
        ImmutableMap.of(MACHINE_NAME, mockInternalMachineConfig(new HashMap<>()));

    factory.doCreate(recipe, machines, Collections.emptyList());

    final long[] actualLimits = machinesRam(machines.values(), MEMORY_LIMIT_ATTRIBUTE);
    final long[] expectedLimits = new long[actualLimits.length];
    fill(expectedLimits, 1024 * BYTES_IN_MB);
    assertTrue(Arrays.equals(actualLimits, expectedLimits));
    final long[] actualRequests = machinesRam(machines.values(), MEMORY_REQUEST_ATTRIBUTE);
    final long[] expectedRequests = new long[actualRequests.length];
    fill(expectedRequests, 1024 * BYTES_IN_MB);
    assertTrue(Arrays.equals(actualRequests, expectedRequests));
  }

  private static InternalMachineConfig mockInternalMachineConfig(Map<String, String> attributes) {
    final InternalMachineConfig machineConfigMock = mock(InternalMachineConfig.class);
    when(machineConfigMock.getAttributes()).thenReturn(attributes);
    return machineConfigMock;
  }

  private static long[] machinesRam(Collection<InternalMachineConfig> configs, String attribute) {
    return configs
        .stream()
        .mapToLong(m -> Long.parseLong(m.getAttributes().get(attribute)))
        .toArray();
  }
}
