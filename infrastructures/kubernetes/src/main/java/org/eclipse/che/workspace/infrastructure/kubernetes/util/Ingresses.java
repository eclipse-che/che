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
package org.eclipse.che.workspace.infrastructure.kubernetes.util;

import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.kubernetes.api.model.extensions.HTTPIngressPath;
import io.fabric8.kubernetes.api.model.extensions.Ingress;
import io.fabric8.kubernetes.api.model.extensions.IngressBackend;
import io.fabric8.kubernetes.api.model.extensions.IngressRule;
import java.util.Collection;
import java.util.Optional;

/** Util class that helps working with k8s Ingresses */
public class Ingresses {

  /**
   * In given {@code ingresses} finds {@link IngressRule} for given {@code service} and {@code
   * port}.
   *
   * @return found {@link IngressRule} or {@link Optional#empty()}
   */
  public static Optional<IngressRule> findIngressRuleForServicePort(
      Collection<Ingress> ingresses, Service service, int port) {
    Optional<ServicePort> foundPort = Services.findPort(service, port);
    if (!foundPort.isPresent()) {
      return Optional.empty();
    }

    for (Ingress ingress : ingresses) {
      for (IngressRule rule : ingress.getSpec().getRules()) {
        for (HTTPIngressPath path : rule.getHttp().getPaths()) {
          IngressBackend backend = path.getBackend();
          if (backend.getServiceName().equals(service.getMetadata().getName())
              && matchesServicePort(backend.getServicePort(), foundPort.get())) {
            return Optional.of(rule);
          }
        }
      }
    }
    return Optional.empty();
  }

  private static boolean matchesServicePort(IntOrString backendPort, ServicePort servicePort) {
    if (backendPort.getStrVal() != null && backendPort.getStrVal().equals(servicePort.getName())) {
      return true;
    }
    if (backendPort.getIntVal() != null && backendPort.getIntVal().equals(servicePort.getPort())) {
      return true;
    }
    return false;
  }
}
