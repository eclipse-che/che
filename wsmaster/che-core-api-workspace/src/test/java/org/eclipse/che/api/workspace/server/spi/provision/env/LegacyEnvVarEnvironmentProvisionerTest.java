/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.workspace.server.spi.provision.env;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.installer.server.model.impl.InstallerImpl;
import org.eclipse.che.api.workspace.server.model.impl.RuntimeIdentityImpl;
import org.eclipse.che.api.workspace.server.spi.environment.InternalEnvironment;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.eclipse.che.commons.lang.Pair;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(MockitoTestNGListener.class)
public class LegacyEnvVarEnvironmentProvisionerTest {

  private static final RuntimeIdentity RUNTIME_IDENTITY =
      new RuntimeIdentityImpl("testWsId", "testEnv", "testOwnerId");

  @Mock private LegacyEnvVarProvider provider1;
  @Mock private LegacyEnvVarProvider provider2;
  @Mock private InternalEnvironment internalEnvironment;
  @Mock private InternalMachineConfig machineConfig1;
  @Mock private InternalMachineConfig machineConfig2;
  private Map<String, String> machine1Env;
  private Map<String, String> machine2Env;

  @BeforeMethod
  public void setup() {
    machine1Env = new HashMap<>();
    machine2Env = new HashMap<>();

    when(internalEnvironment.getMachines())
        .thenReturn(ImmutableMap.of("machine1", machineConfig1, "machine2", machineConfig2));

    lenient().when(machineConfig2.getInstallers()).thenReturn(emptyList());

    when(machineConfig1.getEnv()).thenReturn(machine1Env);
    when(machineConfig2.getEnv()).thenReturn(machine2Env);
  }

  @Test
  public void shouldProvisionWorkspacesWithInstallers() throws Exception {
    // given

    // make 1 of the machine configs have an installer - this should make the whole environment
    // be considered legacy and therefore the legacy env vars should be applied.
    when(machineConfig1.getInstallers()).thenReturn(singletonList(mock(InstallerImpl.class)));

    LegacyEnvVarEnvironmentProvisioner provisioner =
        new LegacyEnvVarEnvironmentProvisioner(ImmutableSet.of(provider1, provider2));

    when(provider1.get(any())).thenReturn(Pair.of("test", "test"));
    when(provider2.get(any())).thenReturn(Pair.of("test", "test"));

    // when
    provisioner.provision(RUNTIME_IDENTITY, internalEnvironment);

    // then
    verify(provider1).get(eq(RUNTIME_IDENTITY));
    verify(provider2).get(eq(RUNTIME_IDENTITY));
  }

  @Test
  public void shouldNotProvisionWorkspacesWithoutInstallers() throws Exception {
    // given

    // none of the machines has installers. Therefore we should see no legacy env var provisioning
    when(machineConfig1.getInstallers()).thenReturn(emptyList());

    LegacyEnvVarEnvironmentProvisioner provisioner =
        new LegacyEnvVarEnvironmentProvisioner(ImmutableSet.of(provider1, provider2));

    // when
    provisioner.provision(RUNTIME_IDENTITY, internalEnvironment);

    // then
    verify(provider1, never()).get(eq(RUNTIME_IDENTITY));
    verify(provider2, never()).get(eq(RUNTIME_IDENTITY));
  }

  @Test
  public void shouldAddAllEnvVarsToAllContainers() throws Exception {
    // given
    when(machineConfig1.getInstallers()).thenReturn(singletonList(mock(InstallerImpl.class)));
    LegacyEnvVarEnvironmentProvisioner provisioner =
        new LegacyEnvVarEnvironmentProvisioner(ImmutableSet.of(provider1, provider2));
    Pair<String, String> envVar1 = Pair.of("env1", "value1");
    Pair<String, String> envVar2 = Pair.of("env2", "value2");
    ImmutableMap<String, String> envVarsFromProviders =
        ImmutableMap.of(
            envVar1.first, envVar1.second,
            envVar2.first, envVar2.second);
    when(provider1.get(any(RuntimeIdentity.class))).thenReturn(envVar1);
    when(provider2.get(any(RuntimeIdentity.class))).thenReturn(envVar2);

    // when
    provisioner.provision(RUNTIME_IDENTITY, internalEnvironment);

    // then
    assertEquals(machine1Env, envVarsFromProviders);
    assertEquals(machine2Env, envVarsFromProviders);
  }

  @Test
  public void shouldNotRemoveExistingEnvVarsWithDifferentNames() throws Exception {
    // given
    when(machineConfig1.getInstallers()).thenReturn(singletonList(mock(InstallerImpl.class)));
    LegacyEnvVarEnvironmentProvisioner provisioner =
        new LegacyEnvVarEnvironmentProvisioner(ImmutableSet.of(provider1));
    Pair<String, String> existingEnvVar = Pair.of("existingEnvVar", "some-value");
    machine1Env.put(existingEnvVar.first, existingEnvVar.second);

    Pair<String, String> envVar1 = Pair.of("env1", "value1");
    when(provider1.get(any(RuntimeIdentity.class))).thenReturn(envVar1);

    // when
    provisioner.provision(RUNTIME_IDENTITY, internalEnvironment);

    // then
    assertEquals(
        ImmutableMap.of(existingEnvVar.first, existingEnvVar.second, envVar1.first, envVar1.second),
        machine1Env);
  }

  @Test
  public void shouldNotReplaceExistingEnvVarsWithMatchingNames() throws Exception {
    // given
    when(machineConfig1.getInstallers()).thenReturn(singletonList(mock(InstallerImpl.class)));
    LegacyEnvVarEnvironmentProvisioner provisioner =
        new LegacyEnvVarEnvironmentProvisioner(ImmutableSet.of(provider1));
    String existingEnvVarName = "existingEnvVar";
    String oldEnvVarValue = "some-value";
    machine1Env.put(existingEnvVarName, oldEnvVarValue);

    String envVarValueFromProvider = "value1";
    when(provider1.get(any(RuntimeIdentity.class)))
        .thenReturn(Pair.of(existingEnvVarName, envVarValueFromProvider));

    // when
    provisioner.provision(RUNTIME_IDENTITY, internalEnvironment);

    // then
    assertEquals(ImmutableMap.of(existingEnvVarName, oldEnvVarValue), machine1Env);
  }
}
