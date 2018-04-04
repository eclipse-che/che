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

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.installer.server.model.impl.InstallerImpl;
import org.eclipse.che.api.workspace.server.model.impl.ServerConfigImpl;
import org.eclipse.che.api.workspace.server.spi.InternalInfrastructureException;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.InstallerServersPortProvisioner.ServersPorts;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Test {@link InstallerServersPortProvisioner}.
 *
 * @author Sergii Leshchenko
 */
@Listeners(MockitoTestNGListener.class)
public class InstallerServersPortProvisionerTest {

  @Mock private KubernetesEnvironment k8sEnv;

  private InstallerServersPortProvisioner portProvisioner;

  @BeforeMethod
  public void setUp() throws Exception {
    portProvisioner = spy(new InstallerServersPortProvisioner(10_000, 20_000));
  }

  @Test
  public void shouldGroupMachinesByPodsAndLaunchPortsConflictResolvingOnProvisioning()
      throws Exception {
    // given
    doNothing().when(portProvisioner).fixInstallersPortsConflicts(anyList());
    when(k8sEnv.getPods())
        .thenReturn(
            ImmutableMap.of(
                "pod1",
                createPod("pod1", "container1", "container2"),
                "pod2",
                createPod("pod2", "container3")));

    InternalMachineConfig machine1 = mock(InternalMachineConfig.class);
    InternalMachineConfig machine2 = mock(InternalMachineConfig.class);
    InternalMachineConfig machine3 = mock(InternalMachineConfig.class);

    when(k8sEnv.getMachines())
        .thenReturn(
            ImmutableMap.of(
                "pod1/container1",
                machine1,
                "pod1/container2",
                machine2,
                "pod2/container3",
                machine3));

    // when
    portProvisioner.provision(k8sEnv, mock(RuntimeIdentity.class));

    // then
    verify(portProvisioner).fixInstallersPortsConflicts(asList(machine1, machine2));
    verify(portProvisioner).fixInstallersPortsConflicts(singletonList(machine3));
  }

  @Test
  public void shouldFixInstallerPortsConflicts() throws Exception {
    Map<String, ServerConfigImpl> servers1 = new HashMap<>();
    servers1.put("server1", new ServerConfigImpl("8080/tcp", "http", "/api", emptyMap()));

    InstallerImpl installer1 =
        new InstallerImpl(
            "installer1", "name", "v1", "description", emptyList(), emptyMap(), "script", servers1);

    InternalMachineConfig machine1 =
        new InternalMachineConfig(
            singletonList(installer1), servers1, new HashMap<>(), emptyMap(), emptyMap());

    Map<String, ServerConfigImpl> servers2 = new HashMap<>();
    servers2.put("server2-http", new ServerConfigImpl("8080/tcp", "http", "/api", emptyMap()));
    servers2.put("server2-ws", new ServerConfigImpl("8080/tcp", "ws", "/api", emptyMap()));

    InstallerImpl installer2 =
        new InstallerImpl(
            "installer2", "name", "v1", "description", emptyList(), emptyMap(), "script", servers2);

    InternalMachineConfig machine2 =
        new InternalMachineConfig(
            singletonList(installer2), servers2, new HashMap<>(), emptyMap(), emptyMap());

    portProvisioner.fixInstallersPortsConflicts(asList(machine1, machine2));

    assertTrue(machine1.getEnv().isEmpty());
    assertEquals(machine1.getServers().get("server1").getPort(), "8080/tcp");
    assertEquals(machine1.getInstallers().get(0).getServers().get("server1").getPort(), "8080/tcp");

    assertEquals(machine2.getEnv().get("CHE_SERVER_SERVER2_HTTP_PORT"), "10000");

    String newHttpServerPort = machine2.getServers().get("server2-http").getPort();
    String newWsServerPort = machine2.getServers().get("server2-ws").getPort();
    assertEquals(newHttpServerPort, "10000/tcp");
    assertEquals(newWsServerPort, "10000/tcp");

    String newInstallerHttpServerPort =
        machine2.getInstallers().get(0).getServers().get("server2-http").getPort();

    String newInstallerWsServerPort =
        machine2.getInstallers().get(0).getServers().get("server2-ws").getPort();
    assertEquals(newInstallerHttpServerPort, "10000/tcp");
    assertEquals(newInstallerWsServerPort, "10000/tcp");

    assertEquals(newHttpServerPort, newInstallerHttpServerPort);
    assertEquals(newWsServerPort, newInstallerWsServerPort);
  }

