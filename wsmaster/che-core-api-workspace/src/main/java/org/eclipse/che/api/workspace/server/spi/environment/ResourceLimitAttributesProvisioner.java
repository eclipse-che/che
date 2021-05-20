/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
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

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.eclipse.che.api.core.model.workspace.config.MachineConfig.CPU_LIMIT_ATTRIBUTE;
import static org.eclipse.che.api.core.model.workspace.config.MachineConfig.CPU_REQUEST_ATTRIBUTE;
import static org.eclipse.che.api.core.model.workspace.config.MachineConfig.MEMORY_LIMIT_ATTRIBUTE;
import static org.eclipse.che.api.core.model.workspace.config.MachineConfig.MEMORY_REQUEST_ATTRIBUTE;

import java.util.Map;
import org.eclipse.che.api.core.model.workspace.config.MachineConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configures resource limits attributes for a given machine, if they are not present in {@link
 * MachineConfig} the attributes are taken from recipe, when available, by the specific
 * infrastructure implementation, or from wsmaster properties as a fallback
 *
 * <p>if default requested memory allocation is greater then default memory limit, requested memory
 * allocation is set to be equal to memory limit.
 */
public class ResourceLimitAttributesProvisioner {

  private static final Logger LOG =
      LoggerFactory.getLogger(ResourceLimitAttributesProvisioner.class);

  /**
   * Configures memory attributes, if they are missing in {@link MachineConfig}
   *
   * @param machineConfig - given machine configuration
   * @param memoryLimit - resource limit parameter configured by user in specific infra recipe. Can
   *     be 0 if defaults should be used
   * @param memoryRequest - memory request parameter configured by user in specific infra recipe.
   *     Can be 0 if defaults should be used
   * @param defaultMemoryLimit - default memory limit resource parameter value
   * @param defaultMemoryRequest - default memory request resource parameter value
   */
  public static void provisionMemory(
      InternalMachineConfig machineConfig,
      long memoryLimit,
      long memoryRequest,
      long defaultMemoryLimit,
      long defaultMemoryRequest) {
    if (defaultMemoryRequest > defaultMemoryLimit) {
      defaultMemoryRequest = defaultMemoryLimit;
      LOG.warn(
          "Requested default container resource limit is less than default request. Request parameter will be ignored.");
    }

    if (memoryLimit <= 0 && defaultMemoryLimit > 0) {
      memoryLimit = defaultMemoryLimit;
    }
    if (memoryRequest <= 0 && defaultMemoryRequest > 0) {
      memoryRequest = defaultMemoryRequest;
    }
    if (memoryRequest > memoryLimit) { // if both properties are defined, but not consistent
      memoryRequest = memoryLimit;
    }

    final Map<String, String> attributes = machineConfig.getAttributes();
    String configuredLimit = attributes.get(MEMORY_LIMIT_ATTRIBUTE);
    String configuredRequest = attributes.get(MEMORY_REQUEST_ATTRIBUTE);
    // Added < 0  check to avoid bypassing limit if someone is set negative value into attribute.
    if (isNullOrEmpty(configuredLimit) || Long.parseLong(configuredLimit) <= 0) {
      attributes.put(MEMORY_LIMIT_ATTRIBUTE, String.valueOf(memoryLimit));
    }
    if (isNullOrEmpty(configuredRequest) || Long.parseLong(configuredRequest) <= 0) {
      attributes.put(MEMORY_REQUEST_ATTRIBUTE, String.valueOf(memoryRequest));
    }
  }

  /**
   * Configures CPU attributes, if they are missing in {@link MachineConfig}
   *
   * @param machineConfig - given machine configuration
   * @param cpuLimit - CPU resource limit parameter configured by user in specific infra recipe. Can
   *     be 0 if defaults should be used
   * @param cpuRequest - CPU resource request parameter configured by user in specific infra recipe.
   *     Can be 0 if defaults should be used
   * @param defaultCPULimit - default CPU limit resource parameter value
   * @param defaultCPURequest - default CPU request resource parameter value
   */
  public static void provisionCPU(
      InternalMachineConfig machineConfig,
      float cpuLimit,
      float cpuRequest,
      float defaultCPULimit,
      float defaultCPURequest) {
    if (defaultCPURequest > defaultCPULimit) {
      defaultCPURequest = defaultCPULimit;
      LOG.warn(
          "Requested default container resource limit is less than default request. Request parameter will be ignored.");
    }

    if (cpuLimit <= 0 && defaultCPULimit > 0) {
      cpuLimit = defaultCPULimit;
    }
    if (cpuRequest <= 0 && defaultCPURequest > 0) {
      cpuRequest = defaultCPURequest;
    }
    if (cpuRequest > cpuLimit) { // if both properties are defined, but not consistent
      cpuRequest = cpuLimit;
    }

    final Map<String, String> attributes = machineConfig.getAttributes();
    String configuredLimit = attributes.get(CPU_LIMIT_ATTRIBUTE);
    String configuredRequest = attributes.get(CPU_REQUEST_ATTRIBUTE);
    // Added < 0  check to avoid bypassing limit if someone is set negative value into attribute.
    if (isNullOrEmpty(configuredLimit) || Float.parseFloat(configuredLimit) <= 0) {
      attributes.put(CPU_LIMIT_ATTRIBUTE, Float.toString(cpuLimit));
    }
    if (isNullOrEmpty(configuredRequest) || Float.parseFloat(configuredRequest) <= 0) {
      attributes.put(CPU_REQUEST_ATTRIBUTE, Float.toString(cpuRequest));
    }
  }
}
