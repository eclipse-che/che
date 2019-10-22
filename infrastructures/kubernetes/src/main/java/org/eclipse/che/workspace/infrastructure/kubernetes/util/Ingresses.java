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
          if (backend.getServiceName().equals(service.getMetadata().getName())) {
            IntOrString servicePort = backend.getServicePort();
            if ((servicePort.getKind() == IntOrStringConstants.KIND_STRING
                    && backend.getServicePort().getStrVal().equals(foundPort.get().getName()))
                || (servicePort.getKind() == IntOrStringConstants.KIND_INT
                    && backend.getServicePort().getIntVal().equals(foundPort.get().getPort()))) {
              return Optional.of(rule);
            }
          }
        }
      }
    }
    return Optional.empty();
  }
}
