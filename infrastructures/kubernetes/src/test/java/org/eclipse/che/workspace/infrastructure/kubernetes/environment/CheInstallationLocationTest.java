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
package org.eclipse.che.workspace.infrastructure.kubernetes.environment;

import static org.testng.Assert.*;

import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.testng.annotations.Test;

public class CheInstallationLocationTest {
  @Test
  public void returnKubernetesNamespaceWhenBothSet() throws InfrastructureException {
    CheInstallationLocation cheInstallationLocation = new CheInstallationLocation();
    cheInstallationLocation.kubernetesNamespace = "kube";
    cheInstallationLocation.podNamespace = "pod";
    assertEquals(cheInstallationLocation.getInstallationLocationNamespace(), "kube");
  }

  @Test
  public void returnKubernetesNamespaceWhenItsOnlySet() throws InfrastructureException {
    CheInstallationLocation cheInstallationLocation = new CheInstallationLocation();
    cheInstallationLocation.kubernetesNamespace = "kube";
    cheInstallationLocation.podNamespace = null;
    assertEquals(cheInstallationLocation.getInstallationLocationNamespace(), "kube");
  }

  @Test
  public void returnPodNamespaceWhenKubernetesNamespaceNotSet() throws InfrastructureException {
    CheInstallationLocation cheInstallationLocation = new CheInstallationLocation();
    cheInstallationLocation.kubernetesNamespace = null;
    cheInstallationLocation.podNamespace = "pod";
    assertEquals(cheInstallationLocation.getInstallationLocationNamespace(), "pod");
  }

  @Test(expectedExceptions = InfrastructureException.class)
  public void throwExceptionWhenNoneSet() throws InfrastructureException {
    CheInstallationLocation cheInstallationLocation = new CheInstallationLocation();
    cheInstallationLocation.kubernetesNamespace = null;
    cheInstallationLocation.podNamespace = null;
    cheInstallationLocation.getInstallationLocationNamespace();
  }
}
