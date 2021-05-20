/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.kubernetes.server.external;

import static java.lang.String.format;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.eclipse.che.workspace.infrastructure.kubernetes.server.KubernetesServerExposer.SERVER_PREFIX;
import static org.testng.Assert.assertEquals;

import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.kubernetes.api.model.ServicePortBuilder;
import io.fabric8.kubernetes.api.model.extensions.Ingress;
import io.fabric8.kubernetes.api.model.extensions.IngressBackend;
import io.fabric8.kubernetes.api.model.extensions.IngressRule;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.workspace.server.model.impl.ServerConfigImpl;
import org.eclipse.che.workspace.infrastructure.kubernetes.Annotations;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** @author Guy Daich */
public class DefaultHostExternalServiceExposureStrategyTest {

  private static final Map<String, String> ATTRIBUTES_MAP = singletonMap("key", "value");

  private static final String MACHINE_NAME = "pod/main";
  private static final String SERVICE_NAME = SERVER_PREFIX + "12345678" + "-" + MACHINE_NAME;
  private static final String LABELS = "foo=bar";

  private IngressServerExposer externalServerExposer;
  private KubernetesEnvironment kubernetesEnvironment;

  @BeforeMethod
  public void setUp() throws Exception {
    Container container = new ContainerBuilder().withName("main").build();
    Pod pod =
        new PodBuilder()
            .withNewMetadata()
            .withName("pod")
            .endMetadata()
            .withNewSpec()
            .withContainers(container)
            .endSpec()
            .build();

    kubernetesEnvironment =
        KubernetesEnvironment.builder().setPods(ImmutableMap.of("pod", pod)).build();
    externalServerExposer =
        new IngressServerExposer(
            new DefaultHostExternalServiceExposureStrategy(), emptyMap(), LABELS, "%s");
  }

  @Test
  public void shouldCreateIngressForServer() {
    // given
    ServerConfigImpl httpServerConfig =
        new ServerConfigImpl("8080/tcp", "http", "/api", ATTRIBUTES_MAP);
    ServicePort servicePort =
        new ServicePortBuilder()
            .withName("server-8080")
            .withPort(8080)
            .withProtocol("TCP")
            .withTargetPort(new IntOrString(8080))
            .build();
    Map<String, ServerConfig> serversToExpose = ImmutableMap.of("http-server", httpServerConfig);

    // when
    externalServerExposer.expose(
        kubernetesEnvironment, MACHINE_NAME, SERVICE_NAME, null, servicePort, serversToExpose);

    // then
    assertThatExternalServerIsExposed(
        MACHINE_NAME,
        SERVICE_NAME,
        "http-server",
        servicePort,
        new ServerConfigImpl(httpServerConfig).withAttributes(ATTRIBUTES_MAP));
  }

  @Test
  public void shouldCreateSingleIngressForTwoNonUniqueServersWithTheSamePort() {
    // given
    ServerConfigImpl httpServerConfig =
        new ServerConfigImpl("8080/tcp", "http", "/api", ATTRIBUTES_MAP);
    ServerConfigImpl wsServerConfig =
        new ServerConfigImpl("8080/tcp", "ws", "/connect", ATTRIBUTES_MAP);
    ServicePort servicePort =
        new ServicePortBuilder()
            .withName("server-8080")
            .withPort(8080)
            .withProtocol("TCP")
            .withTargetPort(new IntOrString(8080))
            .build();

    Map<String, ServerConfig> serversToExpose =
        ImmutableMap.of(
            "http-server", httpServerConfig,
            "ws-server", wsServerConfig);

    // when
    externalServerExposer.expose(
        kubernetesEnvironment, MACHINE_NAME, SERVICE_NAME, null, servicePort, serversToExpose);

    // then
    assertEquals(kubernetesEnvironment.getIngresses().size(), 1);
    assertThatExternalServerIsExposed(
        MACHINE_NAME,
        SERVICE_NAME,
        "http-server",
        servicePort,
        new ServerConfigImpl(httpServerConfig).withAttributes(ATTRIBUTES_MAP));
    assertThatExternalServerIsExposed(
        MACHINE_NAME,
        SERVICE_NAME,
        "ws-server",
        servicePort,
        new ServerConfigImpl(wsServerConfig).withAttributes(ATTRIBUTES_MAP));
  }

  @SuppressWarnings("SameParameterValue")
  private void assertThatExternalServerIsExposed(
      String machineName,
      String serviceName,
      String serverNameRegex,
      ServicePort servicePort,
      ServerConfigImpl expected) {

    // ensure that required ingress is created
    for (Ingress ingress : kubernetesEnvironment.getIngresses().values()) {
      IngressRule ingressRule = ingress.getSpec().getRules().get(0);
      IngressBackend backend = ingressRule.getHttp().getPaths().get(0).getBackend();
      if (serviceName.equals(backend.getServiceName())) {
        assertEquals(backend.getServicePort().getStrVal(), servicePort.getName());

        Annotations.Deserializer ingressAnnotations =
            Annotations.newDeserializer(ingress.getMetadata().getAnnotations());
        Map<String, ServerConfigImpl> servers = ingressAnnotations.servers();
        ServerConfig serverConfig = servers.get(serverNameRegex);

        if (serverConfig == null) {
          // ok, this ingress is not for this particular server
          continue;
        }

        assertEquals(serverConfig, expected);

        assertEquals(ingressAnnotations.machineName(), machineName);
        return;
      }
    }

    Assert.fail(
        format(
            "Could not find an ingress for machine '%s' and service '%s'",
            machineName, serviceName));
  }
}
