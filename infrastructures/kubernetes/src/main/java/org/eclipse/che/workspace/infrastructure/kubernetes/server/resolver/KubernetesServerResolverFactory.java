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

import static org.eclipse.che.workspace.infrastructure.kubernetes.server.WorkspaceExposureType.GATEWAY;
import static org.eclipse.che.workspace.infrastructure.kubernetes.server.WorkspaceExposureType.NATIVE;

import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.extensions.Ingress;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.external.IngressPathTransformInverter;

/**
 * Factory that decides by configuration, which {@link ServerResolver} implementation to use in
 * Kubernetes environment.
 */
@Singleton
public class KubernetesServerResolverFactory extends AbstractServerResolverFactory<Ingress> {
  @Inject
  public KubernetesServerResolverFactory(
      IngressPathTransformInverter pathTransformInverter,
      @Named("che.host") String cheHost,
      @Named("che.infra.kubernetes.server_strategy") String exposureStrategy,
      @Named("che.infra.kubernetes.singlehost.workspace.exposure") String wsExposureType) {

    super(
        exposureStrategy,
        wsExposureType,
        ImmutableMap.of(
            GATEWAY,
                (ss, is, cs) ->
                    new ConfigMapServerResolver(
                        ss, cs, cheHost, new IngressServerResolver(pathTransformInverter, ss, is)),
            NATIVE, (ss, is, cs) -> new IngressServerResolver(pathTransformInverter, ss, is)),
        "Failed to initialize KubernetesServerResolverFactory for workspace exposure type '%s'.");
  }
}
