/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.openshift;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;

import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.environment.InternalEnvironment;
import org.eclipse.che.workspace.infrastructure.openshift.environment.OpenShiftEnvironment;
import org.eclipse.che.workspace.infrastructure.openshift.provision.UniqueNamesProvisioner;
import org.eclipse.che.workspace.infrastructure.openshift.provision.env.EnvVarsConverter;
import org.eclipse.che.workspace.infrastructure.openshift.provision.restartpolicy.RestartPolicyRewriter;
import org.eclipse.che.workspace.infrastructure.openshift.provision.route.TlsRouteProvisioner;
import org.eclipse.che.workspace.infrastructure.openshift.provision.server.ServersConverter;
import org.eclipse.che.workspace.infrastructure.openshift.provision.volume.PersistentVolumeClaimProvisioner;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests {@link OpenShiftInfrastructureProvisioner}.
 *
 * @author Anton Korneta
 */
@Listeners(MockitoTestNGListener.class)
public class OpenShiftInfrastructureProvisionerTest {

  @Mock private PersistentVolumeClaimProvisioner pvcProvisioner;
  @Mock private UniqueNamesProvisioner uniqueNamesProvisioner;
  @Mock private InternalEnvironment environment;
  @Mock private OpenShiftEnvironment osEnv;
  @Mock private RuntimeIdentity runtimeIdentity;
  @Mock private TlsRouteProvisioner tlsRouteProvisioner;
  @Mock private EnvVarsConverter envVarsProvisioner;
  @Mock private ServersConverter serversProvisioner;
  @Mock private RestartPolicyRewriter restartPolicyRewriter;

  private OpenShiftInfrastructureProvisioner osInfraProvisioner;

  private InOrder provisionOrder;

  @BeforeMethod
  public void setUp() {
    osInfraProvisioner =
        new OpenShiftInfrastructureProvisioner(
            pvcProvisioner,
            uniqueNamesProvisioner,
            tlsRouteProvisioner,
            serversProvisioner,
            envVarsProvisioner,
            restartPolicyRewriter);
    provisionOrder =
        inOrder(
            pvcProvisioner,
            uniqueNamesProvisioner,
            tlsRouteProvisioner,
            serversProvisioner,
            envVarsProvisioner,
            restartPolicyRewriter);
  }

  @Test
  public void performsOrderedProvisioning() throws Exception {
    osInfraProvisioner.provision(environment, osEnv, runtimeIdentity);

    provisionOrder
        .verify(serversProvisioner)
        .provision(eq(environment), eq(osEnv), eq(runtimeIdentity));
    provisionOrder
        .verify(envVarsProvisioner)
        .provision(eq(environment), eq(osEnv), eq(runtimeIdentity));
    provisionOrder
        .verify(restartPolicyRewriter)
        .provision(eq(environment), eq(osEnv), eq(runtimeIdentity));
    provisionOrder
        .verify(pvcProvisioner)
        .provision(eq(environment), eq(osEnv), eq(runtimeIdentity));
    provisionOrder
        .verify(uniqueNamesProvisioner)
        .provision(eq(environment), eq(osEnv), eq(runtimeIdentity));
    provisionOrder
        .verify(tlsRouteProvisioner)
        .provision(eq(environment), eq(osEnv), eq(runtimeIdentity));
    provisionOrder.verifyNoMoreInteractions();
  }
}
