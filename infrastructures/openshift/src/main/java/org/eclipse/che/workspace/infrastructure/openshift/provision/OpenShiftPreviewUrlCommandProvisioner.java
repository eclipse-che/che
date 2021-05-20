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
package org.eclipse.che.workspace.infrastructure.openshift.provision;

import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.openshift.api.model.Route;
import java.util.List;
import java.util.Optional;
import javax.inject.Singleton;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalInfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesNamespace;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.PreviewUrlCommandProvisioner;
import org.eclipse.che.workspace.infrastructure.openshift.environment.OpenShiftEnvironment;
import org.eclipse.che.workspace.infrastructure.openshift.project.OpenShiftProject;
import org.eclipse.che.workspace.infrastructure.openshift.util.Routes;

/**
 * Extends {@link PreviewUrlCommandProvisioner} where needed. For OpenShift, we work with {@link
 * Route}s and {@link OpenShiftProject}. Other than that, logic is the same as for k8s.
 */
@Singleton
public class OpenShiftPreviewUrlCommandProvisioner
    extends PreviewUrlCommandProvisioner<OpenShiftEnvironment, Route> {

  @Override
  protected List<Route> loadExposureObjects(KubernetesNamespace namespace)
      throws InfrastructureException {
    if (!(namespace instanceof OpenShiftProject)) {
      throw new InternalInfrastructureException(
          String.format(
              "OpenShiftProject instance expected, but got '%s'. Please report a bug!",
              namespace.getClass().getCanonicalName()));
    }
    OpenShiftProject project = (OpenShiftProject) namespace;

    return project.routes().get();
  }

  @Override
  protected Optional<String> findHostForServicePort(List<Route> routes, Service service, int port) {
    return Routes.findRouteForServicePort(routes, service, port).map(r -> r.getSpec().getHost());
  }
}
