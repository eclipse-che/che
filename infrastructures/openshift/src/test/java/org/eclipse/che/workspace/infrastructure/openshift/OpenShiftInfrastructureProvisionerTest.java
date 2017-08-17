/*******************************************************************************
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.workspace.infrastructure.openshift;

import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.workspace.infrastructure.openshift.environment.OpenShiftEnvironment;
import org.eclipse.che.workspace.infrastructure.openshift.provision.installer.InstallerConfigProvisioner;
import org.eclipse.che.workspace.infrastructure.openshift.provision.volume.PersistentVolumeClaimProvisioner;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.inOrder;

/**
 * Tests {@link OpenShiftInfrastructureProvisioner}.
 *
 * @author Anton Korneta
 */
@Listeners(MockitoTestNGListener.class)
public class OpenShiftInfrastructureProvisionerTest {

    @Mock
    private InstallerConfigProvisioner       installerProvisioner;
    @Mock
    private PersistentVolumeClaimProvisioner pvcProvisioner;
    @Mock
    private EnvironmentImpl                  environment;
    @Mock
    private OpenShiftEnvironment             osEnv;
    @Mock
    private RuntimeIdentity                  runtimeIdentity;

    private OpenShiftInfrastructureProvisioner osInfraProvisioner;

    private InOrder provisionOrder;

    @BeforeMethod
    public void setUp() {
        osInfraProvisioner = new OpenShiftInfrastructureProvisioner(installerProvisioner, pvcProvisioner);
        provisionOrder = inOrder(installerProvisioner, pvcProvisioner);
    }

    @Test
    public void performsOrderedProvisioning() throws Exception {
        osInfraProvisioner.provision(environment, osEnv, runtimeIdentity);

        provisionOrder.verify(installerProvisioner)
                      .provision(eq(environment), eq(osEnv), eq(runtimeIdentity));
        provisionOrder.verify(pvcProvisioner)
                      .provision(eq(environment), eq(osEnv), eq(runtimeIdentity));
        provisionOrder.verifyNoMoreInteractions();
    }

}
