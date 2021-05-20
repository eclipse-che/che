/*
 * Copyright (c) 2012-2020 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.openshift.server;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.openshift.api.model.Route;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.eclipse.che.api.workspace.server.model.impl.ServerImpl;
import org.eclipse.che.workspace.infrastructure.kubernetes.Annotations;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.RuntimeServerBuilder;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.resolver.AbstractServerResolver;

/**
 * Helps to resolve {@link ServerImpl servers} by machine name according to specified {@link Route
 * routes} and {@link Service services}.
 *
 * <p>Objects annotations are used to check if {@link Service service} or {@link Route route}
 * exposes the specified machine servers.
 *
 * @author Sergii Leshchenko
 * @author Alexander Garagatyi
 * @see Annotations
 */
public class RouteServerResolver extends AbstractServerResolver {

  private final Multimap<String, Route> routes;

  public RouteServerResolver(List<Service> services, List<Route> routes) {
    super(services);

    this.routes = ArrayListMultimap.create();
    for (Route route : routes) {
      String machineName =
          Annotations.newDeserializer(route.getMetadata().getAnnotations()).machineName();
      this.routes.put(machineName, route);
    }
  }

  @Override
  public Map<String, ServerImpl> resolveExternalServers(String machineName) {
    return routes
        .get(machineName)
        .stream()
        .map(r -> resolveRouteServers(machineName, r))
        .flatMap(m -> m.entrySet().stream())
        .collect(Collectors.toMap(Entry::getKey, Entry::getValue, (v1, v2) -> v2));
  }

  private Map<String, ServerImpl> resolveRouteServers(String serviceName, Route route) {
    return Annotations.newDeserializer(route.getMetadata().getAnnotations())
        .servers()
        .entrySet()
        .stream()
        .collect(
            Collectors.toMap(
                Entry::getKey,
                e ->
                    new RuntimeServerBuilder()
                        .protocol(e.getValue().getProtocol())
                        .host(route.getSpec().getHost())
                        .path(e.getValue().getPath())
                        .endpointOrigin(
                            "/") // routes are always multihost, so we don't need to think here...
                        .attributes(e.getValue().getAttributes())
                        .targetPort(e.getValue().getPort())
                        .build(),
                (s1, s2) -> s2));
  }
}
