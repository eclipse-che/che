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
package org.eclipse.che.workspace.infrastructure.openshift.provision.route;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.fabric8.openshift.api.model.Route;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.workspace.infrastructure.openshift.environment.OpenShiftEnvironment;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests {@link TlsRouteProvisioner}.
 *
 * @author Ilya Buziuk
 */
@Listeners(MockitoTestNGListener.class)
public class TlsRouteProvisionerTest {

  @Mock private OpenShiftEnvironment osEnv;
  @Mock private RuntimeIdentity runtimeIdentity;

  @Test
  public void doNothingWhenTlsDisabled() throws Exception {
    TlsRouteProvisioner tlsProvisioner = new TlsRouteProvisioner(false);
    tlsProvisioner.provision(osEnv, runtimeIdentity);
    verify(osEnv, never()).getRoutes();
  }

  @Test
  public void provisionTlsForRoutes() throws Exception {
    TlsRouteProvisioner tlsProvisioner = new TlsRouteProvisioner(true);
    final Map<String, Route> routes = new HashMap<>();
    when(osEnv.getRoutes()).thenReturn(routes);
    tlsProvisioner.provision(osEnv, runtimeIdentity);
    verify(osEnv, times(1)).getRoutes();
  }
}
