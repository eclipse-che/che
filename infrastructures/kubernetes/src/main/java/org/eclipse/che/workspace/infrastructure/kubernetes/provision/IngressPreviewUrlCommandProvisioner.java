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

package org.eclipse.che.workspace.infrastructure.kubernetes.provision;

import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.extensions.Ingress;
import io.fabric8.kubernetes.api.model.extensions.IngressRule;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.inject.Singleton;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalInfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesNamespace;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.Ingresses;

@Singleton
public class IngressPreviewUrlCommandProvisioner extends
    PreviewUrlCommandProvisioner<KubernetesEnvironment, Ingress> {

  @Override
  protected List<Ingress> loadExposureObjects(KubernetesNamespace namespace) throws InfrastructureException {
    return namespace.ingresses().get();
  }

  @Override
  protected Optional<String> findHostForServicePort(List<?> ingressList, Service service, int port)
      throws InternalInfrastructureException {
    final List<Ingress> ingresses;
    try {
      ingresses = ingressList.stream().map(i -> (Ingress) i).collect(Collectors.toList());
    } catch (ClassCastException cce) {
      throw new InternalInfrastructureException(
          "Failed casting to Kubernetes Ingress. This is not expected. Please report a bug!");
    }

    return Ingresses.findIngressRuleForServicePort(ingresses, service, port).map(IngressRule::getHost);
  }
}
