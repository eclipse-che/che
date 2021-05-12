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
package org.eclipse.che.workspace.infrastructure.kubernetes.server;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.LoadBalancerStatusBuilder;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.api.model.ServicePortBuilder;
import io.fabric8.kubernetes.api.model.extensions.HTTPIngressPath;
import io.fabric8.kubernetes.api.model.extensions.HTTPIngressRuleValue;
import io.fabric8.kubernetes.api.model.extensions.Ingress;
import io.fabric8.kubernetes.api.model.extensions.IngressBackend;
import io.fabric8.kubernetes.api.model.extensions.IngressBuilder;
import io.fabric8.kubernetes.api.model.extensions.IngressRule;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.core.model.workspace.runtime.ServerStatus;
import org.eclipse.che.api.workspace.server.model.impl.ServerConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.ServerImpl;
import org.eclipse.che.api.workspace.shared.Constants;
import org.eclipse.che.commons.lang.Pair;
import org.eclipse.che.workspace.infrastructure.kubernetes.Annotations;
import org.eclipse.che.workspace.infrastructure.kubernetes.Annotations.Serializer;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.external.ExternalServiceExposureStrategy;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.external.IngressPathTransformInverter;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.resolver.IngressServerResolver;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.resolver.ServerResolver;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test for {@link IngressServerResolver}.
 *
 * @author Sergii Leshchenko
 */
public class IngressServerResolverTest {

  private static final Map<String, String> ATTRIBUTES_MAP = singletonMap("key", "value");
  private static final int CONTAINER_PORT = 3054;
  private static final String INGRESS_IP = "127.0.0.1";
  private static final String INGRESS_RULE_PATH_PREFIX = "/server-8080";
  private static final String INGRESS_PATH_PREFIX = "server-8080";

  private ExternalServiceExposureStrategy externalServiceExposureStrategy;

  @BeforeMethod
  public void setupMocks() {
    externalServiceExposureStrategy = Mockito.mock(ExternalServiceExposureStrategy.class);
  }

  @Test
  public void
      testResolvingServersWhenThereIsNoTheCorrespondingServiceAndIngressForTheSpecifiedMachine() {
    // given
    Service nonMatchedByPodService =
        createService("nonMatched", "foreignMachine", CONTAINER_PORT, null);
    Ingress ingress =
        createIngress(
            "nonMatched",
            "foreignMachine",
            Pair.of("http-server", new ServerConfigImpl("3054", "http", "/api", ATTRIBUTES_MAP)));

    ServerResolver serverResolver =
        new IngressServerResolver(
            new NoopPathTransformInverter(),
            singletonList(nonMatchedByPodService),
            singletonList(ingress));

    // when
    Map<String, ServerImpl> resolved = serverResolver.resolve("machine");

    // then
    assertTrue(resolved.isEmpty());
  }

  @Test
  public void testResolvingServersWhenThereIsMatchedIngressForTheSpecifiedMachine() {
    Ingress ingress =
        createIngress(
            "matched",
            "machine",
            Pair.of("http-server", new ServerConfigImpl("3054", "http", "/api/", ATTRIBUTES_MAP)));

    ServerResolver serverResolver =
        new IngressServerResolver(
            new NoopPathTransformInverter(), emptyList(), singletonList(ingress));

    Map<String, ServerImpl> resolved = serverResolver.resolve("machine");

    assertEquals(resolved.size(), 1);
    assertEquals(
        resolved.get("http-server"),
        new ServerImpl()
            .withUrl("http://" + INGRESS_IP + INGRESS_RULE_PATH_PREFIX + "/api/")
            .withStatus(ServerStatus.UNKNOWN)
            .withAttributes(
                defaultAttributeAnd(
                    Constants.SERVER_PORT_ATTRIBUTE,
                    "3054",
                    ServerConfig.ENDPOINT_ORIGIN,
                    INGRESS_PATH_PREFIX + "/")));
  }

  @Test
  public void testResolvingServersWhenThereIsMatchedIngressForMachineAndServerPathIsNull() {
    Ingress ingress =
        createIngress(
            "matched",
            "machine",
            Pair.of("http-server", new ServerConfigImpl("3054", "http", null, ATTRIBUTES_MAP)));

    ServerResolver serverResolver =
        new IngressServerResolver(
            new NoopPathTransformInverter(), emptyList(), singletonList(ingress));

    Map<String, ServerImpl> resolved = serverResolver.resolve("machine");

    assertEquals(resolved.size(), 1);
    assertEquals(
        resolved.get("http-server"),
        new ServerImpl()
            .withUrl("http://" + INGRESS_IP + INGRESS_RULE_PATH_PREFIX + "/")
            .withStatus(ServerStatus.UNKNOWN)
            .withAttributes(
                defaultAttributeAnd(
                    Constants.SERVER_PORT_ATTRIBUTE,
                    "3054",
                    ServerConfig.ENDPOINT_ORIGIN,
                    INGRESS_PATH_PREFIX + "/")));
  }

