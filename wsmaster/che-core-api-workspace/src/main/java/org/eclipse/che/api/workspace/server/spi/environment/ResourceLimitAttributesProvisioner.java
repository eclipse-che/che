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

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.eclipse.che.api.core.model.workspace.config.MachineConfig.CPU_LIMIT_ATTRIBUTE;
import static org.eclipse.che.api.core.model.workspace.config.MachineConfig.CPU_REQUEST_ATTRIBUTE;
import static org.eclipse.che.api.core.model.workspace.config.MachineConfig.MEMORY_LIMIT_ATTRIBUTE;
import static org.eclipse.che.api.core.model.workspace.config.MachineConfig.MEMORY_REQUEST_ATTRIBUTE;

import java.util.Map;
import org.eclipse.che.api.core.model.workspace.config.MachineConfig;
import org.eclipse.che.commons.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configures memory attributes for a given machine, if they are not present in {@link
 * MachineConfig} the attributes are taken from recipe, when available, by the specific
 * infrastructure implementation, or from wsmaster properties as a fallback
 *
 * <p>There are two memory-related properties: - che.workspace.default_memory_limit_mb - defines
 * default machine memory limit - che.workspace.default_memory_request_mb - defines default
 * requested machine memory allocation
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
   * <p>Note: Default memory request and limit will only be used if BOTH memoryLimit and
   * memoryRequest are null or 0, otherwise the provided value will be used for both parameters.
   *
   * @param machineConfig - given machine configuration
   * @param limit - resource limit parameter configured by user in specific infra recipe. Can be 0
   *     if defaults should be used
   * @param request - memory request parameter configured by user in specific infra recipe. Can be 0
   *     if defaults should be used
   */
  public static void provisionMemory(
      InternalMachineConfig machineConfig,
      @Nullable long limit,
      @Nullable long request,
      long defaultLimit,
      long defaultRequest) {
    if (defaultRequest > defaultLimit) {
      defaultRequest = defaultLimit;
      LOG.error(
          "Requested default container resource limit is less than default request. Request parameter will be ignored.");
    }

    // if both properties are not defined
    if (limit <= 0 && request <= 0) {
      limit = defaultLimit;
      request = defaultRequest;
    } else if (limit <= 0) { // if limit only is undefined
      limit = request;
    } else if (request <= 0) { // if request only is undefined
      request = limit;
    } else if (request > limit) { // if both properties are defined, but not consistent
      request = limit;
    }

    final Map<String, String> attributes = machineConfig.getAttributes();
    String configuredLimit = attributes.get(MEMORY_LIMIT_ATTRIBUTE);
    String configuredRequest = attributes.get(MEMORY_REQUEST_ATTRIBUTE);
    if (isNullOrEmpty(configuredLimit) && isNullOrEmpty(configuredRequest)) {
      attributes.put(MEMORY_LIMIT_ATTRIBUTE, String.valueOf(limit));
      attributes.put(MEMORY_REQUEST_ATTRIBUTE, String.valueOf(request));
    } else if (isNullOrEmpty(configuredLimit)) {
      attributes.put(MEMORY_LIMIT_ATTRIBUTE, configuredRequest);
    } else if (isNullOrEmpty(configuredRequest)) {
      attributes.put(MEMORY_REQUEST_ATTRIBUTE, configuredLimit);
    }
  }

  /**
   * Configures CPU attributes, if they are missing in {@link MachineConfig}
   *
   * <p>Note: Default CPU request and memory will only be used if BOTH memoryLimit and memoryRequest
   * are null or 0, otherwise the provided value will be used for both parameters.
   *
   * @param machineConfig - given machine configuration
   * @param limit - resource limit parameter configured by user in specific infra recipe. Can be 0
   *     if defaults should be used
   * @param request - memory request parameter configured by user in specific infra recipe. Can be 0
   *     if defaults should be used
   */
  public static void provisionCPU(
      InternalMachineConfig machineConfig,
      @Nullable float limit,
      @Nullable float request,
      float defaultLimit,
      float defaultRequest) {
    if (defaultRequest > defaultLimit) {
      defaultRequest = defaultLimit;
      LOG.error(
          "Requested default container resource limit is less than default request. Request parameter will be ignored.");
    }

    // if both properties are not defined
    if (limit <= 0 && request <= 0) {
      limit = defaultLimit;
      request = defaultRequest;
    } else if (limit <= 0) { // if limit only is undefined
      limit = request;
    } else if (request <= 0) { // if request only is undefined
      request = limit;
    } else if (request > limit) { // if both properties are defined, but not consistent
      request = limit;
    }

    final Map<String, String> attributes = machineConfig.getAttributes();
    String configuredLimit = attributes.get(CPU_LIMIT_ATTRIBUTE);
    String configuredRequest = attributes.get(CPU_REQUEST_ATTRIBUTE);
    if (isNullOrEmpty(configuredLimit) && isNullOrEmpty(configuredRequest)) {
      attributes.put(CPU_LIMIT_ATTRIBUTE, Float.toString(limit));
      attributes.put(CPU_REQUEST_ATTRIBUTE, Float.toString(request));
    } else if (isNullOrEmpty(configuredLimit)) {
      attributes.put(CPU_LIMIT_ATTRIBUTE, configuredRequest);
    } else if (isNullOrEmpty(configuredRequest)) {
      attributes.put(CPU_REQUEST_ATTRIBUTE, configuredLimit);
    }
  }
}
