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
package org.eclipse.che.workspace.infrastructure.kubernetes.wsnext;

import static com.google.common.collect.ImmutableMap.of;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.eclipse.che.api.core.model.workspace.config.MachineConfig.MEMORY_LIMIT_ATTRIBUTE;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodSpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.workspace.server.model.impl.ServerConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.VolumeImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.environment.InternalEnvironment;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.eclipse.che.api.workspace.server.wsnext.model.CheContainer;
import org.eclipse.che.api.workspace.server.wsnext.model.CheContainerPort;
import org.eclipse.che.api.workspace.server.wsnext.model.ChePlugin;
import org.eclipse.che.api.workspace.server.wsnext.model.ChePluginEndpoint;
import org.eclipse.che.api.workspace.server.wsnext.model.EnvVar;
import org.eclipse.che.api.workspace.server.wsnext.model.Volume;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/** @author Alexander Garagatyi */
@Listeners(MockitoTestNGListener.class)
public class KubernetesWorkspaceNextApplierTest {
  private static final String TEST_IMAGE = "testImage/test:test";
  private static final String ENV_VAR = "PLUGINS_ENV_VAR";
  private static final String ENV_VAR_VALUE = "PLUGINS_ENV_VAR_VALUE";
  private static final String POD_NAME = "pod12";
  private static final String VOLUME_NAME = "test_volume_name";
  private static final String VOLUME_MOUNT_PATH = "/path/test";
  private static final int MEMORY_LIMIT_MB = 200;

  @Mock Pod pod;
  @Mock PodSpec podSpec;
  @Mock ObjectMeta meta;
  @Mock KubernetesEnvironment internalEnvironment;

  KubernetesWorkspaceNextApplier applier;
  List<Container> containers;
  Map<String, InternalMachineConfig> machines;

  @BeforeMethod
  public void setUp() {
    applier = new KubernetesWorkspaceNextApplier(MEMORY_LIMIT_MB);
    machines = new HashMap<>();
    containers = new ArrayList<>();

    when(internalEnvironment.getPods()).thenReturn(of(POD_NAME, pod));
    when(pod.getSpec()).thenReturn(podSpec);
    when(podSpec.getContainers()).thenReturn(containers);
    when(pod.getMetadata()).thenReturn(meta);
    when(meta.getName()).thenReturn(POD_NAME);
    when(internalEnvironment.getMachines()).thenReturn(machines);
  }

  @Test
  public void doesNothingIfChePluginsListIsEmpty() throws Exception {
    applier.apply(internalEnvironment, emptyList());

    verifyZeroInteractions(internalEnvironment);
  }

  @Test(
    expectedExceptions = InfrastructureException.class,
    expectedExceptionsMessageRegExp =
        "Workspace.Next configuration can be applied to a workspace with one pod only"
  )
  public void throwsExceptionWhenTheNumberOfPodsIsNot1() throws Exception {
    when(internalEnvironment.getPods()).thenReturn(of("pod1", pod, "pod2", pod));

    applier.apply(internalEnvironment, singletonList(createChePlugin()));
  }

  @Test
  public void addToolingContainerToAPod() throws Exception {
    applier.apply(internalEnvironment, singletonList(createChePlugin()));

    assertEquals(containers.size(), 1);
    Container toolingContainer = containers.get(0);
    verifyContainer(toolingContainer);
  }

  @Test
  public void canAddMultipleToolingContainersToAPodFromOnePlugin() throws Exception {
    applier.apply(internalEnvironment, singletonList(createChePluginWith2Containers()));

    assertEquals(containers.size(), 2);
    for (Container container : containers) {
      verifyContainer(container);
    }
  }

  @Test
  public void canAddMultipleToolingContainersToAPodFromSeveralPlugins() throws Exception {
    applier.apply(internalEnvironment, ImmutableList.of(createChePlugin(), createChePlugin()));

    assertEquals(containers.size(), 2);
    for (Container container : containers) {
      verifyContainer(container);
    }
  }

