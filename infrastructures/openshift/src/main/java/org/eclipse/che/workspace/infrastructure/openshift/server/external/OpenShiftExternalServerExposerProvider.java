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
package org.eclipse.che.workspace.infrastructure.openshift.server.external;

import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.WorkspaceExposureType;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.external.ExternalServerExposer;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.external.ExternalServerExposerProvider;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.external.KubernetesExternalServerExposerProvider;
import org.eclipse.che.workspace.infrastructure.openshift.environment.OpenShiftEnvironment;
import org.eclipse.che.workspace.infrastructure.openshift.server.RouteServerExposer;

public class OpenShiftExternalServerExposerProvider
    extends KubernetesExternalServerExposerProvider<OpenShiftEnvironment>
    implements ExternalServerExposerProvider<OpenShiftEnvironment> {

  @Inject
  public OpenShiftExternalServerExposerProvider(
      @Named("che.infra.kubernetes.server_strategy") String exposureStrategy,
      @Named("che.infra.kubernetes.singlehost.workspace.exposure") String exposureType,
      @Named("che.infra.kubernetes.singlehost.workspace.expose_devfile_endpoints_on_subdomains")
          boolean exposeDevfileEndpointsOnSubdomains,
      @Nullable @Named("che.infra.openshift.route.labels") String labelsProperty,
      Map<WorkspaceExposureType, ExternalServerExposer<OpenShiftEnvironment>> exposers) {
    super(
        exposureStrategy,
        exposureType,
        exposeDevfileEndpointsOnSubdomains,
        null,
        null,
        labelsProperty,
        null,
        exposers);
  }

  @Override
  protected ExternalServerExposer<OpenShiftEnvironment> createSubdomainServerExposer() {
    return new RouteServerExposer(labelsProperty);
  }
}
