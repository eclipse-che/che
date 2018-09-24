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
package org.eclipse.che.workspace.infrastructure.kubernetes.wsplugins;

import static com.google.common.collect.ImmutableMap.of;
import static java.util.Arrays.asList;
import static org.eclipse.che.api.core.model.workspace.config.MachineConfig.MEMORY_LIMIT_ATTRIBUTE;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import io.fabric8.kubernetes.api.model.Container;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.workspace.server.model.impl.ServerConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.VolumeImpl;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.eclipse.che.api.workspace.server.wsplugins.model.CheContainer;
import org.eclipse.che.api.workspace.server.wsplugins.model.ChePluginEndpoint;
import org.eclipse.che.api.workspace.server.wsplugins.model.Volume;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.Containers;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/** @author Oleksandr Garagatyi */
public class MachineResolverTest {

  private static final String DEFAULT_MEM_LIMIT = "100001";

  private List<ChePluginEndpoint> endpoints;
  private CheContainer cheContainer;
  private Container container;
  private MachineResolver resolver;

  @BeforeMethod
  public void setUp() {
    endpoints = new ArrayList<>();
    cheContainer = new CheContainer();
    container = new Container();
    resolver = new MachineResolver(container, cheContainer, DEFAULT_MEM_LIMIT, endpoints);
  }

  @Test
  public void shouldSetVolumesInMachineConfig() {
    List<Volume> sidecarVolumes =
        asList(
            new Volume().name("vol1").mountPath("/path1"),
            new Volume().name("vol2").mountPath("/path2"));
    cheContainer.setVolumes(sidecarVolumes);
    Map<String, Object> expected = of("vol1", volume("/path1"), "vol2", volume("/path2"));

    InternalMachineConfig machineConfig = resolver.resolve();

    assertEquals(machineConfig.getVolumes(), expected);
  }

  @Test(dataProvider = "serverProvider")
  public void shouldSetServersInMachineConfig(
      List<ChePluginEndpoint> containerEndpoints, Map<String, ServerConfig> expected) {
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
  public void shouldSetDefaultMemLimitIfSidecarDoesNotHaveOne() {
    InternalMachineConfig machineConfig = resolver.resolve();

    assertEquals(machineConfig.getAttributes().get(MEMORY_LIMIT_ATTRIBUTE), DEFAULT_MEM_LIMIT);
  }

  @Test
  public void shouldNotSetMemLimitAttributeIfLimitIsInContainer() {
    Containers.addRamLimit(container, 123456789);

    InternalMachineConfig machineConfig = resolver.resolve();

    assertNull(machineConfig.getAttributes().get(MEMORY_LIMIT_ATTRIBUTE));
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