  @Test
  public void addsMachineWithVolumeForEachContainer() throws Exception {
    applier.apply(internalEnvironment, singletonList(createChePlugin()));

    InternalMachineConfig machineConfig = getOneAndOnlyMachine(internalEnvironment);
    Map<String, org.eclipse.che.api.core.model.workspace.config.Volume> volumes =
        machineConfig.getVolumes();
    assertEquals(volumes.size(), 1);
    assertEquals(
        ImmutableMap.of(VOLUME_NAME, new VolumeImpl().withPath(VOLUME_MOUNT_PATH)), volumes);
  }

  @Test
  public void addsMachineWithServersForContainer() throws Exception {
    ChePlugin chePlugin = createChePlugin();
    addPortToSingleContainerPlugin(chePlugin, 80, "test-port", emptyMap(), true);
    applier.apply(internalEnvironment, singletonList(chePlugin));

    InternalMachineConfig machineConfig = getOneAndOnlyMachine(internalEnvironment);
    assertEquals(
        machineConfig.getServers(), expectedSingleServer(80, "test-port", emptyMap(), true));
  }

  @Test
  public void addsTwoServersForContainers() throws Exception {
    ChePlugin chePlugin = createChePlugin();
    addPortToSingleContainerPlugin(chePlugin, 80, "test-port", emptyMap(), true);
    addPortToSingleContainerPlugin(chePlugin, 8090, "another-test-port", emptyMap(), false);
    applier.apply(internalEnvironment, singletonList(chePlugin));

    InternalMachineConfig machineConfig = getOneAndOnlyMachine(internalEnvironment);
    assertEquals(
        machineConfig.getServers(),
        expectedTwoServers(
            80, "test-port", emptyMap(), true, 8090, "another-test-port", emptyMap(), false));
  }

  @Test
  public void addsMachineWithServersThatUseSamePortButDifferentNames() throws Exception {
    ChePlugin chePlugin = createChePlugin();
    addPortToSingleContainerPlugin(chePlugin, 80, "test-port/http", emptyMap(), true);
    addPortToSingleContainerPlugin(chePlugin, 80, "test-port/ws", emptyMap(), true);
    applier.apply(internalEnvironment, singletonList(chePlugin));

    InternalMachineConfig machineConfig = getOneAndOnlyMachine(internalEnvironment);
    assertEquals(
        machineConfig.getServers(),
        expectedTwoServers(
            80, "test-port/http", emptyMap(), true, 80, "test-port/ws", emptyMap(), true));
  }

  @Test
  public void addsMachineWithServersThatSetProtocolAndPath() throws Exception {
    ChePlugin chePlugin = createChePlugin();
    addPortToSingleContainerPlugin(
        chePlugin,
        443,
        "test-port",
        ImmutableMap.of("path", "/path/1", "protocol", "https", "attr1", "value1"),
        true);
    applier.apply(internalEnvironment, singletonList(chePlugin));

    InternalMachineConfig machineConfig = getOneAndOnlyMachine(internalEnvironment);
    assertEquals(
        machineConfig.getServers(),
        expectedSingleServer(
            443, "test-port", singletonMap("attr1", "value1"), true, "https", "/path/1"));
  }

  @Test
  public void setsDefaultMemoryLimitForMachineAssociatedWithContainer() throws Exception {
    applier.apply(internalEnvironment, singletonList(createChePlugin()));

    InternalMachineConfig machineConfig = getOneAndOnlyMachine(internalEnvironment);
    String memoryLimitAttribute = machineConfig.getAttributes().get(MEMORY_LIMIT_ATTRIBUTE);
    assertEquals(memoryLimitAttribute, Integer.toString(MEMORY_LIMIT_MB * 1024 * 1024));
  }

  private ChePlugin createChePlugin() {
    ChePlugin plugin = new ChePlugin();
    plugin.setName("some-name");
    plugin.setId("some-id");
    plugin.setVersion("0.0.3");
    plugin.setContainers(singletonList(createContainer()));
    return plugin;
  }