  @Test
  public void shouldReturnGroupedServersByPort() {
    // given
    Map<String, ServerConfigImpl> installerServers = new HashMap<>();
    installerServers.put(
        "server1-http", new ServerConfigImpl("8080/tcp", "http", "/api", emptyMap()));
    installerServers.put("server1-ws", new ServerConfigImpl("8080/tcp", "ws", "/api", emptyMap()));
    installerServers.put("server1-udp", new ServerConfigImpl("8080/udp", "udp", "", emptyMap()));
    installerServers.put("server2", new ServerConfigImpl("8081/tcp", "http", "/api", emptyMap()));

    InstallerImpl installer =
        new InstallerImpl(
            "installer1",
            "name",
            "v1",
            "description",
            emptyList(),
            emptyMap(),
            "script",
            installerServers);

    // when
    Map<Integer, Collection<String>> portToServersRefs =
        portProvisioner.getServersRefsGroupedByPorts(installer);

    // then
    Collection<String> servers8080 = portToServersRefs.get(8080);
    assertEquals(servers8080.size(), 3);
    assertTrue(
        servers8080.containsAll(ImmutableSet.of("server1-http", "server1-ws", "server1-udp")));

    Collection<String> servers8081 = portToServersRefs.get(8081);
    assertEquals(servers8081.size(), 1);
    assertTrue(servers8081.contains("server2"));
  }

  @Test
  public void shouldAssignNewPortToInstallerServer() throws Exception {
    // given
    Set<String> serversRefs = ImmutableSet.of("server1-http", "server1-ws");

    Map<String, ServerConfigImpl> installerServers = new HashMap<>();
    installerServers.put(
        "server1-http", new ServerConfigImpl("8080/tcp", "http", "/api", emptyMap()));
    installerServers.put("server1-ws", new ServerConfigImpl("8080/tcp", "ws", "/api", emptyMap()));

    Map<String, ServerConfigImpl> machineServers = new HashMap<>();
    machineServers.put("server2", new ServerConfigImpl("8080/tcp", "http", "/api", emptyMap()));
    machineServers.putAll(installerServers);

    InstallerImpl installer =
        new InstallerImpl(
            "installer1",
            "name",
            "v1",
            "description",
            emptyList(),
            emptyMap(),
            "script",
            installerServers);

    InternalMachineConfig machineConfig =
        new InternalMachineConfig(
            singletonList(installer), machineServers, new HashMap<>(), emptyMap(), emptyMap());

    // when
    portProvisioner.assignNewPort(
        serversRefs, 10007, machineConfig, machineConfig.getInstallers().get(0));

    // then
    assertEquals(machineConfig.getEnv().size(), 2);

    assertEquals(machineConfig.getEnv().get("CHE_SERVER_SERVER1_WS_PORT"), "10007");
    assertEquals(machineConfig.getServers().get("server1-ws").getPort(), "10007/tcp");
    assertEquals(
        machineConfig.getInstallers().get(0).getServers().get("server1-ws").getPort(), "10007/tcp");

    assertEquals(machineConfig.getEnv().get("CHE_SERVER_SERVER1_HTTP_PORT"), "10007");
    assertEquals(machineConfig.getServers().get("server1-http").getPort(), "10007/tcp");
    assertEquals(
        machineConfig.getInstallers().get(0).getServers().get("server1-http").getPort(),
        "10007/tcp");
  }

  @Test(dataProvider = "serverToEnvVar")
  public void shouldReturnEnvironmentVariableNameByServerName(String serverName, String envVar) {
    // when
    String envName = portProvisioner.getEnvName(serverName);

    // then
    assertEquals(envName, envVar);
  }

  @DataProvider
  public Object[][] serverToEnvVar() {
    return new Object[][] {
      {"terminal|pty", "CHE_SERVER_TERMINAL_PTY_PORT"},
      {"wsagent/http", "CHE_SERVER_WSAGENT_HTTP_PORT"},
      {"tomcat-8080", "CHE_SERVER_TOMCAT_8080_PORT"},
      {"tomcat@8080", "CHE_SERVER_TOMCAT_8080_PORT"}
    };
  }

  @Test(dataProvider = "occupiedPorts")
  public void shouldFindFirstAvailableFreePort(ServersPorts ports, Integer result)
      throws Exception {
    // given
    int originalOccupiedPortsNumber = ports.getOccupiedPorts().size();

    // when
    Integer availablePort = ports.findFreePort();

    // then
    assertEquals(availablePort, result);
    assertTrue(ports.getOccupiedPorts().contains(result));
    assertEquals(ports.getOccupiedPorts().size(), originalOccupiedPortsNumber + 1);
  }

  @DataProvider
  public Object[][] occupiedPorts() {
    return new Object[][] {
      {portProvisioner.new ServersPorts(10_000, Sets.newHashSet(10_000, 10_001, 10_002)), 10_003},
      {portProvisioner.new ServersPorts(10_000, Sets.newHashSet(10_000, 10_002)), 10_001},
    };
  }

  @Test(
    expectedExceptions = InternalInfrastructureException.class,
    expectedExceptionsMessageRegExp =
        "There is no available port in configured ports range \\[10000, 20000\\]."
  )
  public void shouldThrowExceptionIfThereIsNotFreePort() throws Exception {
    // given
    ServersPorts ports = portProvisioner.new ServersPorts(20_001, emptySet());

    // when
    ports.findFreePort();
  }

  private Pod createPod(String podName, String... containersNames) {
    List<Container> containers =
        Arrays.stream(containersNames)
            .map(name -> new ContainerBuilder().withName(name).build())
            .collect(Collectors.toList());
    return new PodBuilder()
        .withNewMetadata()
        .withName(podName)
        .endMetadata()
        .withNewSpec()
        .withContainers(containers)
        .endSpec()
        .build();
  }
}
