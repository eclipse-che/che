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
import static org.eclipse.che.api.core.model.workspace.config.MachineConfig.MEMORY_REQUEST_ATTRIBUTE;
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
  private static final long DEFAULT_RAM_REQUEST_MB = 1024;
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
            DEFAULT_RAM_LIMIT_MB,
            DEFAULT_RAM_REQUEST_MB);
  }

  @Test
  public void testSetsRamAttributesFromComposeService() throws Exception {
    final long firstMachineLimit = 3072 * BYTES_IN_MB;
    final long firstMachineRequest = 1536 * BYTES_IN_MB;
    final long secondMachineLimit = 1028 * BYTES_IN_MB;
    final long secondMachineRequest = 512 * BYTES_IN_MB;
    final Map<String, InternalMachineConfig> machines =
        ImmutableMap.of(
            MACHINE_NAME_1,
            mockInternalMachineConfig(new HashMap<>()),
            MACHINE_NAME_2,
            mockInternalMachineConfig(new HashMap<>()));
    final Map<String, ComposeService> services =
        ImmutableMap.of(
            MACHINE_NAME_1,
            mockComposeService(firstMachineLimit, firstMachineRequest),
            MACHINE_NAME_2,
            mockComposeService(secondMachineLimit, secondMachineRequest));

    composeEnvironmentFactory.addRamAttributes(machines, services);

    final long[] actualLimits = machinesRam(machines.values(), MEMORY_LIMIT_ATTRIBUTE);
    long[] expectedLimits = new long[] {firstMachineLimit, secondMachineLimit};
    final long[] actualRequests = machinesRam(machines.values(), MEMORY_REQUEST_ATTRIBUTE);
    long[] expectedRequests = new long[] {firstMachineRequest, secondMachineRequest};
    assertTrue(Arrays.equals(actualLimits, expectedLimits));
    assertTrue(Arrays.equals(actualRequests, expectedRequests));
  }

  @Test
  public void testDoNotOverrideRamAttributesWhenTheyAlreadyPresent() throws Exception {
    final long customRamLimit = 3072 * BYTES_IN_MB;
    final long customRamRequest = 1536 * BYTES_IN_MB;
    final Map<String, String> attributes =
        ImmutableMap.of(
            MEMORY_LIMIT_ATTRIBUTE,
            String.valueOf(customRamLimit),
            MEMORY_REQUEST_ATTRIBUTE,
            String.valueOf(customRamRequest));
    final Map<String, InternalMachineConfig> machines =
        ImmutableMap.of(MACHINE_NAME_1, mockInternalMachineConfig(attributes));
    final Map<String, ComposeService> services =
        ImmutableMap.of(MACHINE_NAME_1, mockComposeService(0, 0));

    composeEnvironmentFactory.addRamAttributes(machines, services);

    final long[] actualLimits = machinesRam(machines.values(), MEMORY_LIMIT_ATTRIBUTE);
    final long[] expectedLimits = new long[actualLimits.length];
    fill(expectedLimits, customRamLimit);
    final long[] actualRequests = machinesRam(machines.values(), MEMORY_REQUEST_ATTRIBUTE);
    final long[] expectedRequests = new long[actualRequests.length];
    fill(expectedRequests, customRamRequest);
    assertTrue(Arrays.equals(actualLimits, expectedLimits));
    assertTrue(Arrays.equals(actualRequests, expectedRequests));
  }

  @Test
  public void testAddsMachineConfIntoEnvAndSetsRamLimAttributeWhenMachinePresentOnlyInRecipe()
      throws Exception {
    final long customRamLimit = 3072 * BYTES_IN_MB;
    final long customRamRequest = 1536 * BYTES_IN_MB;
    final Map<String, InternalMachineConfig> machines = new HashMap<>();
    final Map<String, ComposeService> services =
        ImmutableMap.of(MACHINE_NAME_1, mockComposeService(customRamLimit, customRamRequest));

    composeEnvironmentFactory.addRamAttributes(machines, services);

    final long[] actualLimits = machinesRam(machines.values(), MEMORY_LIMIT_ATTRIBUTE);
    final long[] expectedLimits = new long[actualLimits.length];
    fill(expectedLimits, customRamLimit);
    assertTrue(Arrays.equals(actualLimits, expectedLimits));
    final long[] actualRequests = machinesRam(machines.values(), MEMORY_REQUEST_ATTRIBUTE);
    final long[] expectedRequests = new long[actualRequests.length];
    fill(expectedRequests, customRamRequest);
    assertTrue(Arrays.equals(actualRequests, expectedRequests));
  }

  @Test
  public void testSetRamAttributesWhenTheyAreMissingInRecipeAndConfig() throws Exception {
    final Map<String, InternalMachineConfig> machines =
        ImmutableMap.of(MACHINE_NAME_1, mockInternalMachineConfig(new HashMap<>()));
    final Map<String, ComposeService> services =
        ImmutableMap.of(MACHINE_NAME_1, mockComposeService(0, 0));

    composeEnvironmentFactory.addRamAttributes(machines, services);

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
  public void testSetRamAttributesIgnoresRequestDefaultGreaterThanLimit() throws Exception {
    composeEnvironmentFactory =
        new ComposeEnvironmentFactory(
            installerRegistry,
            recipeRetriever,
            machinesValidator,
            composeValidator,
            startStrategy,
            1024,
            2048);
    final Map<String, InternalMachineConfig> machines =
        ImmutableMap.of(MACHINE_NAME_1, mockInternalMachineConfig(new HashMap<>()));
    final Map<String, ComposeService> services =
        ImmutableMap.of(MACHINE_NAME_1, mockComposeService(0, 0));

    composeEnvironmentFactory.addRamAttributes(machines, services);

    final long[] actualLimits = machinesRam(machines.values(), MEMORY_LIMIT_ATTRIBUTE);
    final long[] expectedLimits = new long[actualLimits.length];
    fill(expectedLimits, 1024 * BYTES_IN_MB);
    assertTrue(Arrays.equals(actualLimits, expectedLimits));
    final long[] actualRequests = machinesRam(machines.values(), MEMORY_REQUEST_ATTRIBUTE);
    final long[] expectedRequests = new long[actualRequests.length];
    fill(expectedRequests, 1024 * BYTES_IN_MB);
    assertTrue(Arrays.equals(actualRequests, expectedRequests));
  }

  @Test
  public void testSetsRamAttributesFromWhenOnlyLimitPresentInComposeServiceSetsRequestEqualToLimit()
      throws Exception {
    final long firstMachineLimit = 3072 * BYTES_IN_MB;
    final Map<String, InternalMachineConfig> machines =
        ImmutableMap.of(MACHINE_NAME_1, mockInternalMachineConfig(new HashMap<>()));
    final Map<String, ComposeService> services =
        ImmutableMap.of(MACHINE_NAME_1, mockComposeService(firstMachineLimit, 0));

    composeEnvironmentFactory.addRamAttributes(machines, services);

    final long[] actualLimits = machinesRam(machines.values(), MEMORY_LIMIT_ATTRIBUTE);
    long[] expectedLimits = new long[] {firstMachineLimit};
    final long[] actualRequests = machinesRam(machines.values(), MEMORY_REQUEST_ATTRIBUTE);
    long[] expectedRequests = new long[] {firstMachineLimit};
    assertTrue(Arrays.equals(actualLimits, expectedLimits));
    assertTrue(Arrays.equals(actualRequests, expectedRequests));
  }

  @Test
  public void
      testSetsRamAttributesFromWhenOnlyRequestPresentInComposeServiceSetsLimitEqualToRequest()
          throws Exception {
    final long firstMachineRequest = 1536 * BYTES_IN_MB;
    final Map<String, InternalMachineConfig> machines =
        ImmutableMap.of(MACHINE_NAME_1, mockInternalMachineConfig(new HashMap<>()));
    final Map<String, ComposeService> services =
        ImmutableMap.of(MACHINE_NAME_1, mockComposeService(0, firstMachineRequest));

    composeEnvironmentFactory.addRamAttributes(machines, services);

    final long[] actualLimits = machinesRam(machines.values(), MEMORY_LIMIT_ATTRIBUTE);
    long[] expectedLimits = new long[] {firstMachineRequest};
    final long[] actualRequests = machinesRam(machines.values(), MEMORY_REQUEST_ATTRIBUTE);
    long[] expectedRequests = new long[] {firstMachineRequest};
    assertTrue(Arrays.equals(actualLimits, expectedLimits));
    assertTrue(Arrays.equals(actualRequests, expectedRequests));
  }

  @Test
  public void testSetsRamAttributesFromWhenRequestIsGreaterThanLimitInComposeServiceIgnoresRequest()
      throws Exception {
    final long firstMachineRequest = 3072 * BYTES_IN_MB;
    final long firstMachineLimit = 1536 * BYTES_IN_MB;
    final Map<String, InternalMachineConfig> machines =
        ImmutableMap.of(MACHINE_NAME_1, mockInternalMachineConfig(new HashMap<>()));
    final Map<String, ComposeService> services =
        ImmutableMap.of(MACHINE_NAME_1, mockComposeService(firstMachineLimit, firstMachineRequest));

    composeEnvironmentFactory.addRamAttributes(machines, services);

    final long[] actualLimits = machinesRam(machines.values(), MEMORY_LIMIT_ATTRIBUTE);
    long[] expectedLimits = new long[] {firstMachineLimit};
    final long[] actualRequests = machinesRam(machines.values(), MEMORY_REQUEST_ATTRIBUTE);
    long[] expectedRequests = new long[] {firstMachineLimit};
    assertTrue(Arrays.equals(actualLimits, expectedLimits));
    assertTrue(Arrays.equals(actualRequests, expectedRequests));
  }

  private static InternalMachineConfig mockInternalMachineConfig(Map<String, String> attributes) {
    final InternalMachineConfig machineConfigMock = mock(InternalMachineConfig.class);
    when(machineConfigMock.getAttributes()).thenReturn(attributes);
    return machineConfigMock;
  }

  private static ComposeService mockComposeService(long ramLimit, long ramRequest) {
    final ComposeService composeServiceMock = mock(ComposeService.class);
    when(composeServiceMock.getMemLimit()).thenReturn(ramLimit);
    when(composeServiceMock.getMemRequest()).thenReturn(ramRequest);
    return composeServiceMock;
  }

  private static long[] machinesRam(Collection<InternalMachineConfig> configs, String attribute) {
    return configs
        .stream()
        .mapToLong(m -> Long.parseLong(m.getAttributes().get(attribute)))
        .toArray();
  }
}
