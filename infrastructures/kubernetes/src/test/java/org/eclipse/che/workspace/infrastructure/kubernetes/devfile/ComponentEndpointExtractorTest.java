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
package org.eclipse.che.workspace.infrastructure.kubernetes.devfile;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.eclipse.che.api.core.model.workspace.config.ServerConfig.INTERNAL_SERVER_ATTRIBUTE;
import static org.eclipse.che.api.workspace.server.devfile.Constants.DISCOVERABLE_ENDPOINT_ATTRIBUTE;
import static org.eclipse.che.workspace.infrastructure.kubernetes.devfile.DockerimageComponentToWorkspaceApplier.CHE_COMPONENT_NAME_LABEL;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServicePort;
import java.util.List;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.workspace.server.model.impl.ServerConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.ComponentImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.EndpointImpl;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ComponentEndpointExtractorTest {

  private final static String TEST_ENDPOINT_NAME = "testEndpointName";
  private final static String TEST_COMPONENT_ALIAS = "testComponentName";

  private ComponentEndpointExtractor componentEndpointExtractor;
  private ComponentImpl testComponent;


  @BeforeMethod
  public void setUp() {
    componentEndpointExtractor = new ComponentEndpointExtractor();
    testComponent = new ComponentImpl("kubernetes", "abc");
    testComponent.setAlias(TEST_COMPONENT_ALIAS);
  }

  @Test
  public void testToServerConfigMinimalEndpointShouldTranslateToHttpProtocol() {
    // given
    testComponent
        .setEndpoints(singletonList(new EndpointImpl(TEST_ENDPOINT_NAME, 123, emptyMap())));

    // when
    Map<String, ServerConfigImpl> serverConfigs =
        componentEndpointExtractor.extractServerConfigsFromComponentEndpoints(testComponent);

    // then
    assertTrue(serverConfigs.containsKey(TEST_ENDPOINT_NAME));
    assertEquals(serverConfigs.size(), 1);
    ServerConfig serverConfig = serverConfigs.get(TEST_ENDPOINT_NAME);
    assertEquals(serverConfig.getProtocol(), "http");
  }

  @Test
  public void testToServerConfigMinimalEndpointShouldTranslateToNullPath() {
    // given
    testComponent
        .setEndpoints(singletonList(new EndpointImpl(TEST_ENDPOINT_NAME, 123, emptyMap())));

    // when
    Map<String, ServerConfigImpl> serverConfigs =
        componentEndpointExtractor.extractServerConfigsFromComponentEndpoints(testComponent);

    // then
    assertTrue(serverConfigs.containsKey(TEST_ENDPOINT_NAME));
    assertEquals(serverConfigs.size(), 1);
    ServerConfig serverConfig = serverConfigs.get(TEST_ENDPOINT_NAME);
    assertNull(serverConfig.getPath());
  }

  @Test
  public void testToServerConfigMinimalEndpointShouldHaveEmptyAttributes() {
    // given
    testComponent
        .setEndpoints(singletonList(new EndpointImpl(TEST_ENDPOINT_NAME, 123, emptyMap())));

    // when
    Map<String, ServerConfigImpl> serverConfigs =
        componentEndpointExtractor.extractServerConfigsFromComponentEndpoints(testComponent);

    // then
    assertTrue(serverConfigs.containsKey(TEST_ENDPOINT_NAME));
    assertEquals(serverConfigs.size(), 1);
    ServerConfig serverConfig = serverConfigs.get(TEST_ENDPOINT_NAME);
    assertTrue(serverConfig.getAttributes().isEmpty());
  }

  @Test
  public void testToServerConfigCustomAttributesShouldPreserveInAttributes() {
    // given
    Map<String, String> customAttributes = ImmutableMap.of("k1", "v1", "k2", "v2");
    testComponent
        .setEndpoints(singletonList(new EndpointImpl(TEST_ENDPOINT_NAME, 123, customAttributes)));

    // when
    Map<String, ServerConfigImpl> serverConfigs =
        componentEndpointExtractor.extractServerConfigsFromComponentEndpoints(testComponent);

    // then
    assertTrue(serverConfigs.containsKey(TEST_ENDPOINT_NAME));
    assertEquals(serverConfigs.size(), 1);
    ServerConfig serverConfig = serverConfigs.get(TEST_ENDPOINT_NAME);
    assertEquals(serverConfig.getAttributes().get("k1"), "v1");
    assertEquals(serverConfig.getAttributes().get("k2"), "v2");
    assertEquals(serverConfig.getAttributes().size(), 2);
  }

  @Test
  public void testToServerConfigTranslatePath() {
    // given
    testComponent.setEndpoints(
        singletonList(new EndpointImpl(TEST_ENDPOINT_NAME, 123, singletonMap("path", "hello"))));

    // when
    Map<String, ServerConfigImpl> serverConfigs =
        componentEndpointExtractor.extractServerConfigsFromComponentEndpoints(testComponent);

    // then
    assertTrue(serverConfigs.containsKey(TEST_ENDPOINT_NAME));
    assertEquals(serverConfigs.size(), 1);
    ServerConfig serverConfig = serverConfigs.get(TEST_ENDPOINT_NAME);
    assertTrue(serverConfig.getAttributes().isEmpty());
    assertEquals(serverConfig.getPath(), "hello");
  }

  @Test
  public void testToServerConfigTranslateProtocol() {
    // given
    testComponent.setEndpoints(singletonList(
        new EndpointImpl(TEST_ENDPOINT_NAME, 123, singletonMap("protocol", "hello"))));

    // when
    Map<String, ServerConfigImpl> serverConfigs =
        componentEndpointExtractor.extractServerConfigsFromComponentEndpoints(testComponent);

    // then
    assertTrue(serverConfigs.containsKey(TEST_ENDPOINT_NAME));
    assertEquals(serverConfigs.size(), 1);
    ServerConfig serverConfig = serverConfigs.get(TEST_ENDPOINT_NAME);
    assertTrue(serverConfig.getAttributes().isEmpty());
    assertEquals(serverConfig.getProtocol(), "hello");
  }

  @Test
  public void testToServerConfigTranslatePublicTrue() {
    // given
    testComponent.setEndpoints(
        singletonList(new EndpointImpl(TEST_ENDPOINT_NAME, 123, singletonMap("public", "true"))));

    // when
    Map<String, ServerConfigImpl> serverConfigs =
        componentEndpointExtractor.extractServerConfigsFromComponentEndpoints(testComponent);

    // then
    assertTrue(serverConfigs.containsKey(TEST_ENDPOINT_NAME));
    assertEquals(serverConfigs.size(), 1);
    ServerConfig serverConfig = serverConfigs.get(TEST_ENDPOINT_NAME);
    assertTrue(serverConfig.getAttributes().isEmpty());
  }

  @Test
  public void testToServerConfigTranslatePublicWhatever() {
    // given
    testComponent.setEndpoints(singletonList(
        new EndpointImpl(TEST_ENDPOINT_NAME, 123, singletonMap("public", "whatever"))));

    // when
    Map<String, ServerConfigImpl> serverConfigs =
        componentEndpointExtractor.extractServerConfigsFromComponentEndpoints(testComponent);

    // then
    assertTrue(serverConfigs.containsKey(TEST_ENDPOINT_NAME));
    assertEquals(serverConfigs.size(), 1);
    ServerConfig serverConfig = serverConfigs.get(TEST_ENDPOINT_NAME);
    assertTrue(serverConfig.getAttributes().isEmpty());
  }

  @Test
  public void testToServerConfigTranslatePublicFalse() {
    // given
    testComponent.setEndpoints(
        singletonList(new EndpointImpl(TEST_ENDPOINT_NAME, 123, singletonMap("public", "false"))));

    // when
    Map<String, ServerConfigImpl> serverConfigs =
        componentEndpointExtractor.extractServerConfigsFromComponentEndpoints(testComponent);

    // then
    assertTrue(serverConfigs.containsKey(TEST_ENDPOINT_NAME));
    assertEquals(serverConfigs.size(), 1);
    ServerConfig serverConfig = serverConfigs.get(TEST_ENDPOINT_NAME);
    assertFalse(serverConfig.getAttributes().isEmpty());
    assertEquals(
        serverConfig.getAttributes().get(INTERNAL_SERVER_ATTRIBUTE), Boolean.TRUE.toString());
  }

  @Test
  public void testCreateService() {
    // given
    testComponent.setEndpoints(singletonList(new EndpointImpl(TEST_ENDPOINT_NAME, 1234,
        singletonMap(DISCOVERABLE_ENDPOINT_ATTRIBUTE, Boolean.TRUE.toString()))));

    // when
    List<Service> services = componentEndpointExtractor
        .extractServicesFromComponentEndpoints(testComponent);

    // then
    assertEquals(services.size(), 1);
    Service service = services.get(0);

    assertEquals(service.getMetadata().getName(), TEST_ENDPOINT_NAME);
    assertEquals(service.getSpec().getSelector().get(CHE_COMPONENT_NAME_LABEL), TEST_COMPONENT_ALIAS);
    List<ServicePort> ports = service.getSpec().getPorts();
    assertEquals(ports.size(), 1);
    assertEquals(ports.get(0).getPort().intValue(), 1234);
    assertEquals(ports.get(0).getTargetPort().getIntVal().intValue(), 1234);
    assertEquals(ports.get(0).getProtocol(), "TCP");
  }
}
