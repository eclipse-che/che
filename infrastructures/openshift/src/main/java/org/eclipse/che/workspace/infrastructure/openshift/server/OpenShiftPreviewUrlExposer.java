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
package org.eclipse.che.workspace.infrastructure.openshift.server;

import com.google.common.annotations.VisibleForTesting;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.openshift.api.model.Route;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.PreviewUrlExposer;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.external.ExternalServerExposer;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.external.ExternalServerExposerProvider;
import org.eclipse.che.workspace.infrastructure.openshift.environment.OpenShiftEnvironment;
import org.eclipse.che.workspace.infrastructure.openshift.util.Routes;

/**
 * Extends {@link PreviewUrlExposer} with OpenShift capabilities. We work with {@link Route} and
 * {@link OpenShiftEnvironment}.
 */
@Singleton
public class OpenShiftPreviewUrlExposer extends PreviewUrlExposer<OpenShiftEnvironment> {

  @Inject
  public OpenShiftPreviewUrlExposer(
      ExternalServerExposerProvider<OpenShiftEnvironment> externalServerExposer) {
    super(externalServerExposer);
  }

  @VisibleForTesting
  protected OpenShiftPreviewUrlExposer(
      ExternalServerExposer<OpenShiftEnvironment> externalServerExposer) {
    super(externalServerExposer);
  }

  @Override
  protected boolean hasMatchingEndpoint(OpenShiftEnvironment env, Service service, int port) {
    return Routes.findRouteForServicePort(env.getRoutes().values(), service, port).isPresent();
  }
}
