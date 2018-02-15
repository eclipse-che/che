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
package org.eclipse.che.workspace.infrastructure.kubernetes.namespace;

import static org.eclipse.che.workspace.infrastructure.kubernetes.Constants.CHE_WORKSPACE_ID_LABEL;
import static org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesObjectUtil.putLabel;

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
import org.eclipse.che.workspace.infrastructure.kubernetes.KubernetesClientFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.KubernetesInfrastructureException;

/**
 * Defines an internal API for managing {@link Ingress} instances in {@link
 * KubernetesIngresses#namespace predefined namespace}.
 *
 * @author Sergii Leshchenko
 * @author Guy Daich
 */
public class KubernetesIngresses {

  private final String namespace;
  private final String workspaceId;
  private final KubernetesClientFactory clientFactory;

  KubernetesIngresses(String namespace, String workspaceId, KubernetesClientFactory clientFactory) {
    this.namespace = namespace;
    this.workspaceId = workspaceId;
    this.clientFactory = clientFactory;
  }

  public Ingress create(Ingress ingress) throws InfrastructureException {
    putLabel(ingress, CHE_WORKSPACE_ID_LABEL, workspaceId);
    try {
      return clientFactory
          .create()
          .extensions()
          .ingresses()
          .inNamespace(namespace)
          .withName(ingress.getMetadata().getName())
          .create(ingress);
    } catch (KubernetesClientException e) {
      throw new KubernetesInfrastructureException(e);
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
      throw new KubernetesInfrastructureException(e);
    } finally {
      if (watch != null) {
        watch.close();
      }
    }
  }

  public void delete() throws InfrastructureException {
    try {
      clientFactory
          .create()
          .extensions()
          .ingresses()
          .inNamespace(namespace)
          .withLabel(CHE_WORKSPACE_ID_LABEL, workspaceId)
          .delete();
    } catch (KubernetesClientException e) {
      throw new KubernetesInfrastructureException(e);
    }
  }
}
