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
package org.eclipse.che.workspace.infrastructure.kubernetes.server.external;

import static java.lang.String.format;

import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import org.eclipse.che.inject.ConfigurationException;

@Singleton
public class IngressServiceExposureStrategyProvider
    implements Provider<ExternalServiceExposureStrategy> {

  public static final String STRATEGY_PROPERTY = "che.infra.kubernetes.server_strategy";

  private final ExternalServiceExposureStrategy namingStrategy;

  @Inject
  public IngressServiceExposureStrategyProvider(
      @Named(STRATEGY_PROPERTY) String strategy,
      Map<String, ExternalServiceExposureStrategy> strategies) {

    namingStrategy = strategies.get(strategy);

    if (namingStrategy == null) {
      throw new ConfigurationException(
          format("Unsupported server naming strategy '%s' configured", strategy));
    }
  }

  @Override
  public ExternalServiceExposureStrategy get() {
    return namingStrategy;
  }
}
