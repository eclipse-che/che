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
package org.eclipse.che.workspace.infrastructure.openshift;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;

import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.workspace.infrastructure.openshift.environment.OpenShiftEnvironment;
import org.eclipse.che.workspace.infrastructure.openshift.project.pvc.WorkspaceVolumesStrategy;
import org.eclipse.che.workspace.infrastructure.openshift.provision.UniqueNamesProvisioner;
import org.eclipse.che.workspace.infrastructure.openshift.provision.env.EnvVarsConverter;
import org.eclipse.che.workspace.infrastructure.openshift.provision.limits.ram.RamLimitProvisioner;
import org.eclipse.che.workspace.infrastructure.openshift.provision.restartpolicy.RestartPolicyRewriter;
import org.eclipse.che.workspace.infrastructure.openshift.provision.route.TlsRouteProvisioner;
import org.eclipse.che.workspace.infrastructure.openshift.provision.server.ServersConverter;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests {@link OpenShiftEnvironmentProvisioner}.
 *
 * @author Anton Korneta
 */
@Listeners(MockitoTestNGListener.class)
public class OpenShiftEnvironmentProvisionerTest {

  @Mock private WorkspaceVolumesStrategy volumesStrategy;
  @Mock private UniqueNamesProvisioner uniqueNamesProvisioner;
  @Mock private OpenShiftEnvironment osEnv;
  @Mock private RuntimeIdentity runtimeIdentity;
  @Mock private TlsRouteProvisioner tlsRouteProvisioner;
  @Mock private EnvVarsConverter envVarsProvisioner;
  @Mock private ServersConverter serversProvisioner;
  @Mock private RestartPolicyRewriter restartPolicyRewriter;
  @Mock private RamLimitProvisioner ramLimitProvisioner;

  private OpenShiftEnvironmentProvisioner osInfraProvisioner;

  private InOrder provisionOrder;

  @BeforeMethod
  public void setUp() {
    osInfraProvisioner =
        new OpenShiftEnvironmentProvisioner(
            true,
            uniqueNamesProvisioner,
            tlsRouteProvisioner,
            serversProvisioner,
            envVarsProvisioner,
            restartPolicyRewriter,
            volumesStrategy,
            ramLimitProvisioner);
    provisionOrder =
        inOrder(
            volumesStrategy,
            uniqueNamesProvisioner,
            tlsRouteProvisioner,
            serversProvisioner,
            envVarsProvisioner,
            restartPolicyRewriter,
            ramLimitProvisioner);
  }

  @Test
  public void performsOrderedProvisioning() throws Exception {
    osInfraProvisioner.provision(osEnv, runtimeIdentity);

    provisionOrder.verify(serversProvisioner).provision(eq(osEnv), eq(runtimeIdentity));
    provisionOrder.verify(envVarsProvisioner).provision(eq(osEnv), eq(runtimeIdentity));
    provisionOrder.verify(volumesStrategy).provision(eq(osEnv), eq(runtimeIdentity));
    provisionOrder.verify(restartPolicyRewriter).provision(eq(osEnv), eq(runtimeIdentity));
    provisionOrder.verify(uniqueNamesProvisioner).provision(eq(osEnv), eq(runtimeIdentity));
    provisionOrder.verify(tlsRouteProvisioner).provision(eq(osEnv), eq(runtimeIdentity));
    provisionOrder.verify(ramLimitProvisioner).provision(eq(osEnv), eq(runtimeIdentity));
    provisionOrder.verifyNoMoreInteractions();
  }
}
