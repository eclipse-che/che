package org.eclipse.che.workspace.infrastructure.kubernetes.server.external;

/**
 * Generates config for single external server that we want to expose in the Gateway.
 * <p>
 * Implementation provides configuration for specific Gateway technology (e.g., Traefik).
 */
public interface GatewayRouteConfigGenerator {

  /**
   * Generates content of configuration for service, defined by passed parameters, that should be
   * exposed by the Gateway.
   * <p>
   * Implementation must ensure that Gateway configured with returned content will route the
   * requests on {@code path} into {@code serviceUrl}. Also it must strip {@code path} from request
   * url.
   *
   * @param name       name of the service
   * @param serviceUrl url of service we want to route to
   * @param path       path to route and strip
   * @return full content of configuration for the service
   */
  String generate(String name, String serviceUrl, String path);
}
