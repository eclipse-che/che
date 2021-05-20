/*
 * Copyright (c) 2012-2020 Red Hat, Inc.
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

import static org.eclipse.che.api.core.model.workspace.config.MachineConfig.CPU_LIMIT_ATTRIBUTE;
import static org.eclipse.che.api.core.model.workspace.config.MachineConfig.CPU_REQUEST_ATTRIBUTE;
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

/** Tests {@link ResourceLimitAttributesProvisioner} */
@Listeners(MockitoTestNGListener.class)
public class ResourceLimitAttributesProvisionerTest {

  @Test
  public void testSetsRamDefaultAttributesWhenTheyAreMissingInConfigAndNotPassedInRecipe() {
    long defaultMemoryLimit = 2048L;
    long defaultMemoryRequest = 1024L;
    InternalMachineConfig machineConfig = mockInternalMachineConfig(new HashMap<>());

    ResourceLimitAttributesProvisioner.provisionMemory(
        machineConfig, 0L, 0L, defaultMemoryLimit, defaultMemoryRequest);
    long memLimit = Long.parseLong(machineConfig.getAttributes().get(MEMORY_LIMIT_ATTRIBUTE));
    long memRequest = Long.parseLong(machineConfig.getAttributes().get(MEMORY_REQUEST_ATTRIBUTE));

    assertEquals(memLimit, defaultMemoryLimit);
    assertEquals(memRequest, defaultMemoryRequest);
  }

  @Test
  public void testRamDefaultMemoryRequestIsIgnoredIfGreaterThanDefaultRamLimit() {
    long defaultMemoryLimit = 1024L;
    long defaultMemoryRequest = 2048L;
    InternalMachineConfig machineConfig = mockInternalMachineConfig(new HashMap<>());

    ResourceLimitAttributesProvisioner.provisionMemory(
        machineConfig, 0L, 0L, defaultMemoryLimit, defaultMemoryRequest);
    long memLimit = Long.parseLong(machineConfig.getAttributes().get(MEMORY_LIMIT_ATTRIBUTE));
    long memRequest = Long.parseLong(machineConfig.getAttributes().get(MEMORY_REQUEST_ATTRIBUTE));

    assertEquals(memLimit, defaultMemoryLimit);
    assertEquals(memRequest, defaultMemoryLimit);
  }

  @Test
  public void testSkipDefaultMemoryAttributesWhenTheyAreNegative() {
    long defaultMemoryLimit = -1L;
    long defaultMemoryRequest = -1024L;
    InternalMachineConfig machineConfig = mockInternalMachineConfig(new HashMap<>());

    ResourceLimitAttributesProvisioner.provisionMemory(
        machineConfig, 0L, 0L, defaultMemoryLimit, defaultMemoryRequest);

    long memLimit = Long.parseLong(machineConfig.getAttributes().get(MEMORY_LIMIT_ATTRIBUTE));
    long memRequest = Long.parseLong(machineConfig.getAttributes().get(MEMORY_REQUEST_ATTRIBUTE));

    assertEquals(memLimit, 0L);
    assertEquals(memRequest, 0L);
  }

