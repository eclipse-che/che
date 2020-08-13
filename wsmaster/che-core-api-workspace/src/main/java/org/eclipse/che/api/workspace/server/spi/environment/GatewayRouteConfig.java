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
package org.eclipse.che.api.workspace.server.spi.environment;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

public class GatewayRouteConfig {
  private final String name;
  private final String serviceName;
  private final String servicePort;
  private final String routePath;
  private final Map<String, String> annotations;

  public GatewayRouteConfig(
      String name,
      String serviceName,
      String servicePort,
      String routePath,
      Map<String, String> annotations) {
    this.name = name;
    this.serviceName = serviceName;
    this.servicePort = servicePort;
    this.routePath = routePath;
    this.annotations = new HashMap<>(annotations);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", GatewayRouteConfig.class.getSimpleName() + "[", "]")
        .add("name='" + name + "'")
        .add("serviceName='" + serviceName + "'")
        .add("servicePort='" + servicePort + "'")
        .add("routePath='" + routePath + "'")
        .add("annotations=" + annotations)
        .toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GatewayRouteConfig that = (GatewayRouteConfig) o;
    return Objects.equals(name, that.name)
        && Objects.equals(serviceName, that.serviceName)
        && Objects.equals(servicePort, that.servicePort)
        && Objects.equals(routePath, that.routePath)
        && Objects.equals(annotations, that.annotations);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, serviceName, servicePort, routePath, annotations);
  }

  public String getName() {
    return name;
  }

  public String getServiceName() {
    return serviceName;
  }

  public String getRoutePath() {
    return routePath;
  }

  public Map<String, String> getAnnotations() {
    return annotations;
  }

  public String getServicePort() {
    return servicePort;
  }
}
