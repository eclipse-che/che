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
package org.eclipse.che.workspace.infrastructure.kubernetes.server;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.eclipse.che.api.core.model.workspace.config.ServerConfig.DISCOVERABLE_SERVER_ATTRIBUTE;
import static org.eclipse.che.api.core.model.workspace.config.ServerConfig.SERVER_NAME_ATTRIBUTE;
import static org.eclipse.che.workspace.infrastructure.kubernetes.server.KubernetesServerExposer.SERVER_PREFIX;
import static org.eclipse.che.workspace.infrastructure.kubernetes.server.KubernetesServerExposer.SERVER_UNIQUE_PART_SIZE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.ContainerPortBuilder;
import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.kubernetes.api.model.ServicePortBuilder;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.regex.Pattern;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.workspace.server.model.impl.ServerConfigImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.Annotations;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment.PodData;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.external.IngressServerExposer;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.secure.SecureServerExposer;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Test for {@link KubernetesServerExposer}.
 *
 * @author Sergii Leshchenko
 */
@Listeners(MockitoTestNGListener.class)
public class KubernetesServerExposerTest {

  @Mock private IngressServerExposer externalServerExposer;
  @Mock private SecureServerExposer<KubernetesEnvironment> secureServerExposer;

  private static final Map<String, String> ATTRIBUTES_MAP = singletonMap("key", "value");
  private static final Map<String, String> INTERNAL_SERVER_ATTRIBUTE_MAP =
      singletonMap(ServerConfig.INTERNAL_SERVER_ATTRIBUTE, Boolean.TRUE.toString());

  private static final Map<String, String> SECURE_SERVER_ATTRIBUTE_MAP =
      singletonMap(ServerConfig.SECURE_SERVER_ATTRIBUTE, Boolean.TRUE.toString());

  private static final Pattern SERVER_PREFIX_REGEX =
      Pattern.compile('^' + SERVER_PREFIX + "[A-z0-9]{" + SERVER_UNIQUE_PART_SIZE + "}-pod-main$");
  private static final String MACHINE_NAME = "pod/main";
  private static final Map<String, String> UNIQUE_SERVER_ATTRIBUTES =
      ImmutableMap.of("key", "value", ServerConfig.UNIQUE_SERVER_ATTRIBUTE, "true");
  private static final String SERVICE_NAME = SERVER_PREFIX + "12345678" + "-" + MACHINE_NAME;

  private KubernetesServerExposer<KubernetesEnvironment> serverExposer;
  private KubernetesEnvironment kubernetesEnvironment;
  private Container container;

  @BeforeMethod
  public void setUp() throws Exception {
    container = new ContainerBuilder().withName("main").build();
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

    PodData podData = new PodData(pod.getSpec(), pod.getMetadata());
    this.serverExposer =
        new KubernetesServerExposer<>(
            externalServerExposer,
            secureServerExposer,
            MACHINE_NAME,
            podData,
            container,
            kubernetesEnvironment);
  }

  @Test
  public void shouldExposeContainerPortAndCreateServiceForServer() throws Exception {
    // given
    ServerConfigImpl httpServerConfig =
        new ServerConfigImpl("8080/tcp", "http", "/api", ATTRIBUTES_MAP);
    Map<String, ServerConfigImpl> serversToExpose =
        ImmutableMap.of("http-server", httpServerConfig);

    // when
    serverExposer.expose(serversToExpose);

    // then
    assertThatExternalServerIsExposed(
        MACHINE_NAME,
        "tcp",
        8080,
        "http-server",
        new ServerConfigImpl(httpServerConfig).withAttributes(ATTRIBUTES_MAP));
  }

