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
package org.eclipse.che.workspace.infrastructure.kubernetes.server;

import static com.google.common.base.Strings.isNullOrEmpty;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.extensions.Ingress;
import io.fabric8.kubernetes.api.model.extensions.IngressRule;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.che.api.workspace.server.model.impl.ServerImpl;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.workspace.infrastructure.kubernetes.Annotations;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.external.IngressPathTransformInverter;

/**
 * Helps to resolve {@link ServerImpl servers} by machine name according to specified {@link Ingress
 * ingresses} and {@link Service services}.
 *
 * <p>Objects annotations are used to check if {@link Service service} or {@link Ingress ingress}
 * exposes the specified machine servers.
 *
 * @author Sergii Leshchenko
 * @author Alexander Garagatyi
 * @see KubernetesServerExposer
 * @see Annotations
 */
public class KubernetesServerResolver {
  private final Multimap<String, Service> services;
  private final Multimap<String, Ingress> ingresses;
  private IngressPathTransformInverter pathTransformInverter;

  public KubernetesServerResolver(
      IngressPathTransformInverter pathTransformInverter,
      List<Service> services,
      List<Ingress> ingresses) {
    this.pathTransformInverter = pathTransformInverter;
    this.services = ArrayListMultimap.create();
    for (Service service : services) {
      String machineName =
          Annotations.newDeserializer(service.getMetadata().getAnnotations()).machineName();
      this.services.put(machineName, service);
    }

    this.ingresses = ArrayListMultimap.create();
    for (Ingress ingress : ingresses) {
      String machineName =
          Annotations.newDeserializer(ingress.getMetadata().getAnnotations()).machineName();
      this.ingresses.put(machineName, ingress);
    }
  }

  /**
   * Resolves servers by the specified machine name.
   *
   * @param machineName machine to resolve servers
   * @return resolved servers
   */
  public Map<String, ServerImpl> resolve(String machineName) {
    Map<String, ServerImpl> servers = new HashMap<>();
    fillInternalServers(machineName, servers);
    fillExternalServers(machineName, servers);
    return servers;
  }

  private void fillInternalServers(String machineName, Map<String, ServerImpl> servers) {
    services.get(machineName).forEach(service -> fillServiceServers(service, servers));
  }

  protected void fillExternalServers(String machineName, Map<String, ServerImpl> servers) {
    ingresses.get(machineName).forEach(ingress -> fillIngressServers(ingress, servers));
  }

  private void fillServiceServers(Service service, Map<String, ServerImpl> servers) {
    Annotations.newDeserializer(service.getMetadata().getAnnotations())
        .servers()
        .forEach(
            (name, config) ->
                servers.put(
                    name,
                    new RuntimeServerBuilder()
                        .protocol(config.getProtocol())
                        .host(service.getMetadata().getName())
                        .port(config.getPort())
                        .path(config.getPath())
                        .attributes(config.getAttributes())
                        .targetPort(config.getPort())
                        .build()));
  }

  private void fillIngressServers(Ingress ingress, Map<String, ServerImpl> servers) {
    IngressRule ingressRule = ingress.getSpec().getRules().get(0);

    // host either set by rule, or determined by LB ip
    final String host =
        ingressRule.getHost() != null
            ? ingressRule.getHost()
            : ingress.getStatus().getLoadBalancer().getIngress().get(0).getIp();

    Annotations.newDeserializer(ingress.getMetadata().getAnnotations())
        .servers()
        .forEach(
            (name, config) -> {
              String path =
                  buildPath(
                      pathTransformInverter.undoPathTransformation(
                          ingressRule.getHttp().getPaths().get(0).getPath()),
                      config.getPath());
              servers.put(
                  name,
                  new RuntimeServerBuilder()
                      .protocol(config.getProtocol())
                      .host(host)
                      .path(path)
                      .attributes(config.getAttributes())
                      .targetPort(config.getPort())
                      .build());
            });
  }

  private String buildPath(String fragment1, @Nullable String fragment2) {
    StringBuilder sb = new StringBuilder(fragment1);

    if (!isNullOrEmpty(fragment2)) {
      if (!fragment1.endsWith("/")) {
        sb.append('/');
      }

      if (fragment2.startsWith("/")) {
        sb.append(fragment2.substring(1));
      } else {
        sb.append(fragment2);
      }
    }

    // always end server URLs with a slash, so that they can be safely sub-path'd..
    if (sb.charAt(sb.length() - 1) != '/') {
      sb.append('/');
    }

    return sb.toString();
  }
}
