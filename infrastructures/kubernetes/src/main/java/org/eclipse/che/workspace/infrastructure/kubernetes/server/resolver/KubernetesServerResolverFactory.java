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
package org.eclipse.che.workspace.infrastructure.kubernetes.server.resolver;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.extensions.Ingress;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.external.ExternalServerExposer;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.external.IngressPathTransformInverter;

/**
 * Factory that decides by configuration, which {@link ServerResolver} implementation to use in
 * Kubernetes environment.
 */
@Singleton
public class KubernetesServerResolverFactory {
  private final ExternalServerExposer.Type type;
  private final IngressPathTransformInverter pathTransformInverter;
  private final String cheHost;

  @Inject
  public KubernetesServerResolverFactory(
      IngressPathTransformInverter pathTransformInverter,
      @Named("che.host") String cheHost,
      @Named("che.infra.kubernetes.single_host.workspace.exposure") String exposureStrategy) {
    type = ExternalServerExposer.Type.fromConfigurationValue(exposureStrategy);
    this.pathTransformInverter = pathTransformInverter;
    this.cheHost = cheHost;
  }

  /**
   * Create {@link ServerResolver} for configured server strategy.
   *
   * @return {@link ServerResolver} instance
   */
  public ServerResolver create(
      List<Service> services, List<Ingress> ingresses, List<ConfigMap> configMaps) {
    switch (type) {
      case NATIVE:
        return new IngressServerResolver(pathTransformInverter, services, ingresses);
      case GATEWAY:
        return new ConfigMapServerResolver(services, configMaps, cheHost);
      default:
        throw new IllegalStateException(
            "Unhandled server resolver strategy " + type + ". This is a bug.");
    }
  }
}
