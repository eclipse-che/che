/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.kubernetes.provision;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests {@link ExternalServerIngressTlsProvisioner}.
 *
 * @author Ilya Buziuk
 * @author Sergii Leshchenko
 * @author Guy Daich
 */
@Listeners(MockitoTestNGListener.class)
public class ExternalServerIngressTlsProvisionerTest {

  @Mock private KubernetesEnvironment k8sEnv;
  @Mock private RuntimeIdentity runtimeIdentity;

  @Test
  public void doNothingWhenTlsDisabled() throws Exception {
    // given
    ExternalServerIngressTlsProvisioner externalServerIngressTlsProvisioner =
        new ExternalServerIngressTlsProvisioner(false, "", "");

    // when
    externalServerIngressTlsProvisioner.provision(k8sEnv, runtimeIdentity);

    // then
    verify(k8sEnv, never()).getIngresses();
  }
}
