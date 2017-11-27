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

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerPort;
import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.openshift.api.model.Route;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.eclipse.che.api.core.model.workspace.runtime.ServerStatus;
import org.eclipse.che.api.workspace.server.model.impl.ServerImpl;

/**
 * Helps to resolve {@link ServerImpl servers} by container in pod according to specified {@link
 * Route routes} and {@link Service services}.
 *
 * <p>How {@link Container}, {@link Pod}, {@link Service} and {@link Route} are linked described in
 * {@link ServerExposer}.
 *
 * @author Sergii Leshchenko
 * @see ServerExposer
 */
public class ServerResolver {
  private final List<Service> services;
  private final List<Route> routes;

  private ServerResolver(List<Service> services, List<Route> routes) {
    this.services = services;
    this.routes = routes;
  }

  public static ServerResolver of(List<Service> services, List<Route> routes) {
    return new ServerResolver(services, routes);
  }

  /**
   * Resolves servers by the specified container in the pod.
   *
   * @param pod pod that should be matched by services
   * @param container container that expose ports for services
   * @return resolved servers
   */
  public Map<String, ServerImpl> resolve(Pod pod, Container container) {
    Set<String> matchedServices =
        getMatchedServices(pod, container)
            .stream()
            .map(s -> s.getMetadata().getName())
            .collect(Collectors.toSet());
    Map<String, ServerImpl> servers = new HashMap<>();
    for (Route route : routes) {
      if (matchedServices.contains(route.getSpec().getTo().getName())) {
        RoutesAnnotations.newDeserializer(route.getMetadata().getAnnotations())
            .servers()
            .forEach(
                (name, config) ->
                    servers.put(
                        name,
                        newServer(
                            config.getProtocol(), route.getSpec().getHost(), config.getPath())));
      }
    }
    return servers;
  }

  private ServerImpl newServer(String protocol, String host, String path) {
    StringBuilder ub = new StringBuilder();
    if (protocol != null) {
      ub.append(protocol).append("://");
    } else {
      ub.append("tcp://");
    }
    ub.append(host);
    if (path != null) {
      if (!path.isEmpty() && !path.startsWith("/")) {
        ub.append("/");
      }
      ub.append(path);
    }
    return new ServerImpl().withUrl(ub.toString()).withStatus(ServerStatus.UNKNOWN);
  }

  private List<Service> getMatchedServices(Pod pod, Container container) {
    return services
        .stream()
        .filter(service -> isExposedByService(pod, service))
        .filter(service -> isExposedByService(container, service))
        .collect(Collectors.toList());
  }

  private boolean isExposedByService(Pod pod, Service service) {
    Map<String, String> labels = pod.getMetadata().getLabels();
    Map<String, String> selectorLabels = service.getSpec().getSelector();
    if (labels == null) {
      return false;
    }
    for (Map.Entry<String, String> selectorLabelEntry : selectorLabels.entrySet()) {
      if (!selectorLabelEntry.getValue().equals(labels.get(selectorLabelEntry.getKey()))) {
        return false;
      }
    }
    return true;
  }

  private boolean isExposedByService(Container container, Service service) {
    for (ServicePort servicePort : service.getSpec().getPorts()) {
      IntOrString targetPort = servicePort.getTargetPort();
      if (targetPort.getIntVal() != null) {
        for (ContainerPort containerPort : container.getPorts()) {
          if (targetPort.getIntVal().equals(containerPort.getContainerPort())) {
            return true;
          }
        }
      } else {
        for (ContainerPort containerPort : container.getPorts()) {
          if (targetPort.getStrVal().equals(containerPort.getName())) {
            return true;
          }
        }
      }
    }
    return false;
  }
}
