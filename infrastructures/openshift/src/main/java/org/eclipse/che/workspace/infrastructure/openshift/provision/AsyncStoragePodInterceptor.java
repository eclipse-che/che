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
package org.eclipse.che.workspace.infrastructure.openshift.provision;

import static java.lang.String.format;
import static org.eclipse.che.workspace.infrastructure.kubernetes.namespace.pvc.CommonPVCStrategy.COMMON_STRATEGY;
import static org.eclipse.che.workspace.infrastructure.kubernetes.namespace.pvc.EphemeralWorkspaceUtility.isEphemeral;
import static org.eclipse.che.workspace.infrastructure.openshift.provision.AsyncStorageProvisioner.ASYNC_STORAGE;

import io.fabric8.kubernetes.api.model.DoneablePod;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.dsl.PodResource;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.KubernetesInfrastructureException;
import org.eclipse.che.workspace.infrastructure.openshift.OpenShiftClientFactory;
import org.eclipse.che.workspace.infrastructure.openshift.environment.OpenShiftEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This interceptor checks whether the starting workspace is configured with persistent storage and
 * makes sure to stop the async storage pod (if any is running) to prevent "Multi-Attach error for
 * volume". After the async storage pod is stopped and deleted, the workspace start is resumed.
 */
public class AsyncStoragePodInterceptor {

  private static final Logger LOG = LoggerFactory.getLogger(AsyncStoragePodInterceptor.class);
  private static final int DELETE_POD_TIMEOUT_IN_MIN = 5;

  private final OpenShiftClientFactory clientFactory;
  private final String pvcStrategy;

  @Inject
  public AsyncStoragePodInterceptor(
      @Named("che.infra.kubernetes.pvc.strategy") String pvcStrategy,
      OpenShiftClientFactory openShiftClientFactory) {
    this.pvcStrategy = pvcStrategy;
    this.clientFactory = openShiftClientFactory;
  }

  public void intercept(OpenShiftEnvironment osEnv, RuntimeIdentity identity)
      throws InfrastructureException {
    if (!COMMON_STRATEGY.equals(pvcStrategy) || isEphemeral(osEnv.getAttributes())) {
      return;
    }

    String namespace = identity.getInfrastructureNamespace();
    String workspaceId = identity.getWorkspaceId();

    PodResource<Pod, DoneablePod> asyncStoragePodResource =
        getAsyncStoragePodResource(namespace, workspaceId);

    if (asyncStoragePodResource.get() == null) { // pod doesn't exist
      return;
    }

    try {
      deleteAsyncStoragePod(asyncStoragePodResource)
          .get(DELETE_POD_TIMEOUT_IN_MIN, TimeUnit.MINUTES);
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
      throw new InfrastructureException(
          format(
              "Interrupted while waiting for pod '%s' removal. " + ex.getMessage(), ASYNC_STORAGE),
          ex);
    } catch (ExecutionException ex) {
      throw new InfrastructureException(
          format(
              "Error occurred while waiting for pod '%s' removal. " + ex.getMessage(),
              ASYNC_STORAGE),
          ex);
    } catch (TimeoutException ex) {
      throw new InfrastructureException(
          format("Pod '%s' removal timeout reached " + ex.getMessage(), ASYNC_STORAGE), ex);
    }
  }

  private PodResource<Pod, DoneablePod> getAsyncStoragePodResource(
      String namespace, String workspaceId) throws InfrastructureException {
    return clientFactory.create(workspaceId).pods().inNamespace(namespace).withName(ASYNC_STORAGE);
  }

  private CompletableFuture<Void> deleteAsyncStoragePod(PodResource<Pod, DoneablePod> podResource)
      throws InfrastructureException {
    Watch toCloseOnException = null;
    try {
      final CompletableFuture<Void> deleteFuture = new CompletableFuture<>();
      final Watch watch = podResource.watch(new DeleteWatcher<>(deleteFuture));
      toCloseOnException = watch;

      Boolean deleteSucceeded = podResource.withPropagationPolicy("Background").delete();
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
    public void onClose(KubernetesClientException e) {
      // if event about removing is received then this completion has no effect
      future.completeExceptionally(
          new RuntimeException(
              "WebSocket connection is closed. But event about removing is not received.", e));
    }
  }
}
