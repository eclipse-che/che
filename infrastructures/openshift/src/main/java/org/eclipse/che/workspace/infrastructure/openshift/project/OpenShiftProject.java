/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.openshift.project;

import static java.lang.String.format;

import com.google.common.annotations.VisibleForTesting;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.openshift.api.model.Project;
import io.fabric8.openshift.api.model.ProjectRequestBuilder;
import io.fabric8.openshift.api.model.Route;
import io.fabric8.openshift.client.OpenShiftClient;
import java.util.Map;
import java.util.concurrent.Executor;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalInfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.KubernetesClientFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.KubernetesInfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesConfigsMaps;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesDeployments;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesIngresses;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesNamespace;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesPersistentVolumeClaims;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesSecrets;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesServices;
import org.eclipse.che.workspace.infrastructure.openshift.OpenShiftClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Defines an internal API for managing subset of objects inside {@link Project} instance.
 *
 * @author Sergii Leshchenko
 */
public class OpenShiftProject extends KubernetesNamespace {

  private static final Logger LOG = LoggerFactory.getLogger(OpenShiftProject.class);

  private final OpenShiftRoutes routes;
  private final OpenShiftClientFactory clientFactory;

  @VisibleForTesting
  OpenShiftProject(
      OpenShiftClientFactory clientFactory,
      KubernetesClientFactory cheClientFactory,
      String workspaceId,
      String name,
      KubernetesDeployments deployments,
      KubernetesServices services,
      OpenShiftRoutes routes,
      KubernetesPersistentVolumeClaims pvcs,
      KubernetesIngresses ingresses,
      KubernetesSecrets secrets,
      KubernetesConfigsMaps configMaps) {
    super(
        clientFactory,
        cheClientFactory,
        workspaceId,
        name,
        deployments,
        services,
        pvcs,
        ingresses,
        secrets,
        configMaps);
    this.clientFactory = clientFactory;
    this.routes = routes;
  }

  public OpenShiftProject(
      OpenShiftClientFactory clientFactory,
      KubernetesClientFactory cheClientFactory,
      Executor executor,
      String name,
      String workspaceId) {
    super(clientFactory, cheClientFactory, executor, name, workspaceId);
    this.clientFactory = clientFactory;
    this.routes = new OpenShiftRoutes(name, workspaceId, clientFactory);
  }

  /**
   * Prepare a project for using.
   *
   * <p>Preparing includes creating if needed and waiting for default service account.
   *
   * @param canCreate defines what to do when the project is not found. The project is created when
   *     {@code true}, otherwise an exception is thrown.
   * @throws InfrastructureException if any exception occurs during project preparation or if the
   *     project doesn't exist and {@code canCreate} is {@code false}.
   */
  void prepare(boolean canCreate, Map<String, String> labels) throws InfrastructureException {
    String workspaceId = getWorkspaceId();
    String projectName = getName();

    KubernetesClient kubeClient = clientFactory.create(workspaceId);
    OpenShiftClient osClient = clientFactory.createOC(workspaceId);

    Project project = get(projectName, osClient);

    if (project == null) {
      if (!canCreate) {
        throw new InfrastructureException(
            format(
                "Creating the namespace '%s' is not allowed, yet" + " it was not found.",
                projectName));
      }

      create(projectName, osClient);
      waitDefaultServiceAccount(projectName, kubeClient);
    }
    label(osClient.namespaces().withName(projectName).get(), labels);
  }

  /**
   * Deletes the project. Deleting a non-existent projects is not an error as is not an attempt to
   * delete a project that is already being deleted.
   *
   * @throws InfrastructureException if any unexpected exception occurs during project deletion
   */
  void delete() throws InfrastructureException {
    String workspaceId = getWorkspaceId();
    String projectName = getName();

    OpenShiftClient osClient = clientFactory.createOC(workspaceId);

    try {
      delete(projectName, osClient);
    } catch (KubernetesClientException e) {
      if (e.getCode() == 403) {
        throw new InfrastructureException(
            format(
                "Could not access the project %s when deleting it for workspace %s",
                projectName, workspaceId),
            e);
      }

      throw new KubernetesInfrastructureException(e);
    }
  }

  /** Returns object for managing {@link Route} instances inside project. */
  public OpenShiftRoutes routes() {
    return routes;
  }

  /** Removes all object except persistent volume claims inside project. */
  public void cleanUp() throws InfrastructureException {
    doRemove(
        routes::delete,
        services()::delete,
        deployments()::delete,
        secrets()::delete,
        configMaps()::delete);
  }

  private void create(String projectName, OpenShiftClient osClient) throws InfrastructureException {
    try {
      osClient
          .projectrequests()
          .create(
              new ProjectRequestBuilder()
                  .withNewMetadata()
                  .withName(projectName)
                  .endMetadata()
                  .build());
    } catch (KubernetesClientException e) {
      if (e.getCode() == 403) {
        LOG.error(
            "Unable to create new OpenShift project due to lack of permissions."
                + "HINT: When using workspace project name placeholders, os-oauth or service account with more lenient permissions (cluster-admin) must be used.");
      }
      throw new KubernetesInfrastructureException(e);
    }
  }

  private void update(Project project, OpenShiftClient client) throws InfrastructureException {
    try {
      client.projects().createOrReplace(project);
    } catch (KubernetesClientException e) {
      if (e.getCode() == 403) {
        LOG.error(
            "Unable to update new Kubernetes project due to lack of permissions."
                + "When using workspace namespace placeholders, service account with lenient permissions (cluster-admin) must be used.");
      }
      throw new KubernetesInfrastructureException(e);
    }
  }

  private void delete(String projectName, OpenShiftClient osClient) throws InfrastructureException {
    try {
      osClient.projects().withName(projectName).delete();
    } catch (KubernetesClientException e) {
      if (e.getCode() == 404) {
        LOG.warn(
            format(
                "Tried to delete project '%s' but it doesn't exist in the cluster.", projectName),
            e);
      } else if (e.getCode() == 409) {
        LOG.info(format("The project '%s' is currently being deleted.", projectName), e);
      } else {
        throw new KubernetesInfrastructureException(e);
      }
    }
  }

  private Project get(String projectName, OpenShiftClient client) throws InfrastructureException {
    try {
      return client.projects().withName(projectName).get();
    } catch (KubernetesClientException e) {
      if (e.getCode() == 403) {
        // project is foreign or doesn't exist
        LOG.warn(
            "Trying to get namespace '{}', but failed because the lack of permissions.",
            projectName);
        return null;
      } else {
        throw new KubernetesInfrastructureException(e);
      }
    }
  }

  private boolean isProjectManaged(OpenShiftClient client) throws InfrastructureException {
    try {
      Project namespace = client.projects().withName(getName()).get();
      return namespace.getMetadata().getLabels() != null
          && "true".equals(namespace.getMetadata().getLabels().get(MANAGED_NAMESPACE_LABEL));
    } catch (KubernetesClientException e) {
      if (e.getCode() == 403) {
        throw new InfrastructureException(
            format(
                "Could not access the project %s when trying to determine if it is managed "
                    + "for workspace %s",
                getName(), getWorkspaceId()),
            e);
      } else if (e.getCode() == 404) {
        // we don't want to block whatever work the caller is doing on the namespace. The caller
        // will fail anyway if the project doesn't exist.
        return true;
      }

      throw new InternalInfrastructureException(
          format(
              "Failed to determine whether the project"
                  + " %s is managed. OpenShift client said: %s",
              getName(), e.getMessage()),
          e);
    }
  }
}
