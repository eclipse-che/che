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

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.eclipse.che.workspace.infrastructure.kubernetes.server.KubernetesServerExposer.SERVER_PREFIX;
import static org.eclipse.che.workspace.infrastructure.kubernetes.server.external.MultiHostExternalServiceExposureStrategy.MULTI_HOST_STRATEGY;
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
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** @author Guy Daich */
public class MultiHostExternalServiceExposureStrategyTest {

  private static final Map<String, String> ATTRIBUTES_MAP = singletonMap("key", "value");
  private static final Map<String, String> UNIQUE_SERVER_ATTRIBUTES =
      ImmutableMap.of("key", "value", ServerConfig.UNIQUE_SERVER_ATTRIBUTE, "true");
  private static final String MACHINE_NAME = "pod/main";
  private static final String SERVICE_NAME = SERVER_PREFIX + "12345678" + "-" + MACHINE_NAME;
  private static final String DOMAIN = "che.com";
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
            new MultiHostExternalServiceExposureStrategy(DOMAIN, MULTI_HOST_STRATEGY),
            emptyMap(),
            LABELS,
            "%s");
  }

  @Test
  public void shouldCreateIngressForServer() {
    // given
    ServerConfigImpl httpServerConfig =
        new ServerConfigImpl("8080/tcp", "http", "/api", ATTRIBUTES_MAP);
    IntOrString targetPort = new IntOrString(8080);
    ServicePort servicePort =
        new ServicePortBuilder()
            .withName("server-8080")
            .withPort(8080)
            .withProtocol("TCP")
            .withTargetPort(targetPort)
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
        "tcp",
        8080,
        servicePort,
        new ServerConfigImpl(httpServerConfig).withAttributes(ATTRIBUTES_MAP));
  }

  private void assertThatExternalServerIsExposed(
      String machineName,
      String serviceName,
      String serverNameRegex,
      String portProtocol,
      Integer port,
      ServicePort servicePort,
      ServerConfigImpl expected) {

    // ensure that required ingress is created
    String ingressName =
        serviceName + "-" + (expected.isUnique() ? serverNameRegex : servicePort.getName());
    Ingress ingress = kubernetesEnvironment.getIngresses().get(ingressName);
    IngressRule ingressRule = ingress.getSpec().getRules().get(0);
    assertEquals(ingressRule.getHost(), ingressName + "." + DOMAIN);
    assertEquals(ingressRule.getHttp().getPaths().get(0).getPath(), "/");
    IngressBackend backend = ingressRule.getHttp().getPaths().get(0).getBackend();
    assertEquals(backend.getServiceName(), serviceName);
    assertEquals(backend.getServicePort().getStrVal(), servicePort.getName());

    Annotations.Deserializer ingressAnnotations =
        Annotations.newDeserializer(ingress.getMetadata().getAnnotations());
    Map<String, ServerConfigImpl> servers = ingressAnnotations.servers();
    ServerConfig serverConfig = servers.get(serverNameRegex);
    assertEquals(serverConfig, expected);

    assertEquals(ingressAnnotations.machineName(), machineName);
    assertEquals(ingress.getMetadata().getLabels().get("foo"), "bar");
  }
}
