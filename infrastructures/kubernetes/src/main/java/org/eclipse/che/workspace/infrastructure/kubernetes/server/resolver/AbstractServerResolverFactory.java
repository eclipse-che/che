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
package org.eclipse.che.workspace.infrastructure.kubernetes.server.resolver;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.Service;
import java.util.List;
import java.util.Map;
import javax.inject.Named;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.AbstractExposureStrategyAwareProvider;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.WorkspaceExposureType;

/**
 * An abstract class that specifies the logic for creating the server resolvers for given cluster
 * type.
 *
 * @param <T> Type representing the object for exposing endpoints in the cluster. I.e. ingress or
 *     route.
 */
public class AbstractServerResolverFactory<T> {

  private final ResolverConstructorProvider<T> constructorProvider;

  protected AbstractServerResolverFactory(
      @Named("che.infra.kubernetes.server_strategy") String exposureStrategy,
      @Named("che.infra.kubernetes.singlehost.workspace.exposure") String wsExposureType,
      Map<WorkspaceExposureType, ResolverConstructor<T>> mapping,
      String errorMessageTemplate) {
    constructorProvider =
        new ResolverConstructorProvider<T>(
            exposureStrategy, wsExposureType, mapping, errorMessageTemplate);
  }

  /**
   * Create {@link ServerResolver} for configured server strategy.
   *
   * @return {@link ServerResolver} instance
   */
  public ServerResolver create(
      List<Service> services, List<T> ingresses, List<ConfigMap> configMaps) {
    return constructorProvider.get().create(services, ingresses, configMaps);
  }

  /** Constructs a new {@link ServerResolver} instance from the provided parameters. */
  protected interface ResolverConstructor<T> {
    ServerResolver create(List<Service> services, List<T> ingresses, List<ConfigMap> configMaps);
  }

  /**
   * Let's reuse the logic for picking the right "thing" based on the server and workspace exposure
   * types that is implemented in {@link AbstractExposureStrategyAwareProvider}.
   */
  private static final class ResolverConstructorProvider<T>
      extends AbstractExposureStrategyAwareProvider<ResolverConstructor<T>> {

    protected ResolverConstructorProvider(
        String exposureStrategy,
        String wsExposureType,
        Map<WorkspaceExposureType, ResolverConstructor<T>> mapping,
        String errorMessageTemplate) {
      super(exposureStrategy, wsExposureType, mapping, errorMessageTemplate);
    }
  }
}
