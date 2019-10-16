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

package org.eclipse.che.workspace.infrastructure.openshift.provision;

import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.openshift.api.model.Route;
import io.fabric8.openshift.api.model.RouteSpec;
import java.util.Optional;
import javax.inject.Singleton;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesNamespace;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.PreviewUrlCommandProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.Services;
import org.eclipse.che.workspace.infrastructure.openshift.environment.OpenShiftEnvironment;
import org.eclipse.che.workspace.infrastructure.openshift.project.OpenShiftProject;

/**
 * Extends {@link PreviewUrlCommandProvisioner} where needed. For OpenShift, we work with {@link
 * Route}s and {@link OpenShiftProject}. Other than that, logic is the same as for k8s.
 */
@Singleton
public class OpenShiftPreviewUrlCommandProvisioner
    extends PreviewUrlCommandProvisioner<OpenShiftEnvironment> {

  @Override
  protected Optional<String> findHostForServicePort(
      KubernetesNamespace namespace, Service service, int port) throws InfrastructureException {
    if (!(namespace instanceof OpenShiftProject)) {
      throw new InfrastructureException("namespace is not OpenShiftProject. Why???");
    }
    OpenShiftProject project = (OpenShiftProject) namespace;

    Optional<ServicePort> foundPort = Services.findPort(service, port);
    if (!foundPort.isPresent()) {
      return Optional.empty();
    }

    for (Route route : project.routes().get()) {
      RouteSpec spec = route.getSpec();
      if (spec.getTo().getName().equals(service.getMetadata().getName())
          && spec.getPort().getTargetPort().getStrVal().equals(foundPort.get().getName())) {
        return Optional.of(route.getSpec().getHost());
      }
    }
    return Optional.empty();
  }
}