  @Test
  public void testRamAttributesAreTakenFromRecipeWhenNotPresentInConfig() {
    long defaultMemoryLimit = 1024L;
    long defaultMemoryRequest = 2048L;
    InternalMachineConfig machineConfig = mockInternalMachineConfig(new HashMap<>());

    long recipeLimit = 4096L;
    long recipeRequest = 2048L;
    ResourceLimitAttributesProvisioner.provisionMemory(
        machineConfig, recipeLimit, recipeRequest, defaultMemoryLimit, defaultMemoryRequest);

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
    InternalMachineConfig machineConfig = mockInternalMachineConfig(new HashMap<>());

    // inconsistent attributes mean request > limit
    long recipeLimit = 2048L;
    long recipeRequest = 4096L;
    ResourceLimitAttributesProvisioner.provisionMemory(
        machineConfig, recipeLimit, recipeRequest, defaultMemoryLimit, defaultMemoryRequest);

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
        mockInternalMachineConfig(
            ImmutableMap.of(MEMORY_LIMIT_ATTRIBUTE, "1526", MEMORY_REQUEST_ATTRIBUTE, "512"));

    long recipeLimit = 4096L;
    long recipeRequest = 2048L;
    ResourceLimitAttributesProvisioner.provisionMemory(
        machineConfig, recipeLimit, recipeRequest, defaultMemoryLimit, defaultMemoryRequest);

    assertEquals(machineConfig.getAttributes().get(MEMORY_LIMIT_ATTRIBUTE), String.valueOf(1526L));
    assertEquals(machineConfig.getAttributes().get(MEMORY_REQUEST_ATTRIBUTE), String.valueOf(512L));
  }

  @Test
  public void testWhenRamAttributesArePresentInMachineAreNegativeDefaultsShouldBeApplied() {
    long defaultMemoryLimit = 2048L;
    long defaultMemoryRequest = 1024L;
    Map<String, String> attributes = new HashMap<>();
    attributes.put(MEMORY_LIMIT_ATTRIBUTE, "-1");
    attributes.put(MEMORY_REQUEST_ATTRIBUTE, "-1");
    InternalMachineConfig machineConfig = mockInternalMachineConfig(attributes);

    ResourceLimitAttributesProvisioner.provisionMemory(
        machineConfig, 0L, 0L, defaultMemoryLimit, defaultMemoryRequest);

    assertEquals(
        machineConfig.getAttributes().get(MEMORY_LIMIT_ATTRIBUTE),
        String.valueOf(defaultMemoryLimit));
    assertEquals(
        machineConfig.getAttributes().get(MEMORY_REQUEST_ATTRIBUTE),
        String.valueOf(defaultMemoryRequest));
  }

  @Test
  public void testWhenRamRequestAttributeIsPresentInMachineConfigValuesInRecipeAreIgnored() {
    long defaultMemoryLimit = 1024L;
    long defaultMemoryRequest = 2048L;
    Map<String, String> attributes = new HashMap<>();
    attributes.put(MEMORY_REQUEST_ATTRIBUTE, "512");
    InternalMachineConfig machineConfig = mockInternalMachineConfig(attributes);

    long recipeLimit = 4096L;
    long recipeRequest = 2048L;
    ResourceLimitAttributesProvisioner.provisionMemory(
        machineConfig, recipeLimit, recipeRequest, defaultMemoryLimit, defaultMemoryRequest);

    assertEquals(
        machineConfig.getAttributes().get(MEMORY_LIMIT_ATTRIBUTE), String.valueOf(recipeLimit));
    assertEquals(machineConfig.getAttributes().get(MEMORY_REQUEST_ATTRIBUTE), String.valueOf(512L));
  }

  @Test
  public void testWhenRamLimitAttributeIsPresentInMachineConfigValuesInRecipeAreIgnored() {
    long defaultMemoryLimit = 1024L;
    long defaultMemoryRequest = 2048L;
    Map<String, String> attributes = new HashMap<>();
    attributes.put(MEMORY_LIMIT_ATTRIBUTE, "1526");
    InternalMachineConfig machineConfig = mockInternalMachineConfig(attributes);

    long recipeLimit = 4096L;
    long recipeRequest = 2048L;
    ResourceLimitAttributesProvisioner.provisionMemory(
        machineConfig, recipeLimit, recipeRequest, defaultMemoryLimit, defaultMemoryRequest);

    assertEquals(machineConfig.getAttributes().get(MEMORY_LIMIT_ATTRIBUTE), String.valueOf(1526L));
    assertEquals(
        machineConfig.getAttributes().get(MEMORY_REQUEST_ATTRIBUTE),
        String.valueOf(defaultMemoryRequest));
  }

