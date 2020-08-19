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
package org.eclipse.che.workspace.infrastructure.kubernetes.server.external;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class TraefikGatewayRouteConfigGeneratorTest {

  private final GatewayRouteConfigGenerator gatewayConfigGenerator =
      new TraefikGatewayRouteConfigGenerator();

//  @Test
//  public void testGenerateGatewayConfig() throws InfrastructureException {
//    String expectedConfig =
//        "http:\n"
//            + "  routers:\n"
//            + "    external-server-1:\n"
//            + "      rule: \"PathPrefix(`/blabol-cesta`)\"\n"
//            + "      service: \"external-server-1\"\n"
//            + "      middlewares:\n"
//            + "      - \"external-server-1\"\n"
//            + "      - \"external-server-1_headers\"\n"
//            + "      priority: 100\n"
//            + "  services:\n"
//            + "    external-server-1:\n"
//            + "      loadBalancer:\n"
//            + "        servers:\n"
//            + "        - url: \"http://service-url.che-namespace.svc.cluster.local:1234\"\n"
//            + "  middlewares:\n"
//            + "    external-server-1:\n"
//            + "      stripPrefix:\n"
//            + "        prefixes:\n"
//            + "        - \"/blabol-cesta\"\n"
//            + "    external-server-1_headers:\n"
//            + "      headers:\n"
//            + "        customRequestHeaders:\n"
//            + "          X-Forwarded-Proto: \"http\"";
//
//    GatewayRouteConfig routeConfig =
//        new GatewayRouteConfig(
//            "external-server-1",
//            "service-url",
//            "1234",
//            "/blabol-cesta",
//            "http",
//            Collections.emptyMap());
//    gatewayConfigGenerator.addRouteConfig(routeConfig);
//    Map<String, String> generatedConfig = gatewayConfigGenerator.generate("che-namespace");
//
//    assertTrue(generatedConfig.containsKey("external-server-1.yml"));
//    assertEquals(generatedConfig.get("external-server-1.yml"), expectedConfig);
//  }
//
//  @Test
//  public void testMultipleRouteConfigsAreGeneratedAsMultipleMapEntries()
//      throws InfrastructureException {
//    GatewayRouteConfig c1 = new GatewayRouteConfig("c1", "", "", "", "", Collections.emptyMap());
//    GatewayRouteConfig c2 = new GatewayRouteConfig("c2", "", "", "", "", Collections.emptyMap());
//    gatewayConfigGenerator.addRouteConfig(c1);
//    gatewayConfigGenerator.addRouteConfig(c2);
//    Map<String, String> generatedConfig = gatewayConfigGenerator.generate("che-namespace");
//
//    assertTrue(generatedConfig.containsKey("c1.yml"));
//    assertTrue(generatedConfig.containsKey("c2.yml"));
//  }
}
