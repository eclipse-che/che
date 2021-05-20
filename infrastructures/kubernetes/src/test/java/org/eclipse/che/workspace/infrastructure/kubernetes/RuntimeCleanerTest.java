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
package org.eclipse.che.workspace.infrastructure.kubernetes;

import static org.eclipse.che.workspace.infrastructure.kubernetes.server.WorkspaceExposureType.GATEWAY;
import static org.eclipse.che.workspace.infrastructure.kubernetes.server.WorkspaceExposureType.NATIVE;
import static org.eclipse.che.workspace.infrastructure.kubernetes.server.external.DefaultHostExternalServiceExposureStrategy.DEFAULT_HOST_STRATEGY;
import static org.eclipse.che.workspace.infrastructure.kubernetes.server.external.MultiHostExternalServiceExposureStrategy.MULTI_HOST_STRATEGY;
import static org.eclipse.che.workspace.infrastructure.kubernetes.server.external.SingleHostExternalServiceExposureStrategy.SINGLE_HOST_STRATEGY;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.*;

import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.CheNamespace;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesNamespace;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(MockitoTestNGListener.class)
public class RuntimeCleanerTest {
  private final String WS_ID = "123";

  @Mock private CheNamespace cheNamespace;
  @Mock private KubernetesNamespace kubeNamespace;

  @Test
  public void shouldCleanupEverythingIfSinglehostGateway() throws InfrastructureException {
    RuntimeCleaner runtimeCleaner =
        new RuntimeCleaner(SINGLE_HOST_STRATEGY, GATEWAY.getConfigValue(), cheNamespace);

    runtimeCleaner.cleanUp(kubeNamespace, WS_ID);

    verify(cheNamespace).cleanUp(WS_ID);
    verify(kubeNamespace).cleanUp();
  }

  @Test(dataProvider = "notSinglehostGateway")
  public void shouldNotCleanupCheNamespaceIfNotSinglehostGateway(
      String serverStrategy, String singleHostStrategy) throws InfrastructureException {
    RuntimeCleaner runtimeCleaner =
        new RuntimeCleaner(serverStrategy, singleHostStrategy, cheNamespace);

    runtimeCleaner.cleanUp(kubeNamespace, WS_ID);

    verify(kubeNamespace).cleanUp();
    verify(cheNamespace, never()).cleanUp(any());
  }

  @DataProvider
  public static Object[][] notSinglehostGateway() {
    return new Object[][] {
      {SINGLE_HOST_STRATEGY, NATIVE.getConfigValue()},
      {MULTI_HOST_STRATEGY, GATEWAY.getConfigValue()},
      {MULTI_HOST_STRATEGY, NATIVE.getConfigValue()},
      {DEFAULT_HOST_STRATEGY, GATEWAY.getConfigValue()},
      {DEFAULT_HOST_STRATEGY, NATIVE.getConfigValue()},
    };
  }
}
