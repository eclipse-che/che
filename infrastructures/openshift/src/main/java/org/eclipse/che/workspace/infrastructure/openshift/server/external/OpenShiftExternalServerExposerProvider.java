/*
 * Copyright (c) 2012-2020 Red Hat, Inc.
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
import org.eclipse.che.workspace.infrastructure.kubernetes.server.WorkspaceExposureType;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.external.CombinedSingleHostServerExposer;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.external.ExternalServerExposer;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.external.ExternalServerExposerProvider;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.external.KubernetesExternalServerExposerProvider;
import org.eclipse.che.workspace.infrastructure.openshift.environment.OpenShiftEnvironment;
import org.eclipse.che.workspace.infrastructure.openshift.server.RouteServerExposer;

/**
 * Provides {@link ExternalServerExposer} based on `che.infra.kubernetes.server_strategy` and
 * `che.infra.kubernetes.singlehost.workspace.exposure` properties.
 *
 * <p>Based on server strategy, it can create a {@link CombinedSingleHostServerExposer} with
 * OpenShift specific {@link RouteServerExposer} for exposing servers on subdomains.
 */
public class OpenShiftExternalServerExposerProvider
    extends KubernetesExternalServerExposerProvider<OpenShiftEnvironment>
    implements ExternalServerExposerProvider<OpenShiftEnvironment> {

  @Inject
  public OpenShiftExternalServerExposerProvider(
      @Named("che.infra.kubernetes.server_strategy") String exposureStrategy,
      @Named("che.infra.kubernetes.singlehost.workspace.exposure") String exposureType,
      @Named("che.infra.kubernetes.singlehost.workspace.devfile_endpoint_exposure")
          String devfileEndpointExposure,
      @Named("multihost-exposer") ExternalServerExposer<OpenShiftEnvironment> multihostExposer,
      Map<WorkspaceExposureType, ExternalServerExposer<OpenShiftEnvironment>> exposers) {
    super(exposureStrategy, exposureType, devfileEndpointExposure, multihostExposer, exposers);
  }
}
