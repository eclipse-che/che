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

import static org.eclipse.che.api.core.model.workspace.config.ServerConfig.SERVICE_NAME_ATTRIBUTE;
import static org.eclipse.che.api.core.model.workspace.config.ServerConfig.SERVICE_PORT_ATTRIBUTE;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.workspace.server.model.impl.ServerConfigImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.Annotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TraefikGatewayRouteConfigGeneratorTest {

  private GatewayRouteConfigGenerator gatewayConfigGenerator;

  @BeforeMethod
  public void setUp() {
    gatewayConfigGenerator = new TraefikGatewayRouteConfigGenerator();
  }

  @Test
  public void testGenerateGatewayConfig() throws InfrastructureException {
    String expectedConfig =
        "http:\n"
            + "  routers:\n"
            + "    external-server-1:\n"
            + "      rule: \"PathPrefix(`/blabol-cesta`)\"\n"
            + "      service: \"external-server-1\"\n"
            + "      middlewares:\n"
            + "      - \"external-server-1\"\n"
            + "      priority: 100\n"
            + "  services:\n"
            + "    external-server-1:\n"
            + "      loadBalancer:\n"
            + "        servers:\n"
            + "        - url: \"http://service-url.che-namespace.svc.cluster.local:1234\"\n"
            + "  middlewares:\n"
            + "    external-server-1:\n"
            + "      stripPrefix:\n"
            + "        prefixes:\n"
            + "        - \"/blabol-cesta\"";

    ServerConfigImpl serverConfig =
        new ServerConfigImpl(
            "123",
            "https",
            "/",
            ImmutableMap.of(
                SERVICE_NAME_ATTRIBUTE,
                "service-url",
                SERVICE_PORT_ATTRIBUTE,
                "1234",
                ServerConfig.ENDPOINT_ORIGIN,
                "/blabol-cesta"));
    Map<String, String> annotations =
        new Annotations.Serializer().server("s1", serverConfig).annotations();
    ConfigMap routeConfig =
        new ConfigMapBuilder()
            .withNewMetadata()
            .withName("route")
            .withAnnotations(annotations)
            .endMetadata()
            .build();

    gatewayConfigGenerator.addRouteConfig("external-server-1", routeConfig);
    Map<String, String> generatedConfig = gatewayConfigGenerator.generate("che-namespace");

    assertTrue(generatedConfig.containsKey("external-server-1.yml"));
    assertEquals(generatedConfig.get("external-server-1.yml"), expectedConfig);
  }

  @Test
  public void testMultipleRouteConfigsAreGeneratedAsMultipleMapEntries()
      throws InfrastructureException {
    ServerConfigImpl serverConfig =
        new ServerConfigImpl(
            "123",
            "https",
            "/",
            ImmutableMap.of(
                SERVICE_NAME_ATTRIBUTE,
                "service-url",
                SERVICE_PORT_ATTRIBUTE,
                "1234",
                ServerConfig.ENDPOINT_ORIGIN,
                "/blabol-cesta"));
    Map<String, String> annotations =
        new Annotations.Serializer().server("s1", serverConfig).annotations();
    ConfigMap routeConfig =
        new ConfigMapBuilder()
            .withNewMetadata()
            .withName("route")
            .withAnnotations(annotations)
            .endMetadata()
            .build();
    gatewayConfigGenerator.addRouteConfig("c1", routeConfig);
    gatewayConfigGenerator.addRouteConfig("c2", routeConfig);
    Map<String, String> generatedConfig = gatewayConfigGenerator.generate("che-namespace");

    assertTrue(generatedConfig.containsKey("c1.yml"));
    assertTrue(generatedConfig.containsKey("c2.yml"));
  }

  @Test(expectedExceptions = InfrastructureException.class)
  public void failWhenMultipleServersInConfigmapAnnotations() throws InfrastructureException {
    ServerConfigImpl serverConfig =
        new ServerConfigImpl(
            "123",
            "https",
            "/",
            ImmutableMap.of(
                SERVICE_NAME_ATTRIBUTE,
                "service-url",
                SERVICE_PORT_ATTRIBUTE,
                "1234",
                ServerConfig.ENDPOINT_ORIGIN,
                "/blabol-cesta"));
    Map<String, String> annotations =
        new Annotations.Serializer()
            .server("s1", serverConfig)
            .server("s2", serverConfig)
            .annotations();
    ConfigMap routeConfig =
        new ConfigMapBuilder()
            .withNewMetadata()
            .withName("route")
            .withAnnotations(annotations)
            .endMetadata()
            .build();
    gatewayConfigGenerator.addRouteConfig("c1", routeConfig);

    gatewayConfigGenerator.generate("che-namespace");
  }
}
