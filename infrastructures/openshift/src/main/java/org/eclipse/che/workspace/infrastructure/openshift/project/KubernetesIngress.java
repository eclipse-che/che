/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.openshift.project;

import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.extensions.HTTPIngressPath;
import io.fabric8.kubernetes.api.model.extensions.HTTPIngressPathBuilder;
import io.fabric8.kubernetes.api.model.extensions.HTTPIngressRuleValue;
import io.fabric8.kubernetes.api.model.extensions.HTTPIngressRuleValueBuilder;
import io.fabric8.kubernetes.api.model.extensions.Ingress;
import io.fabric8.kubernetes.api.model.extensions.IngressBackend;
import io.fabric8.kubernetes.api.model.extensions.IngressBackendBuilder;
import io.fabric8.kubernetes.api.model.extensions.IngressBuilder;
import io.fabric8.kubernetes.api.model.extensions.IngressRule;
import io.fabric8.kubernetes.api.model.extensions.IngressRuleBuilder;
import io.fabric8.kubernetes.api.model.extensions.IngressSpec;
import io.fabric8.kubernetes.api.model.extensions.IngressSpecBuilder;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.openshift.api.model.Route;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.openshift.OpenShiftClientFactory;

/** Created by I313632 on 05/01/2018. */
public class KubernetesIngress {
  private final String namespace;
  private final String workspaceId;
  private final OpenShiftClientFactory clientFactory;

  KubernetesIngress(String namespace, String workspaceId, OpenShiftClientFactory clientFactory) {
    this.namespace = namespace;
    this.workspaceId = workspaceId;
    this.clientFactory = clientFactory;
  }

  /**
   * Creates specified route.
   *
   * @param routes - List of OS routes to create as an ingress
   * @return created route
   * @throws InfrastructureException when any exception occurs
   */
  public Ingress create(List<Route> routes) throws InfrastructureException {
    try {
      List<HTTPIngressPath> httpIngressPaths = new ArrayList<>();
      HashSet<String> servers = new HashSet<>();
      for (Route route : routes) {
        String server = route.getSpec().getPort().getTargetPort().getStrVal();
        if (!servers.contains(server)) {
          IngressBackend ingressBackend =
              new IngressBackendBuilder()
                  .withServiceName(route.getSpec().getTo().getName())
                  .withNewServicePort(server)
                  .build();

          String serverPath = "/" + workspaceId + "/" + server;

          HTTPIngressPath httpIngressPath =
              new HTTPIngressPathBuilder()
                  // .withPath(route.getSpec().getPath())
                  .withPath(serverPath)
                  .withBackend(ingressBackend)
                  .build();
          servers.add(server);
          httpIngressPaths.add(httpIngressPath);
        }
      }

      HTTPIngressRuleValue httpIngressRuleValue =
          new HTTPIngressRuleValueBuilder().withPaths(httpIngressPaths).build();
      IngressRule ingressRule = new IngressRuleBuilder().withHttp(httpIngressRuleValue).build();
      IngressSpec ingressSpec = new IngressSpecBuilder().withRules(ingressRule).build();
      Map<String, String> ingressAnontations = new HashMap<>();
      ingressAnontations.put("ingress.kubernetes.io/rewrite-target", "/");
      ingressAnontations.put("ingress.kubernetes.io/ssl-redirect", "false");
      ingressAnontations.put("kubernetes.io/ingress.class", "nginx");
      Ingress ingress =
          new IngressBuilder()
              .withSpec(ingressSpec)
              .withMetadata(
                  new ObjectMetaBuilder()
                      .withName(workspaceId + "-ingress")
                      .withAnnotations(ingressAnontations)
                      .build())
              .build();
      return clientFactory
          .create()
          .extensions()
          .ingresses()
          .inNamespace(namespace)
          .withName(workspaceId + "-ingress")
          .create(ingress);
    } catch (KubernetesClientException e) {
      throw new InfrastructureException(e.getMessage(), e);
    }
  }

  public void delete() throws InfrastructureException {
    clientFactory
        .create()
        .extensions()
        .ingresses()
        .inNamespace(namespace)
        .withName(workspaceId + "-ingress")
        .delete();
  }
}