  private ChePlugin createChePluginWith2Containers() {
    ChePlugin plugin = new ChePlugin();
    plugin.setName("some-name");
    plugin.setId("some-id");
    plugin.setVersion("0.0.3");
    plugin.setContainers(asList(createContainer(), createContainer()));
    return plugin;
  }

  private CheContainer createContainer() {
    CheContainer cheContainer = new CheContainer();
    cheContainer.setImage(TEST_IMAGE);
    cheContainer.setEnv(singletonList(new EnvVar().name(ENV_VAR).value(ENV_VAR_VALUE)));
    cheContainer.setVolumes(
        singletonList(new Volume().name(VOLUME_NAME).mountPath(VOLUME_MOUNT_PATH)));
    return cheContainer;
  }

  private void verifyContainer(Container toolingContainer) {
    assertEquals(toolingContainer.getImage(), TEST_IMAGE);
    assertEquals(
        toolingContainer.getEnv(),
        singletonList(new io.fabric8.kubernetes.api.model.EnvVar(ENV_VAR, ENV_VAR_VALUE, null)));
  }

  private InternalMachineConfig getOneAndOnlyMachine(InternalEnvironment internalEnvironment) {
    Map<String, InternalMachineConfig> machines = internalEnvironment.getMachines();
    assertEquals(machines.size(), 1);
    return machines.values().iterator().next();
  }

  private void addPortToSingleContainerPlugin(
      ChePlugin plugin,
      int port,
      String portName,
      Map<String, String> attributes,
      boolean isPublic) {

    assertEquals(plugin.getContainers().size(), 1);

    ChePluginEndpoint endpoint =
        new ChePluginEndpoint()
            .attributes(attributes)
            .name(portName)
            .setPublic(isPublic)
            .targetPort(port);
    plugin.getEndpoints().add(endpoint);
    List<CheContainerPort> ports = plugin.getContainers().get(0).getPorts();
    if (ports
        .stream()
        .map(CheContainerPort::getExposedPort)
        .noneMatch(integer -> integer == port)) {
      ports.add(new CheContainerPort().exposedPort(port));
    }
  }

  @SuppressWarnings("SameParameterValue")
  private Map<String, ServerConfig> expectedSingleServer(
      int port, String portName, Map<String, String> attributes, boolean isExternal) {
    Map<String, ServerConfig> servers = new HashMap<>();
    addExpectedServer(servers, port, portName, attributes, isExternal, null, null);
    return servers;
  }

  @SuppressWarnings("SameParameterValue")
  private Map<String, ServerConfig> expectedSingleServer(
      int port,
      String portName,
      Map<String, String> attributes,
      boolean isExternal,
      String protocol,
      String path) {
    Map<String, ServerConfig> servers = new HashMap<>();
    addExpectedServer(servers, port, portName, attributes, isExternal, protocol, path);
    return servers;
  }

  @SuppressWarnings("SameParameterValue")
  private Map<String, ServerConfig> expectedTwoServers(
      int port,
      String portName,
      Map<String, String> attributes,
      boolean isExternal,
      int port2,
      String portName2,
      Map<String, String> attributes2,
      boolean isExternal2) {
    Map<String, ServerConfig> servers = new HashMap<>();
    addExpectedServer(servers, port, portName, attributes, isExternal, null, null);
    addExpectedServer(servers, port2, portName2, attributes2, isExternal2, null, null);
    return servers;
  }

  private void addExpectedServer(
      Map<String, ServerConfig> servers,
      int port,
      String portName,
      Map<String, String> attributes,
      boolean isExternal,
      String protocol,
      String path) {
    Map<String, String> serverAttributes = new HashMap<>(attributes);
    serverAttributes.put("internal", Boolean.toString(!isExternal));
    servers.put(
        portName,
        new ServerConfigImpl(Integer.toString(port) + "/tcp", protocol, path, serverAttributes));
  }
}
