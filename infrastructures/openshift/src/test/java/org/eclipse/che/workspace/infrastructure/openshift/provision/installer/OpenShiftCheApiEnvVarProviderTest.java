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
package org.eclipse.che.workspace.infrastructure.openshift.provision.installer;

import static org.testng.Assert.assertEquals;

import org.eclipse.che.commons.lang.Pair;
import org.eclipse.che.workspace.infrastructure.openshift.provision.OpenShiftCheApiEnvVarProvider;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests {@link OpenShiftCheApiEnvVarProvider}.
 *
 * @author Sergii Leshchenko
 */
@Listeners(MockitoTestNGListener.class)
public class OpenShiftCheApiEnvVarProviderTest {

  private static final String CHE_SERVER_ENDPOINT = "localhost:8080";

  private OpenShiftCheApiEnvVarProvider openShiftCheApiEnvVarProvider;

  @BeforeMethod
  public void setUp() throws Exception {
    openShiftCheApiEnvVarProvider = new OpenShiftCheApiEnvVarProvider(CHE_SERVER_ENDPOINT);
  }

  @Test
  public void shouldReturnCheApiEnv() throws Exception {
    // when
    Pair<String, String> cheApiEnv = openShiftCheApiEnvVarProvider.get(null);

    // then
    assertEquals(cheApiEnv.first, OpenShiftCheApiEnvVarProvider.API_ENDPOINT_URL_VARIABLE);
    assertEquals(cheApiEnv.second, CHE_SERVER_ENDPOINT);
  }
}
