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

import com.google.common.annotations.VisibleForTesting;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.DoneableServiceAccount;
import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceAccount;
import io.fabric8.kubernetes.api.model.extensions.Ingress;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.dsl.Resource;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalInfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.KubernetesClientFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.KubernetesInfrastructureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Defines an internal API for managing subset of objects inside {@link Namespace} instance.
 *
 * @author Sergii Leshchenko
 */
public class KubernetesNamespace {

  private static final Logger LOG = LoggerFactory.getLogger(KubernetesNamespace.class);

  /** An experimental value used to determine how long to wait for the 'default' service account. */
  private static final int SERVICE_ACCOUNT_READINESS_TIMEOUT_SEC = 3;
  /**
   * Default service account is used to get access to the API server so we need to be sure that it
   * is accessible, more detailed information about service accounts by default you can find here:
   * https://kubernetes.io/docs/tasks/configure-pod-container/configure-service-account/
   */
  private static final String DEFAULT_SERVICE_ACCOUNT_NAME = "default";

  private final String workspaceId;
  private final String name;

  private final KubernetesDeployments deployments;
  private final KubernetesServices services;
  private final KubernetesPersistentVolumeClaims pvcs;
  private final KubernetesIngresses ingresses;
  private final KubernetesClientFactory clientFactory;
  private final KubernetesSecrets secrets;
  private final KubernetesConfigsMaps configMaps;

  @VisibleForTesting
  protected KubernetesNamespace(
      KubernetesClientFactory clientFactory,
      String workspaceId,
      String name,
      KubernetesDeployments deployments,
      KubernetesServices services,
      KubernetesPersistentVolumeClaims pvcs,
      KubernetesIngresses kubernetesIngresses,
      KubernetesSecrets secrets,
      KubernetesConfigsMaps configMaps) {
    this.clientFactory = clientFactory;
    this.workspaceId = workspaceId;
    this.name = name;
    this.deployments = deployments;
    this.services = services;
    this.pvcs = pvcs;
    this.ingresses = kubernetesIngresses;
    this.secrets = secrets;
    this.configMaps = configMaps;
  }

  public KubernetesNamespace(
      KubernetesClientFactory clientFactory, String name, String workspaceId) {
    this.clientFactory = clientFactory;
    this.workspaceId = workspaceId;
    this.name = name;
    this.deployments = new KubernetesDeployments(name, workspaceId, clientFactory);
    this.services = new KubernetesServices(name, workspaceId, clientFactory);
    this.pvcs = new KubernetesPersistentVolumeClaims(name, workspaceId, clientFactory);
    this.ingresses = new KubernetesIngresses(name, workspaceId, clientFactory);
    this.secrets = new KubernetesSecrets(name, workspaceId, clientFactory);
    this.configMaps = new KubernetesConfigsMaps(name, workspaceId, clientFactory);
  }

  /**
   * Prepare namespace for using.
   *
   * <p>Preparing includes creating if needed and waiting for default service account.
   *
   * @throws InfrastructureException if any exception occurs during namespace preparing
   */
  void prepare() throws InfrastructureException {
    KubernetesClient client = clientFactory.create(workspaceId);
    if (get(name, client) == null) {
      create(name, client);
    }
  }

  /** Returns namespace name */
  public String getName() {
    return name;
  }

  /** Returns identifier of the workspace for which current namespace is used */
  public String getWorkspaceId() {
    return workspaceId;
  }

  /** Returns object for managing {@link Pod} instances inside namespace. */
  public KubernetesDeployments deployments() {
    return deployments;
  }

  /** Returns object for managing {@link Service} instances inside namespace. */
  public KubernetesServices services() {
    return services;
  }

  /** Returns object for managing {@link PersistentVolumeClaim} instances inside namespace. */
  public KubernetesPersistentVolumeClaims persistentVolumeClaims() {
    return pvcs;
  }

  /** Returns object for managing {@link Ingress} instances inside namespace. */
  public KubernetesIngresses ingresses() {
    return ingresses;
  }

  /** Returns object for managing {@link Secret} instances inside namespace. */
  public KubernetesSecrets secrets() {
    return secrets;
  }

  /** Returns object for managing {@link ConfigMap} instances inside namespace. */
  public KubernetesConfigsMaps configMaps() {
    return configMaps;
  }

