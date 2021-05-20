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
package org.eclipse.che.api.core.jsonrpc.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.eclipse.che.api.core.jsonrpc.commons.RequestProcessorConfigurationProvider;

@Singleton
public class ServerSideRequestProcessorConfigurator
    implements RequestProcessorConfigurationProvider {

  private final Map<String, Configuration> configurations;

  @Inject
  public ServerSideRequestProcessorConfigurator(
      Set<RequestProcessorConfigurationProvider.Configuration> configurations) {
    this.configurations =
        configurations
            .stream()
            .collect(Collectors.toMap(Configuration::getEndpointId, Function.identity()));
  }

  @Override
  public Configuration get(String endpointId) {
    return configurations.get(endpointId);
  }
}
