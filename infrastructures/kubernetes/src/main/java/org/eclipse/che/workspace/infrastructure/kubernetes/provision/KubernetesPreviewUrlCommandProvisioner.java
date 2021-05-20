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
package org.eclipse.che.workspace.infrastructure.kubernetes.provision;

import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.extensions.Ingress;
import io.fabric8.kubernetes.api.model.extensions.IngressRule;
import java.util.List;
import java.util.Optional;
import javax.inject.Singleton;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesNamespace;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.Ingresses;

/**
 * Extends {@link PreviewUrlCommandProvisioner} where needed. For Kubernetes, we work with {@link
 * Ingress}es and {@link KubernetesNamespace}.
 */
@Singleton
public class KubernetesPreviewUrlCommandProvisioner
    extends PreviewUrlCommandProvisioner<KubernetesEnvironment, Ingress> {

  @Override
  protected List<Ingress> loadExposureObjects(KubernetesNamespace namespace)
      throws InfrastructureException {
    return namespace.ingresses().get();
  }

  @Override
  protected Optional<String> findHostForServicePort(
      List<Ingress> ingresses, Service service, int port) {
    return Ingresses.findIngressRuleForServicePort(ingresses, service, port)
        .map(IngressRule::getHost);
  }
}
