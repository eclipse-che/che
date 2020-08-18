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

import static com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature.WRITE_DOC_START_MARKER;

import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.environment.GatewayRouteConfig;

/** Config generator for Traefik Gateway */
public class TraefikGatewayRouteConfigGenerator implements GatewayRouteConfigGenerator {

  private final List<GatewayRouteConfig> routeConfigs = new ArrayList<>();

  private final String serviceNamespace;

  public TraefikGatewayRouteConfigGenerator(String serviceNamespace) {
    this.serviceNamespace = serviceNamespace;
  }

  @Override
  public void addRouteConfig(GatewayRouteConfig routeConfig) {
    this.routeConfigs.add(routeConfig);
  }

  /**
   * Generate Traefik configuration for all added {@link GatewayRouteConfig}s.
   *
   * <p>Each {@link GatewayRouteConfig} is translated into Traefik configuration under extra key in
   * returned {@link Map} `{GatewayRouteConfig#name}.yml`.
   *
   * <p>Content of single service configuration looks like this:
   *
   * <pre>
   * http:
   *   routers:
   *     {name}:
   *       rule: "PathPrefix(`{GatewayRouteConfig#routePath}`)"
   *       service: {GatewayRouteConfig#name}
   *       middlewares:
   *       - "{GatewayRouteConfig#name}"
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
   *         - "{GatewayRouteConfig#routePath}"
   * </pre>
   */
  @Override
  public Map<String, String> generate() throws InfrastructureException {
    Map<String, String> cmData = new HashMap<>();
    for (GatewayRouteConfig routeConfig : routeConfigs) {
      String traefikRouteConfig =
          generate(
              routeConfig.getName(),
              createServiceUrl(routeConfig.getServiceName(), routeConfig.getServicePort()),
              routeConfig.getRoutePath(),
              routeConfig.getProtocol());
      cmData.put(routeConfig.getName() + ".yml", traefikRouteConfig);
    }
    return cmData;
  }

  /**
   * Generates Traefik specific configuration for single service.
   *
   * @param name name of the service
   * @param serviceUrl url of service we want to route to
   * @param path path to route and strip
   * @return traefik service route config
   */
  private String generate(String name, String serviceUrl, String path, String protocol)
      throws InfrastructureException {
    StringWriter sw = new StringWriter();
    try {
      YAMLGenerator generator =
          YAMLFactory.builder().disable(WRITE_DOC_START_MARKER).build().createGenerator(sw);

      generator.writeStartObject();
      generator.writeFieldName("http");
      generator.writeStartObject();

      generator.writeFieldName("routers");
      generateRouters(generator, name, path);

      generator.writeFieldName("services");
      generateServices(generator, name, serviceUrl);

      generator.writeFieldName("middlewares");
      generateMiddlewares(generator, name, path, protocol);

      generator.flush();

      return sw.toString();
    } catch (IOException e) {
      throw new InfrastructureException(e);
    }
  }

  /**
   * generates Routers part of Traefik config
   *
   * <pre>
   * {name}:
   *   rule: "PathPrefix(`{path}`)"
   *   service: "{name}"
   *   middlewares:
   *   - "{name}"
   *   priority: 100
   * </pre>
   */
  private void generateRouters(YAMLGenerator generator, String name, String path)
      throws IOException {
    generator.writeStartObject();
    generator.writeFieldName(name);
    generator.writeStartObject();
    generator.writeFieldName("rule");
    generator.writeString("PathPrefix(`" + path + "`)");
    generator.writeFieldName("service");
    generator.writeString(name);
    generator.writeFieldName("middlewares");
    generator.writeStartArray();
    generator.writeString(name);
    generator.writeString(name + "_headers");
    generator.writeEndArray();
    generator.writeFieldName("priority");
    generator.writeNumber(100);
    generator.writeEndObject();
    generator.writeEndObject();
  }

  /**
   * generates Services part of Traefik config
   *
   * <pre>
   * {name}:
   *   loadBalancer:
   *     servers:
   *     - url: "{serviceUrl}"
   * </pre>
   */
  private void generateServices(YAMLGenerator generator, String name, String serviceUrl)
      throws IOException {
    generator.writeStartObject();
    generator.writeFieldName(name);
    generator.writeStartObject();
    generator.writeFieldName("loadBalancer");
    generator.writeStartObject();
    generator.writeFieldName("servers");
    generator.writeStartArray();
    generator.writeStartObject();
    generator.writeFieldName("url");
    generator.writeString(serviceUrl);
    generator.writeEndObject();
    generator.writeEndArray();
    generator.writeEndObject();
    generator.writeEndObject();
    generator.writeEndObject();
  }

  /**
   * generates Middlewares part of Traefik config
   *
   * <pre>
   * {name}:
   *   stripPrefix:
   *     prefixes:
   *     - "{path}"
   * </pre>
   */
  private void generateMiddlewares(
      YAMLGenerator generator, String name, String path, String protocol) throws IOException {
    generator.writeStartObject();
    generator.writeFieldName(name);
    generator.writeStartObject();
    generator.writeFieldName("stripPrefix");
    generator.writeStartObject();
    generator.writeFieldName("prefixes");
    generator.writeStartArray();
    generator.writeString(path);
    generator.writeEndArray();
    generator.writeEndObject();
    generator.writeEndObject();

    generator.writeFieldName(name + "_headers");
    generator.writeStartObject();
    generator.writeFieldName("headers");
    generator.writeStartObject();
    generator.writeFieldName("customRequestHeaders");
    generator.writeStartObject();
    generator.writeFieldName("X-Forwarded-Proto");
    generator.writeString(protocol);
    generator.writeEndObject();
    generator.writeEndObject();
    generator.writeEndObject();

    generator.writeEndObject();
  }

  private String createServiceUrl(String serviceName, String servicePort) {
    return String.format(
        "http://%s.%s.svc.cluster.local:%s", serviceName, serviceNamespace, servicePort);
  }
}