  @Test
  public void shouldExposeContainerPortAndCreateServiceAndForServerWhenTwoServersHasTheSamePort()
      throws Exception {
    // given
    ServerConfigImpl httpServerConfig =
        new ServerConfigImpl("8080/tcp", "http", "/api", ATTRIBUTES_MAP);
    ServerConfigImpl wsServerConfig =
        new ServerConfigImpl("8080/tcp", "ws", "/connect", ATTRIBUTES_MAP);
    Map<String, ServerConfigImpl> serversToExpose =
        ImmutableMap.of(
            "http-server", httpServerConfig,
            "ws-server", wsServerConfig);

    // when
    serverExposer.expose(serversToExpose);

    // then
    assertEquals(kubernetesEnvironment.getServices().size(), 1);

    assertThatExternalServersAreExposed(
        MACHINE_NAME,
        "tcp",
        8080,
        ImmutableMap.of(
            "http-server",
            new ServerConfigImpl(httpServerConfig).withAttributes(ATTRIBUTES_MAP),
            "ws-server",
            new ServerConfigImpl(wsServerConfig).withAttributes(ATTRIBUTES_MAP)));
  }

  @Test
  public void shouldExposeContainerPortsAndCreateServiceForServerWhenTwoServersHasDifferentPorts()
      throws Exception {
    // given
    ServerConfigImpl httpServerConfig =
        new ServerConfigImpl("8080/tcp", "http", "/api", ATTRIBUTES_MAP);
    ServerConfigImpl wsServerConfig =
        new ServerConfigImpl("8081/tcp", "ws", "/connect", ATTRIBUTES_MAP);
    Map<String, ServerConfigImpl> serversToExpose =
        ImmutableMap.of(
            "http-server", httpServerConfig,
            "ws-server", wsServerConfig);

    // when
    serverExposer.expose(serversToExpose);

    // then
    assertEquals(kubernetesEnvironment.getServices().size(), 1);

    assertThatExternalServerIsExposed(
        MACHINE_NAME,
        "tcp",
        8080,
        "http-server",
        new ServerConfigImpl(httpServerConfig).withAttributes(ATTRIBUTES_MAP));
    assertThatExternalServerIsExposed(
        MACHINE_NAME,
        "tcp",
        8081,
        "ws-server",
        new ServerConfigImpl(wsServerConfig).withAttributes(ATTRIBUTES_MAP));
  }

  @Test
  public void shouldExposeTcpContainerPortsAndCreateServiceAndForServerWhenProtocolIsMissedInPort()
      throws Exception {
    // given
    ServerConfigImpl httpServerConfig =
        new ServerConfigImpl("8080", "http", "/api", ATTRIBUTES_MAP);
    Map<String, ServerConfigImpl> serversToExpose =
        ImmutableMap.of("http-server", httpServerConfig);

    // when
    serverExposer.expose(serversToExpose);

    // then
    assertEquals(kubernetesEnvironment.getServices().size(), 1);

    assertThatExternalServerIsExposed(
        MACHINE_NAME,
        "TCP",
        8080,
        "http-server",
        new ServerConfigImpl(httpServerConfig).withAttributes(ATTRIBUTES_MAP));
  }

  @Test
  public void shouldNotAddAdditionalContainerPortWhenItIsAlreadyExposed() throws Exception {
    // given
    ServerConfigImpl httpServerConfig =
        new ServerConfigImpl("8080/tcp", "http", "/api", ATTRIBUTES_MAP);
    Map<String, ServerConfigImpl> serversToExpose =
        ImmutableMap.of("http-server", httpServerConfig);
    container.setPorts(
        singletonList(
            new ContainerPortBuilder()
                .withName("port-8080")
                .withContainerPort(8080)
                .withProtocol("TCP")
                .build()));

    // when
    serverExposer.expose(serversToExpose);

    // then
    assertThatExternalServerIsExposed(
        MACHINE_NAME,
        "tcp",
        8080,
        "http-server",
        new ServerConfigImpl(httpServerConfig).withAttributes(ATTRIBUTES_MAP));
  }

  @Test
  public void shouldAddAdditionalContainerPortWhenThereIsTheSameButWithDifferentProtocol()
      throws Exception {
    // given
    ServerConfigImpl udpServerConfig =
        new ServerConfigImpl("8080/udp", "udp", "/api", ATTRIBUTES_MAP);
    Map<String, ServerConfigImpl> serversToExpose = ImmutableMap.of("server", udpServerConfig);
    container.setPorts(
        new ArrayList<>(
            singletonList(
                new ContainerPortBuilder()
                    .withName("port-8080")
                    .withContainerPort(8080)
                    .withProtocol("TCP")
                    .build())));

    // when
    serverExposer.expose(serversToExpose);

    // then
    assertEquals(container.getPorts().size(), 2);
    assertEquals(container.getPorts().get(1).getContainerPort(), new Integer(8080));
    assertEquals(container.getPorts().get(1).getProtocol(), "UDP");
    assertThatExternalServerIsExposed(
        MACHINE_NAME,
        "udp",
        8080,
        "server",
        new ServerConfigImpl(udpServerConfig).withAttributes(ATTRIBUTES_MAP));
  }

