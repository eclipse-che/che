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
package org.eclipse.che.workspace.infrastructure.docker.environment.compose;

import static java.util.Arrays.fill;
import static org.eclipse.che.api.core.model.workspace.config.MachineConfig.MEMORY_LIMIT_ATTRIBUTE;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.api.installer.server.InstallerRegistry;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.eclipse.che.api.workspace.server.spi.environment.MachineConfigsValidator;
import org.eclipse.che.api.workspace.server.spi.environment.RecipeRetriever;
import org.eclipse.che.workspace.infrastructure.docker.environment.compose.model.ComposeService;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests {@link ComposeEnvironmentFactory}.
 *
 * @author Anton Korneta
 */
@Listeners(MockitoTestNGListener.class)
public class ComposeEnvironmentFactoryTest {

  private static final long DEFAULT_RAM_LIMIT_MB = 2048;
  private static final long BYTES_IN_MB = 1024 * 1024;
  private static final String MACHINE_NAME_1 = "machine1";
  private static final String MACHINE_NAME_2 = "machine2";

  @Mock private InstallerRegistry installerRegistry;
  @Mock private RecipeRetriever recipeRetriever;
  @Mock private MachineConfigsValidator machinesValidator;
  @Mock private ComposeEnvironmentValidator composeValidator;
  @Mock private ComposeServicesStartStrategy startStrategy;

  private ComposeEnvironmentFactory composeEnvironmentFactory;

  @BeforeMethod
  public void setup() {
    composeEnvironmentFactory =
        new ComposeEnvironmentFactory(
            installerRegistry,
            recipeRetriever,
            machinesValidator,
            composeValidator,
            startStrategy,
            DEFAULT_RAM_LIMIT_MB);
  }

  @Test
  public void testSetsRamLimitAttributeFromComposeService() throws Exception {
    final long firstMachineLimit = 3072 * BYTES_IN_MB;
    final long secondMachineLimit = 1028 * BYTES_IN_MB;
    final Map<String, InternalMachineConfig> machines =
        ImmutableMap.of(
            MACHINE_NAME_1,
            mockInternalMachineConfig(new HashMap<>()),
            MACHINE_NAME_2,
            mockInternalMachineConfig(new HashMap<>()));
    final Map<String, ComposeService> services =
        ImmutableMap.of(
            MACHINE_NAME_1,
            mockComposeService(firstMachineLimit),
            MACHINE_NAME_2,
            mockComposeService(secondMachineLimit));

    composeEnvironmentFactory.addRamLimitAttribute(machines, services);

    final long[] actual = machinesRam(machines.values());
    long[] expected = new long[] {firstMachineLimit, secondMachineLimit};
    assertTrue(Arrays.equals(actual, expected));
  }

  @Test
  public void testDoNotOverrideRamLimitAttributeWhenItAlreadyPresent() throws Exception {
    final long customRamLimit = 3072 * BYTES_IN_MB;
    final Map<String, String> attributes =
        ImmutableMap.of(MEMORY_LIMIT_ATTRIBUTE, String.valueOf(customRamLimit));
    final Map<String, InternalMachineConfig> machines =
        ImmutableMap.of(MACHINE_NAME_1, mockInternalMachineConfig(attributes));
    final Map<String, ComposeService> services =
        ImmutableMap.of(MACHINE_NAME_1, mockComposeService(0));

    composeEnvironmentFactory.addRamLimitAttribute(machines, services);

    final long[] actual = machinesRam(machines.values());
    final long[] expected = new long[actual.length];
    fill(expected, customRamLimit);
    assertTrue(Arrays.equals(actual, expected));
  }

  @Test
  public void testAddsMachineConfIntoEnvAndSetsRamLimAttributeWhenMachinePresentOnlyInRecipe()
      throws Exception {
    final long customRamLimit = 3072 * BYTES_IN_MB;
    final Map<String, InternalMachineConfig> machines = new HashMap<>();
    final Map<String, ComposeService> services =
        ImmutableMap.of(MACHINE_NAME_1, mockComposeService(customRamLimit));

    composeEnvironmentFactory.addRamLimitAttribute(machines, services);

    final long[] actual = machinesRam(machines.values());
    final long[] expected = new long[actual.length];
    fill(expected, customRamLimit);
    assertTrue(Arrays.equals(actual, expected));
  }

  @Test
  public void testSetRamLimitAttributeWhenRamLimitIsMissingInRecipeAndConfig() throws Exception {
    final Map<String, InternalMachineConfig> machines =
        ImmutableMap.of(MACHINE_NAME_1, mockInternalMachineConfig(new HashMap<>()));
    final Map<String, ComposeService> services =
        ImmutableMap.of(MACHINE_NAME_1, mockComposeService(0));

    composeEnvironmentFactory.addRamLimitAttribute(machines, services);

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

  private static ComposeService mockComposeService(long ramLimit) {
    final ComposeService composeServiceMock = mock(ComposeService.class);
    when(composeServiceMock.getMemLimit()).thenReturn(ramLimit);
    return composeServiceMock;
  }

  private static long[] machinesRam(Collection<InternalMachineConfig> configs) {
    return configs
        .stream()
        .mapToLong(m -> Long.parseLong(m.getAttributes().get(MEMORY_LIMIT_ATTRIBUTE)))
        .toArray();
  }
}
