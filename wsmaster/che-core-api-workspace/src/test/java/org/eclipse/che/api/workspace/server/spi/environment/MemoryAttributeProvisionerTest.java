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
package org.eclipse.che.api.workspace.server.spi.environment;

import static org.eclipse.che.api.core.model.workspace.config.MachineConfig.MEMORY_LIMIT_ATTRIBUTE;
import static org.eclipse.che.api.core.model.workspace.config.MachineConfig.MEMORY_REQUEST_ATTRIBUTE;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;

import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Map;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/** Tests {@link MemoryAttributeProvisioner} */
@Listeners(MockitoTestNGListener.class)
public class MemoryAttributeProvisionerTest {

  private MemoryAttributeProvisioner memoryAttributeProvisioner;

  @Test
  public void testSetsRamDefaultAttributesWhenTheyAreMissingInConfigAndNotPassedInRecipe() {
    long defaultMemoryLimit = 2048L;
    long defaultMemoryRequest = 1024L;
    InternalMachineConfig machineConfig =
        getInternalMachineConfig(defaultMemoryLimit, defaultMemoryRequest, new HashMap<>());

    memoryAttributeProvisioner.provision(machineConfig, 0L, 0L);
    long memLimit = Long.parseLong(machineConfig.getAttributes().get(MEMORY_LIMIT_ATTRIBUTE));
    long memRequest = Long.parseLong(machineConfig.getAttributes().get(MEMORY_REQUEST_ATTRIBUTE));

    assertEquals(memLimit, defaultMemoryLimit * 1024 * 1024);
    assertEquals(memRequest, defaultMemoryRequest * 1024 * 1024);
  }

  @Test
  public void testRamDefaultMemoryRequestIsIgnoredIfGreaterThanDefaultRamLimit() {
    long defaultMemoryLimit = 1024L;
    long defaultMemoryRequest = 2048L;
    InternalMachineConfig machineConfig =
        getInternalMachineConfig(defaultMemoryLimit, defaultMemoryRequest, new HashMap<>());

    memoryAttributeProvisioner.provision(machineConfig, 0L, 0L);
    long memLimit = Long.parseLong(machineConfig.getAttributes().get(MEMORY_LIMIT_ATTRIBUTE));
    long memRequest = Long.parseLong(machineConfig.getAttributes().get(MEMORY_REQUEST_ATTRIBUTE));

    assertEquals(memLimit, defaultMemoryLimit * 1024 * 1024);
    assertEquals(memRequest, defaultMemoryLimit * 1024 * 1024);
  }

  @Test
  public void testRamAttributesAreTakenFromRecipeWhenNotPresentInConfig() {
    long defaultMemoryLimit = 1024L;
    long defaultMemoryRequest = 2048L;
    InternalMachineConfig machineConfig =
        getInternalMachineConfig(defaultMemoryLimit, defaultMemoryRequest, new HashMap<>());

    long recipeLimit = 4096L;
    long recipeRequest = 2048L;
    memoryAttributeProvisioner.provision(machineConfig, recipeLimit, recipeRequest);

    assertEquals(
        machineConfig.getAttributes().get(MEMORY_LIMIT_ATTRIBUTE), String.valueOf(recipeLimit));
    assertEquals(
        machineConfig.getAttributes().get(MEMORY_REQUEST_ATTRIBUTE), String.valueOf(recipeRequest));
  }

  @Test
  public void
      testWhenRamAttributesTakenFromRecipeAreInconsistentAndNotPresentInConfigRequestIsIgnored() {
    long defaultMemoryLimit = 1024L;
    long defaultMemoryRequest = 2048L;
    InternalMachineConfig machineConfig =
        getInternalMachineConfig(defaultMemoryLimit, defaultMemoryRequest, new HashMap<>());

    // inconsistent attributes mean request > limit
    long recipeLimit = 2048L;
    long recipeRequest = 4096L;
    memoryAttributeProvisioner.provision(machineConfig, recipeLimit, recipeRequest);

    assertEquals(
        machineConfig.getAttributes().get(MEMORY_LIMIT_ATTRIBUTE), String.valueOf(recipeLimit));
    assertEquals(
        machineConfig.getAttributes().get(MEMORY_REQUEST_ATTRIBUTE), String.valueOf(recipeLimit));
  }

