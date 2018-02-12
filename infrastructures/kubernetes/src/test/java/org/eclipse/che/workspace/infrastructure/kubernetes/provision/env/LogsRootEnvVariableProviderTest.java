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
package org.eclipse.che.workspace.infrastructure.kubernetes.provision.env;

import static org.eclipse.che.workspace.infrastructure.kubernetes.provision.env.LogsRootEnvVariableProvider.WORKSPACE_LOGS_ROOT_ENV_VAR;
import static org.testng.Assert.assertEquals;

import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.commons.lang.Pair;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests {@link LogsRootEnvVariableProvider}.
 *
 * @author Anton Korneta
 */
@Listeners(MockitoTestNGListener.class)
public class LogsRootEnvVariableProviderTest {

  private static final String WORKSPACE_LOGS_ROOT_PATH = "/workspace_logs";

  @Mock private RuntimeIdentity identity;

  private LogsRootEnvVariableProvider logsRootEnvVariableProvider;

  @BeforeMethod
  public void setup() {
    logsRootEnvVariableProvider = new LogsRootEnvVariableProvider(WORKSPACE_LOGS_ROOT_PATH);
  }

  @Test
  public void testProviderReturnsLogsEnvironmentVariable() throws Exception {
    final Pair<String, String> eVar = logsRootEnvVariableProvider.get(identity);

    assertEquals(eVar.first, WORKSPACE_LOGS_ROOT_ENV_VAR);
    assertEquals(eVar.second, WORKSPACE_LOGS_ROOT_PATH);
  }
}
