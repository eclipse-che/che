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
package org.eclipse.che.workspace.infrastructure.kubernetes.provision;

import static java.util.Collections.emptyMap;
import static org.eclipse.che.api.core.model.workspace.config.ServerConfig.SERVICE_NAME_ATTRIBUTE;
import static org.eclipse.che.api.core.model.workspace.config.ServerConfig.SERVICE_PORT_ATTRIBUTE;
import static org.eclipse.che.workspace.infrastructure.kubernetes.provision.GatewayRouterProvisioner.GATEWAY_CONFIGMAP_LABELS;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import java.util.Collections;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.model.impl.ServerConfigImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.Annotations;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.external.GatewayRouteConfigGenerator;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.external.GatewayRouteConfigGeneratorFactory;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(MockitoTestNGListener.class)
public class GatewayRouterProvisionerTest {

  private final String NAMESPACE = "nejmspejs";

  @Mock private GatewayRouteConfigGeneratorFactory configGeneratorFactory;
  @Mock private GatewayRouteConfigGenerator gatewayRouteConfigGenerator;
  @Mock private KubernetesEnvironment env;
  @Mock private RuntimeIdentity identity;

  private GatewayRouterProvisioner gatewayRouterProvisioner;
  private final ServerConfigImpl serverConfigWithoutAttributes =
      new ServerConfigImpl("1234", "http", "/hello/there", emptyMap());
  private final ServerConfigImpl serverConfig =
      new ServerConfigImpl(
          "1234",
          "http",
          "/hello/there",
          ImmutableMap.of(SERVICE_NAME_ATTRIBUTE, "serviceName", SERVICE_PORT_ATTRIBUTE, "1111"));

  @BeforeMethod
  public void setUp() {
    lenient().when(configGeneratorFactory.create()).thenReturn(gatewayRouteConfigGenerator);
    lenient().when(identity.getInfrastructureNamespace()).thenReturn(NAMESPACE);

    gatewayRouterProvisioner = new GatewayRouterProvisioner(configGeneratorFactory);
  }

  @Test(expectedExceptions = InfrastructureException.class)
  public void testFailWhenNoServersInConfigmapAnnotations() throws InfrastructureException {
    // given
    ConfigMap gatewayRouteConfigMap =
        new ConfigMapBuilder()
            .withNewMetadata()
            .withName("route")
            .withLabels(GATEWAY_CONFIGMAP_LABELS)
            .endMetadata()
            .build();
    when(env.getConfigMaps()).thenReturn(Collections.singletonMap("route", gatewayRouteConfigMap));

    // when
    gatewayRouterProvisioner.provision(env, identity);

    // then exception
  }

  @Test(expectedExceptions = InfrastructureException.class)
  public void testFailWhenMoreThanOneServerInConfigmapAnnotations() throws InfrastructureException {
    // given
    Map<String, String> annotationsWith2Servers =
        new Annotations.Serializer()
            .server("s1", serverConfigWithoutAttributes)
            .server("s2", serverConfigWithoutAttributes)
            .annotations();

    ConfigMap gatewayRouteConfigMap =
        new ConfigMapBuilder()
            .withNewMetadata()
            .withName("route")
            .withLabels(GATEWAY_CONFIGMAP_LABELS)
            .withAnnotations(annotationsWith2Servers)
            .endMetadata()
            .build();
    when(env.getConfigMaps()).thenReturn(Collections.singletonMap("route", gatewayRouteConfigMap));

    // when
    gatewayRouterProvisioner.provision(env, identity);

    // then exception
  }

  @Test(expectedExceptions = InfrastructureException.class)
  public void testFailWhenServerHasNotAllNeededAttributes() throws InfrastructureException {
    // given
    Map<String, String> annotationsWith2Servers =
        new Annotations.Serializer().server("s1", serverConfigWithoutAttributes).annotations();

    ConfigMap gatewayRouteConfigMap =
        new ConfigMapBuilder()
            .withNewMetadata()
            .withName("route")
            .withLabels(GATEWAY_CONFIGMAP_LABELS)
            .withAnnotations(annotationsWith2Servers)
            .endMetadata()
            .build();
    when(env.getConfigMaps()).thenReturn(Collections.singletonMap("route", gatewayRouteConfigMap));

    // when
    gatewayRouterProvisioner.provision(env, identity);

    // then exception
  }

  @Test
  public void testProvision() throws InfrastructureException {
    // given
    Map<String, String> annotationsWith2Servers =
        new Annotations.Serializer().server("s1", serverConfig).annotations();

    ConfigMap gatewayRouteConfigMap =
        new ConfigMapBuilder()
            .withNewMetadata()
            .withName("route")
            .withLabels(GATEWAY_CONFIGMAP_LABELS)
            .withAnnotations(annotationsWith2Servers)
            .endMetadata()
            .build();
    when(env.getConfigMaps()).thenReturn(Collections.singletonMap("route", gatewayRouteConfigMap));
    Map<String, String> expectedData =
        Collections.singletonMap("data.yml", "this is for sure generated configuration");
    when(gatewayRouteConfigGenerator.generate(NAMESPACE)).thenReturn(expectedData);

    // when
    gatewayRouterProvisioner.provision(env, identity);

    // then
    verify(configGeneratorFactory).create();
    verify(gatewayRouteConfigGenerator).addRouteConfig("route", gatewayRouteConfigMap);
    verify(gatewayRouteConfigGenerator).generate(NAMESPACE);

    Map<String, ServerConfigImpl> serverConfigsAfterProvisioning =
        new Annotations.Deserializer(gatewayRouteConfigMap.getMetadata().getAnnotations())
            .servers();
    assertEquals(serverConfigsAfterProvisioning.size(), 1);
    ServerConfigImpl server =
        serverConfigsAfterProvisioning.get(
            serverConfigsAfterProvisioning.keySet().iterator().next());

    // verify that provisioner removes the internal attributes
    assertFalse(server.getAttributes().containsKey(SERVICE_NAME_ATTRIBUTE));
    assertFalse(server.getAttributes().containsKey(SERVICE_PORT_ATTRIBUTE));

    // verify that provisioner included the data info configmap
    Map<String, String> actualData = gatewayRouteConfigMap.getData();
    assertEquals(actualData, expectedData);
  }

  @Test(dataProvider = "isGatewayConfigData")
  public void testIsGatewayConfig(Map<String, String> labels, boolean isGatewayConfigExpected) {
    ConfigMap cm =
        new ConfigMapBuilder().withNewMetadata().withLabels(labels).endMetadata().build();
    assertEquals(GatewayRouterProvisioner.isGatewayConfig(cm), isGatewayConfigExpected);
  }

  @DataProvider
  public Object[][] isGatewayConfigData() {
    return new Object[][] {
      {GATEWAY_CONFIGMAP_LABELS, true},
      {ImmutableMap.builder().putAll(GATEWAY_CONFIGMAP_LABELS).put("other", "value").build(), true},
      {emptyMap(), false},
      {ImmutableMap.of("one", "two"), false},
      {ImmutableMap.of(), false},
      {ImmutableMap.of("app", "yes", "role", "no"), false},
      {ImmutableMap.of("app", GATEWAY_CONFIGMAP_LABELS.get("app"), "role", "no"), false},
      {ImmutableMap.of("app", "no", "role", GATEWAY_CONFIGMAP_LABELS.get("role")), false},
    };
  }
}
