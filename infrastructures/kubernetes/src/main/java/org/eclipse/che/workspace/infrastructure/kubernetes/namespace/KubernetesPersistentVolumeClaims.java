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
package org.eclipse.che.workspace.infrastructure.kubernetes.namespace;

import static java.util.stream.Collectors.toSet;

import io.fabric8.kubernetes.api.model.DoneablePersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.dsl.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalInfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.KubernetesClientFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.KubernetesInfrastructureException;

/**
 * Defines an internal API for managing {@link PersistentVolumeClaim} instances in {@link
 * KubernetesPersistentVolumeClaims#namespace predefined namespace}.
 *
 * @author Sergii Leshchenko
 */
public class KubernetesPersistentVolumeClaims {

  public static final String PVC_BOUND_PHASE = "Bound";

  private final String namespace;
  private final String workspaceId;
  private final KubernetesClientFactory clientFactory;

  KubernetesPersistentVolumeClaims(
      String namespace, String workspaceId, KubernetesClientFactory clientFactory) {
    this.namespace = namespace;
    this.clientFactory = clientFactory;
    this.workspaceId = workspaceId;
  }

  /**
   * Creates specified persistent volume claim.
   *
   * @param pvc persistent volume claim to create
   * @return created persistent volume claim
   * @throws InfrastructureException when any exception occurs
   */
  public PersistentVolumeClaim create(PersistentVolumeClaim pvc) throws InfrastructureException {
    try {
      return clientFactory
          .create(workspaceId)
          .persistentVolumeClaims()
          .inNamespace(namespace)
          .create(pvc);
    } catch (KubernetesClientException e) {
      throw new KubernetesInfrastructureException(e);
    }
  }

  /**
   * Returns all existing persistent volume claims.
   *
   * @throws InfrastructureException when any exception occurs
   */
  public List<PersistentVolumeClaim> get() throws InfrastructureException {
    try {
      return clientFactory
          .create(workspaceId)
          .persistentVolumeClaims()
          .inNamespace(namespace)
          .list()
          .getItems();
    } catch (KubernetesClientException e) {
      throw new KubernetesInfrastructureException(e);
    }
  }

  /**
   * Returns all existing persistent volume claims with given label.
   *
   * @param labelName name of provided label
   * @param labelValue value of provided label
   * @return list of matched PVCs
   * @throws InfrastructureException when any exception occurs while fetching the pvcs
   */
  public List<PersistentVolumeClaim> getByLabel(String labelName, String labelValue)
      throws InfrastructureException {
    try {
      return clientFactory
          .create(workspaceId)
          .persistentVolumeClaims()
          .inNamespace(namespace)
          .withLabel(labelName, labelValue)
          .list()
          .getItems();
    } catch (KubernetesClientException e) {
      throw new KubernetesInfrastructureException(e);
    }
  }

  /**
   * Creates all PVCs which are not present in current Kubernetes namespace.
   *
   * @param toCreate collection of PVCs to create
   * @throws InfrastructureException when any error occurs while creation
   */
  public void createIfNotExist(Collection<PersistentVolumeClaim> toCreate)
      throws InfrastructureException {
    final Set<String> existing =
        get().stream().map(p -> p.getMetadata().getName()).collect(toSet());
    for (PersistentVolumeClaim pvc : toCreate) {
      if (!existing.contains(pvc.getMetadata().getName())) {
        create(pvc);
      }
    }
  }

  /**
   * Removes all PVCs which have the specified labels.
   *
   * @param labels labels to filter PVCs
   * @throws InfrastructureException when any error occurs while removing
   */
  public void delete(Map<String, String> labels) throws InfrastructureException {
    try {
      clientFactory
          .create(workspaceId)
          .persistentVolumeClaims()
          .inNamespace(namespace)
          .withLabels(labels)
          .delete();
    } catch (KubernetesClientException e) {
      throw new KubernetesInfrastructureException(e);
    }
  }

  /**
   * Waits until persistent volume claim state is bound.
   *
   * @param name name of persistent volume claim that should be watched
   * @param timeoutMillis waiting timeout in milliseconds
   * @return persistent volume claim that satisfies the specified predicate
   * @throws InfrastructureException when specified timeout is reached
   * @throws InfrastructureException when {@link Thread} is interrupted while waiting
   * @throws InfrastructureException when any other exception occurs
   */
  public PersistentVolumeClaim waitBound(String name, long timeoutMillis)
      throws InfrastructureException {
    return wait(name, timeoutMillis, pvc -> pvc.getStatus().getPhase().equals(PVC_BOUND_PHASE));
  }

  /**
   * Waits until persistent volume claim state will suit for specified predicate.
   *
   * @param name name of persistent volume claim that should be watched
   * @param timeoutMillis waiting timeout in milliseconds
   * @param predicate predicate to perform state check
   * @return persistent volume claim that satisfies the specified predicate
   * @throws InfrastructureException when specified timeout is reached
   * @throws InfrastructureException when {@link Thread} is interrupted while waiting
   * @throws InfrastructureException when any other exception occurs
   */
  public PersistentVolumeClaim wait(
      String name, long timeoutMillis, Predicate<PersistentVolumeClaim> predicate)
      throws InfrastructureException {
    CompletableFuture<PersistentVolumeClaim> future = new CompletableFuture<>();
    Watch watch = null;
    try {
      Resource<PersistentVolumeClaim, DoneablePersistentVolumeClaim> pvcResource =
          clientFactory
              .create(workspaceId)
              .persistentVolumeClaims()
              .inNamespace(namespace)
              .withName(name);

      watch =
          pvcResource.watch(
              new Watcher<PersistentVolumeClaim>() {
                @Override
                public void eventReceived(Action action, PersistentVolumeClaim pvc) {
                  if (predicate.test(pvc)) {
                    future.complete(pvc);
                  }
                }

                @Override
                public void onClose(KubernetesClientException cause) {
                  future.completeExceptionally(
                      new InfrastructureException(
                          "Waiting for persistent volume claim '" + name + "' was interrupted"));
                }
              });

      PersistentVolumeClaim actualPvc = pvcResource.get();
      if (predicate.test(actualPvc)) {
        return actualPvc;
      }
      try {
        return future.get(timeoutMillis, TimeUnit.MILLISECONDS);
      } catch (ExecutionException e) {
        // May happen only if WebSocket Connection is closed before needed event received.
        // Throw internal exception because there may be some cluster/network issues that admin
        // should take a look.
        throw new InternalInfrastructureException(e.getCause().getMessage(), e);
      } catch (TimeoutException e) {
        // May happen when PVC is not bound in the time.
        // Throw internal exception because there may be some cluster configuration/performance
        // issues that admin should take a look.
        throw new InternalInfrastructureException(
            "Waiting for persistent volume claim '" + name + "' reached timeout");
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new InfrastructureException(
            "Waiting for persistent volume claim '" + name + "' was interrupted");
      }
    } catch (KubernetesClientException e) {
      throw new KubernetesInfrastructureException(e);
    } finally {
      if (watch != null) {
        watch.close();
      }
    }
  }
}
