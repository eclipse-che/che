package org.eclipse.che.workspace.infrastructure.kubernetes.server.external;

import static org.testng.Assert.*;

import org.testng.annotations.Test;


public class TraefikGatewayConfigGeneratorTest {
  private final GatewayRouteConfigGenerator gatewayConfigGenerator = new TraefikGatewayRouteConfigGenerator();

  @Test
  public void testGenerateGatewayConfig() {
    String expectedConfig = "http:\n"
        + "  routers:\n"
        + "    external-server-1:\n"
        + "      rule: \"PathPrefix(`/blabol-cesta`)\"\n"
        + "      service: external-server-1\n"
        + "      middlewares:\n"
        + "      - \"external-server-1\"\n"
        + "      priority: 100\n"
        + "  services:\n"
        + "    external-server-1:\n"
        + "      loadBalancer:\n"
        + "        servers:\n"
        + "        - url: 'http://service-url:1234'\n"
        + "  middlewares:\n"
        + "    external-server-1:\n"
        + "      stripPrefix:\n"
        + "        prefixes:\n"
        + "        - \"/blabol-cesta\"";

    String generatedConfig = gatewayConfigGenerator
        .generate("external-server-1", "http://service-url:1234", "/blabol-cesta");
    System.out.println(generatedConfig);

    assertEquals(generatedConfig, expectedConfig);
  }
}
