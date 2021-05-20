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
package org.eclipse.che.workspace.infrastructure.kubernetes.server.secure.jwtproxy;

import static java.lang.String.format;
import static org.eclipse.che.workspace.infrastructure.kubernetes.server.external.DefaultHostExternalServiceExposureStrategy.DEFAULT_HOST_STRATEGY;
import static org.eclipse.che.workspace.infrastructure.kubernetes.server.external.MultiHostExternalServiceExposureStrategy.MULTI_HOST_STRATEGY;
import static org.eclipse.che.workspace.infrastructure.kubernetes.server.external.SingleHostExternalServiceExposureStrategy.SINGLE_HOST_STRATEGY;

import io.fabric8.kubernetes.api.model.ServicePort;
import java.util.function.BiFunction;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.external.ServiceExposureStrategyProvider;

/**
 * The cookie path for the access token cookie is server-strategy dependent. This class represents
 * the different strategies for getting the cookie path.
 *
 * <p>Note that instead of going with full-blown strategy pattern and different implementations of
 * some interface and a provider for the currently active strategy (as is done for example with
 * {@link
 * org.eclipse.che.workspace.infrastructure.kubernetes.server.external.ExternalServiceExposureStrategy}),
 * this class merely internally uses different functions for different service exposure strategies.
 * This is done because the full-blown strategy pattern implementation felt like over-engineering
 * when compared with the simplicity of the functions.
 */
@Singleton
public class CookiePathStrategy {

  private final BiFunction<String, ServicePort, String> getCookiePath;

  @Inject
  public CookiePathStrategy(
      @Named(ServiceExposureStrategyProvider.STRATEGY_PROPERTY) String serverStrategy) {
    switch (serverStrategy) {
      case MULTI_HOST_STRATEGY:
        getCookiePath = (__, ___) -> "/";
        break;
      case SINGLE_HOST_STRATEGY:
      case DEFAULT_HOST_STRATEGY:
        getCookiePath = (serviceName, __) -> serviceName;
        break;
      default:
        throw new IllegalArgumentException(
            format("Unsupported server strategy: %s", serverStrategy));
    }
  }

  public String get(String serviceName, ServicePort port) {
    return getCookiePath.apply(serviceName, port);
  }
}
