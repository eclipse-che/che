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
package org.eclipse.che.workspace.infrastructure.openshift;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.eclipse.che.api.core.model.workspace.runtime.ServerStatus.UNKNOWN;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.api.model.ServicePortBuilder;
import io.fabric8.openshift.api.model.Route;
import io.fabric8.openshift.api.model.RouteBuilder;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.api.workspace.server.model.impl.ServerConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.ServerImpl;
import org.eclipse.che.api.workspace.shared.Constants;
import org.eclipse.che.workspace.infrastructure.kubernetes.Annotations;
import org.eclipse.che.workspace.infrastructure.kubernetes.Annotations.Serializer;
import org.eclipse.che.workspace.infrastructure.openshift.server.OpenShiftServerResolver;
import org.testng.annotations.Test;

/**
 * Test for {@link OpenShiftServerResolver}.
 *
 * @author Sergii Leshchenko
 */
public class OpenShiftServerResolverTest {

  private static final Map<String, String> ATTRIBUTES_MAP = singletonMap("key", "value");
  private static final int CONTAINER_PORT = 3054;
  private static final String ROUTE_HOST = "localhost";

  @Test
  public void
      testResolvingServersWhenThereIsNoTheCorrespondingServiceAndRouteForTheSpecifiedMachine() {
    // given
    Service nonMatchedByPodService =
        createService("nonMatched", "foreignMachine", CONTAINER_PORT, null);
    Route route =
        createRoute(
            "nonMatched",
            "foreignMachine",
            ImmutableMap.of(
                "http-server", new ServerConfigImpl("3054", "http", "/api", ATTRIBUTES_MAP)));

    OpenShiftServerResolver serverResolver =
        new OpenShiftServerResolver(singletonList(nonMatchedByPodService), singletonList(route));

    // when
    Map<String, ServerImpl> resolved = serverResolver.resolve("machine");

    // then
    assertTrue(resolved.isEmpty());
  }

  @Test
  public void testResolvingServersWhenThereIsMatchedRouteForTheSpecifiedMachine() {
    Route route =
        createRoute(
            "matched",
            "machine",
            ImmutableMap.of(
                "http-server", new ServerConfigImpl("3054", "http", "/api", ATTRIBUTES_MAP)));

    OpenShiftServerResolver serverResolver =
        new OpenShiftServerResolver(emptyList(), singletonList(route));

    Map<String, ServerImpl> resolved = serverResolver.resolve("machine");

    assertEquals(resolved.size(), 1);
    assertEquals(
        resolved.get("http-server"),
        new ServerImpl()
            .withUrl("http://localhost/api")
            .withStatus(UNKNOWN)
            .withAttributes(defaultAttributeAnd(Constants.SERVER_PORT_ATTRIBUTE, "3054")));
  }

  @Test
  public void testResolvingServersWhenThereIsMatchedRouteForMachineAndServerPathIsNull() {
    Route route =
        createRoute(
            "matched",
            "machine",
            singletonMap(
                "http-server", new ServerConfigImpl("3054", "http", null, ATTRIBUTES_MAP)));

    OpenShiftServerResolver serverResolver =
        new OpenShiftServerResolver(emptyList(), singletonList(route));

    Map<String, ServerImpl> resolved = serverResolver.resolve("machine");

    assertEquals(resolved.size(), 1);
    assertEquals(
        resolved.get("http-server"),
        new ServerImpl()
            .withUrl("http://localhost")
            .withStatus(UNKNOWN)
            .withAttributes(defaultAttributeAnd(Constants.SERVER_PORT_ATTRIBUTE, "3054")));
  }

  @Test
  public void testResolvingServersWhenThereIsMatchedRouteForMachineAndServerPathIsEmpty() {
    Route route =
        createRoute(
            "matched",
            "machine",
            singletonMap("http-server", new ServerConfigImpl("3054", "http", "", ATTRIBUTES_MAP)));

    OpenShiftServerResolver serverResolver =
        new OpenShiftServerResolver(emptyList(), singletonList(route));

    Map<String, ServerImpl> resolved = serverResolver.resolve("machine");

    assertEquals(resolved.size(), 1);
    assertEquals(
        resolved.get("http-server"),
        new ServerImpl()
            .withUrl("http://localhost")
            .withStatus(UNKNOWN)
            .withAttributes(defaultAttributeAnd(Constants.SERVER_PORT_ATTRIBUTE, "3054")));
  }

