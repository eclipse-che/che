package org.eclipse.che.workspace.infrastructure.kubernetes.server.external;

import javax.inject.Singleton;

@Singleton
public class GatewayRouteConfigGeneratorFactory {

  public GatewayRouteConfigGenerator create(String namespace) {
    return new TraefikGatewayRouteConfigGenerator(namespace);
  }
}