  @Test
  public void shouldExposeContainerPortAndCreateServiceForInternalServer() throws Exception {
    // given
    ServerConfigImpl httpServerConfig =
        new ServerConfigImpl("8080/tcp", "http", "/api", INTERNAL_SERVER_ATTRIBUTE_MAP);
    Map<String, ServerConfigImpl> serversToExpose =
        ImmutableMap.of("http-server", httpServerConfig);

    // when
    serverExposer.expose(serversToExpose);

    // then
    assertThatInternalServerIsExposed(
        MACHINE_NAME,
        "http-server",
        "tcp",
        8080,
        new ServerConfigImpl(httpServerConfig).withAttributes(INTERNAL_SERVER_ATTRIBUTE_MAP));
  }

  @Test
  public void shouldExposeInternalAndExternalAndSecureServers() throws Exception {
    // given
    ServerConfigImpl secureServerConfig =
        new ServerConfigImpl("8282/tcp", "http", "/api", SECURE_SERVER_ATTRIBUTE_MAP);
    ServerConfigImpl internalServerConfig =
        new ServerConfigImpl("8080/tcp", "http", "/api", INTERNAL_SERVER_ATTRIBUTE_MAP);
    ServerConfigImpl externalServerConfig =
        new ServerConfigImpl("9090/tcp", "http", "/api", ATTRIBUTES_MAP);
    Map<String, ServerConfigImpl> serversToExpose =
        ImmutableMap.of(
            "int-server",
            internalServerConfig,
            "ext-server",
            externalServerConfig,
            "secure-server",
            secureServerConfig);

    // when
    serverExposer.expose(serversToExpose);

    // then
    assertThatInternalServerIsExposed(
        MACHINE_NAME, "int-server", "tcp", 8080, new ServerConfigImpl(internalServerConfig));
    assertThatExternalServerIsExposed(
        MACHINE_NAME, "tcp", 9090, "ext-server", new ServerConfigImpl(externalServerConfig));
    assertThatSecureServerIsExposed(
        MACHINE_NAME, "tcp", 8282, "secure-server", new ServerConfigImpl(secureServerConfig));
  }

  @Test
  public void shouldCreateIngressPerUniqueServerWithTheSamePort() throws Exception {
    // given
    ServerConfigImpl httpServerConfig =
        new ServerConfigImpl("8080/tcp", "http", "/api", UNIQUE_SERVER_ATTRIBUTES);
    ServerConfigImpl wsServerConfig =
        new ServerConfigImpl("8080/tcp", "ws", "/connect", UNIQUE_SERVER_ATTRIBUTES);
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
    serverExposer.expose(serversToExpose);

    // then
    assertThatExternalServerIsExposed(
        MACHINE_NAME,
        "tcp",
        8080,
        "http-server",
        new ServerConfigImpl(httpServerConfig).withAttributes(UNIQUE_SERVER_ATTRIBUTES));
    assertThatExternalServerIsExposed(
        MACHINE_NAME,
        "tcp",
        8080,
        "ws-server",
        new ServerConfigImpl(wsServerConfig).withAttributes(UNIQUE_SERVER_ATTRIBUTES));
  }

