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
package org.eclipse.che.workspace.infrastructure.openshift;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.eclipse.che.api.core.model.workspace.runtime.ServerStatus.UNKNOWN;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.ContainerPortBuilder;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.api.model.ServicePortBuilder;
import io.fabric8.openshift.api.model.Route;
import io.fabric8.openshift.api.model.RouteBuilder;
import java.util.Map;
import org.eclipse.che.api.workspace.server.model.impl.ServerConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.ServerImpl;
import org.testng.annotations.Test;

/**
 * Test for {@link ServerResolver}.
 *
 * @author Sergii Leshchenko
 */
public class ServerResolverTest {
  private static final int CONTAINER_PORT = 3054;
  private static final String ROUTE_HOST = "localhost";

  @Test
  public void testResolvingServersWhenThereIsNoMatchedServiceByPodLabels() {
    // given
    Container container = createContainer();
    Pod pod = createPod(ImmutableMap.of("kind", "web-app"));
    Service nonMatchedByPodService =
        createService("nonMatched", CONTAINER_PORT, ImmutableMap.of("kind", "db"));
    Route route =
        createRoute(
            "nonMatched",
            ImmutableMap.of(
                "http-server",
                new ServerConfigImpl("3054", "http", "/api", singletonMap("key", "value"))));

    ServerResolver serverResolver =
        ServerResolver.of(singletonList(nonMatchedByPodService), singletonList(route));

    // when
    Map<String, ServerImpl> resolved = serverResolver.resolve(pod, container);

    // then
    assertTrue(resolved.isEmpty());
  }

  @Test
  public void testResolvingServersWhenThereIsNoMatchedServiceByContainerPort() {
    Container container = createContainer();
    Pod pod = createPod(ImmutableMap.of("kind", "web-app"));
    Service nonMatchedByPodService =
        createService("nonMatched", 7777, ImmutableMap.of("kind", "web-app"));
    Route route =
        createRoute(
            "nonMatched",
            ImmutableMap.of(
                "http-server",
                new ServerConfigImpl("3054", "http", "/api", singletonMap("key", "value"))));

    ServerResolver serverResolver =
        ServerResolver.of(singletonList(nonMatchedByPodService), singletonList(route));

    Map<String, ServerImpl> resolved = serverResolver.resolve(pod, container);

    assertTrue(resolved.isEmpty());
  }

  @Test
  public void testResolvingServersWhenThereIsMatchedServiceForContainer() {
    Container container = createContainer();
    Pod pod = createPod(ImmutableMap.of("kind", "web-app"));
    Service nonMatchedByPodService =
        createService("matched", CONTAINER_PORT, ImmutableMap.of("kind", "web-app"));
    Route route =
        createRoute(
            "matched",
            ImmutableMap.of(
                "http-server",
                new ServerConfigImpl("3054", "http", "/api", singletonMap("key", "value")),
                "ws-server",
                new ServerConfigImpl("3054", "ws", "/connect", singletonMap("key", "value"))));

    ServerResolver serverResolver =
        ServerResolver.of(singletonList(nonMatchedByPodService), singletonList(route));

    Map<String, ServerImpl> resolved = serverResolver.resolve(pod, container);

    assertEquals(resolved.size(), 2);
    assertEquals(
        resolved.get("http-server"),
        new ServerImpl().withUrl("http://localhost/api").withStatus(UNKNOWN));
    assertEquals(
        resolved.get("ws-server"),
        new ServerImpl().withUrl("ws://localhost/connect").withStatus(UNKNOWN));
  }

  @Test
  public void testResolvingServersWhenThereIsMatchedServiceForContainerAndServerPathIsNull() {
    Container container = createContainer();
    Pod pod = createPod(singletonMap("kind", "web-app"));
    Service nonMatchedByPodService =
        createService("matched", CONTAINER_PORT, singletonMap("kind", "web-app"));
    Route route =
        createRoute(
            "matched",
            singletonMap(
                "http-server",
                new ServerConfigImpl("3054", "http", null, singletonMap("key", "value"))));

    ServerResolver serverResolver =
        ServerResolver.of(singletonList(nonMatchedByPodService), singletonList(route));

    Map<String, ServerImpl> resolved = serverResolver.resolve(pod, container);

    assertEquals(resolved.size(), 1);
    assertEquals(
        resolved.get("http-server"),
        new ServerImpl().withUrl("http://localhost").withStatus(UNKNOWN));
  }

  @Test
  public void testResolvingServersWhenThereIsMatchedServiceForContainerAndServerPathIsEmpty() {
    Container container = createContainer();
    Pod pod = createPod(singletonMap("kind", "web-app"));
    Service nonMatchedByPodService =
        createService("matched", CONTAINER_PORT, singletonMap("kind", "web-app"));
    Route route =
        createRoute(
            "matched",
            singletonMap(
                "http-server",
                new ServerConfigImpl("3054", "http", "", singletonMap("key", "value"))));

    ServerResolver serverResolver =
        ServerResolver.of(singletonList(nonMatchedByPodService), singletonList(route));

    Map<String, ServerImpl> resolved = serverResolver.resolve(pod, container);

    assertEquals(resolved.size(), 1);
    assertEquals(
        resolved.get("http-server"),
        new ServerImpl().withUrl("http://localhost").withStatus(UNKNOWN));
  }

  @Test
  public void testResolvingServersWhenThereIsMatchedServiceForContainerAndServerPathIsRelative() {
    Container container = createContainer();
    Pod pod = createPod(singletonMap("kind", "web-app"));
    Service nonMatchedByPodService =
        createService("matched", CONTAINER_PORT, singletonMap("kind", "web-app"));
    Route route =
        createRoute(
            "matched",
            singletonMap(
                "http-server",
                new ServerConfigImpl("3054", "http", "api", singletonMap("key", "value"))));

    ServerResolver serverResolver =
        ServerResolver.of(singletonList(nonMatchedByPodService), singletonList(route));

    Map<String, ServerImpl> resolved = serverResolver.resolve(pod, container);

    assertEquals(resolved.size(), 1);
    assertEquals(
        resolved.get("http-server"),
        new ServerImpl().withUrl("http://localhost/api").withStatus(UNKNOWN));
  }

  private Pod createPod(Map<String, String> labels) {
    return new PodBuilder().withNewMetadata().withLabels(labels).endMetadata().build();
  }

  private Container createContainer() {
    return new ContainerBuilder()
        .withPorts(new ContainerPortBuilder().withContainerPort(CONTAINER_PORT).build())
        .build();
  }

  private Service createService(String name, Integer port, Map<String, String> selector) {
    return new ServiceBuilder()
        .withNewMetadata()
        .withName(name)
        .endMetadata()
        .withNewSpec()
        .withSelector(selector)
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

  // TODO Think about common builders
  private Route createRoute(String name, Map<String, ServerConfigImpl> servers) {
    return new RouteBuilder()
        .withNewMetadata()
        .withName(name)
        .withAnnotations(RoutesAnnotations.newSerializer().servers(servers).annotations())
        .endMetadata()
        .withNewSpec()
        .withHost(ROUTE_HOST)
        .withNewTo()
        .withName(name)
        .endTo()
        .endSpec()
        .build();
  }
}