  @Test
  public void testWhenRamAttributesAreNotPresentInMachineConfigAndOnlyRequestIsProvidedInRecipe() {
    long defaultMemoryLimit = 2048L;
    long defaultMemoryRequest = 1024L;
    InternalMachineConfig machineConfig = mockInternalMachineConfig(new HashMap<>());

    long recipeRequest = 1526L;
    ResourceLimitAttributesProvisioner.provisionMemory(
        machineConfig, 0, recipeRequest, defaultMemoryLimit, defaultMemoryRequest);

    assertEquals(
        machineConfig.getAttributes().get(MEMORY_LIMIT_ATTRIBUTE),
        String.valueOf(defaultMemoryLimit));
    assertEquals(
        machineConfig.getAttributes().get(MEMORY_REQUEST_ATTRIBUTE), String.valueOf(recipeRequest));
  }

  @Test
  public void testWhenRamAttributesAreNotPresentInMachineConfigAndOnlyLimitIsProvidedInRecipe() {
    long defaultMemoryLimit = 2048L;
    long defaultMemoryRequest = 1024L;
    InternalMachineConfig machineConfig = mockInternalMachineConfig(new HashMap<>());

    long recipeLimit = 1526L;
    ResourceLimitAttributesProvisioner.provisionMemory(
        machineConfig, recipeLimit, 0, defaultMemoryLimit, defaultMemoryRequest);

    assertEquals(
        machineConfig.getAttributes().get(MEMORY_LIMIT_ATTRIBUTE), String.valueOf(recipeLimit));
    assertEquals(
        machineConfig.getAttributes().get(MEMORY_REQUEST_ATTRIBUTE),
        String.valueOf(defaultMemoryRequest));
  }

  @Test
  public void testSetsCPUDefaultAttributesWhenTheyAreMissingInConfigAndNotPassedInRecipe() {
    float defaultCPULimit = 0.500f;
    float defaultCPURequest = 0.200f;
    InternalMachineConfig machineConfig = mockInternalMachineConfig(new HashMap<>());

    ResourceLimitAttributesProvisioner.provisionCPU(
        machineConfig, 0, 0, defaultCPULimit, defaultCPURequest);
    float cpuLimit = Float.parseFloat(machineConfig.getAttributes().get(CPU_LIMIT_ATTRIBUTE));
    float cpuRequest = Float.parseFloat(machineConfig.getAttributes().get(CPU_REQUEST_ATTRIBUTE));

    assertEquals(cpuLimit, defaultCPULimit);
    assertEquals(cpuRequest, defaultCPURequest);
  }

  @Test
  public void testSkipDefaultCPUAttributesWhenTheyAreNegative() {
    float defaultCPULimit = -1;
    float defaultCPURequest = -1;
    InternalMachineConfig machineConfig = mockInternalMachineConfig(new HashMap<>());

    ResourceLimitAttributesProvisioner.provisionCPU(
        machineConfig, 0, 0, defaultCPULimit, defaultCPURequest);
    float cpuLimit = Float.parseFloat(machineConfig.getAttributes().get(CPU_LIMIT_ATTRIBUTE));
    float cpuRequest = Float.parseFloat(machineConfig.getAttributes().get(CPU_REQUEST_ATTRIBUTE));

    assertEquals(cpuLimit, 0);
    assertEquals(cpuRequest, 0);
  }

  @Test
  public void testRamDefaultCPURequestIsIgnoredIfGreaterThanDefaultCPULimit() {
    float defaultCPULimit = 0.2f;
    float defaultCPURequest = 0.5f;
    InternalMachineConfig machineConfig = mockInternalMachineConfig(new HashMap<>());

    ResourceLimitAttributesProvisioner.provisionCPU(
        machineConfig, 0L, 0L, defaultCPULimit, defaultCPURequest);
    float memLimit = Float.parseFloat(machineConfig.getAttributes().get(CPU_LIMIT_ATTRIBUTE));
    float memRequest = Float.parseFloat(machineConfig.getAttributes().get(CPU_REQUEST_ATTRIBUTE));

    assertEquals(memLimit, defaultCPULimit);
    assertEquals(memRequest, defaultCPULimit);
  }

