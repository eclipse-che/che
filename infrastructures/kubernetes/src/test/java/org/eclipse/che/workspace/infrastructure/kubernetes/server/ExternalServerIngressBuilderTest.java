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
package org.eclipse.che.workspace.infrastructure.kubernetes.server;

import static java.util.Collections.singletonMap;
import static org.testng.Assert.*;

import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.extensions.HTTPIngressPath;
import io.fabric8.kubernetes.api.model.extensions.Ingress;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.workspace.server.model.impl.ServerConfigImpl;
import org.eclipse.che.workspace.infrastructure.kubernetes.Annotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** @author Guy Daich */
public class ExternalServerIngressBuilderTest {

  private static final Map<String, String> ATTRIBUTES_MAP = singletonMap("key", "value");
  private static final ServerConfig SERVER_CONFIG =
      new ServerConfigImpl("8080/tcp", "http", "/api", ATTRIBUTES_MAP);
  private final Map<String, ServerConfig> SERVERS = ImmutableMap.of("http-server", SERVER_CONFIG);
  private static final Map<String, String> ANNOTATIONS =
      singletonMap("annotation-key", "annotation-value");
  private static final String MACHINE_NAME = "machine";
  private static final String NAME = "IngressName";
  private static final String SERVICE_NAME = "ServiceName";
  private static final String SERVICE_PORT = "server-port";

  private ExternalServerIngressBuilder externalServerIngressBuilder;

  @BeforeMethod
  public void setUp() throws Exception {
    this.externalServerIngressBuilder = new ExternalServerIngressBuilder();
  }

  @Test
  public void shouldCreateIngress() {
    // given
    final String path = "/path/to/server";
    final String host = "host-to-server";

    // when
    Ingress ingress =
        externalServerIngressBuilder
            .withPath(path)
            .withHost(host)
            .withAnnotations(ANNOTATIONS)
            .withMachineName(MACHINE_NAME)
            .withName(NAME)
            .withServers(SERVERS)
            .withServiceName(SERVICE_NAME)
            .withServicePort(SERVICE_PORT)
            .build();

    // then
    AssertIngressSpec(path, host, ingress);
  }

  @Test
  public void shouldCreateIngressWithNoPath() {
    // given
    final String host = "host-to-server";

    // when
    Ingress ingress =
        externalServerIngressBuilder
            .withHost(host)
            .withAnnotations(ANNOTATIONS)
            .withMachineName(MACHINE_NAME)
            .withName(NAME)
            .withServers(SERVERS)
            .withServiceName(SERVICE_NAME)
            .withServicePort(SERVICE_PORT)
            .build();

    // then
    AssertIngressSpec(null, host, ingress);
  }

  @Test
  public void shouldCreateIngressWithNoHost() {
    // given
    final String path = "/path/to/server";

    // when
    Ingress ingress =
        externalServerIngressBuilder
            .withPath(path)
            .withAnnotations(ANNOTATIONS)
            .withMachineName(MACHINE_NAME)
            .withName(NAME)
            .withServers(SERVERS)
            .withServiceName(SERVICE_NAME)
            .withServicePort(SERVICE_PORT)
            .build();

    // then
    AssertIngressSpec(path, null, ingress);
  }

  private void AssertIngressSpec(String path, String host, Ingress ingress) {
    assertEquals(ingress.getSpec().getRules().get(0).getHost(), host);
    HTTPIngressPath httpIngressPath =
        ingress.getSpec().getRules().get(0).getHttp().getPaths().get(0);
    assertEquals(httpIngressPath.getPath(), path);
    assertEquals(httpIngressPath.getBackend().getServiceName(), SERVICE_NAME);
    assertEquals(httpIngressPath.getBackend().getServicePort().getStrVal(), SERVICE_PORT);

    assertEquals(ingress.getMetadata().getName(), NAME);
    assertTrue(ingress.getMetadata().getAnnotations().containsKey("annotation-key"));
    assertEquals(ingress.getMetadata().getAnnotations().get("annotation-key"), "annotation-value");

    Annotations.Deserializer ingressAnnotations =
        Annotations.newDeserializer(ingress.getMetadata().getAnnotations());
    Map<String, ServerConfigImpl> servers = ingressAnnotations.servers();
    ServerConfig serverConfig = servers.get("http-server");
    assertEquals(serverConfig, SERVER_CONFIG);

    assertEquals(ingressAnnotations.machineName(), MACHINE_NAME);
  }
}
