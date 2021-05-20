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
package org.eclipse.che.workspace.infrastructure.kubernetes.util;

import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServicePort;
import java.util.Collection;
import java.util.Optional;

/** Utility class to help work with {@link Service}s */
public class Services {

  /**
   * Try to find port in given service.
   *
   * @return {@link Optional} of found {@link ServicePort}, or {@link Optional#empty()} when not
   *     found.
   */
  public static Optional<ServicePort> findPort(Service service, int port) {
    if (service == null || service.getSpec() == null || service.getSpec().getPorts() == null) {
      return Optional.empty();
    }
    return service
        .getSpec()
        .getPorts()
        .stream()
        .filter(p -> p.getPort() != null && p.getPort() == port)
        .findFirst();
  }

  /**
   * Go through all given services and finds one that has given port.
   *
   * @return {@link Optional} of found {@link Service}, or {@link Optional#empty()} when not found.
   */
  public static Optional<Service> findServiceWithPort(Collection<Service> services, int port) {
    if (services == null) {
      return Optional.empty();
    }
    return services.stream().filter(s -> Services.findPort(s, port).isPresent()).findFirst();
  }
}
