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
/// *
// * Copyright (c) 2012-2018 Red Hat, Inc.
// * This program and the accompanying materials are made
// * available under the terms of the Eclipse Public License 2.0
// * which is available at https://www.eclipse.org/legal/epl-2.0/
// *
// * SPDX-License-Identifier: EPL-2.0
// *
// * Contributors:
// *   Red Hat, Inc. - initial API and implementation
// */
// package org.eclipse.che.workspace.infrastructure.kubernetes.server.external;
//
// import static org.testng.Assert.*;
//
// import java.util.Map;
// import org.testng.annotations.Test;
//
// public class TraefikGatewayConfigGeneratorTest {
//  private final GatewayRouteConfigGenerator gatewayConfigGenerator =
//      new TraefikGatewayRouteConfigGenerator();
//
//  @Test
//  public void testGenerateGatewayConfig() {
//    String expectedConfig =
//        "http:\n"
//            + "  routers:\n"
//            + "    external-server-1:\n"
//            + "      rule: \"PathPrefix(`/blabol-cesta`)\"\n"
//            + "      service: \"external-server-1\"\n"
//            + "      middlewares:\n"
//            + "      - \"external-server-1\"\n"
//            + "      priority: 100\n"
//            + "  services:\n"
//            + "    external-server-1:\n"
//            + "      loadBalancer:\n"
//            + "        servers:\n"
//            + "        - url: \"http://service-url:1234\"\n"
//            + "  middlewares:\n"
//            + "    external-server-1:\n"
//            + "      stripPrefix:\n"
//            + "        prefixes:\n"
//            + "        - \"/blabol-cesta\"";
//
//    Map<String, String> generatedConfig =
//        gatewayConfigGenerator.generate(
//            "external-server-1", "http://service-url:1234", "/blabol-cesta");
//
//    assertTrue(generatedConfig.containsKey("external-server-1.yml"));
//    assertEquals(generatedConfig.get("external-server-1.yml"), expectedConfig);
//  }
// }
