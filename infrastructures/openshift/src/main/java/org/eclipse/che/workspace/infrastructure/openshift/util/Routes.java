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
package org.eclipse.che.workspace.infrastructure.openshift.util;

import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.openshift.api.model.Route;
import io.fabric8.openshift.api.model.RouteSpec;
import java.util.Collection;
import java.util.Optional;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.Services;

/** Util class that helps working with OpenShift Routes */
public class Routes {

  /**
   * In given {@code routes} finds route that for given {@code service} and {@code port}
   *
   * @return found {@link Route} or {@link Optional#empty()}
   */
  public static Optional<Route> findRouteForServicePort(
      Collection<Route> routes, Service service, int port) {
    Optional<ServicePort> foundPort = Services.findPort(service, port);
    if (!foundPort.isPresent()) {
      return Optional.empty();
    }

    for (Route route : routes) {
      RouteSpec spec = route.getSpec();
      if (spec.getTo().getName().equals(service.getMetadata().getName())
          && matchesPort(foundPort.get(), spec.getPort().getTargetPort())) {
        return Optional.of(route);
      }
    }
    return Optional.empty();
  }

  private static boolean matchesPort(ServicePort servicePort, IntOrString routePort) {
    if (routePort.getStrVal() != null && routePort.getStrVal().equals(servicePort.getName())) {
      return true;
    }

    if (routePort.getIntVal() != null && routePort.getIntVal().equals(servicePort.getPort())) {
      return true;
    }

    return false;
  }
}
