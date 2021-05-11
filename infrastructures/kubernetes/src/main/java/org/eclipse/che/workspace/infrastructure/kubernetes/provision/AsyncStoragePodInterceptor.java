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
package org.eclipse.che.workspace.infrastructure.kubernetes.provision;

import static io.fabric8.kubernetes.api.model.DeletionPropagation.BACKGROUND;
import static java.lang.String.format;
import static org.eclipse.che.workspace.infrastructure.kubernetes.namespace.pvc.CommonPVCStrategy.COMMON_STRATEGY;
import static org.eclipse.che.workspace.infrastructure.kubernetes.namespace.pvc.EphemeralWorkspaceUtility.isEphemeral;
import static org.eclipse.che.workspace.infrastructure.kubernetes.provision.AsyncStorageProvisioner.ASYNC_STORAGE;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.WatcherException;
import io.fabric8.kubernetes.client.dsl.PodResource;
import io.fabric8.kubernetes.client.dsl.RollableScalableResource;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.KubernetesClientFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.KubernetesInfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This interceptor checks whether the starting workspace is configured with persistent storage and
 * makes sure to stop the async storage deployment (if any is running) to prevent "Multi-Attach
 * error for volume". After the async storage deployment is stopped and deleted, the workspace start
 * is resumed.
 */
@Singleton
public class AsyncStoragePodInterceptor {

  private static final Logger LOG = LoggerFactory.getLogger(AsyncStoragePodInterceptor.class);
  private static final int DELETE_DEPLOYMENT_TIMEOUT_IN_MIN = 5;

  private final KubernetesClientFactory kubernetesClientFactory;
  private final String pvcStrategy;

  @Inject
  public AsyncStoragePodInterceptor(
      @Named("che.infra.kubernetes.pvc.strategy") String pvcStrategy,
      KubernetesClientFactory kubernetesClientFactory) {
    this.pvcStrategy = pvcStrategy;
    this.kubernetesClientFactory = kubernetesClientFactory;
  }

  public void intercept(KubernetesEnvironment k8sEnv, RuntimeIdentity identity)
      throws InfrastructureException {
    if (!COMMON_STRATEGY.equals(pvcStrategy) || isEphemeral(k8sEnv.getAttributes())) {
      return;
    }

    removeAsyncStoragePodWithoutDeployment(identity);

    String namespace = identity.getInfrastructureNamespace();
    String workspaceId = identity.getWorkspaceId();

    RollableScalableResource<Deployment> asyncStorageDeploymentResource =
        getAsyncStorageDeploymentResource(namespace, workspaceId);

    if (asyncStorageDeploymentResource.get() == null) { // deployment doesn't exist
      return;
    }

    try {
      deleteAsyncStorageDeployment(asyncStorageDeploymentResource)
          .get(DELETE_DEPLOYMENT_TIMEOUT_IN_MIN, TimeUnit.MINUTES);
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
      throw new InfrastructureException(
          format(
              "Interrupted while waiting for deployment '%s' removal. " + ex.getMessage(),
              ASYNC_STORAGE),
          ex);
    } catch (ExecutionException ex) {
      throw new InfrastructureException(
          format(
              "Error occurred while waiting for deployment '%s' removal. " + ex.getMessage(),
              ASYNC_STORAGE),
          ex);
    } catch (TimeoutException ex) {
      throw new InfrastructureException(
          format("Pod '%s' removal timeout reached " + ex.getMessage(), ASYNC_STORAGE), ex);
    }
  }

  private RollableScalableResource<Deployment> getAsyncStorageDeploymentResource(
      String namespace, String workspaceId) throws InfrastructureException {
    return kubernetesClientFactory
        .create(workspaceId)
        .apps()
        .deployments()
        .inNamespace(namespace)
        .withName(ASYNC_STORAGE);
  }

  private CompletableFuture<Void> deleteAsyncStorageDeployment(
      RollableScalableResource<Deployment> resource) throws InfrastructureException {
    Watch toCloseOnException = null;
    try {
      final CompletableFuture<Void> deleteFuture = new CompletableFuture<>();
      final Watch watch = resource.watch(new DeleteWatcher<>(deleteFuture));
      toCloseOnException = watch;

      Boolean deleteSucceeded = resource.withPropagationPolicy(BACKGROUND).delete();
      if (deleteSucceeded == null || !deleteSucceeded) {
        deleteFuture.complete(null);
      }
      return deleteFuture.whenComplete(
          (v, e) -> {
            if (e != null) {
              LOG.warn("Failed to remove deployment {} cause {}", ASYNC_STORAGE, e.getMessage());
            }
            watch.close();
          });
    } catch (KubernetesClientException e) {
      if (toCloseOnException != null) {
        toCloseOnException.close();
      }
      throw new KubernetesInfrastructureException(e);
    } catch (Exception e) {
      if (toCloseOnException != null) {
        toCloseOnException.close();
      }
      throw e;
    }
  }

  /**
   * Cleanup existed Async Storage pods running without Deployment see
   * https://github.com/eclipse/che/issues/17616. Method can be removed in 7.20.x
   */
  private void removeAsyncStoragePodWithoutDeployment(RuntimeIdentity identity)
      throws InfrastructureException {
    String namespace = identity.getInfrastructureNamespace();
    String workspaceId = identity.getWorkspaceId();

    PodResource<Pod> asyncStoragePodResource =
        kubernetesClientFactory
            .create(workspaceId)
            .pods()
            .inNamespace(namespace)
            .withName(ASYNC_STORAGE);

    if (asyncStoragePodResource.get()
        != null) { // remove existed pod to replace it with deployment on provision step
      deleteAsyncStoragePod(asyncStoragePodResource);
    }
  }

  private CompletableFuture<Void> deleteAsyncStoragePod(PodResource<Pod> podResource)
      throws InfrastructureException {
    Watch toCloseOnException = null;
    try {
      final CompletableFuture<Void> deleteFuture = new CompletableFuture<>();
      final Watch watch = podResource.watch(new DeleteWatcher<>(deleteFuture));
      toCloseOnException = watch;

      Boolean deleteSucceeded = podResource.withPropagationPolicy(BACKGROUND).delete();
      if (deleteSucceeded == null || !deleteSucceeded) {
        deleteFuture.complete(null);
      }
      return deleteFuture.whenComplete(
          (v, e) -> {
            if (e != null) {
              LOG.warn("Failed to remove pod {} cause {}", ASYNC_STORAGE, e.getMessage());
            }
            watch.close();
          });
    } catch (KubernetesClientException e) {
      if (toCloseOnException != null) {
        toCloseOnException.close();
      }
      throw new KubernetesInfrastructureException(e);
    } catch (Exception e) {
      if (toCloseOnException != null) {
        toCloseOnException.close();
      }
      throw e;
    }
  }

  private static class DeleteWatcher<T> implements Watcher<T> {

    private final CompletableFuture<Void> future;

    private DeleteWatcher(CompletableFuture<Void> future) {
      this.future = future;
    }

    @Override
    public void eventReceived(Action action, T hasMetadata) {
      if (action == Action.DELETED) {
        future.complete(null);
      }
    }

    @Override
    public void onClose(WatcherException e) {
      // if event about removing is received then this completion has no effect
      future.completeExceptionally(
          new RuntimeException(
              "WebSocket connection is closed. But event about removing is not received.", e));
    }
  }
}
