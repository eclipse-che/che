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

import io.fabric8.kubernetes.api.model.extensions.DoneableIngress;
import io.fabric8.kubernetes.api.model.extensions.Ingress;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.dsl.Resource;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;
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

  public Ingress create(Ingress ingress) throws InfrastructureException {
    try {
      return clientFactory
          .create()
          .extensions()
          .ingresses()
          .inNamespace(namespace)
          .withName(ingress.getMetadata().getName())
          .create(ingress);
    } catch (KubernetesClientException e) {
      throw new InfrastructureException(e.getMessage(), e);
    }
  }

  public Ingress wait(String name, int timeoutMin, Predicate<Ingress> predicate)
      throws InfrastructureException {
    CompletableFuture<Ingress> future = new CompletableFuture<>();
    Watch watch = null;
    try {

      Resource<Ingress, DoneableIngress> ingressResource =
          clientFactory.create().extensions().ingresses().inNamespace(namespace).withName(name);

      watch =
          ingressResource.watch(
              new Watcher<Ingress>() {
                @Override
                public void eventReceived(Action action, Ingress ingress) {
                  if (predicate.test(ingress)) {
                    future.complete(ingress);
                  }
                }

                @Override
                public void onClose(KubernetesClientException cause) {
                  future.completeExceptionally(
                      new InfrastructureException(
                          "Waiting for ingress '" + name + "' was interrupted"));
                }
              });

      Ingress actualIngress = ingressResource.get();
      if (actualIngress == null) {
        throw new InfrastructureException("Specified ingress " + name + " doesn't exist");
      }
      if (predicate.test(actualIngress)) {
        return actualIngress;
      }
      try {
        return future.get(timeoutMin, TimeUnit.MINUTES);
      } catch (ExecutionException e) {
        throw new InfrastructureException(e.getCause().getMessage(), e);
      } catch (TimeoutException e) {
        throw new InfrastructureException("Waiting for ingress '" + name + "' reached timeout");
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new InfrastructureException("Waiting for ingress '" + name + "' was interrupted");
      }
    } catch (KubernetesClientException e) {
      throw new InfrastructureException(e.getMessage(), e);
    } finally {
      if (watch != null) {
        watch.close();
      }
    }
  }

  /**
   * Creates specified route.
   *
   * @param routes - List of OS routes to create as an ingress
   * @return created route
   * @throws InfrastructureException when any exception occurs
   */
  //  public Ingress create(List<Route> routes) throws InfrastructureException {
  //    try {
  //      List<HTTPIngressPath> httpIngressPaths = new ArrayList<>();
  //      HashSet<String> servers = new HashSet<>();
  //      for (Route route : routes) {
  //        String server = route.getSpec().getPort().getTargetPort().getStrVal();
  //        if (!servers.contains(server)) {
  //          IngressBackend ingressBackend =
  //              new IngressBackendBuilder()
  //                  .withServiceName(route.getSpec().getTo().getName())
  //                  .withNewServicePort(server)
  //                  .build();
  //
  //          String serverPath = "/" + workspaceId + "/" + server;
  //
  //          HTTPIngressPath httpIngressPath =
  //              new HTTPIngressPathBuilder()
  //                  // .withPath(route.getSpec().getPath())
  //                  .withPath(serverPath)
  //                  .withBackend(ingressBackend)
  //                  .build();
  //          servers.add(server);
  //          httpIngressPaths.add(httpIngressPath);
  //        }
  //      }
  //
  //      HTTPIngressRuleValue httpIngressRuleValue =
  //          new HTTPIngressRuleValueBuilder().withPaths(httpIngressPaths).build();
  //      IngressRule ingressRule = new IngressRuleBuilder().withHttp(httpIngressRuleValue).build();
  //      IngressSpec ingressSpec = new IngressSpecBuilder().withRules(ingressRule).build();
  //      Map<String, String> ingressAnontations = new HashMap<>();
  //      ingressAnontations.put("ingress.kubernetes.io/rewrite-target", "/");
  //      ingressAnontations.put("ingress.kubernetes.io/ssl-redirect", "false");
  //      ingressAnontations.put("kubernetes.io/ingress.class", "nginx");
  //      Ingress ingress =
  //          new IngressBuilder()
  //              .withSpec(ingressSpec)
  //              .withMetadata(
  //                  new ObjectMetaBuilder()
  //                      .withName(workspaceId + "-ingress")
  //                      .withAnnotations(ingressAnontations)
  //                      .build())
  //              .build();
  //      return clientFactory
  //          .create()
  //          .extensions()
  //          .ingresses()
  //          .inNamespace(namespace)
  //          .withName(workspaceId + "-ingress")
  //          .create(ingress);
  //    } catch (KubernetesClientException e) {
  //      throw new InfrastructureException(e.getMessage(), e);
  //    }
  //  }

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