  @Test
  public void shouldCreateIngressForServerWhenTwoServersHasTheSamePort()
      throws InfrastructureException {
    // given
    ServerConfigImpl httpServerConfig =
        new ServerConfigImpl("8080/tcp", "http", "/api", ATTRIBUTES_MAP);
    ServerConfigImpl wsServerConfig =
        new ServerConfigImpl("8080/tcp", "ws", "/connect", ATTRIBUTES_MAP);
    IntOrString targetPort = new IntOrString(8080);

    ServicePort servicePort =
        new ServicePortBuilder()
            .withName("server-8080")
            .withPort(8080)
            .withProtocol("TCP")
            .withTargetPort(targetPort)
            .build();

    Map<String, ServerConfig> serversToExpose =
        ImmutableMap.of(
            "http-server", httpServerConfig,
            "ws-server", wsServerConfig);

    // when
    serverExposer.expose(serversToExpose);

    // then
    assertThatExternalServersAreExposed(
        MACHINE_NAME,
        "tcp",
        8080,
        ImmutableMap.of(
            "http-server", new ServerConfigImpl(httpServerConfig).withAttributes(ATTRIBUTES_MAP),
            "ws-server", new ServerConfigImpl(wsServerConfig).withAttributes(ATTRIBUTES_MAP)));
  }

  @Test
  public void testCreateExtraServiceForDiscoverableServerConfig() throws InfrastructureException {
    // given
    ServerConfigImpl httpServerConfig =
        new ServerConfigImpl(
            "8080/tcp",
            "http",
            "/api",
            ImmutableMap.of(SERVER_NAME_ATTRIBUTE, "hello", DISCOVERABLE_SERVER_ATTRIBUTE, "true"));
    Map<String, ServerConfigImpl> serversToExpose =
        ImmutableMap.of("http-server", httpServerConfig);

    // when
    serverExposer.expose(serversToExpose);

    assertEquals(kubernetesEnvironment.getServices().size(), 2);
    assertTrue(kubernetesEnvironment.getServices().containsKey("hello"));
    assertEquals(kubernetesEnvironment.getServices().get("hello").getMetadata().getName(), "hello");
  }

  @Test
  public void testDiscoverableServerConfigWithoutNameAttributeIsNotProvisioned()
      throws InfrastructureException {
    // given
    ServerConfigImpl httpServerConfig =
        new ServerConfigImpl(
            "8080/tcp", "http", "/api", ImmutableMap.of(DISCOVERABLE_SERVER_ATTRIBUTE, "true"));
    Map<String, ServerConfigImpl> serversToExpose =
        ImmutableMap.of("http-server", httpServerConfig);

    // when
    serverExposer.expose(serversToExpose);

    assertEquals(kubernetesEnvironment.getServices().size(), 1);
    assertFalse(kubernetesEnvironment.getServices().containsKey("hello"));
  }

  @Test
  public void testExposeUniqueSecureServersWithOnlyMatchingServers()
      throws InfrastructureException {
    // https://github.com/eclipse/che/issues/16330
    // given 2 servers which one of them is unique
    ServerConfigImpl theiaSC =
        new ServerConfigImpl(
            "3100/tcp",
            "https",
            null,
            ImmutableMap.of(
                "internal", "false", "discoverable", "false", "secure", "true", "type", "ide"));

    ServerConfigImpl webviewsSC =
        new ServerConfigImpl(
            "3100/tcp",
            "https",
            null,
            ImmutableMap.of(
                "internal",
                "false",
                "discoverable",
                "false",
                "unique",
                "true",
                "secure",
                "true",
                "type",
                "webview"));
    Map<String, ServerConfigImpl> serversToExpose =
        ImmutableMap.of("theia", theiaSC, "webviews", webviewsSC);

    // when
    serverExposer.expose(serversToExpose);

    // then expose is called twice with only one server each
    ArgumentCaptor<Map<String, ServerConfig>> serversCaptor = ArgumentCaptor.forClass(Map.class);
    verify(secureServerExposer, times(2))
        .expose(any(), any(), any(), any(), any(), any(), serversCaptor.capture());
    for (Map<String, ServerConfig> captures : serversCaptor.getAllValues()) {
      assertEquals(captures.size(), 1);
    }
  }

  @SuppressWarnings("SameParameterValue")
  private void assertThatExternalServerIsExposed(
      String machineName,
      String portProtocol,
      Integer port,
      String serverName,
      ServerConfig expectedServer) {
    assertThatExternalServersAreExposed(
        machineName, portProtocol, port, ImmutableMap.of(serverName, expectedServer));
  }

