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
package org.eclipse.che.workspace.infrastructure.openshift.provision.installer;

import static java.lang.String.format;
import static java.util.Collections.singletonMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InternalEnvironment;
import org.eclipse.che.api.workspace.server.spi.InternalMachineConfig;
import org.eclipse.che.api.workspace.server.token.MachineTokenProvider;
import org.eclipse.che.api.workspace.shared.Constants;
import org.eclipse.che.workspace.infrastructure.openshift.environment.OpenShiftEnvironment;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests {@link InstallerConfigProvisioner}.
 *
 * @author Anton Korneta
 * @author Sergii Leshchenko
 * @author Alexander Garagatyi
 */
@Listeners(MockitoTestNGListener.class)
public class InstallerConfigProvisionerTest {
  private static final String CHE_SERVER_ENDPOINT = "localhost:8080";
  private static final String WORKSPACE_ID = "workspace123";

  @Mock private MachineTokenProvider machineTokenProvider;
  @Mock private RuntimeIdentity runtimeIdentity;
  @Mock protected OpenShiftEnvironment osEnvironment;

  private InstallerConfigProvisioner installerConfigProvisioner;

  @BeforeMethod
  public void setUp() throws Exception {
    installerConfigProvisioner =
        new InstallerConfigProvisioner(machineTokenProvider, CHE_SERVER_ENDPOINT);

    when(runtimeIdentity.getWorkspaceId()).thenReturn(WORKSPACE_ID);
  }

  @Test
  public void provisionWithAgentsRequiredEnvs() throws Exception {
    // given
    when(machineTokenProvider.getToken(WORKSPACE_ID)).thenReturn("superToken");

    InternalMachineConfig machine1 =
        createMachine(new HashMap<>(singletonMap("env1", "val1")), true);
    InternalMachineConfig machine2 = createMachine(new HashMap<>(), false);
    final Map<String, InternalMachineConfig> machines =
        ImmutableMap.of("pod1/wsagent", machine1, "pod2/machine", machine2);

    InternalEnvironment environment = createEnvironment(machines);

    // when
    installerConfigProvisioner.provision(environment, osEnvironment, runtimeIdentity);

    // then
    Map<String, String> env = machine1.getEnv();
    verifyContainsEnv(env, "CHE_API", CHE_SERVER_ENDPOINT);
    verifyContainsEnv(env, "CHE_MACHINE_TOKEN", "superToken");
    verifyContainsEnv(env, "CHE_WORKSPACE_ID", WORKSPACE_ID);

    env = machine2.getEnv();
    verifyContainsEnv(env, "CHE_API", CHE_SERVER_ENDPOINT);
    verifyContainsEnv(env, "CHE_MACHINE_TOKEN", "superToken");
    assertFalse(
        env.containsKey("CHE_WORKSPACE_ID"), "Environment variable '%s' found CHE_WORKSPACE_ID");
  }

  private InternalMachineConfig createMachine(Map<String, String> env, boolean isDev) {
    InternalMachineConfig machineConfig = mock(InternalMachineConfig.class);
    when(machineConfig.getEnv()).thenReturn(env);
    if (isDev) {
      when(machineConfig.getServers())
          .thenReturn(
              singletonMap(Constants.SERVER_WS_AGENT_HTTP_REFERENCE, mock(ServerConfig.class)));
    }
    return machineConfig;
  }

  private InternalEnvironment createEnvironment(Map<String, InternalMachineConfig> machines) {
    InternalEnvironment environment = mock(InternalEnvironment.class);
    when(environment.getMachines()).thenReturn(machines);
    return environment;
  }

  private void verifyContainsEnv(Map<String, String> env, String name, String expectedValue) {
    assertTrue(env.containsKey(name), format("Expected environment variable '%s' not found", name));

    String actualValue = env.get(name);
    assertEquals(
        actualValue,
        expectedValue,
        format(
            "Environment variable '%s' expected with " + "value '%s' but found with '%s'",
            name, expectedValue, actualValue));
  }
}