  @Test
  public void testWhenRamAttributesArePresentInMachineConfigValuesInRecipeAreIgnored() {
    long defaultMemoryLimit = 1024L;
    long defaultMemoryRequest = 2048L;
    InternalMachineConfig machineConfig =
        getInternalMachineConfig(
            defaultMemoryLimit,
            defaultMemoryRequest,
            ImmutableMap.of(MEMORY_LIMIT_ATTRIBUTE, "1526", MEMORY_REQUEST_ATTRIBUTE, "512"));

    long recipeLimit = 4096L;
    long recipeRequest = 2048L;
    memoryAttributeProvisioner.provision(machineConfig, recipeLimit, recipeRequest);

    assertEquals(machineConfig.getAttributes().get(MEMORY_LIMIT_ATTRIBUTE), String.valueOf(1526L));
    assertEquals(machineConfig.getAttributes().get(MEMORY_REQUEST_ATTRIBUTE), String.valueOf(512L));
  }

  @Test
  public void testWhenRamRequestAttributeIsPresentInMachineConfigValuesInRecipeAreIgnored() {
    long defaultMemoryLimit = 1024L;
    long defaultMemoryRequest = 2048L;
    Map<String, String> attributes = new HashMap<>();
    attributes.put(MEMORY_REQUEST_ATTRIBUTE, "512");
    InternalMachineConfig machineConfig =
        getInternalMachineConfig(defaultMemoryLimit, defaultMemoryRequest, attributes);

    long recipeLimit = 4096L;
    long recipeRequest = 2048L;
    memoryAttributeProvisioner.provision(machineConfig, recipeLimit, recipeRequest);

    assertEquals(machineConfig.getAttributes().get(MEMORY_LIMIT_ATTRIBUTE), String.valueOf(512L));
    assertEquals(machineConfig.getAttributes().get(MEMORY_REQUEST_ATTRIBUTE), String.valueOf(512L));
  }

  @Test
  public void testWhenRamLimitAttributeIsPresentInMachineConfigValuesInRecipeAreIgnored() {
    long defaultMemoryLimit = 1024L;
    long defaultMemoryRequest = 2048L;
    Map<String, String> attributes = new HashMap<>();
    attributes.put(MEMORY_LIMIT_ATTRIBUTE, "1526");
    InternalMachineConfig machineConfig =
        getInternalMachineConfig(defaultMemoryLimit, defaultMemoryRequest, attributes);

    long recipeLimit = 4096L;
    long recipeRequest = 2048L;
    memoryAttributeProvisioner.provision(machineConfig, recipeLimit, recipeRequest);

    assertEquals(machineConfig.getAttributes().get(MEMORY_LIMIT_ATTRIBUTE), String.valueOf(1526L));
    assertEquals(
        machineConfig.getAttributes().get(MEMORY_REQUEST_ATTRIBUTE), String.valueOf(1526L));
  }

  @Test
  public void testWhenRamAttributesAreNotPresentInMachineConfigAndOnlyRequestIsProvidedInRecipe() {
    long defaultMemoryLimit = 1024L;
    long defaultMemoryRequest = 2048L;
    InternalMachineConfig machineConfig =
        getInternalMachineConfig(defaultMemoryLimit, defaultMemoryRequest, new HashMap<>());

    long recipeRequest = 1526L;
    memoryAttributeProvisioner.provision(machineConfig, null, recipeRequest);

    assertEquals(
        machineConfig.getAttributes().get(MEMORY_LIMIT_ATTRIBUTE), String.valueOf(recipeRequest));
    assertEquals(
        machineConfig.getAttributes().get(MEMORY_REQUEST_ATTRIBUTE), String.valueOf(recipeRequest));
  }

  @Test
  public void testWhenRamAttributesAreNotPresentInMachineConfigAndOnlyLimitIsProvidedInRecipe() {
    long defaultMemoryLimit = 1024L;
    long defaultMemoryRequest = 2048L;
    InternalMachineConfig machineConfig =
        getInternalMachineConfig(defaultMemoryLimit, defaultMemoryRequest, new HashMap<>());

    long recipeLimit = 1526L;
    memoryAttributeProvisioner.provision(machineConfig, recipeLimit, null);

    assertEquals(
        machineConfig.getAttributes().get(MEMORY_LIMIT_ATTRIBUTE), String.valueOf(recipeLimit));
    assertEquals(
        machineConfig.getAttributes().get(MEMORY_REQUEST_ATTRIBUTE), String.valueOf(recipeLimit));
  }

  private InternalMachineConfig getInternalMachineConfig(
      long defaultMemoryLimit, long defaultMemoryRequest, Map<String, String> attributes) {
    memoryAttributeProvisioner =
        new MemoryAttributeProvisioner(defaultMemoryLimit, defaultMemoryRequest);
    return mockInternalMachineConfig(attributes);
  }

  private static InternalMachineConfig mockInternalMachineConfig(Map<String, String> attributes) {
    final InternalMachineConfig machineConfigMock = mock(InternalMachineConfig.class);
    when(machineConfigMock.getAttributes()).thenReturn(attributes);
    return machineConfigMock;
  }
}
