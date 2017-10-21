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
package org.eclipse.che.workspace.infrastructure.docker.provisioner.server;

import static java.util.Collections.singleton;
import static java.util.Collections.singletonMap;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.model.impl.RuntimeIdentityImpl;
import org.eclipse.che.api.workspace.server.spi.InternalEnvironment;
import org.eclipse.che.commons.lang.Pair;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerContainerConfig;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerEnvironment;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/** @author Alexander Garagatyi */
@Listeners(MockitoTestNGListener.class)
public class ToolingServersEnvVarsProvisionerTest {
  private static final RuntimeIdentity RUNTIME_IDENTITY =
      new RuntimeIdentityImpl("testWsId", "testEnv", "testOwner");
  private static final String CONTAINER_1_NAME = "cont1";
  private static final String CONTAINER_2_NAME = "cont2";

  @Mock private ServerEnvironmentVariableProvider provider1;
  @Mock private ServerEnvironmentVariableProvider provider2;
  @Mock private InternalEnvironment envConfig;

  private DockerEnvironment dockerEnvironment;

  @BeforeMethod
  public void setUp() throws Exception {
    dockerEnvironment = new DockerEnvironment();
    dockerEnvironment.getContainers().put(CONTAINER_1_NAME, new DockerContainerConfig());
  }

  @Test
  public void shouldProvideRuntimeIdentityToEnvProviders() throws Exception {
    // given
    ToolingServersEnvVarsProvisioner provisioner =
        new ToolingServersEnvVarsProvisioner(singleton(provider1));

    // when
    provisioner.provision(envConfig, dockerEnvironment, RUNTIME_IDENTITY);

    // then
    verify(provider1).get(eq(RUNTIME_IDENTITY));
  }

  @Test
  public void shouldCallAllProviders() throws Exception {
    // given
    ToolingServersEnvVarsProvisioner provisioner =
        new ToolingServersEnvVarsProvisioner(ImmutableSet.of(provider1, provider2));

    // when
    provisioner.provision(envConfig, dockerEnvironment, RUNTIME_IDENTITY);

    // then
    verify(provider1).get(eq(RUNTIME_IDENTITY));
    verify(provider2).get(eq(RUNTIME_IDENTITY));
  }

  @Test
  public void shouldAddAllEnvVarsToAllContainers() throws Exception {
    // given
    ToolingServersEnvVarsProvisioner provisioner =
        new ToolingServersEnvVarsProvisioner(ImmutableSet.of(provider1, provider2));
    dockerEnvironment.getContainers().put(CONTAINER_2_NAME, new DockerContainerConfig());
    Pair<String, String> envVar1 = Pair.of("env1", "value1");
    Pair<String, String> envVar2 = Pair.of("env2", "value2");
    ImmutableMap<String, String> envVarsFromProviders =
        ImmutableMap.of(
            envVar1.first, envVar1.second,
            envVar2.first, envVar2.second);
    when(provider1.get(any(RuntimeIdentity.class))).thenReturn(envVar1);
    when(provider2.get(any(RuntimeIdentity.class))).thenReturn(envVar2);
    DockerEnvironment expected = new DockerEnvironment();
    expected
        .getContainers()
        .put(CONTAINER_1_NAME, new DockerContainerConfig().setEnvironment(envVarsFromProviders));
    expected
        .getContainers()
        .put(CONTAINER_2_NAME, new DockerContainerConfig().setEnvironment(envVarsFromProviders));

    // when
    provisioner.provision(envConfig, dockerEnvironment, RUNTIME_IDENTITY);

    // then
    assertEquals(expected, dockerEnvironment);
  }

  @Test
  public void shouldIgnoreNullResultsFromProviders() throws Exception {
    // given
    ToolingServersEnvVarsProvisioner provisioner =
        new ToolingServersEnvVarsProvisioner(ImmutableSet.of(provider1, provider2));
    Pair<String, String> envVar1 = Pair.of("env1", "value1");
    when(provider1.get(any(RuntimeIdentity.class))).thenReturn(envVar1);
    when(provider2.get(any(RuntimeIdentity.class))).thenReturn(null);
    DockerEnvironment expected =
        new DockerEnvironment()
            .setContainers(
                singletonMap(
                    CONTAINER_1_NAME,
                    new DockerContainerConfig().setEnvironment(singletonMap("env1", "value1"))));

    // when
    provisioner.provision(envConfig, dockerEnvironment, RUNTIME_IDENTITY);

    // then
    assertEquals(expected, dockerEnvironment);
  }

  @Test
  public void shouldNotRemoveExistingEnvVarsWithDifferentNames() throws Exception {
    // given
    ToolingServersEnvVarsProvisioner provisioner =
        new ToolingServersEnvVarsProvisioner(singleton(provider1));
    Pair<String, String> envVar1 = Pair.of("env1", "value1");
    Pair<String, String> existingEnvVar = Pair.of("existingEnvVar", "some-value");
    when(provider1.get(any(RuntimeIdentity.class))).thenReturn(envVar1);
    dockerEnvironment
        .getContainers()
        .get(CONTAINER_1_NAME)
        .getEnvironment()
        .put(existingEnvVar.first, existingEnvVar.second);
    DockerEnvironment expected = new DockerEnvironment();
    expected
        .getContainers()
        .put(
            CONTAINER_1_NAME,
            new DockerContainerConfig()
                .setEnvironment(
                    ImmutableMap.of(
                        envVar1.first,
                        envVar1.second,
                        existingEnvVar.first,
                        existingEnvVar.second)));

    // when
    provisioner.provision(envConfig, dockerEnvironment, RUNTIME_IDENTITY);

    // then
    assertEquals(expected, dockerEnvironment);
  }

  @Test
  public void shouldReplaceExistingEnvVarsWithMatchingNames() throws Exception {
    // given
    ToolingServersEnvVarsProvisioner provisioner =
        new ToolingServersEnvVarsProvisioner(singleton(provider1));
    String existingEnvVarName = "existingEnvVar";
    String envVarValueFromProvider = "value1";
    String oldEnvVarValue = "some-value";
    when(provider1.get(any(RuntimeIdentity.class)))
        .thenReturn(Pair.of(existingEnvVarName, envVarValueFromProvider));
    dockerEnvironment
        .getContainers()
        .get(CONTAINER_1_NAME)
        .getEnvironment()
        .put(existingEnvVarName, oldEnvVarValue);
    DockerEnvironment expected = new DockerEnvironment();
    expected
        .getContainers()
        .put(
            CONTAINER_1_NAME,
            new DockerContainerConfig()
                .setEnvironment(singletonMap(existingEnvVarName, envVarValueFromProvider)));

    // when
    provisioner.provision(envConfig, dockerEnvironment, RUNTIME_IDENTITY);

    // then
    assertEquals(expected, dockerEnvironment);
  }
}