  @Test
  public void testCPUAttributesAreTakenFromRecipeWhenNotPresentInConfig() {
    float defaultCPULimit = 1.0f;
    float defaultCPURequest = 0.2f;
    InternalMachineConfig machineConfig = mockInternalMachineConfig(new HashMap<>());

    float recipeLimit = 4f;
    float recipeRequest = 2f;
    ResourceLimitAttributesProvisioner.provisionCPU(
        machineConfig, recipeLimit, recipeRequest, defaultCPULimit, defaultCPURequest);

    assertEquals(
        machineConfig.getAttributes().get(CPU_LIMIT_ATTRIBUTE), String.valueOf(recipeLimit));
    assertEquals(
        machineConfig.getAttributes().get(CPU_REQUEST_ATTRIBUTE), String.valueOf(recipeRequest));
  }

  @Test
  public void
      testWhenCPUAttributesTakenFromRecipeAreInconsistentAndNotPresentInConfigRequestIsIgnored() {
    float defaultCPULimit = 0.2f;
    float defaultCPURequest = 0.5f;
    InternalMachineConfig machineConfig = mockInternalMachineConfig(new HashMap<>());

    // inconsistent attributes mean request > limit
    float recipeLimit = 0.3f;
    float recipeRequest = 0.6f;
    ResourceLimitAttributesProvisioner.provisionCPU(
        machineConfig, recipeLimit, recipeRequest, defaultCPULimit, defaultCPURequest);

    assertEquals(
        machineConfig.getAttributes().get(CPU_LIMIT_ATTRIBUTE), String.valueOf(recipeLimit));
    assertEquals(
        machineConfig.getAttributes().get(CPU_REQUEST_ATTRIBUTE), String.valueOf(recipeLimit));
  }

  @Test
  public void testWhenCPUAttributesArePresentInMachineConfigValuesInRecipeAreIgnored() {
    float defaultCPULimit = 0.5f;
    float defaultCPURequest = 0.2f;
    InternalMachineConfig machineConfig =
        mockInternalMachineConfig(
            ImmutableMap.of(CPU_LIMIT_ATTRIBUTE, "0.512", CPU_REQUEST_ATTRIBUTE, "0.152"));

    float recipeLimit = 0.6f;
    float recipeRequest = 0.3f;
    ResourceLimitAttributesProvisioner.provisionCPU(
        machineConfig, recipeLimit, recipeRequest, defaultCPULimit, defaultCPURequest);

    assertEquals(machineConfig.getAttributes().get(CPU_LIMIT_ATTRIBUTE), String.valueOf(0.512f));
    assertEquals(machineConfig.getAttributes().get(CPU_REQUEST_ATTRIBUTE), String.valueOf(0.152f));
  }

  @Test
  public void testWhenCPURequestAttributeIsPresentInMachineConfigValuesInRecipeAreIgnored() {
    float defaultCPULimit = 0.5f;
    float defaultCPURequest = 0.2f;
    Map<String, String> attributes = new HashMap<>();
    attributes.put(CPU_REQUEST_ATTRIBUTE, "0.512");
    InternalMachineConfig machineConfig = mockInternalMachineConfig(attributes);

    float recipeLimit = 0.6f;
    float recipeRequest = 0.3f;
    ResourceLimitAttributesProvisioner.provisionCPU(
        machineConfig, recipeLimit, recipeRequest, defaultCPULimit, defaultCPURequest);

    assertEquals(
        machineConfig.getAttributes().get(CPU_LIMIT_ATTRIBUTE), String.valueOf(recipeLimit));
    assertEquals(machineConfig.getAttributes().get(CPU_REQUEST_ATTRIBUTE), String.valueOf(0.512f));
  }

