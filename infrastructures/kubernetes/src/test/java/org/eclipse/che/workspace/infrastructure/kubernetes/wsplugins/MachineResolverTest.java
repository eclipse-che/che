/*
 * Copyright (c) 2012-2020 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.kubernetes.wsplugins;

import static com.google.common.collect.ImmutableMap.of;
import static java.util.Arrays.asList;
import static org.eclipse.che.api.core.model.workspace.config.MachineConfig.CPU_LIMIT_ATTRIBUTE;
import static org.eclipse.che.api.core.model.workspace.config.MachineConfig.CPU_REQUEST_ATTRIBUTE;
import static org.eclipse.che.api.core.model.workspace.config.MachineConfig.MEMORY_LIMIT_ATTRIBUTE;
import static org.eclipse.che.api.core.model.workspace.config.MachineConfig.MEMORY_REQUEST_ATTRIBUTE;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.Container;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.workspace.server.model.impl.ServerConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.VolumeImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.ComponentImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.EndpointImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.eclipse.che.api.workspace.server.wsplugins.model.CheContainer;
import org.eclipse.che.api.workspace.server.wsplugins.model.ChePluginEndpoint;
import org.eclipse.che.api.workspace.server.wsplugins.model.Volume;
import org.eclipse.che.api.workspace.shared.Constants;
import org.eclipse.che.commons.lang.Pair;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.Containers;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.KubernetesSize;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/** @author Oleksandr Garagatyi */
@Listeners(MockitoTestNGListener.class)
public class MachineResolverTest {

  private static final String DEFAULT_MEM_LIMIT = "100001";
  private static final String DEFAULT_MEM_REQUEST = "5001";
  private static final String DEFAULT_CPU_LIMIT = "2";
  private static final String DEFAULT_CPU_REQUEST = "1";
  private static final String PLUGIN_NAME = "testplugin";
  private static final String PLUGIN_PUBLISHER = "testpublisher";
  private static final String PLUGIN_PUBLISHER_NAME = PLUGIN_PUBLISHER + "/" + PLUGIN_NAME;
  private static final String PLUGIN_ID = PLUGIN_PUBLISHER_NAME + "/" + "latest";
  private static final String PROJECTS_ENV_VAR = "env_with_with_location_of_projects";
  private static final String PROJECTS_MOUNT_PATH = "/wherever/i/may/roam";

  private List<ChePluginEndpoint> endpoints;
  private CheContainer cheContainer;
  private Container container;
  private MachineResolver resolver;
  private ComponentImpl component;

  @BeforeMethod
  public void setUp() {
    endpoints = new ArrayList<>();
    cheContainer = new CheContainer();
    container = new Container();
    component = new ComponentImpl("chePlugin", PLUGIN_ID);
    resolver =
        new MachineResolver(
            new Pair<>(PROJECTS_ENV_VAR, PROJECTS_MOUNT_PATH),
            container,
            cheContainer,
            DEFAULT_MEM_LIMIT,
            DEFAULT_MEM_REQUEST,
            DEFAULT_CPU_LIMIT,
            DEFAULT_CPU_REQUEST,
            endpoints,
            component);
  }

  @Test(dataProvider = "serverProvider")
  public void shouldSetServersInMachineConfig(
      List<ChePluginEndpoint> containerEndpoints, Map<String, ServerConfig> expected)
      throws InfrastructureException {
    endpoints.addAll(containerEndpoints);

    InternalMachineConfig machineConfig = resolver.resolve();

    assertEquals(machineConfig.getServers(), expected);
  }

  @DataProvider
  public static Object[][] serverProvider() {
    return new Object[][] {
      // default minimal case
      {
        asList(endpt("endp1", 8080), endpt("endp2", 10000)),
        of("endp1", server(8080), "endp2", server(10000))
      },
      // case with publicity setting
      {
        asList(endpt("endp1", 8080, false), endpt("endp2", 10000, true)),
        of("endp1", server(8080, false), "endp2", server(10000, true))
      },
      // case with protocol attribute
      {
        asList(endptPrtc("endp1", 8080, "http"), endptPrtc("endp2", 10000, "ws")),
        of("endp1", serverPrtc(8080, "http"), "endp2", serverPrtc(10000, "ws"))
      },
      // case with path attribute
      {
        asList(endptPath("endp1", 8080, "/"), endptPath("endp2", 10000, "/some/thing")),
        of("endp1", serverPath(8080, "/"), "endp2", serverPath(10000, "/some/thing"))
      },
      // case with other attributes
      {
        asList(
            endpt("endp1", 8080, of("a1", "v1")),
            endpt("endp2", 10000, of("a2", "v1", "a3", "v3"))),
        of(
            "endp1",
            server(8080, of("a1", "v1")),
            "endp2",
            server(10000, of("a2", "v1", "a3", "v3")))
      },
    };
  }

