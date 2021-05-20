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
package org.eclipse.che.workspace.infrastructure.kubernetes.provision;

import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.AbstractExposureStrategyAwareProvider;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.WorkspaceExposureType;

/**
 * Provides {@link TlsProvisioner} based on `che.infra.kubernetes.server_strategy` and
 * `che.infra.kubernetes.singlehost.workspace.exposure` properties.
 *
 * @param <T> type of environment
 */
public class TlsProvisionerProvider<T extends KubernetesEnvironment>
    extends AbstractExposureStrategyAwareProvider<TlsProvisioner<T>> {

  @Inject
  public TlsProvisionerProvider(
      @Named("che.infra.kubernetes.server_strategy") String exposureStrategy,
      @Named("che.infra.kubernetes.singlehost.workspace.exposure") String wsExposureType,
      Map<WorkspaceExposureType, TlsProvisioner<T>> mapping) {
    super(
        exposureStrategy,
        wsExposureType,
        mapping,
        "Could not initialize TLS provisioners for workspace exposure type '%s'.");
  }
}
