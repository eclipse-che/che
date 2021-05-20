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
package org.eclipse.che.workspace.infrastructure.kubernetes.environment;

import com.google.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;

/**
 * This class checks the KUBERNETES_NAMESPACE and POD_NAMESPACE environment variables to determine
 * what namespace Che is installed in. Users should use this class to retrieve the installation
 * namespace name.
 *
 * @author Tom George
 */
@Singleton
public class CheInstallationLocation {
  @Inject(optional = true)
  @Named("env.KUBERNETES_NAMESPACE")
  String kubernetesNamespace;

  @Inject(optional = true)
  @Named("env.POD_NAMESPACE")
  String podNamespace;

  /**
   * @return The name of the namespace where Che is installed or null if both {@code
   *     KUBERNETES_NAMESPACE} and {@code POD_NAMESPACE} environment variables are not set
   * @throws InfrastructureException when both {@code KUBERNETES_NAMESPACE} and {@code
   *     POD_NAMESPACE} are null
   */
  public String getInstallationLocationNamespace() throws InfrastructureException {
    if (kubernetesNamespace == null && podNamespace == null) {
      throw new InfrastructureException(
          "Neither KUBERNETES_NAMESPACE nor POD_NAMESPACE is defined. Unable to determine Che installation location");
    }
    return kubernetesNamespace == null ? podNamespace : kubernetesNamespace;
  }
}
