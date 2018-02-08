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

import static org.testng.Assert.assertEquals;

import org.eclipse.che.commons.lang.Pair;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests {@link KubernetesCheApiEnvVarProvider}.
 *
 * @author Sergii Leshchenko
 */
@Listeners(MockitoTestNGListener.class)
public class KubernetesCheApiEnvVarProviderTest {

  private static final String CHE_SERVER_ENDPOINT = "localhost:8080";

  private KubernetesCheApiEnvVarProvider kubernetesCheApiEnvVarProvider;

  @BeforeMethod
  public void setUp() throws Exception {
    kubernetesCheApiEnvVarProvider = new KubernetesCheApiEnvVarProvider(CHE_SERVER_ENDPOINT);
  }

  @Test
  public void shouldReturnCheApiEnv() throws Exception {
    // when
    Pair<String, String> cheApiEnv = kubernetesCheApiEnvVarProvider.get(null);

    // then
    assertEquals(cheApiEnv.first, KubernetesCheApiEnvVarProvider.API_ENDPOINT_URL_VARIABLE);
    assertEquals(cheApiEnv.second, CHE_SERVER_ENDPOINT);
  }
}
