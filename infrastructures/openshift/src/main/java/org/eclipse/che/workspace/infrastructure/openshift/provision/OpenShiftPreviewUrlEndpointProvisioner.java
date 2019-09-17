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
import io.fabric8.openshift.api.model.Route;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.PreviewUrlEndpointsProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.external.ExternalServerExposer;
import org.eclipse.che.workspace.infrastructure.openshift.environment.OpenShiftEnvironment;

@Singleton
public class OpenShiftPreviewUrlEndpointProvisioner<T extends KubernetesEnvironment>
    extends PreviewUrlEndpointsProvisioner<OpenShiftEnvironment> {

  @Inject
  public OpenShiftPreviewUrlEndpointProvisioner(
      ExternalServerExposer<OpenShiftEnvironment> externalServerExposer) {
    super(externalServerExposer);
  }

  @Override
  protected boolean hasMatchingEndpoint(OpenShiftEnvironment env, Service service, int port) {
    for (Route route : env.getRoutes().values()) {
      if (route.getSpec().getTo().getName().equals(service.getMetadata().getName())
          && route.getSpec().getPort().getTargetPort().getStrVal().equals("server-" + port)) {
        return true;
      }
    }
    return false;
  }
}
