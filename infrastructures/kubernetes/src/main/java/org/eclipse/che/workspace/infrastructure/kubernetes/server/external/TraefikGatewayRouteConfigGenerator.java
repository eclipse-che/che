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
import java.util.Collections;
import java.util.Map;

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
   * @param name name of the service
   * @param serviceUrl url of service we want to route to
   * @param path path to route and strip
   * @return traefik service route config
   */
  @Override
  public Map<String, String> generate(String name, String serviceUrl, String path) {
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

      generator.flush();

      return Collections.singletonMap(name + ".yml", sw.toString());
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
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
}
