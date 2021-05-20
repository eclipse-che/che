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
package org.eclipse.che.workspace.infrastructure.kubernetes.server.external;

import io.fabric8.kubernetes.api.model.extensions.Ingress;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;

/**
 * Uses Kubernetes {@link Ingress}es to expose the services using subdomains a.k.a. multi-host.
 *
 * @see ExternalServerExposer
 */
@Singleton
public class MultihostIngressServerExposer<T extends KubernetesEnvironment>
    extends IngressServerExposer<T> implements ExternalServerExposer<T> {
  @Inject
  public MultihostIngressServerExposer(
      MultiHostExternalServiceExposureStrategy serviceExposureStrategy,
      @Named("infra.kubernetes.ingress.annotations") Map<String, String> annotations,
      @Nullable @Named("che.infra.kubernetes.ingress.labels") String labelsProperty,
      @Nullable @Named("che.infra.kubernetes.ingress.path_transform") String pathTransformFmt) {
    super(serviceExposureStrategy, annotations, labelsProperty, pathTransformFmt);
  }
}
