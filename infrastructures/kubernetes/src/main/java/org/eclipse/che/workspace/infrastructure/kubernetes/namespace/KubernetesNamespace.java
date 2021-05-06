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

import static io.fabric8.kubernetes.api.model.DeletionPropagation.BACKGROUND;
import static java.lang.String.format;

import com.google.common.annotations.VisibleForTesting;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.NamespaceBuilder;
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
import io.fabric8.kubernetes.client.WatcherException;
import io.fabric8.kubernetes.client.dsl.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
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

  protected static final String MANAGED_NAMESPACE_LABEL = "che-managed";

  private final String workspaceId;
  private final String name;

  private final KubernetesDeployments deployments;
  private final KubernetesServices services;
  private final KubernetesPersistentVolumeClaims pvcs;
  private final KubernetesIngresses ingresses;
  /** Factory for workspace related operations clients */
  private final KubernetesClientFactory clientFactory;
  /** Factory for cluster related operations clients (like labeling the namespaces) */
  private final KubernetesClientFactory cheSAClientFactory;

  private final KubernetesSecrets secrets;
  private final KubernetesConfigsMaps configMaps;

  @VisibleForTesting
  protected KubernetesNamespace(
      KubernetesClientFactory clientFactory,
      KubernetesClientFactory cheSAClientFactory,
      String workspaceId,
      String name,
      KubernetesDeployments deployments,
      KubernetesServices services,
      KubernetesPersistentVolumeClaims pvcs,
      KubernetesIngresses kubernetesIngresses,
      KubernetesSecrets secrets,
      KubernetesConfigsMaps configMaps) {
    this.clientFactory = clientFactory;
    this.cheSAClientFactory = cheSAClientFactory;
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
      KubernetesClientFactory clientFactory,
      KubernetesClientFactory cheSAClientFactory,
      Executor executor,
      String name,
      String workspaceId) {
    this.clientFactory = clientFactory;
    this.cheSAClientFactory = cheSAClientFactory;
    this.workspaceId = workspaceId;
    this.name = name;
    this.deployments = new KubernetesDeployments(name, workspaceId, clientFactory, executor);
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
   * <p>The method will try to label the namespace with provided `labels`. It does not matter if the
   * namespace already exists or we create new one. If update labels operation fail due to lack of
   * permission, we do not fail completely.
   *
   * @param canCreate defines what to do when the namespace is not found. The namespace is created
   *     when {@code true}, otherwise an exception is thrown.
   * @param labels labels that should be set to the namespace
   * @throws InfrastructureException if any exception occurs during namespace preparation or if the
   *     namespace doesn't exist and {@code canCreate} is {@code false}.
   */
  void prepare(boolean canCreate, Map<String, String> labels) throws InfrastructureException {
    KubernetesClient client = clientFactory.create(workspaceId);
    Namespace namespace = get(name, client);

    if (namespace == null) {
      if (!canCreate) {
        throw new InfrastructureException(
            format("Creating the namespace '%s' is not allowed, yet it was not found.", name));
      }
      namespace = create(name, client);
    }
    label(namespace, labels);
  }

  /**
   * Applies given `ensureLabels` into given `namespace` and update the `namespace` in the
   * Kubernetes.
   *
   * <p>If we do not have permissions to do so (code=403), this method does not throw any exception.
   *
   * @param namespace namespace to label
   * @param ensureLabels these labels should be applied on given `namespace`
   * @throws InfrastructureException if something goes wrong with update, except lack of permissions
   */
  protected void label(Namespace namespace, Map<String, String> ensureLabels)
      throws InfrastructureException {
    if (ensureLabels.isEmpty()) {
      return;
    }
    Map<String, String> currentLabels = namespace.getMetadata().getLabels();
    Map<String, String> newLabels =
        currentLabels != null ? new HashMap<>(currentLabels) : new HashMap<>();

    if (newLabels.entrySet().containsAll(ensureLabels.entrySet())) {
      LOG.debug(
          "Nothing to do, namespace [{}] already has all required labels.",
          namespace.getMetadata().getName());
      return;
    }

    try {
      // update the namespace with new labels
      cheSAClientFactory
          .create()
          .namespaces()
          .createOrReplace(
              new NamespaceBuilder(namespace)
                  .editMetadata()
                  .addToLabels(ensureLabels)
                  .endMetadata()
                  .build());
    } catch (KubernetesClientException kce) {
      if (kce.getCode() == 403) {
        LOG.warn(
            "Can't label the namespace due to lack of permissions. Grant cluster-wide permissions "
                + "to `get` and `update` the `namespaces` to the `che` service account "
                + "(Che operator might have already prepared a cluster role called "
                + "`che-namespace-editor` for this, depending on its configuration). "
                + "Alternatively, consider disabling the feature by setting "
                + "`che.infra.kubernetes.namepsace.label` to `false`.");
        return;
      }
      throw new InfrastructureException(kce);
    }
  }

  /**
   * Deletes the namespace. Deleting a non-existent namespace is not an error as is not an attempt
   * to delete a namespace that is already being deleted.
   *
   * @throws InfrastructureException if any unexpected exception occurs during namespace deletion
   */
  void delete() throws InfrastructureException {
    KubernetesClient client = clientFactory.create(workspaceId);

    try {
      delete(name, client);
    } catch (KubernetesClientException e) {
      if (e.getCode() == 403) {
        throw new InfrastructureException(
            format(
                "Could not access the namespace %s when deleting it for workspace %s",
                name, workspaceId),
            e);
      }

      throw new KubernetesInfrastructureException(e);
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

  private Namespace create(String namespaceName, KubernetesClient client)
      throws InfrastructureException {
    try {
      Namespace namespace =
          client
              .namespaces()
              .create(
                  new NamespaceBuilder()
                      .withNewMetadata()
                      .withName(namespaceName)
                      .endMetadata()
                      .build());
      waitDefaultServiceAccount(namespaceName, client);
      return namespace;
    } catch (KubernetesClientException e) {
      if (e.getCode() == 403) {
        LOG.error(
            "Unable to create new Kubernetes project due to lack of permissions."
                + "When using workspace namespace placeholders, service account with lenient permissions (cluster-admin) must be used.");
      }
      throw new KubernetesInfrastructureException(e);
    }
  }

  private void delete(String namespaceName, KubernetesClient client)
      throws InfrastructureException {
    try {
      client.namespaces().withName(namespaceName).withPropagationPolicy(BACKGROUND).delete();
    } catch (KubernetesClientException e) {
      if (e.getCode() == 404) {
        LOG.warn(
            format(
                "Tried to delete namespace '%s' but it doesn't exist in the cluster.",
                namespaceName),
            e);
      } else if (e.getCode() == 409) {
        LOG.info(format("The namespace '%s' is currently being deleted.", namespaceName), e);
      } else {
        throw new KubernetesInfrastructureException(e);
      }
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
      final Resource<ServiceAccount> saResource =
          client
              .serviceAccounts()
              .inNamespace(namespaceName)
              .withName(DEFAULT_SERVICE_ACCOUNT_NAME);
      watch =
          saResource.watch(
              new Watcher<>() {
                @Override
                public void eventReceived(Action action, ServiceAccount serviceAccount) {
                  if (predicate.test(serviceAccount)) {
                    future.complete(serviceAccount);
                  }
                }

                @Override
                public void onClose(WatcherException cause) {
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
        LOG.warn(
            "Trying to get namespace '{}', but failed because the lack of permissions.",
            namespaceName);
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