  @Test
  public void testResolvingServersWhenThereIsMatchedIngressForMachineAndServerPathIsEmpty() {
    Ingress ingress =
        createIngress(
            "matched",
            "machine",
            Pair.of("http-server", new ServerConfigImpl("3054", "http", "", ATTRIBUTES_MAP)));

    ServerResolver serverResolver =
        new IngressServerResolver(
            new NoopPathTransformInverter(), emptyList(), singletonList(ingress));

    Map<String, ServerImpl> resolved = serverResolver.resolve("machine");

    assertEquals(resolved.size(), 1);
    assertEquals(
        resolved.get("http-server"),
        new ServerImpl()
            .withUrl("http://" + INGRESS_IP + INGRESS_RULE_PATH_PREFIX + "/")
            .withStatus(ServerStatus.UNKNOWN)
            .withAttributes(
                defaultAttributeAnd(
                    Constants.SERVER_PORT_ATTRIBUTE,
                    "3054",
                    ServerConfig.ENDPOINT_ORIGIN,
                    INGRESS_PATH_PREFIX + "/")));
  }

  @Test
  public void testResolvingServersWhenThereIsMatchedIngressForMachineAndServerPathIsRelative() {
    Ingress ingress =
        createIngress(
            "matched",
            "machine",
            Pair.of("http-server", new ServerConfigImpl("3054", "http", "api", ATTRIBUTES_MAP)));

    ServerResolver serverResolver =
        new IngressServerResolver(
            new NoopPathTransformInverter(), emptyList(), singletonList(ingress));

    Map<String, ServerImpl> resolved = serverResolver.resolve("machine");

    assertEquals(resolved.size(), 1);
    assertEquals(
        resolved.get("http-server"),
        new ServerImpl()
            .withUrl("http://" + INGRESS_IP + INGRESS_RULE_PATH_PREFIX + "/api/")
            .withStatus(ServerStatus.UNKNOWN)
            .withAttributes(
                defaultAttributeAnd(
                    Constants.SERVER_PORT_ATTRIBUTE,
                    "3054",
                    ServerConfig.ENDPOINT_ORIGIN,
                    INGRESS_PATH_PREFIX + "/")));
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

    ServerResolver serverResolver =
        new IngressServerResolver(
            new NoopPathTransformInverter(), singletonList(service), emptyList());

    Map<String, ServerImpl> resolved = serverResolver.resolve("machine");

    assertEquals(resolved.size(), 1);
    assertEquals(
        resolved.get("http-server"),
        new ServerImpl()
            .withUrl("http://service11:3054/api")
            .withStatus(ServerStatus.UNKNOWN)
            .withAttributes(
                defaultAttributeAnd(
                    Constants.SERVER_PORT_ATTRIBUTE, "3054", ServerConfig.ENDPOINT_ORIGIN, "/")));
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

    ServerResolver serverResolver =
        new IngressServerResolver(
            new NoopPathTransformInverter(), singletonList(service), emptyList());

    Map<String, ServerImpl> resolved = serverResolver.resolve("machine");

    assertEquals(resolved.size(), 1);
    assertEquals(
        resolved.get("http-server"),
        new ServerImpl()
            .withUrl("xxx://service11:3054/api")
            .withStatus(ServerStatus.UNKNOWN)
            .withAttributes(
                defaultAttributeAnd(
                    Constants.SERVER_PORT_ATTRIBUTE, "3054", ServerConfig.ENDPOINT_ORIGIN, "/")));
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

  private Ingress createIngress(
      String name, String machineName, Pair<String, ServerConfig> server) {
    Serializer serializer = Annotations.newSerializer();
    serializer.machineName(machineName);
    serializer.server(server.first, server.second);

    return new IngressBuilder()
        .withNewMetadata()
        .withName(name)
        .withAnnotations(serializer.annotations())
        .endMetadata()
        .withNewSpec()
        .withRules(
            new IngressRule(
                null,
                new HTTPIngressRuleValue(
                    singletonList(
                        new HTTPIngressPath(
                            new IngressBackend(null, name, new IntOrString("8080")),
                            INGRESS_PATH_PREFIX,
                            null)))))
        .endSpec()
        .withNewStatus()
        .withLoadBalancer(
            new LoadBalancerStatusBuilder()
                .addNewIngress()
                .withIp("127.0.0.1")
                .endIngress()
                .build())
        .endStatus()
        .build();
  }

  private Map<String, String> defaultAttributeAnd(String... keyValues) {
    HashMap<String, String> attributes = new HashMap<>(ATTRIBUTES_MAP);
    String key = null;
    for (String v : keyValues) {
      if (key == null) {
        key = v;
      } else {
        attributes.put(key, v);
        key = null;
      }
    }
    return attributes;
  }

  private static final class NoopPathTransformInverter extends IngressPathTransformInverter {
    NoopPathTransformInverter() {
      super("%s");
    }
  }
}
