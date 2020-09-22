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

import static org.eclipse.che.workspace.infrastructure.kubernetes.server.external.MultiHostExternalServiceExposureStrategy.INGRESS_DOMAIN_PROPERTY;
import static org.eclipse.che.workspace.infrastructure.kubernetes.server.external.MultiHostExternalServiceExposureStrategy.MULTI_HOST_STRATEGY;

import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.AbstractExposureStrategyAwareProvider;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.WorkspaceExposureType;

/**
 * Provides {@link ExternalServerExposer} based on `che.infra.kubernetes.server_strategy` and
 * `che.infra.kubernetes.singlehost.workspace.exposure` properties.
 *
 * @param <T> type of environment
 */
@Singleton
public class ExternalServerExposerProvider<T extends KubernetesEnvironment>
    extends AbstractExposureStrategyAwareProvider<ExternalServerExposer<T>> {

  private final boolean forceSubdomainEndpoints;
  private final ExternalServerExposer<T> combinedInstance;

  @Inject
  public ExternalServerExposerProvider(
      @Named("che.infra.kubernetes.server_strategy") String exposureStrategy,
      @Named("che.infra.kubernetes.singlehost.workspace.exposure") String exposureType,
      @Named("che.infra.kubernetes.singlehost.workspace.force_subdomain_devfile_endpoints")
          boolean forceSubdomainEndpoints,
      @Named(INGRESS_DOMAIN_PROPERTY) String domain,
      @Named("infra.kubernetes.ingress.annotations") Map<String, String> annotations,
      @Nullable @Named("che.infra.kubernetes.ingress.labels") String labelsProperty,
      @Nullable @Named("che.infra.kubernetes.ingress.path_transform") String pathTransformFmt,
      Map<WorkspaceExposureType, ExternalServerExposer<T>> exposers) {

    super(
        exposureStrategy,
        exposureType,
        exposers,
        "Could not find an external server exposer implementation for the exposure type '%s'.");
    this.forceSubdomainEndpoints = forceSubdomainEndpoints;
    if (forceSubdomainEndpoints) {
      this.combinedInstance =
          new CombinedSingleHostServerExposer<>(
              new IngressServerExposer<>(
                  new MultiHostExternalServiceExposureStrategy(domain, MULTI_HOST_STRATEGY),
                  annotations,
                  labelsProperty,
                  pathTransformFmt),
              instance);
    } else {
      this.combinedInstance = null;
    }
  }

  @Override
  public ExternalServerExposer<T> get() {
    if (forceSubdomainEndpoints) {
      return combinedInstance;
    } else {
      return instance;
    }
  }
}
