/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.openshift;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.openshift.api.model.Route;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.core.model.workspace.runtime.ServerStatus;
import org.eclipse.che.api.workspace.server.model.impl.ServerImpl;

/**
 * Helps to resolve {@link ServerImpl servers} by machine name according to specified {@link Route
 * routes} and {@link Service services}.
 *
 * <p>Objects annotations are used to check if {@link Service service} or {@link Route route}
 * exposes the specified machine servers.
 *
 * @author Sergii Leshchenko
 * @author Alexander Garagatyi
 * @see ServerExposer
 * @see Annotations
 */
public class ServerResolver {
  private final Multimap<String, Service> services;
  private final Multimap<String, Route> routes;

  private ServerResolver(List<Service> services, List<Route> routes) {
    this.services = ArrayListMultimap.create();
    for (Service service : services) {
      String machineName =
          Annotations.newDeserializer(service.getMetadata().getAnnotations()).machineName();
      this.services.put(machineName, service);
    }

    this.routes = ArrayListMultimap.create();
    for (Route route : routes) {
      String machineName =
          Annotations.newDeserializer(route.getMetadata().getAnnotations()).machineName();
      this.routes.put(machineName, route);
    }
  }

  public static ServerResolver of(List<Service> services, List<Route> routes) {
    return new ServerResolver(services, routes);
  }

  /**
   * Resolves servers by the specified machine name.
   *
   * @param machineName machine to resolve servers
   * @return resolved servers
   */
  public Map<String, ServerImpl> resolve(String machineName) {
    Map<String, ServerImpl> servers = new HashMap<>();
    services.get(machineName).forEach(service -> fillServiceServers(service, servers));
    routes.get(machineName).forEach(route -> fillRouteServers(route, servers));
    return servers;
  }

  private void fillServiceServers(Service service, Map<String, ServerImpl> servers) {
    Annotations.newDeserializer(service.getMetadata().getAnnotations())
        .servers()
        .forEach(
            (name, config) ->
                servers.put(
                    name,
                    newServer(
                        config.getProtocol(),
                        service.getMetadata().getName(),
                        config.getPort(),
                        config.getPath(),
                        config.getAttributes())));
  }

  private void fillRouteServers(Route route, Map<String, ServerImpl> servers) {
    Annotations.newDeserializer(route.getMetadata().getAnnotations())
        .servers()
        .forEach(
            (name, config) ->
                servers.put(
                    name,
                    newServer(
                        config.getProtocol(),
                        route.getSpec().getHost(),
                        null,
                        config.getPath(),
                        config.getAttributes())));
  }

  /** Constructs {@link ServerImpl} instance from provided parameters. */
  private ServerImpl newServer(
      String protocol, String host, String port, String path, Map<String, String> attributes) {
    StringBuilder ub = new StringBuilder();
    if (protocol != null) {
      ub.append(protocol).append("://");
    } else {
      ub.append("tcp://");
    }
    ub.append(host);
    if (port != null) {
      ub.append(':').append(removeSuffix(port));
    }
    if (path != null) {
      if (!path.isEmpty() && !path.startsWith("/")) {
        ub.append("/");
      }
      ub.append(path);
    }
    return new ServerImpl()
        .withUrl(ub.toString())
        .withStatus(ServerStatus.UNKNOWN)
        .withAttributes(attributes);
  }

  /** Removes suffix of {@link ServerConfig} such as "/tcp" when port value "8080/tcp". */
  private String removeSuffix(String port) {
    return port.split("/")[0];
  }
}