  @Test
  public void shouldSetDefaultMemLimitAndRequestIfSidecarDoesNotHaveOne()
      throws InfrastructureException {
    InternalMachineConfig machineConfig = resolver.resolve();

    assertEquals(machineConfig.getAttributes().get(MEMORY_LIMIT_ATTRIBUTE), DEFAULT_MEM_LIMIT);
    assertEquals(machineConfig.getAttributes().get(MEMORY_REQUEST_ATTRIBUTE), DEFAULT_MEM_REQUEST);
  }

  @Test
  public void shouldSetDefaultCPULimitAndRequestIfSidecarDoesNotHaveOne()
      throws InfrastructureException {
    InternalMachineConfig machineConfig = resolver.resolve();

    assertEquals(machineConfig.getAttributes().get(CPU_LIMIT_ATTRIBUTE), DEFAULT_CPU_LIMIT);
    assertEquals(machineConfig.getAttributes().get(CPU_REQUEST_ATTRIBUTE), DEFAULT_CPU_REQUEST);
  }

  @Test(dataProvider = "memoryLimitAttributeProvider")
  public void shouldSetMemoryLimitOfASidecarIfCorrespondingComponentFieldIsSet(
      String memoryLimit, String expectedMemLimit) throws InfrastructureException {
    component.setMemoryLimit(memoryLimit);

    InternalMachineConfig machineConfig = resolver.resolve();

    assertEquals(machineConfig.getAttributes().get(MEMORY_LIMIT_ATTRIBUTE), expectedMemLimit);
  }

  @Test(dataProvider = "memoryRequestAttributeProvider")
  public void shouldSetMemoryRequestOfASidecarIfCorrespondingComponentFieldIsSet(
      String memoryRequest, String expectedMemRequest) throws InfrastructureException {
    component.setMemoryRequest(memoryRequest);

    InternalMachineConfig machineConfig = resolver.resolve();

    assertEquals(machineConfig.getAttributes().get(MEMORY_REQUEST_ATTRIBUTE), expectedMemRequest);
  }

  @DataProvider
  public static Object[][] memoryLimitAttributeProvider() {
    return new Object[][] {
      {"", DEFAULT_MEM_LIMIT},
      {null, DEFAULT_MEM_LIMIT},
      {"100Ki", toBytesString("100Ki")},
      {"1M", toBytesString("1M")},
      {"10Gi", toBytesString("10Gi")},
    };
  }

  @DataProvider
  public static Object[][] memoryRequestAttributeProvider() {
    return new Object[][] {
      {"", DEFAULT_MEM_REQUEST},
      {null, DEFAULT_MEM_REQUEST},
      {"100Ki", toBytesString("100Ki")},
      {"1M", toBytesString("1M")},
      {"10Gi", toBytesString("10Gi")},
    };
  }

  @Test(dataProvider = "cpuAttributeLimitProvider")
  public void shouldSetCPULimitOfASidecarIfCorrespondingComponentFieldIsSet(
      String cpuLimit, String expectedCpuLimit) throws InfrastructureException {
    component.setCpuLimit(cpuLimit);

    InternalMachineConfig machineConfig = resolver.resolve();

    assertEquals(machineConfig.getAttributes().get(CPU_LIMIT_ATTRIBUTE), expectedCpuLimit);
  }

  @Test(dataProvider = "cpuAttributeRequestProvider")
  public void shouldSetCPURequestOfASidecarIfCorrespondingComponentFieldIsSet(
      String cpuRequest, String expectedCpuRequest) throws InfrastructureException {
    component.setCpuRequest(cpuRequest);

    InternalMachineConfig machineConfig = resolver.resolve();

    assertEquals(machineConfig.getAttributes().get(CPU_REQUEST_ATTRIBUTE), expectedCpuRequest);
  }

  @DataProvider
  public static Object[][] cpuAttributeLimitProvider() {
    return new Object[][] {
      {"", DEFAULT_CPU_LIMIT},
      {null, DEFAULT_CPU_LIMIT},
      {"100m", "0.1"},
      {"1", "1.0"},
      {"1578m", "1.578"},
    };
  }

  @DataProvider
  public static Object[][] cpuAttributeRequestProvider() {
    return new Object[][] {
      {"", DEFAULT_CPU_REQUEST},
      {null, DEFAULT_CPU_REQUEST},
      {"100m", "0.1"},
      {"1", "1.0"},
      {"1578m", "1.578"},
    };
  }

  @Test
  public void shouldOverrideMemoryLimitOfASidecarIfCorrespondingWSConfigAttributeIsSet()
      throws InfrastructureException {
    String memoryLimit = "300Mi";
    String expectedMemLimit = toBytesString(memoryLimit);
    Containers.addRamLimit(container, 123456789);
    component.setMemoryLimit(memoryLimit);

    InternalMachineConfig machineConfig = resolver.resolve();

    assertEquals(machineConfig.getAttributes().get(MEMORY_LIMIT_ATTRIBUTE), expectedMemLimit);
  }

