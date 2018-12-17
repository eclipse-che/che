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
package org.eclipse.che.api.core.jsonrpc.commons;

import java.util.concurrent.ExecutorService;

/** Request processor configurator */
public interface RequestProcessorConfigurator {
  /**
   * Configure processing for specified endpoint
   *
   * @param endpointId endpointId
   * @param configuration configuration
   */
  void put(String endpointId, Configuration configuration);

  /**
   * Get request processor configuration for specified endpoint, returns null if no corresponding
   * configuration is found.
   *
   * @param endpointId endpoint id
   * @return
   */
  Configuration getOrNull(String endpointId);

  /**
   * Get request processor configuration for specified endpoint, returns default value if no
   * corresponding configuration is found.
   *
   * @param endpointId endpoint id
   * @return
   */
  Configuration getOrDefault(String endpointId);

  /** Request processor configuration */
  interface Configuration {
    ExecutorService getExecutionService();
  }
}
