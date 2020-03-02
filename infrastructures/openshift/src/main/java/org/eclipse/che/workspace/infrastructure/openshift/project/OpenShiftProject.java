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
package org.eclipse.che.workspace.infrastructure.openshift.project;

import static java.lang.String.format;
import static org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesObjectUtil.isLabeled;

import com.google.common.annotations.VisibleForTesting;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.openshift.api.model.DoneableProjectRequest;
import io.fabric8.openshift.api.model.Project;
import io.fabric8.openshift.api.model.ProjectRequestFluent;
import io.fabric8.openshift.api.model.Route;
import io.fabric8.openshift.client.OpenShiftClient;
import java.util.concurrent.Executor;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.KubernetesInfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesConfigsMaps;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesDeployments;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesIngresses;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesNamespace;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesObjectUtil;
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
      OpenShiftClientFactory clientFactory, Executor executor, String name, String workspaceId) {
    super(clientFactory, executor, name, workspaceId);
    this.clientFactory = clientFactory;
    this.routes = new OpenShiftRoutes(name, workspaceId, clientFactory);
  }

  /**
   * Prepare a project for using.
   *
   * <p>Preparing includes creating if needed and waiting for default service account.
   *
   * @param markManaged mark the project as managed by Che. Also applies for already existing
   *     projects.
   * @param canCreate defines what to do when the project is not found. The project is created when
   *     {@code true}, otherwise an exception is thrown.
   * @throws InfrastructureException if any exception occurs during project preparation or if the
   *     project doesn't exist and {@code canCreate} is {@code false}.
   */
  void prepare(boolean markManaged, boolean canCreate) throws InfrastructureException {
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

      create(projectName, osClient, markManaged);
      waitDefaultServiceAccount(projectName, kubeClient);
      return;
    }

    if (markManaged && !isLabeled(project, MANAGED_NAMESPACE_LABEL, "true")) {
      // provision managed label is marking is requested but label is missing
      KubernetesObjectUtil.putLabel(project, MANAGED_NAMESPACE_LABEL, "true");
      update(project, osClient);
    }
  }

  /**
   * Deletes the project. Deleting a non-existent projects is not an error as is not an attempt to
   * delete a project that is already being deleted. If the project is not marked as managed, it is
   * silently not deleted.
   *
   * @throws InfrastructureException if any unexpected exception occurs during project deletion
   */
  void deleteIfManaged() throws InfrastructureException {
    String workspaceId = getWorkspaceId();
    String projectName = getName();

    OpenShiftClient osClient = clientFactory.createOC(workspaceId);

    if (!isProjectManaged(osClient)) {
      LOG.debug(
          "Project {} for workspace {} is not marked as managed. Ignoring the delete request.",
          projectName,
          workspaceId);
      return;
    }

    delete(projectName, osClient);
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

  private void create(String projectName, OpenShiftClient osClient, boolean markManaged)
      throws InfrastructureException {
    try {
      ProjectRequestFluent.MetadataNested<DoneableProjectRequest> metadata =
          osClient.projectrequests().createNew().withNewMetadata().withName(projectName);

      if (markManaged) {
        metadata.addToLabels(MANAGED_NAMESPACE_LABEL, "true");
      }

      metadata.endMetadata().done();
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

      throw new KubernetesInfrastructureException(e);
    }
  }
}
