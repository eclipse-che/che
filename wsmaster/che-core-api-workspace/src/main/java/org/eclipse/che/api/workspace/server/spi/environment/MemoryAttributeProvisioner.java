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
import static org.eclipse.che.api.core.model.workspace.config.MachineConfig.MEMORY_LIMIT_ATTRIBUTE;
import static org.eclipse.che.api.core.model.workspace.config.MachineConfig.MEMORY_REQUEST_ATTRIBUTE;

import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
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
@Singleton
public class MemoryAttributeProvisioner {

  private static final Logger LOG = LoggerFactory.getLogger(MemoryAttributeProvisioner.class);

  private final String defaultMachineMaxMemorySizeAttribute;
  private final String defaultMachineRequestMemorySizeAttribute;

  @Inject
  public MemoryAttributeProvisioner(
      @Named("che.workspace.default_memory_limit_mb") long defaultMachineMaxMemorySizeAttribute,
      @Named("che.workspace.default_memory_request_mb")
          long defaultMachineRequestMemorySizeAttribute) {
    // if the passed default request is greater than the default limit, request is ignored
    if (defaultMachineRequestMemorySizeAttribute > defaultMachineMaxMemorySizeAttribute) {
      defaultMachineRequestMemorySizeAttribute = defaultMachineMaxMemorySizeAttribute;
      LOG.error(
          "Requested default container memory limit is less than default memory request. Memory request parameter is ignored.");
    }

    this.defaultMachineMaxMemorySizeAttribute =
        String.valueOf(defaultMachineMaxMemorySizeAttribute * 1024 * 1024);
    this.defaultMachineRequestMemorySizeAttribute =
        String.valueOf(defaultMachineRequestMemorySizeAttribute * 1024 * 1024);
  }

  /**
   * Configures memory attributes, if they are missing in {@link MachineConfig}
   *
   * <p>Note: Default memory request and memory will only be used if BOTH memoryLimit and
   * memoryRequest are null or 0, otherwise the provided value will be used for both parameters.
   *
   * @param machineConfig - given machine configuration
   * @param memoryLimit - memory limit parameter configured by user in specific infra recipe. Can be
   *     null or 0 if defaults should be used
   * @param memoryRequest - memory request parameter configured by user in specific infra recipe.
   *     Can be null or 0 if defaults should be used
   */
  public void provision(
      InternalMachineConfig machineConfig,
      @Nullable Long memoryLimit,
      @Nullable Long memoryRequest) {
    // if both properties are not defined
    if ((memoryLimit == null || memoryLimit <= 0)
        && (memoryRequest == null || memoryRequest <= 0)) {
      memoryLimit = Long.valueOf(defaultMachineMaxMemorySizeAttribute);
      memoryRequest = Long.valueOf(defaultMachineRequestMemorySizeAttribute);
    } else if ((memoryLimit == null || memoryLimit <= 0)) { // if memoryLimit only is undefined
      memoryLimit = memoryRequest;
    } else if ((memoryRequest == null
        || memoryRequest <= 0)) { // if memoryRequest only is undefined
      memoryRequest = memoryLimit;
    } else if (memoryRequest > memoryLimit) { // if both properties are defined, but not consistent
      memoryRequest = memoryLimit;
    }

    final Map<String, String> attributes = machineConfig.getAttributes();
    String configuredLimit = attributes.get(MEMORY_LIMIT_ATTRIBUTE);
    String configuredRequest = attributes.get(MEMORY_REQUEST_ATTRIBUTE);
    if (isNullOrEmpty(configuredLimit) && isNullOrEmpty(configuredRequest)) {
      attributes.put(MEMORY_LIMIT_ATTRIBUTE, String.valueOf(memoryLimit));
      attributes.put(MEMORY_REQUEST_ATTRIBUTE, String.valueOf(memoryRequest));
    } else if (isNullOrEmpty(configuredLimit)) {
      attributes.put(MEMORY_LIMIT_ATTRIBUTE, configuredRequest);
    } else if (isNullOrEmpty(configuredRequest)) {
      attributes.put(MEMORY_REQUEST_ATTRIBUTE, configuredLimit);
    }
  }
}
