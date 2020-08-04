package org.eclipse.che.workspace.infrastructure.kubernetes.server.external;

/**
 * Traefik configuration for single route looks like this (values in {} are parameters of {@link
 * GatewayRouteConfigGenerator#generate(String, String, String)} method):
 *
 * <pre>
 * http:
 *   routers:
 *     {name}:
 *       rule: "PathPrefix(`{path}`)"
 *       service: {name}
 *       middlewares:
 *       - "{name}"
 *       priority: 100
 *   services:
 *     {name}:
 *       loadBalancer:
 *         servers:
 *         - url: '{serviceUrl}'
 *   middlewares:
 *     {name}:
 *       stripPrefix:
 *         prefixes:
 *         - "{path}"
 * </pre>
 */
public class TraefikGatewayRouteConfigGenerator implements GatewayRouteConfigGenerator {

  /**
   * Generates Traefik specific configuration for single service.
   *
   * @param name       name of the service
   * @param serviceUrl url of service we want to route to
   * @param path       path to route and strip
   * @return traefik service route config
   */
  @Override
  public String generate(String name, String serviceUrl, String path) {
    return "http:"
        + "\n"
        + "  routers:"
        + "\n"
        + "    "
        + name
        + ":"
        + "\n"
        + "      rule: \"PathPrefix(`"
        + path
        + "`)\""
        + "\n"
        + "      service: "
        + name
        + "\n"
        + "      middlewares:"
        + "\n"
        + "      - \""
        + name
        + "\""
        + "\n"
        + "      priority: 100"
        + "\n"
        + "  services:"
        + "\n"
        + "    "
        + name
        + ":"
        + "\n"
        + "      loadBalancer:"
        + "\n"
        + "        servers:"
        + "\n"
        + "        - url: '"
        + serviceUrl
        + "'"
        + "\n"
        + "  middlewares:"
        + "\n"
        + "    "
        + name
        + ":"
        + "\n"
        + "      stripPrefix:"
        + "\n"
        + "        prefixes:"
        + "\n"
        + "        - \""
        + path
        + "\"";
  }
}
