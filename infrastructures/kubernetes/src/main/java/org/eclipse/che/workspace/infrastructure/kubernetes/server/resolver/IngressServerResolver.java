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
package org.eclipse.che.workspace.infrastructure.kubernetes.server.resolver;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.extensions.Ingress;
import io.fabric8.kubernetes.api.model.extensions.IngressRule;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.eclipse.che.api.workspace.server.model.impl.ServerImpl;
import org.eclipse.che.workspace.infrastructure.kubernetes.Annotations;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.KubernetesServerExposer;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.RuntimeServerBuilder;
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
public class IngressServerResolver extends AbstractServerResolver {
  private final Multimap<String, Ingress> ingresses;
  private final IngressPathTransformInverter pathTransformInverter;

  public IngressServerResolver(
      IngressPathTransformInverter pathTransformInverter,
      List<Service> services,
      List<Ingress> ingresses) {
    super(services);
    this.pathTransformInverter = pathTransformInverter;

    this.ingresses = ArrayListMultimap.create();
    for (Ingress ingress : ingresses) {
      String machineName =
          Annotations.newDeserializer(ingress.getMetadata().getAnnotations()).machineName();
      this.ingresses.put(machineName, ingress);
    }
  }

  @Override
  public Map<String, ServerImpl> resolveExternalServers(String machineName) {
    return ingresses
        .get(machineName)
        .stream()
        .map(this::fillIngressServers)
        .flatMap(m -> m.entrySet().stream())
        .collect(Collectors.toMap(Entry::getKey, Entry::getValue, (v1, v2) -> v2));
  }

  private Map<String, ServerImpl> fillIngressServers(Ingress ingress) {
    IngressRule ingressRule = ingress.getSpec().getRules().get(0);

    // host either set by rule, or determined by LB ip
    final String host =
        ingressRule.getHost() != null
            ? ingressRule.getHost()
            : ingress.getStatus().getLoadBalancer().getIngress().get(0).getIp();

    return Annotations.newDeserializer(ingress.getMetadata().getAnnotations())
        .servers()
        .entrySet()
        .stream()
        .collect(
            Collectors.toMap(
                Entry::getKey,
                e -> {
                  String root =
                      pathTransformInverter.undoPathTransformation(
                          ingressRule.getHttp().getPaths().get(0).getPath());

                  String path = buildPath(root, e.getValue().getPath());

                  // the /jwt/auth needs to be based on the webroot of the server, not the path of
                  // the endpoint.
                  String endpointOrigin = buildPath(root, "/");

                  return new RuntimeServerBuilder()
                      .protocol(e.getValue().getProtocol())
                      .host(host)
                      .path(path)
                      .endpointOrigin(endpointOrigin)
                      .attributes(e.getValue().getAttributes())
                      .targetPort(e.getValue().getPort())
                      .build();
                }));
  }
}
