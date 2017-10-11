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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodSpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.installer.server.model.impl.InstallerImpl;
import org.eclipse.che.api.workspace.server.model.impl.ServerConfigImpl;
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
 */
@Listeners(MockitoTestNGListener.class)
public class InstallerConfigProvisionerTest {
  private static final String CHE_SERVER_ENDPOINT = "localhost:8080";
  private static final String WORKSPACE_ID = "workspace123";

  @Mock private MachineTokenProvider machineTokenProvider;
  @Mock private RuntimeIdentity runtimeIdentity;

  private InstallerConfigProvisioner installerConfigProvisioner;

  @BeforeMethod
  public void setUp() throws Exception {
    installerConfigProvisioner =
        new InstallerConfigProvisioner(machineTokenProvider, CHE_SERVER_ENDPOINT);

    when(runtimeIdentity.getWorkspaceId()).thenReturn(WORKSPACE_ID);
  }

  @Test
  public void provisionWithEnvsFromInstallersAttributes() throws Exception {
    //given
    final Pod pod = new PodBuilder().setName("test").setContainers("machine").build();
    OpenShiftEnvironment osEnvironment =
        OpenShiftEnvironment.builder()
            .setPods(ImmutableMap.of(pod.getMetadata().getName(), pod))
            .build();

    final Map<String, InternalMachineConfig> machines =
        ImmutableMap.of(
            "test/machine",
            new MachineConfigBuilder()
                .setInstallers(
                    new InstallerImpl()
                        .withProperties(ImmutableMap.of("environment", "INSTALLER1=localhost")),
                    new InstallerImpl()
                        .withProperties(ImmutableMap.of("environment", "INSTALLER2=agent")))
                .setServer(Constants.SERVER_WS_AGENT_HTTP_REFERENCE, new ServerConfigImpl())
                .build());

    InternalEnvironment environment = createEnvironment(machines);

    //when
    installerConfigProvisioner.provision(environment, osEnvironment, runtimeIdentity);

    //then
    Container container = pod.getSpec().getContainers().get(0);
    List<EnvVar> envs = container.getEnv();
    verifyContainsEnv(envs, "INSTALLER1", "localhost");
    verifyContainsEnv(envs, "INSTALLER2", "agent");
  }

  @Test
  public void provisionWithAgentsRequiredEnvs() throws Exception {
    //given
    when(machineTokenProvider.getToken(WORKSPACE_ID)).thenReturn("superToken");

    final Pod podWithAgent = new PodBuilder().setName("pod1").setContainers("wsagent").build();

    final Pod pod = new PodBuilder().setName("pod2").setContainers("machine").build();

    OpenShiftEnvironment osEnvironment =
        OpenShiftEnvironment.builder()
            .setPods(
                ImmutableMap.of(
                    podWithAgent.getMetadata().getName(),
                    podWithAgent,
                    pod.getMetadata().getName(),
                    pod))
            .build();

    final Map<String, InternalMachineConfig> machines =
        ImmutableMap.of(
            "pod1/wsagent",
            new MachineConfigBuilder()
                .setServer(Constants.SERVER_WS_AGENT_HTTP_REFERENCE, new ServerConfigImpl())
                .build(),
            "pod2/machine",
            new MachineConfigBuilder()
                .setServer(Constants.SERVER_TERMINAL_REFERENCE, new ServerConfigImpl())
                .build());

    InternalEnvironment environment = createEnvironment(machines);

    //when
    installerConfigProvisioner.provision(environment, osEnvironment, runtimeIdentity);

    //then
    Container container = podWithAgent.getSpec().getContainers().get(0);
    List<EnvVar> envs = container.getEnv();
    verifyContainsEnv(envs, "CHE_API", CHE_SERVER_ENDPOINT);
    verifyContainsEnv(envs, "USER_TOKEN", "superToken");
    verifyContainsEnv(envs, "CHE_WORKSPACE_ID", WORKSPACE_ID);

    Container container2 = pod.getSpec().getContainers().get(0);
    List<EnvVar> envs2 = container2.getEnv();
    verifyContainsEnv(envs2, "CHE_API", CHE_SERVER_ENDPOINT);
    verifyContainsEnv(envs, "USER_TOKEN", "superToken");
    verifyDoesNotContainEnv(envs2, "CHE_WORKSPACE_ID");
  }

  private InternalEnvironment createEnvironment(Map<String, InternalMachineConfig> machines) {
    InternalEnvironment environment = mock(InternalEnvironment.class);
    when(environment.getMachines()).thenReturn(machines);
    return environment;
  }

  private void verifyDoesNotContainEnv(List<EnvVar> envs, String name) {
    Optional<EnvVar> env = envs.stream().filter(e -> e.getName().equals(name)).findAny();

    assertFalse(env.isPresent(), format("Environment variable '%s' found", name));
  }

  private void verifyContainsEnv(List<EnvVar> envs, String name, String expectedValue) {
    Optional<EnvVar> env = envs.stream().filter(e -> e.getName().equals(name)).findAny();

    assertTrue(env.isPresent(), format("Expected environment variable '%s' not found", name));

    String actualValue = env.get().getValue();
    assertEquals(
        actualValue,
        expectedValue,
        format(
            "Environment variable '%s' expected with " + "value '%s' but found with '%s'",
            name, expectedValue, actualValue));
  }

  private static class MachineConfigBuilder {

    private List<InstallerImpl> installers = new ArrayList<>();
    private Map<String, ServerConfig> servers = new HashMap<>();

    MachineConfigBuilder setInstallers(InstallerImpl... installers) {
      this.installers = Arrays.asList(installers);
      return this;
    }

    MachineConfigBuilder setServer(String name, ServerConfig server) {
      this.servers.put(name, server);
      return this;
    }

    InternalMachineConfig build() {
      final InternalMachineConfig machineConfig = mock(InternalMachineConfig.class);
      when(machineConfig.getInstallers()).thenReturn(installers);
      when(machineConfig.getServers()).thenReturn(servers);
      return machineConfig;
    }
  }

  private static class PodBuilder {

    private String name;
    private List<String> containersNames;

    PodBuilder setName(String name) {
      this.name = name;
      return this;
    }

    PodBuilder setContainers(String... names) {
      this.containersNames = Arrays.asList(names);
      return this;
    }

    Pod build() {
      final Pod pod = mock(Pod.class);
      final ObjectMeta podMeta = mock(ObjectMeta.class);
      when(pod.getMetadata()).thenReturn(podMeta);
      when(podMeta.getName()).thenReturn(name);

      final PodSpec podSpec = mock(PodSpec.class);
      when(pod.getSpec()).thenReturn(podSpec);

      final List<Container> containers = new ArrayList<>();
      for (String containerName : containersNames) {
        final Container container = mock(Container.class);
        when(container.getName()).thenReturn(containerName);
        when(container.getEnv()).thenReturn(new ArrayList<>());

        containers.add(container);
      }

      when(podSpec.getContainers()).thenReturn(containers);

      return pod;
    }
  }
}