  @SuppressWarnings("SameParameterValue")
  private void assertThatExternalServersAreExposed(
      String machineName,
      String portProtocol,
      Integer port,
      Map<String, ServerConfig> expectedServers) {
    // then
    assertThatContainerPortIsExposed(portProtocol, port);
    // ensure that service is created

    Service service = findContainerRelatedService();
    assertNotNull(service);

    // ensure that required service port is exposed
    ServicePort servicePort = assertThatServicePortIsExposed(port, service);

    Annotations.Deserializer serviceAnnotations =
        Annotations.newDeserializer(service.getMetadata().getAnnotations());
    assertEquals(serviceAnnotations.machineName(), machineName);
    // check that we did not create servers for public endpoints
    assertFalse(
        serviceAnnotations
            .servers()
            .keySet()
            .stream()
            .anyMatch(key -> expectedServers.containsKey(key)));

    verify(externalServerExposer)
        .expose(
            eq(kubernetesEnvironment),
            eq(machineName),
            eq(service.getMetadata().getName()),
            any(),
            eq(servicePort),
            eq(expectedServers));
  }

  @SuppressWarnings("SameParameterValue")
  private void assertThatSecureServerIsExposed(
      String machineName,
      String portProtocol,
      Integer port,
      String serverName,
      ServerConfig serverConfig)
      throws Exception {
    // then
    assertThatContainerPortIsExposed(portProtocol, port);
    // ensure that service is created

    Service service = findContainerRelatedService();
    assertNotNull(service);

    // ensure that no service port is exposed
    assertTrue(
        service
            .getSpec()
            .getPorts()
            .stream()
            .noneMatch(p -> p.getTargetPort().getIntVal().equals(port)));

    ServicePort servicePort =
        new ServicePortBuilder()
            .withName("server-" + port)
            .withPort(port)
            .withProtocol(portProtocol.toUpperCase())
            .withNewTargetPort(port)
            .build();

    verify(secureServerExposer)
        .expose(
            eq(kubernetesEnvironment),
            any(),
            eq(machineName),
            isNull(), // no service exists for the backed server
            isNull(), // a non-unique server
            eq(servicePort),
            eq(ImmutableMap.of(serverName, serverConfig)));
  }

  @SuppressWarnings("SameParameterValue")
  private void assertThatInternalServerIsExposed(
      String machineName,
      String serverNameRegex,
      String portProtocol,
      Integer port,
      ServerConfigImpl expected) {
    assertThatContainerPortIsExposed(portProtocol, port);

    // ensure that service is created

    Service service = findContainerRelatedService();
    assertNotNull(service);

    // ensure that required service port is exposed
    assertThatServicePortIsExposed(port, service);

    Annotations.Deserializer serviceAnnotations =
        Annotations.newDeserializer(service.getMetadata().getAnnotations());
    assertEquals(serviceAnnotations.machineName(), machineName);

    Map<String, ServerConfigImpl> servers = serviceAnnotations.servers();
    ServerConfig serverConfig = servers.get(serverNameRegex);
    assertEquals(serverConfig, expected);
  }

  private void assertThatContainerPortIsExposed(String portProtocol, Integer port) {
    assertTrue(
        container
            .getPorts()
            .stream()
            .anyMatch(
                p ->
                    p.getContainerPort().equals(port)
                        && p.getProtocol().equals(portProtocol.toUpperCase())));
  }

  private Service findContainerRelatedService() {
    Service service = null;
    for (Entry<String, Service> entry : kubernetesEnvironment.getServices().entrySet()) {
      if (SERVER_PREFIX_REGEX.matcher(entry.getKey()).matches()) {
        service = entry.getValue();
        break;
      }
    }
    return service;
  }

  private ServicePort assertThatServicePortIsExposed(Integer port, Service service) {
    Optional<ServicePort> servicePortOpt =
        service
            .getSpec()
            .getPorts()
            .stream()
            .filter(p -> p.getTargetPort().getIntVal().equals(port))
            .findAny();
    assertTrue(servicePortOpt.isPresent());
    ServicePort servicePort = servicePortOpt.get();
    assertEquals(servicePort.getTargetPort().getIntVal(), port);
    assertEquals(servicePort.getPort(), port);
    assertEquals(servicePort.getName(), SERVER_PREFIX + "-" + port);
    return servicePort;
  }
}