  /** Removes all object except persistent volume claims inside namespace. */
  public void cleanUp() throws InfrastructureException {
    doRemove(
        ingresses::delete,
        services::delete,
        deployments::delete,
        secrets::delete,
        configMaps::delete);
  }

  /**
   * Performs all the specified operations and throw exception with composite message if errors
   * occurred while any operation execution
   */
  protected void doRemove(RemoveOperation... operations) throws InfrastructureException {
    StringBuilder errors = new StringBuilder();
    for (RemoveOperation operation : operations) {
      try {
        operation.perform();
      } catch (InternalInfrastructureException e) {
        LOG.warn(
            "Internal infra error occurred while cleaning up the namespace for workspace with id "
                + workspaceId,
            e);
        errors.append(" ").append(e.getMessage());
      } catch (InfrastructureException e) {
        errors.append(" ").append(e.getMessage());
      }
    }

    if (errors.length() > 0) {
      throw new InfrastructureException(
          "Error(s) occurs while cleaning up the namespace." + errors.toString());
    }
  }

  private void create(String namespaceName, KubernetesClient client)
      throws InfrastructureException {
    try {
      client
          .namespaces()
          .createNew()
          .withNewMetadata()
          .withName(namespaceName)
          .endMetadata()
          .done();
      waitDefaultServiceAccount(namespaceName, client);
    } catch (KubernetesClientException e) {
      if (e.getCode() == 403) {
        LOG.error(
            "Unable to create new Kubernetes project due to lack of permissions."
                + "When using workspace namespace placeholders, service account with lenient permissions (cluster-admin) must be used.");
      }
      throw new KubernetesInfrastructureException(e);
    }
  }

  /**
   * Waits few seconds until 'default' service account become available otherwise an infrastructure
   * exception will be thrown.
   */
  protected void waitDefaultServiceAccount(String namespaceName, KubernetesClient client)
      throws InfrastructureException {
    final Predicate<ServiceAccount> predicate = Objects::nonNull;
    final CompletableFuture<ServiceAccount> future = new CompletableFuture<>();
    Watch watch = null;
    try {
      final Resource<ServiceAccount, DoneableServiceAccount> saResource =
          client
              .serviceAccounts()
              .inNamespace(namespaceName)
              .withName(DEFAULT_SERVICE_ACCOUNT_NAME);
      watch =
          saResource.watch(
              new Watcher<ServiceAccount>() {
                @Override
                public void eventReceived(Action action, ServiceAccount serviceAccount) {
                  if (predicate.test(serviceAccount)) {
                    future.complete(serviceAccount);
                  }
                }

                @Override
                public void onClose(KubernetesClientException cause) {
                  future.completeExceptionally(
                      new InfrastructureException(
                          "Waiting for service account '"
                              + DEFAULT_SERVICE_ACCOUNT_NAME
                              + "' was interrupted"));
                }
              });
      if (predicate.test(saResource.get())) {
        return;
      }
      try {
        future.get(SERVICE_ACCOUNT_READINESS_TIMEOUT_SEC, TimeUnit.SECONDS);
      } catch (ExecutionException ex) {
        throw new InfrastructureException(ex.getCause().getMessage(), ex);
      } catch (TimeoutException ex) {
        throw new InfrastructureException(
            "Waiting for service account '" + DEFAULT_SERVICE_ACCOUNT_NAME + "' reached timeout");
      } catch (InterruptedException ex) {
        Thread.currentThread().interrupt();
        throw new InfrastructureException(
            "Waiting for service account '" + DEFAULT_SERVICE_ACCOUNT_NAME + "' was interrupted");
      }
    } catch (KubernetesClientException ex) {
      throw new KubernetesInfrastructureException(ex);
    } finally {
      if (watch != null) {
        watch.close();
      }
    }
  }

  private Namespace get(String namespaceName, KubernetesClient client)
      throws InfrastructureException {
    try {
      return client.namespaces().withName(namespaceName).get();
    } catch (KubernetesClientException e) {
      if (e.getCode() == 403) {
        // namespace is foreign or doesn't exist
        return null;
      } else {
        throw new KubernetesInfrastructureException(e);
      }
    }
  }

  protected interface RemoveOperation {
    void perform() throws InfrastructureException;
  }
}
