/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.docker.environment.dockerfile;

import static java.util.Arrays.fill;
import static org.eclipse.che.api.core.model.workspace.config.MachineConfig.MEMORY_LIMIT_ATTRIBUTE;
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
public class DockerfileEnvironmentFactoryTest {

  private static final long DEFAULT_RAM_LIMIT_MB = 2048;
  private static final long BYTES_IN_MB = 1024 * 1024;
  private static final String MACHINE_NAME = "machine";

  @Mock private InternalRecipe recipe;
  @Mock private InstallerRegistry installerRegistry;
  @Mock private RecipeRetriever recipeRetriever;
  @Mock private MachineConfigsValidator machinesValidator;

  private DockerfileEnvironmentFactory factory;

  @BeforeMethod
  public void setUp() throws Exception {
    factory =
        new DockerfileEnvironmentFactory(
            installerRegistry, recipeRetriever, machinesValidator, DEFAULT_RAM_LIMIT_MB);

    when(recipe.getType()).thenReturn(DockerfileEnvironment.TYPE);
    when(recipe.getContent()).thenReturn("");
  }

  @Test
  public void testSetRamLimitAttributeWhenRamLimitIsMissingInConfig() throws Exception {
    final Map<String, InternalMachineConfig> machines =
        ImmutableMap.of(MACHINE_NAME, mockInternalMachineConfig(new HashMap<>()));

    factory.doCreate(recipe, machines, Collections.emptyList());

    final long[] actual = machinesRam(machines.values());
    final long[] expected = new long[actual.length];
    fill(expected, DEFAULT_RAM_LIMIT_MB * BYTES_IN_MB);
    assertTrue(Arrays.equals(actual, expected));
  }

  private static InternalMachineConfig mockInternalMachineConfig(Map<String, String> attributes) {
    final InternalMachineConfig machineConfigMock = mock(InternalMachineConfig.class);
    when(machineConfigMock.getAttributes()).thenReturn(attributes);
    return machineConfigMock;
  }

  private static long[] machinesRam(Collection<InternalMachineConfig> configs) {
    return configs
        .stream()
        .mapToLong(m -> Long.parseLong(m.getAttributes().get(MEMORY_LIMIT_ATTRIBUTE)))
        .toArray();
  }
}