  @Test
  public void testWhenCPULimitAttributeIsPresentInMachineConfigValuesInRecipeAreIgnored() {
    float defaultCPULimit = 0.5f;
    float defaultCPURequest = 0.2f;
    Map<String, String> attributes = new HashMap<>();
    attributes.put(CPU_LIMIT_ATTRIBUTE, "0.152");
    InternalMachineConfig machineConfig = mockInternalMachineConfig(attributes);

    float recipeLimit = 0.6f;
    float recipeRequest = 0.3f;
    ResourceLimitAttributesProvisioner.provisionCPU(
        machineConfig, recipeLimit, recipeRequest, defaultCPULimit, defaultCPURequest);

    assertEquals(machineConfig.getAttributes().get(CPU_LIMIT_ATTRIBUTE), String.valueOf(0.152f));
    assertEquals(
        machineConfig.getAttributes().get(CPU_REQUEST_ATTRIBUTE), String.valueOf(recipeRequest));
  }

  @Test
  public void testWhenCPULimitAttributeIsPresentInMachineAreNegativeDefaultsShouldBeApplied() {
    float defaultCPULimit = 0.5f;
    float defaultCPURequest = 0.2f;
    Map<String, String> attributes = new HashMap<>();
    attributes.put(CPU_LIMIT_ATTRIBUTE, "-1");
    attributes.put(CPU_REQUEST_ATTRIBUTE, "-1");
    InternalMachineConfig machineConfig = mockInternalMachineConfig(attributes);

    ResourceLimitAttributesProvisioner.provisionCPU(
        machineConfig, 0, 0, defaultCPULimit, defaultCPURequest);

    assertEquals(
        machineConfig.getAttributes().get(CPU_LIMIT_ATTRIBUTE), String.valueOf(defaultCPULimit));
    assertEquals(
        machineConfig.getAttributes().get(CPU_REQUEST_ATTRIBUTE),
        String.valueOf(defaultCPURequest));
  }

  @Test
  public void testWhenCPUAttributesAreNotPresentInMachineConfigAndOnlyRequestIsProvidedInRecipe() {
    float defaultCPULimit = 0.5f;
    float defaultCPURequest = 0.2f;
    InternalMachineConfig machineConfig = mockInternalMachineConfig(new HashMap<>());

    float recipeRequest = 0.1526f;
    ResourceLimitAttributesProvisioner.provisionCPU(
        machineConfig, 0, recipeRequest, defaultCPULimit, defaultCPURequest);

    assertEquals(
        machineConfig.getAttributes().get(CPU_LIMIT_ATTRIBUTE), String.valueOf(defaultCPULimit));
    assertEquals(
        machineConfig.getAttributes().get(CPU_REQUEST_ATTRIBUTE), String.valueOf(recipeRequest));
  }

  @Test
  public void testWhenCPUAttributesAreNotPresentInMachineConfigAndOnlyLimitIsProvidedInRecipe() {
    float defaultCPULimit = 0.5f;
    float defaultCPURequest = 0.2f;
    InternalMachineConfig machineConfig = mockInternalMachineConfig(new HashMap<>());

    float recipeLimit = 0.152f;
    ResourceLimitAttributesProvisioner.provisionCPU(
        machineConfig, recipeLimit, 0, defaultCPULimit, defaultCPURequest);

    assertEquals(
        machineConfig.getAttributes().get(CPU_LIMIT_ATTRIBUTE), String.valueOf(recipeLimit));
    assertEquals(
        machineConfig.getAttributes().get(CPU_REQUEST_ATTRIBUTE), String.valueOf(recipeLimit));
  }

  private static InternalMachineConfig mockInternalMachineConfig(Map<String, String> attributes) {
    final InternalMachineConfig machineConfigMock = mock(InternalMachineConfig.class);
    when(machineConfigMock.getAttributes()).thenReturn(attributes);
    return machineConfigMock;
  }
}
