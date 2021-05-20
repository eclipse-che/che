/*
 * Copyright (c) 2012-2019 Red Hat, Inc.
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
public interface RequestProcessorConfigurationProvider {

  /**
   * Get request processor configuration for specified endpoint.
   *
   * @param endpointId endpoint id
   */
  Configuration get(String endpointId);

  /** Request processor configuration */
  interface Configuration {

    String getEndpointId();

    ExecutorService getExecutorService();
  }
}
