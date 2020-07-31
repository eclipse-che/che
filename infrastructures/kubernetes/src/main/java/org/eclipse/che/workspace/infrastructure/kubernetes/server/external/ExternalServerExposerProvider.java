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
package org.eclipse.che.workspace.infrastructure.kubernetes.server.external;

import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.AbstractExposureStrategyAwareProvider;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.WorkspaceExposureType;

/**
 * Provides {@link ExternalServerExposer} based on `che.infra.kubernetes.server_strategy` and
 * `che.infra.kubernetes.single_host.workspace.exposure` properties.
 *
 * @param <T> type of environment
 */
@Singleton
public class ExternalServerExposerProvider<T extends KubernetesEnvironment>
    extends AbstractExposureStrategyAwareProvider<ExternalServerExposer<T>> {
  @Inject
  public ExternalServerExposerProvider(
      @Named("che.infra.kubernetes.server_strategy") String exposureStrategy,
      @Named("che.infra.kubernetes.single_host.workspace.exposure") String exposureType,
      Map<WorkspaceExposureType, ExternalServerExposer<T>> exposers) {

    super(
        exposureStrategy,
        exposureType,
        exposers,
        "Could not find an external server exposer implementation for the exposure type '%s'.");
  }
}
