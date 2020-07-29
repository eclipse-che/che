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

@Singleton
public class ExternalServerExposerProvider<T extends KubernetesEnvironment> {

  private final ExternalServerExposer<T> exposer;

  @Inject
  public ExternalServerExposerProvider(
      @Named("che.infra.kubernetes.single_host.workspace.exposure") String exposureType,
      Map<ExternalServerExposer.Type, ExternalServerExposer<T>> exposers) {

    this.exposer = exposers.get(ExternalServerExposer.Type.fromConfigurationValue(exposureType));
    if (exposer == null) {
      throw new IllegalStateException(
          "Could not find an external server exposer implementation for the exposure type '"
              + exposureType
              + "'.");
    }
  }

  public ExternalServerExposer<T> getExposer() {
    return exposer;
  }
}
