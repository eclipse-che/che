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

import static com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature.WRITE_DOC_START_MARKER;
import static org.eclipse.che.api.core.model.workspace.config.ServerConfig.SERVICE_NAME_ATTRIBUTE;
import static org.eclipse.che.api.core.model.workspace.config.ServerConfig.SERVICE_PORT_ATTRIBUTE;

import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import io.fabric8.kubernetes.api.model.ConfigMap;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.eclipse.che.api.workspace.server.model.impl.ServerConfigImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.Annotations;

/**
 * Config generator for Traefik Gateway.
 *
 * <p>Content of single service configuration looks like this:
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
 *         - "{GatewayRouteConfig#routePath}"
 * </pre>
 */
public class TraefikGatewayRouteConfigGenerator implements GatewayRouteConfigGenerator {

  private static final String SERVICE_URL_FORMAT = "http://%s.%s.svc.%s:%s";

  private final String clusterDomain;

  public TraefikGatewayRouteConfigGenerator(String clusterDomain) {
    this.clusterDomain = clusterDomain;
  }

  private final Map<String, ConfigMap> routeConfigs = new HashMap<>();

  @Override
  public void addRouteConfig(String name, ConfigMap routeConfig) {
    this.routeConfigs.put(name, routeConfig);
  }

  /**
   * Generates configuration for all configs added by {@link
   * TraefikGatewayRouteConfigGenerator#addRouteConfig(String, ConfigMap)} so far. It does not
   * change them, so this method can be used repeatedly.
   *
   * <p>Returned {@code Map<String, String>} has keys created from {@code name} parameter of {@link
   * TraefikGatewayRouteConfigGenerator#addRouteConfig(String, ConfigMap)} + '.yml' suffix. Values
   * are full configuration for single gateway route. This map is suppose to be directly used as
   * {@link ConfigMap}'s data.
   *
   * @return map with added routes configurations
   */
  @Override
  public Map<String, String> generate(String namespace) throws InfrastructureException {
    Map<String, String> cmData = new HashMap<>();
    for (Entry<String, ConfigMap> routeConfig : routeConfigs.entrySet()) {
      Map<String, ServerConfigImpl> servers =
          new Annotations.Deserializer(routeConfig.getValue().getMetadata().getAnnotations())
              .servers();
      if (servers.size() != 1) {
        throw new InfrastructureException(
            "Expected exactly 1 server [" + routeConfig.getValue().toString() + "]");
      }
      ServerConfigImpl server = servers.get(servers.keySet().iterator().next());
      String serviceName = server.getAttributes().get(SERVICE_NAME_ATTRIBUTE);
      String servicePort = server.getAttributes().get(SERVICE_PORT_ATTRIBUTE);

      String traefikRouteConfig =
          generate(
              routeConfig.getKey(),
              createServiceUrl(serviceName, servicePort, namespace),
              server.getEndpointOrigin());
      cmData.put(routeConfig.getKey() + ".yml", traefikRouteConfig);
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
  private String generate(String name, String serviceUrl, String path)
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
      generateMiddlewares(generator, name, path);

      generator.writeEndObject();
      generator.writeEndObject();

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
  private void generateMiddlewares(YAMLGenerator generator, String name, String path)
      throws IOException {
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
    generator.writeEndObject();
  }

  private String createServiceUrl(String serviceName, String servicePort, String serviceNamespace) {
    return String.format(
        SERVICE_URL_FORMAT, serviceName, serviceNamespace, clusterDomain, servicePort);
  }
}