  @Test
  public void testResolvingServersWhenThereIsMatchedRouteForMachineAndServerPathIsRelative() {
    Route route =
        createRoute(
            "matched",
            "machine",
            singletonMap(
                "http-server", new ServerConfigImpl("3054", "http", "api", ATTRIBUTES_MAP)));

    OpenShiftServerResolver serverResolver =
        new OpenShiftServerResolver(emptyList(), singletonList(route));

    Map<String, ServerImpl> resolved = serverResolver.resolve("machine");

    assertEquals(resolved.size(), 1);
    assertEquals(
        resolved.get("http-server"),
        new ServerImpl()
            .withUrl("http://localhost/api")
            .withStatus(UNKNOWN)
            .withAttributes(defaultAttributeAnd(Constants.SERVER_PORT_ATTRIBUTE, "3054")));
  }

  @Test
  public void testResolvingInternalServers() {
    Service service =
        createService(
            "service11",
            "machine",
            CONTAINER_PORT,
            singletonMap(
                "http-server", new ServerConfigImpl("3054", "http", "api", ATTRIBUTES_MAP)));
    Route route = createRoute("matched", "machine", null);

    OpenShiftServerResolver serverResolver =
        new OpenShiftServerResolver(singletonList(service), singletonList(route));

    Map<String, ServerImpl> resolved = serverResolver.resolve("machine");

    assertEquals(resolved.size(), 1);
    assertEquals(
        resolved.get("http-server"),
        new ServerImpl()
            .withUrl("http://service11:3054/api")
            .withStatus(UNKNOWN)
            .withAttributes(defaultAttributeAnd(Constants.SERVER_PORT_ATTRIBUTE, "3054")));
  }

  @Test
  public void testResolvingInternalServersWithPortWithTransportProtocol() {
    Service service =
        createService(
            "service11",
            "machine",
            CONTAINER_PORT,
            singletonMap(
                "http-server", new ServerConfigImpl("3054/udp", "xxx", "api", ATTRIBUTES_MAP)));
    Route route = createRoute("matched", "machine", null);

    OpenShiftServerResolver serverResolver =
        new OpenShiftServerResolver(singletonList(service), singletonList(route));

    Map<String, ServerImpl> resolved = serverResolver.resolve("machine");

    assertEquals(resolved.size(), 1);
    assertEquals(
        resolved.get("http-server"),
        new ServerImpl()
            .withUrl("xxx://service11:3054/api")
            .withStatus(UNKNOWN)
            .withAttributes(defaultAttributeAnd(Constants.SERVER_PORT_ATTRIBUTE, "3054")));
  }

  private Service createService(
      String name, String machineName, Integer port, Map<String, ServerConfigImpl> servers) {
    Serializer serializer = Annotations.newSerializer();
    serializer.machineName(machineName);
    if (servers != null) {
      serializer.servers(servers);
    }

    return new ServiceBuilder()
        .withNewMetadata()
        .withName(name)
        .withAnnotations(serializer.annotations())
        .endMetadata()
        .withNewSpec()
        .withPorts(
            new ServicePortBuilder()
                .withPort(port)
                .withNewTargetPort()
                .withIntVal(port)
                .endTargetPort()
                .build())
        .endSpec()
        .build();
  }

  private Route createRoute(
      String name, String machineName, Map<String, ServerConfigImpl> servers) {
    Serializer serializer = Annotations.newSerializer();
    serializer.machineName(machineName);
    if (servers != null) {
      serializer.servers(servers);
    }
    return new RouteBuilder()
        .withNewMetadata()
        .withName(name)
        .withAnnotations(serializer.annotations())
        .endMetadata()
        .withNewSpec()
        .withHost(ROUTE_HOST)
        .withNewTo()
        .withName(name)
        .endTo()
        .endSpec()
        .build();
  }

  private Map<String, String> defaultAttributeAnd(String key, String value) {
    HashMap<String, String> map = new HashMap<>(ATTRIBUTES_MAP);
    map.put(key, value);
    return map;
  }
}