  @Test
  public void shouldNotSetMemLimitAttributeIfLimitIsInContainer() throws InfrastructureException {
    Containers.addRamLimit(container, 123456789);

    InternalMachineConfig machineConfig = resolver.resolve();

    assertNull(machineConfig.getAttributes().get(MEMORY_LIMIT_ATTRIBUTE));
  }

  @Test(expectedExceptions = InfrastructureException.class)
  public void shouldRefuseToMountProjectsManually() throws InfrastructureException {
    cheContainer.setMountSources(false);
    Volume volume = new Volume();
    volume.setName(Constants.PROJECTS_VOLUME_NAME);
    volume.setMountPath("anything, like");
    cheContainer.getVolumes().add(volume);

    resolver.resolve();
  }

  @Test
  public void shouldAddProjectMountPointWhenMountSources() throws InfrastructureException {
    cheContainer.setMountSources(true);

    InternalMachineConfig config = resolver.resolve();

    assertEquals(1, config.getVolumes().size());
    assertEquals(
        PROJECTS_MOUNT_PATH, config.getVolumes().get(Constants.PROJECTS_VOLUME_NAME).getPath());
  }

  @Test
  public void shouldAddVolumesFromDevfileComponent() throws InfrastructureException {

    component.setVolumes(
        asList(
            new org.eclipse.che.api.workspace.server.model.impl.devfile.VolumeImpl("foo", "/bar"),
            new org.eclipse.che.api.workspace.server.model.impl.devfile.VolumeImpl(
                "test", "/foo/test")));

    InternalMachineConfig config = resolver.resolve();

    assertEquals(2, config.getVolumes().size());
    assertEquals("/bar", config.getVolumes().get("foo").getPath());
    assertEquals("/foo/test", config.getVolumes().get("test").getPath());
  }

  @Test(expectedExceptions = InfrastructureException.class)
  public void shouldFailWhenEndpointNameAlreadyExist() throws InfrastructureException {
    // given
    ChePluginEndpoint pluginEndpoint = new ChePluginEndpoint();
    pluginEndpoint.setName("test-endpoint");
    endpoints.add(pluginEndpoint);
    component.setEndpoints(
        Collections.singletonList(new EndpointImpl("test-endpoint", 8080, Collections.emptyMap())));

    // when -> then exception
    resolver.resolve();
  }

  @Test
  public void shouldAddEndpointsFromDevfileComponent() throws InfrastructureException {
    component.setEndpoints(
        asList(
            new EndpointImpl("endpoint8080", 8080, Collections.emptyMap()),
            new EndpointImpl("endpoint9999", 9999, ImmutableMap.of("secure", "true"))));

    InternalMachineConfig config = resolver.resolve();

    assertEquals(config.getServers().size(), 2);
    assertEquals(config.getServers().get("endpoint8080").getPort(), "8080");
    assertEquals(config.getServers().get("endpoint9999").getPort(), "9999");
    assertEquals(config.getServers().get("endpoint9999").getAttributes().get("secure"), "true");
  }

  private static String toBytesString(String k8sMemorySize) {
    return Long.toString(KubernetesSize.toBytes(k8sMemorySize));
  }

  private static ChePluginEndpoint endptPath(String name, int port, String path) {
    return new ChePluginEndpoint().name(name).targetPort(port).attributes(of("path", path));
  }

  private static ChePluginEndpoint endptPrtc(String name, int port, String protocol) {
    return new ChePluginEndpoint().name(name).targetPort(port).attributes(of("protocol", protocol));
  }

  private static ChePluginEndpoint endpt(String name, int port, boolean isPublic) {
    return new ChePluginEndpoint().name(name).targetPort(port).setPublic(isPublic);
  }

  private static ChePluginEndpoint endpt(String name, int port, Map<String, String> attributes) {
    return new ChePluginEndpoint().name(name).targetPort(port).attributes(attributes);
  }

  private static ChePluginEndpoint endpt(String name, int port) {
    return new ChePluginEndpoint().name(name).targetPort(port);
  }

  private static ServerConfigImpl server(int port) {
    return server(port, false);
  }

  private static ServerConfig server(int port, Map<String, String> attributes) {
    ServerConfigImpl server = server(port);
    server.getAttributes().putAll(attributes);
    return server;
  }

  private static ServerConfigImpl serverPath(int port, String path) {
    return server(port).withPath(path);
  }

  private static ServerConfigImpl serverPrtc(int port, String protocol) {
    return server(port).withProtocol(protocol);
  }

  private static ServerConfigImpl server(int port, boolean external) {
    return new ServerConfigImpl()
        .withPort(port + "/tcp")
        .withAttributes(of("internal", Boolean.toString(!external)));
  }

  private org.eclipse.che.api.core.model.workspace.config.Volume volume(String mountPath) {
    return new VolumeImpl().withPath(mountPath);
  }
}
